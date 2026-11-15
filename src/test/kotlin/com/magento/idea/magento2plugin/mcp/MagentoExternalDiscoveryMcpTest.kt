/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.mcpserver.McpSessionInvocationMode
import com.intellij.mcpserver.impl.McpServerService
import com.intellij.mcpserver.settings.McpServerSettings
import com.intellij.mcpserver.settings.McpToolFilterSettings
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.PlatformTestUtil
import com.magento.idea.magento2plugin.project.Settings
import org.json.JSONObject
import java.net.ServerSocket
import java.nio.file.Files
import java.nio.file.Path

/** Uses the global external-client endpoint, not an authorizedSession with a plugin-only filter. */
class MagentoExternalDiscoveryMcpTest : McpHttpTestCase() {
    fun testFreshExternalRouterSessionBeforeSetup() {
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = false
        settings.magentoPath = null
        settings.excludeGeneratedFiles = null
        val before = settings.state
        val root = Path.of(project.basePath!!)
        val filters = McpToolFilterSettings.getInstance()
        val serverSettings = McpServerSettings.getInstance()
        val service = McpServerService.getInstance()
        val oldMode = filters.invocationMode
        val oldFilter = filters.toolsFilter
        val oldPort = serverSettings.mcpServerPort
        val oldEnabled = serverSettings.enableMcpServer
        val oldConsent = serverSettings.consent
        try {
            filters.invocationMode = McpSessionInvocationMode.VIA_ROUTER
            filters.toolsFilter = McpToolFilterSettings.DEFAULT_FILTER
            serverSettings.mcpServerPort = ServerSocket(0).use { it.localPort }
            onBackgroundThread { service.start() }
            // No private-session token or session option overrides: normal external-client routing.
            McpHttpClient(service.port, "", "", root.toString()) { future ->
                PlatformTestUtil.waitWithEventsDispatching("External MCP response", { future.isDone }, 40)
                future.get()
            }.use { client ->
                client.initialize()
                val tools = client.tools() // First request after the MCP initialization handshake.
                val names = tools.map { it.getString("name") }.sorted()
                println("External MCP: IDE=${ApplicationInfo.getInstance().build}, endpoint=/stream, mode=${filters.invocationMode}, tools/list=$names")
                for (name in listOf("execute_tool", "magento_intelligence", "magento_skills")) {
                    assertTrue(names.toString(), name in names)
                }
                val direct = McpHttpClient.text(client.call("magento_intelligence", JSONObject().put("tool", "configure_magento_project")))
                val routed = McpHttpClient.text(client.call("execute_tool", JSONObject()
                    .put("command", "magento_intelligence --tool configure_magento_project").put("projectPath", root.toString())))
                assertEquals(direct, routed)
                for (field in listOf("configure_magento_project", "properties", "action", "magentoPath", "enabled", "projectPath")) {
                    assertTrue(direct, direct.contains(field))
                }
                val status = JSONObject().put("mode", "status").put("projectPath", root.toString())
                val directSkills = McpHttpClient.text(client.call("magento_skills", status))
                val routedSkills = McpHttpClient.text(client.call("execute_tool", JSONObject()
                    .put("command", "magento_skills --mode status").put("projectPath", root.toString())))
                assertEquals(directSkills, routedSkills)
                for (skill in listOf("magento-inspect", "magento-scaffold", "magento-magerun")) {
                    assertTrue(directSkills, directSkills.contains("$skill: missing"))
                }
                fun routedCall(command: String) = McpHttpClient.text(client.call("execute_tool", JSONObject()
                    .put("command", command).put("projectPath", root.toString())))
                assertGuidance(routedCall("describe_magento_cli_environment"), "Magento plugin support is disabled for this project.")
                assertGuidance(routedCall("get_magento_root_path"), "Magento root path is not configured for this project.")
                assertEquals(before, settings.state)
                settings.pluginEnabled = true
                assertGuidance(routedCall("get_magento_root_path"), "Magento root path is not configured for this project.")
                settings.pluginEnabled = false
                assertEquals(before, settings.state)
                assertFalse(Files.exists(root.resolve("AGENTS.md")))
                for (directory in listOf("skills", ".agents", ".codex", ".claude")) {
                    assertFalse(Files.exists(root.resolve(directory)))
                }
                println("External MCP: direct/routed configuration schema and skill status passed; disabled/missing-root recovery passed; project settings unchanged")
            }
        } finally {
            onBackgroundThread { service.stop() }
            filters.invocationMode = oldMode
            filters.toolsFilter = oldFilter
            serverSettings.mcpServerPort = oldPort
            serverSettings.consent = oldConsent
            if (oldEnabled) onBackgroundThread { service.start() }
        }
    }

    private fun onBackgroundThread(action: () -> Unit) {
        val future = ApplicationManager.getApplication().executeOnPooledThread(Runnable(action))
        PlatformTestUtil.waitWithEventsDispatching("External MCP server lifecycle", { future.isDone }, 40)
        future.get()
    }

    private fun assertGuidance(response: String, reason: String) {
        assertTrue(response, response.startsWith(reason))
        assertTrue(response, response.contains("Direct: call magento_intelligence"))
        assertTrue(response, response.contains("\"tool\":\"configure_magento_project\""))
        assertTrue(response, response.contains("Router: call execute_tool"))
        assertTrue(response, response.contains("\"command\":\"magento_intelligence --tool configure_magento_project\""))
        assertTrue(response, response.contains("\"projectPath\":${JSONObject.quote(project.basePath)}"))
    }
}
