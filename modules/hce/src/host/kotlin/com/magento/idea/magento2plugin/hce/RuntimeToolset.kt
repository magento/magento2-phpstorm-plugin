/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.hce

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import com.intellij.mcpserver.project
import com.magento.idea.magento2plugin.mcp.MagentoSkillCommands
import com.magento.idea.magento2plugin.mcp.MagentoMagerunQueries
import com.magento.idea.magento2plugin.mcp.MagentoMagerunService
import com.magento.idea.magento2plugin.mcp.MagentoMagerunSupport
import com.magento.idea.magento2plugin.mcp.MagerunRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/** Lifecycle adapters; skill installation and metadata parsing/execution remain shared. */
class RuntimeToolset : McpToolset {
    override fun alwaysIncluded() = true
    override fun isExperimental() = false

    @McpTool(name = "magento_skills")
    @McpDescription("Bundled Magento skill help, status (default, read-only), or explicit install. Select skills and project/agents/codex/claude target; customized copies are preserved unless overwrite=true. Installation never edits AGENTS.md or enables missing PHP features. Scaffold skill delivery does not provide scaffolding in HCE.")
    suspend fun skills(mode: String = "status", skills: List<String> = emptyList(), agentTarget: String = "project",
                       overwrite: Boolean = false): String = withContext(Dispatchers.IO) {
        if (mode == "help") return@withContext MagentoSkillCommands.help()
        MagentoSkillCommands.execute(currentCoroutineContext().project, mode, skills, agentTarget, overwrite, "", refreshVfs = false)
    }

    @McpTool(name = "magento_magerun")
    @McpDescription("Inspect the selected Magerun runtime. help/status read files and cached metadata; commands, command_help and mcp_config may execute bounded version/list/help probes. No installation, download, service start, application operation, runtime fallback or MCP-server startup. Cache is bounded per project for this engine process and expires after five minutes.")
    suspend fun magerun(mode: String = "status", command: String = "", search: String = "", offset: Int = 0,
                        limit: Int = 25, refresh: Boolean = false, include: String = "", exclude: String = ""): String =
        withContext(Dispatchers.IO) {
            if (mode == "help") return@withContext MagentoMagerunQueries.help()
            val context = currentCoroutineContext()
            val root = Path.of(requireNotNull(context.project.basePath)).toRealPath()
            val state = settingsStore().load(root)
            if (!state.enabled || state.magentoPath == null) return@withContext JSONObject()
                .put("status", "invalid_configuration").put("message", "Configure and enable Magento integration first.").toString()
            val magentoRoot = try {
                Path.of(state.magentoPath).toRealPath()
            } catch (_: IOException) {
                null
            }
            if (magentoRoot == null || !magentoRoot.startsWith(root) || !Files.isDirectory(magentoRoot)) {
                return@withContext JSONObject().put("status", "invalid_configuration")
                    .put("message", "The configured Magento root must be an existing directory inside the workspace.").toString()
            }
            val relative = root.relativize(magentoRoot).toString().replace('\\', '/').ifEmpty { null }
            val environment = MagentoMagerunSupport.discover(root, relative, state.cliToolCandidates)
            val service = synchronized(services) {
                services[root] ?: MagentoMagerunService(terminalName = "your terminal").also {
                    if (services.size >= 16) services.remove(services.keys.first())
                    services[root] = it
                }
            }
            service.execute(environment, MagerunRequest(mode, command, search, offset, limit, refresh, include, exclude)) {
                context.ensureActive()
            }.toString()
        }

    private companion object {
        val services = linkedMapOf<Path, MagentoMagerunService>()
    }
}
