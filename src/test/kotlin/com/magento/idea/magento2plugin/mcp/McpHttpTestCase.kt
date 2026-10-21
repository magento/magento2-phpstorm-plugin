/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.mcpserver.McpSessionInvocationMode
import com.intellij.mcpserver.McpToolFilter
import com.intellij.mcpserver.impl.McpServerService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.PlatformTestUtil
import com.magento.idea.magento2plugin.PhysicalMagentoTestCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

/** Real JetBrains server, extension discovery, routing and JSON-RPC over a loopback socket. */
abstract class McpHttpTestCase : PhysicalMagentoTestCase() {
    private var client: McpHttpClient? = null
    private var server: Future<*>? = null
    private val stopServer = CompletableDeferred<Unit>()

    protected fun mcp(): McpHttpClient {
        client?.let { return it }
        val endpoint = CompletableFuture<Triple<Int, String, String>>()
        server = ApplicationManager.getApplication().executeOnPooledThread {
            try {
                runBlocking {
                    McpServerService.getInstance().authorizedSession(
                        McpServerService.McpSessionOptions(
                            McpServerService.AskCommandExecutionMode.DONT_ASK,
                            McpToolFilter.MaskBased("-*,+com.magento.idea.magento2plugin.mcp.*"),
                            invocationMode = McpSessionInvocationMode.DIRECT,
                        )
                    ) { port, tokenName, token ->
                        endpoint.complete(Triple(port, tokenName, token))
                        stopServer.await()
                    }
                }
            } catch (failure: Throwable) {
                endpoint.completeExceptionally(failure)
                throw failure
            }
        }
        val (port, tokenName, token) = await(endpoint)
        return McpHttpClient(port, tokenName, token, project.basePath!!, ::await).also {
            client = it // Tear down even if initialization fails.
            it.initialize()
        }
    }

    private fun <T> await(future: Future<T>): T {
        try {
            // Platform fixtures run on the EDT; keep it available to the real tool dispatcher.
            PlatformTestUtil.waitWithEventsDispatching("MCP HTTP response", { future.isDone }, 40)
            return future.get()
        } finally {
            if (!future.isDone) future.cancel(true)
        }
    }

    override fun tearDown() {
        try {
            client?.close()
        } finally {
            try {
                stopServer.complete(Unit)
                server?.let { await(it) }
            } finally {
                super.tearDown()
            }
        }
    }

    companion object {
        val MAGENTO_TOOLS = setOf("get_magento_root_path", "describe_magento_cli_environment", "magento_magerun",
            "magento_scaffold", "magento_inspect", "magento_compatibility")
    }
}

/** A small wire client, deliberately independent of the MCP SDK and plugin implementation. */
class McpHttpClient(
    port: Int,
    private val tokenName: String,
    private val token: String,
    private val projectPath: String,
    private val await: (CompletableFuture<HttpResponse<String>>) -> HttpResponse<String>,
) : AutoCloseable {
    private val endpoint = URI("http://127.0.0.1:$port/stream")
    private val http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
    private var sessionId: String? = null
    private var protocol = "2025-03-26"
    private var nextId = 0
    private var events: InputStream? = null
    private var eventReader: Thread? = null

    fun initialize() {
        val result = request("initialize", JSONObject()
            .put("protocolVersion", protocol)
            .put("capabilities", JSONObject())
            .put("clientInfo", JSONObject().put("name", "magento-http-tests").put("version", "1")))
        protocol = result.getString("protocolVersion")
        check(result.getJSONObject("capabilities").has("tools")) { "Server does not expose tools" }
        check(sessionId != null) { "JetBrains server did not establish an MCP session" }
        // JetBrains requires the GET stream to retain the initialized session. Its POST replies are JSON.
        val stream = http.sendAsync(builder().GET().build(), HttpResponse.BodyHandlers.ofInputStream())
            .get(10, java.util.concurrent.TimeUnit.SECONDS)
        check(stream.statusCode() == 200) { "MCP event stream returned HTTP ${stream.statusCode()}" }
        events = stream.body()
        eventReader = Thread.ofVirtual().name("mcp-test-events").start {
            try { events?.transferTo(OutputStream.nullOutputStream()) } catch (_: java.io.IOException) { /* closed */ }
        }
        post(JSONObject().put("jsonrpc", "2.0").put("method", "notifications/initialized"), 202)
    }

    fun tools(): List<JSONObject> {
        val tools = mutableListOf<JSONObject>()
        var cursor: String? = null
        do {
            val page = request("tools/list", JSONObject().also { cursor?.let { value -> it.put("cursor", value) } })
            tools += page.getJSONArray("tools").objects()
            cursor = page.optString("nextCursor").takeIf { it.isNotEmpty() }
        } while (cursor != null)
        return tools
    }

    fun call(name: String, arguments: JSONObject = JSONObject()): JSONObject =
        request("tools/call", JSONObject().put("name", name).put("arguments", arguments))

    fun callEnvelope(name: String, arguments: JSONObject): JSONObject =
        exchange("tools/call", JSONObject().put("name", name).put("arguments", arguments))

    fun request(method: String, parameters: JSONObject): JSONObject {
        val envelope = exchange(method, parameters)
        check(!envelope.has("error")) { "MCP protocol error: ${envelope.optJSONObject("error")}" }
        return envelope.getJSONObject("result")
    }

    private fun exchange(method: String, parameters: JSONObject): JSONObject {
        val id = ++nextId
        val response = post(JSONObject().put("jsonrpc", "2.0").put("id", id)
            .put("method", method).put("params", parameters), 200)
        sessionId = response.headers().firstValue("Mcp-Session-Id").orElse(sessionId)
        val envelope = JSONObject(response.body())
        check(envelope.getString("jsonrpc") == "2.0" && envelope.getInt("id") == id) { "Unexpected MCP response: $envelope" }
        return envelope
    }

    private fun builder(): HttpRequest.Builder = HttpRequest.newBuilder(endpoint)
        .timeout(Duration.ofSeconds(30))
        .header("Content-Type", "application/json")
        .header("Accept", "application/json, text/event-stream")
        .header("MCP-Protocol-Version", protocol)
        .header(tokenName, token)
        .header("IJ_MCP_SERVER_PROJECT_PATH", projectPath)
        .also { builder -> sessionId?.let { builder.header("Mcp-Session-Id", it) } }

    private fun post(message: JSONObject, expectedStatus: Int): HttpResponse<String> {
        val response = await(http.sendAsync(builder().POST(HttpRequest.BodyPublishers.ofString(message.toString())).build(),
            HttpResponse.BodyHandlers.ofString()))
        check(response.statusCode() == expectedStatus) { "MCP HTTP ${response.statusCode()}: ${response.body()}" }
        return response
    }

    override fun close() {
        try {
            if (sessionId != null && events != null) {
                val response = await(http.sendAsync(builder().DELETE().build(), HttpResponse.BodyHandlers.ofString()))
                check(response.statusCode() == 200) { "MCP session shutdown returned HTTP ${response.statusCode()}" }
            }
        } finally {
            events?.close()
            eventReader?.join(2000)
            http.shutdownNow()
        }
    }

    companion object {
        fun text(result: JSONObject): String = result.getJSONArray("content").objects()
            .filter { it.getString("type") == "text" }.joinToString("\n") { it.getString("text") }

        private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }
    }
}
