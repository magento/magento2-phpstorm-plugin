/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.hce

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import com.intellij.mcpserver.annotations.McpToolHintValue.TRUE
import com.intellij.mcpserver.annotations.McpToolHints
import com.intellij.mcpserver.project
import com.intellij.openapi.application.PathManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import java.nio.file.Path

class ProjectToolset : McpToolset {
    override fun alwaysIncluded() = true
    override fun isExperimental() = false

    @McpTool(name = "configure_magento_project")
    @McpDescription("Read or persist HCE Magento project settings. action=status (default) is read-only; configure explicitly updates enabled, magentoPath and/or cliToolCandidates. Paths belong to the opened project. Indexing exclusions are unavailable. Settings survive tool calls and engine restarts.")
    suspend fun configure(action: String = "status", magentoPath: String = "", enabled: Boolean? = null,
                          cliToolCandidates: List<String>? = null, excludeGeneratedFiles: Boolean? = null): String = withProject { service, project ->
        service.configure(project, action, magentoPath, enabled, cliToolCandidates, excludeGeneratedFiles).toString()
    }

    @McpTool(name = "get_magento_root_path")
    @McpToolHints(readOnlyHint = TRUE)
    @McpDescription("Read this HCE project's configured Magento root without changing settings or requiring indexing.")
    suspend fun root(): String = withProject { service, project -> service.root(project).toString() }

    @McpTool(name = "describe_magento_cli_environment")
    @McpToolHints(readOnlyHint = TRUE)
    @McpDescription("Describe project-local Magento, PHP, Composer and Magerun wrappers, Docker Compose priority, working directories and DDEV configuration. Reads files only: no wrapper, PHP code, Docker command or metadata probe is executed. Requires configured, enabled Magento integration; does not require PHP PSI or indexing.")
    suspend fun describe(): String = withProject { service, project -> service.describeEnvironment(project).toString() }

    private suspend fun withProject(action: (ProjectService, Path) -> String): String = withContext(Dispatchers.IO) {
        val root = requireNotNull(currentCoroutineContext().project.basePath) { "HCE project path is unavailable." }
        action(ProjectService(settingsStore()), Path.of(root))
    }
}

/** Every toolset resolves the same durable store, independent of analysis context lifetime. */
internal fun settingsStore(): SettingsStore {
    val directory = System.getProperty("magento.hce.settings.dir")?.let(Path::of)
        ?: Path.of(PathManager.getConfigPath(), "magento-hce")
    return SettingsStore(directory)
}
