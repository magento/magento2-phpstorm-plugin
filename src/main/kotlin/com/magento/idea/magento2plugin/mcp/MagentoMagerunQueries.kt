/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

internal object MagentoMagerunQueries {
    private val SERVICE = Key.create<MagentoMagerunService>("magento.mcp.magerun.metadata")

    fun help(): String = JSONObject().put("tool", "magento_magerun").put("modes", JSONObject()
        .put("status", "Filesystem detection and cached metadata only; never starts a process. refresh=true clears cached metadata.")
        .put("commands", "Probe the selected runtime for n98-magerun2 and return a searchable command page. search, offset, limit (1..100), refresh are optional.")
        .put("command_help", "Return structured arguments/options for one exact command or alias from the installed catalogue. Requires command.")
        .put("mcp_config", "Generate native stdio MCP configuration if supported. Optional include/exclude command filters. Does not start a server or write client settings."))
        .put("cache", "Per project and selected invocation; expires after five minutes. refresh=true forces a new probe. Local launcher/config/Composer changes invalidate cached metadata.")
        .put("runtime", "Uses the Docker-directory candidate first. Never installs, updates, starts services, executes an application command, or retries through another runtime.")
        .toString(2)

    fun execute(project: Project, request: MagerunRequest, checkCancelled: () -> Unit): String {
        if (request.mode == "help") return help()
        val environment = MagentoMagerunSupport.discover(project)
            ?: return JSONObject().put("status", "project_unavailable").toString()
        val service = synchronized(project) {
            project.getUserData(SERVICE) ?: MagentoMagerunService().also { project.putUserData(SERVICE, it) }
        }
        return service.execute(environment, request, checkCancelled).toString(2)
    }
}

internal data class MagerunRequest(
    val mode: String = "status",
    val command: String = "",
    val search: String = "",
    val offset: Int = 0,
    val limit: Int = 25,
    val refresh: Boolean = false,
    val include: String = "",
    val exclude: String = ""
)

/** Holds only metadata for this IDE project; the last successful runtime snapshot is bounded and expires. */
internal class MagentoMagerunService(
    private val runner: MagerunRunner = MagentoMagerunProcess(),
    private val nanoTime: () -> Long = System::nanoTime
) {
    private val lock = ReentrantLock()
    private var cache: Snapshot? = null
    private val maxAge = TimeUnit.MINUTES.toNanos(5)

    fun execute(environment: MagentoMagerunSupport.Environment, request: MagerunRequest,
                checkCancelled: () -> Unit = {}): JSONObject {
        while (!lock.tryLock(50, TimeUnit.MILLISECONDS)) checkCancelled()
        try {
            checkCancelled()
            validate(request)
            if (request.refresh) cache = null
            val base = environment.json()
            if (environment.selected == null) return base.put("status", "not_found")
            val plan = try {
                MagentoMagerunSupport.plan(environment)
            } catch (error: MagentoMagerunSupport.Unavailable) {
                cache = null
                return base.put("status", error.status).put("message", error.message)
            }
            val fingerprint = MagentoMagerunSupport.fingerprint(environment, plan)
            if (cache?.let { it.fingerprint != fingerprint || nanoTime() - it.createdAt >= maxAge } == true) cache = null
            base.put("invocation", plan.json())
            if (request.mode == "status") {
                if (cache != null) base.getJSONObject("selected").put("identityVerified", true).put("family", "n98-magerun2")
                return base.put("status", if (cache == null) "detected" else "cached")
                    .put("runtimeProbed", false)
                    .put("metadata", cache?.metadata() ?: JSONObject.NULL)
            }
            val fromCache = cache != null
            val snapshot = cache ?: load(plan, fingerprint, checkCancelled).also { cache = it }
            base.put("status", "ok").put("cached", fromCache).put("metadata", snapshot.metadata())
            // Only a successful version check upgrades an ambiguous filename to a verified family.
            base.getJSONObject("selected").put("identityVerified", true).put("family", "n98-magerun2")
            return when (request.mode) {
                "commands" -> commandPage(base, snapshot, request)
                "command_help" -> base.put("command", commandHelp(plan, snapshot, request.command, checkCancelled))
                "mcp_config" -> mcpConfig(base, plan, snapshot, request, checkCancelled)
                else -> error("validated mode")
            }
        } catch (error: MagentoMagerunSupport.Unavailable) {
            return JSONObject().put("status", error.status).put("message", error.message)
        } catch (error: IOException) {
            cache = null
            return JSONObject().put("status", "runtime_unavailable")
                .put("message", "The selected runtime could not be accessed: ${error.message?.take(1000)}")
        } catch (_: JSONException) {
            cache = null
            return JSONObject().put("status", "invalid_output").put("message", "The selected runtime did not return the expected JSON command metadata.")
        } finally {
            lock.unlock()
        }
    }

    private fun validate(request: MagerunRequest) {
        if (request.mode !in setOf("status", "commands", "command_help", "mcp_config")) {
            fail("invalid_request", "mode must be status, commands, command_help, or mcp_config. Use help for usage.")
        }
        if (request.limit !in 1..100 || request.offset < 0 || request.search.length > 200) {
            fail("invalid_request", "limit must be 1..100, offset must be non-negative, and search at most 200 characters.")
        }
        if (request.mode == "command_help" && !COMMAND_NAME.matches(request.command)) {
            fail("invalid_request", "command must be one exact command name or alias, with no arguments or shell syntax.")
        }
        for (filter in listOf(request.include, request.exclude)) {
            if (filter.length > 1000 || (filter.isNotEmpty() && !Regex("[a-zA-Z0-9_:@*?. -]+").matches(filter))) {
                fail("invalid_request", "include/exclude must contain space-separated command patterns or @groups (at most 1000 characters).")
            }
        }
    }

    private fun load(plan: MagentoMagerunSupport.LaunchPlan, fingerprint: String, checkCancelled: () -> Unit): Snapshot {
        cache = null
        plan.preflight?.let { preflight ->
            val result = runner.run(preflight, plan.cwd, checkCancelled)
            if (result.exitCode != 0 && !result.timedOut && !result.outputLimited) {
                if (result.stderr.isBlank()) fail("missing_installation", "The magerun PHAR is not installed in the selected container. No download was attempted.")
                fail("runtime_unavailable", "The selected container is unavailable. ${result.stderr.take(1500)}")
            }
            requireSuccess(result)
        }
        val versionOutput = probe(plan, listOf("--version", "--no-ansi", "--no-interaction"), checkCancelled).trim()
        if (!Regex("(?i)^n98-magerun2(?:-dev)?(?:\\s|$)").containsMatchIn(versionOutput)) {
            fail("incompatible_tool", "The selected executable did not identify itself as n98-magerun2. Magento 1 tools and unverified aliases are not used. No alternative runtime was tried.")
        }
        val json = parseJson(probe(plan, listOf("list", "--format=json", "--no-ansi", "--no-interaction"), checkCancelled))
        val application = json.optJSONObject("application")
        val applicationName = application?.optString("name").orEmpty()
        if (applicationName.isNotEmpty() && !applicationName.startsWith("n98-magerun2", ignoreCase = true)) {
            fail("incompatible_tool", "The command catalogue belongs to a different application.")
        }
        val commands = json.getJSONArray("commands")
        if (commands.length() > 10_000) fail("output_limit", "The installed command catalogue exceeds the supported size.")
        val summaries = (0 until commands.length()).map { index ->
            val command = commands.getJSONObject(index)
            val name = command.getString("name")
            if (!COMMAND_NAME.matches(name)) fail("invalid_output", "The catalogue contains an invalid command name.")
            val usage = command.optJSONArray("usage") ?: JSONArray()
            val aliases = (0 until usage.length()).map { usage.optString(it) }
                .filter { it != name && COMMAND_NAME.matches(it) }.distinct().sorted()
            CommandSummary(name, command.optString("description").take(1000), aliases, command.optBoolean("hidden"))
        }.distinctBy { it.name }.sortedBy { it.name }
        if (summaries.isEmpty()) fail("invalid_output", "The installed command catalogue is empty.")
        return Snapshot(fingerprint, nanoTime(), Instant.now().toString(),
            application?.optString("version")?.takeIf { it.isNotBlank() } ?: versionOutput.take(250), summaries)
    }

    private fun probe(plan: MagentoMagerunSupport.LaunchPlan, arguments: List<String>, checkCancelled: () -> Unit): String {
        val result = runner.run(plan.argv + arguments, plan.cwd, checkCancelled)
        requireSuccess(result)
        return result.stdout
    }

    private fun requireSuccess(result: MagerunProcessResult) {
        if (result.timedOut) fail("timeout", "The selected runtime exceeded the metadata probe timeout. No fallback was attempted.")
        if (result.outputLimited) fail("output_limit", "The selected runtime exceeded the metadata output limit.")
        if (result.exitCode != 0) fail("probe_failed", "Metadata probe failed (exit ${result.exitCode}). ${result.stderr.take(1500)}")
    }

    private fun commandPage(base: JSONObject, snapshot: Snapshot, request: MagerunRequest): JSONObject {
        val matches = snapshot.commands.filter { command ->
            !command.hidden && (request.search.isBlank() || command.name.contains(request.search, true)
                || command.description.contains(request.search, true) || command.aliases.any { it.contains(request.search, true) })
        }
        val page = matches.drop(request.offset).take(request.limit)
        return base.put("total", matches.size).put("offset", request.offset).put("limit", request.limit)
            .put("nextOffset", if (request.offset.toLong() + page.size < matches.size) request.offset + page.size else JSONObject.NULL)
            .put("commands", JSONArray(page.map { it.json() }))
    }

    private fun commandHelp(plan: MagentoMagerunSupport.LaunchPlan, snapshot: Snapshot, name: String,
                            checkCancelled: () -> Unit): JSONObject {
        val summary = snapshot.commands.firstOrNull { it.name == name || name in it.aliases }
            ?: fail("unknown_command", "This command or alias is not in the installed catalogue. Refresh commands if the runtime changed.")
        snapshot.help[summary.name]?.let { return JSONObject(it) }
        val result = parseJson(probe(plan, listOf("help", summary.name, "--format=json", "--no-ansi", "--no-interaction"), checkCancelled))
        if (result.optString("name") != summary.name || result.optJSONObject("definition") == null) {
            fail("invalid_output", "Command help does not describe the requested command.")
        }
        if (result.toString().length > 131_072) fail("output_limit", "The requested command help exceeds the response limit.")
        if (snapshot.help.size >= 32) snapshot.help.remove(snapshot.help.keys.first())
        snapshot.help[summary.name] = result.toString()
        return result
    }

    private fun mcpConfig(base: JSONObject, plan: MagentoMagerunSupport.LaunchPlan, snapshot: Snapshot,
                          request: MagerunRequest, checkCancelled: () -> Unit): JSONObject {
        val server = listOf("mcp:server:start", "magerun:mcp:server").firstOrNull { name -> snapshot.commands.any { it.name == name } }
            ?: fail("native_mcp_unavailable", "This installation does not expose a supported native MCP server command.")
        val options = commandHelp(plan, snapshot, server, checkCancelled).getJSONObject("definition").optJSONObject("options") ?: JSONObject()
        fun supports(name: String): Boolean = options.has(name) || options.has("--$name")
        if (!supports("include") || (request.exclude.isNotBlank() && !supports("exclude"))) {
            fail("native_mcp_filtering_unavailable", "This installation cannot apply the requested native MCP command filters.")
        }
        val defaultCommands = listOf("sys:info", "sys:check", "cache:list", "indexer:info")
            .filter { name -> snapshot.commands.any { it.name == name } }
        val include = request.include.trim().ifEmpty { defaultCommands.joinToString(" ") }
        if (include.isEmpty()) fail("invalid_request", "Specify include command patterns for the native MCP server.")
        val arguments = plan.argv.drop(1) + listOf(server, "--no-ansi", "--no-interaction", "--include=$include") +
            if (request.exclude.isBlank()) emptyList() else listOf("--exclude=${request.exclude.trim()}")
        val config = JSONObject().put("command", plan.argv.first()).put("args", JSONArray(arguments))
            .put("cwd", plan.cwd.toString()).put("env", JSONObject().put("NO_COLOR", "1"))
        return base.put("serverStarted", false).put("configurationWritten", false)
            .put("configuration", JSONObject().put("mcpServers", JSONObject().put("magento-magerun2", config)))
            .put("note", "Client must honor cwd and stdio. This configuration uses the verified invocation; it does not install magerun or start Docker. Review selected commands before enabling it.")
    }

    private fun parseJson(value: String): JSONObject {
        val tokener = JSONTokener(value.trim().removePrefix("\uFEFF"))
        val result = tokener.nextValue()
        if (result !is JSONObject || tokener.nextClean() != '\u0000') {
            fail("invalid_output", "Expected one JSON object from the metadata probe; wrapper banners and trailing output are not parsed as command data.")
        }
        return result
    }

    private fun fail(status: String, message: String): Nothing = throw MagentoMagerunSupport.Unavailable(status, message)

    private data class CommandSummary(val name: String, val description: String, val aliases: List<String>, val hidden: Boolean) {
        fun json(): JSONObject = JSONObject().put("name", name).put("description", description).put("aliases", JSONArray(aliases))
    }

    private data class Snapshot(val fingerprint: String, val createdAt: Long, val checkedAt: String,
                                val version: String, val commands: List<CommandSummary>, val help: LinkedHashMap<String, String> = linkedMapOf()) {
        fun metadata(): JSONObject = JSONObject().put("family", "n98-magerun2").put("version", version)
            .put("checkedAt", checkedAt).put("commandCount", commands.size).put("expiresAfterSeconds", 300)
            .put("nativeMcpAvailable", commands.any { it.name in setOf("mcp:server:start", "magerun:mcp:server") })
    }

    companion object {
        private val COMMAND_NAME = Regex("[a-zA-Z0-9][a-zA-Z0-9_:.-]{0,199}")
    }
}
