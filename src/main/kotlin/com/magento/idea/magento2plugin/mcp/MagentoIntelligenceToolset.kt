/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.mcpserver.McpSessionInvocationMode
import com.intellij.mcpserver.McpToolInvocationMode
import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import com.intellij.mcpserver.annotations.McpToolHintValue.TRUE
import com.intellij.mcpserver.annotations.McpToolHints
import com.intellij.mcpserver.impl.McpServerService
import com.intellij.mcpserver.mcpCallInfo
import com.intellij.mcpserver.settings.McpToolFilterSettings
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import kotlinx.coroutines.currentCoroutineContext

class MagentoIntelligenceToolset : McpToolset {
    override fun alwaysIncluded() = true

    @McpTool(name = "magento_intelligence", includeProjectPath = false)
    @McpToolHints(readOnlyHint = TRUE)
    @McpDescription(
        "Discover Magento and Adobe Commerce MCP tools for project inspection, setup, runtime, " +
        "compatibility, scaffolding, and bundled agent skills. Lists available tools and their arguments on request."
    )
    suspend fun magentoIntelligence(
        @McpDescription("Exact tool name to show its full description and argument schema. Omit for the catalog.")
        tool: String = ""
    ): String {
        val call = currentCoroutineContext().mcpCallInfo
        val options = call.mcpSessionOptions
        val client = Implementation(call.clientInfo.name, call.clientInfo.version)
        val routerMode = (options.invocationMode ?: McpToolFilterSettings.getInstance().invocationMode) ==
            McpSessionInvocationMode.VIA_ROUTER
        val service = McpServerService.getInstance()
        val direct = service.getMcpToolsAsync(
            filter = options.toolFilter, clientInfo = client, sessionOptions = options,
            invocationMode = if (routerMode) McpToolInvocationMode.DIRECT_WITH_ROUTER_ENABLED
                else McpToolInvocationMode.DIRECT
        )
        val routed = if (routerMode && direct.any { it.descriptor.name == "execute_tool" }) {
            service.getMcpToolsAsync(
                filter = options.toolFilter, clientInfo = client, sessionOptions = options,
                invocationMode = McpToolInvocationMode.VIA_ROUTER
            )
        } else {
            emptyList()
        }
        val directNames = direct.mapTo(hashSetOf()) { it.descriptor.name }
        val tools = (direct + routed).map { it.descriptor }
            .filter { it.fullyQualifiedName.startsWith("com.magento.idea.magento2plugin.mcp.") }
            .distinctBy { it.name }
            .sortedBy { it.name }
        val selected = tools.firstOrNull { it.name == tool }
        if (selected != null) {
            val invocation = if (selected.name in directNames) "Call directly as ${selected.name}."
                else "Call execute_tool with ${selected.name} --argument value. Pass arrays and objects as JSON."
            return "${selected.name}\n${selected.description}\n$invocation\n${selected.inputSchema.prettyPrint()}"
        }
        return buildString {
            if (tool.isNotBlank()) appendLine("Magento tool '$tool' is unavailable in this session.")
            appendLine("Magento and Adobe Commerce MCP tools")
            appendLine("For a full description and argument schema, call magento_intelligence with the exact tool name.")
            if (tools.any { it.name !in directNames }) {
                appendLine("For router entries, pass the tool name and --argument value pairs to execute_tool.")
            }
            for (descriptor in tools) {
                val route = if (descriptor.name in directNames) "direct" else "router"
                val summary = descriptor.description.lineSequence().first().substringBefore(". ").take(200)
                appendLine("- ${descriptor.name} ($route): $summary")
            }
        }.trimEnd()
    }
}
