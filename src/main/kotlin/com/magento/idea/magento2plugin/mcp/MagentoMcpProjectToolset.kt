/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * Exposes Magento MCP tools that do not require the JetBrains PHP plugin.
 */
class MagentoMcpProjectToolset : McpToolset {
    /**
     * Returns the Magento root path configured for the current project.
     */
    @McpTool(name = "get_magento_root_path")
    @McpDescription("Return the Magento root path configured for the current IDE project. Use this when a tool or shell command needs an absolute project path. The result is the plugin's resolved Magento root directory, not a filesystem search result.")
    suspend fun getMagentoRootPath(): String = MagentoMcpToolsetSupport.withProjectAction(
        validateProject = false
    ) {
        MagentoProjectQueries.getMagentoRootPath(it)
    }

    /**
     * Detects project-local CLI wrappers such as Mark Shust Docker scripts and explains how to invoke them.
     */
    @McpTool(name = "describe_magento_cli_environment")
    @McpDescription("Inspect executable Magento CLI and environment wrappers, including bin/magento, bin/n98-magerun2, bin/php, bin/composer, bin/start, bin/stop, and bin/restart. Call this before running Magento, PHP, Composer, n98-magerun, or environment commands. Prefer scripts beside the nearest Docker Compose configuration found from the Magento directory up to the project root; never search outside the project. Magento-directory scripts are fallbacks, so ./bin/magento takes precedence over ./src/bin/magento when Docker lives at the project root. Without an in-project Compose configuration, use the Magento directory. The result marks preferred and fallback scripts and gives each working directory. Run examples from that working directory; do not switch to a fallback merely because a command fails. Edits still belong under the configured Magento root.")
    suspend fun describeMagentoCliEnvironment(): String = MagentoMcpToolsetSupport.withProjectAction(
        requireSmartMode = false
    ) {
        MagentoCliToolQueries.describeCliEnvironment(it)
    }

    @McpTool(name = "magento_magerun")
    @McpDescription("Discover and inspect the project's installed n98-magerun2. mode=help explains the tool. mode=status only reads local files and cached metadata. mode=commands probes the selected runtime and returns a searchable page (search, offset, limit 1..100). mode=command_help requires one exact command name and returns its arguments/options. mode=mcp_config generates native stdio MCP settings when supported, with optional include/exclude filters; it does not start a server or change client settings. refresh=true invalidates the five-minute metadata cache. Docker-directory wrappers take priority; commands never fall back to another runtime on failure. Probes only check installation, version, command list and help; no installation, download, service start, or application command execution. Unsupported shell wrappers are reported without executing them.")
    suspend fun magentoMagerun(
        mode: String = "status",
        command: String = "",
        search: String = "",
        offset: Int = 0,
        limit: Int = 25,
        refresh: Boolean = false,
        include: String = "",
        exclude: String = ""
    ): String {
        if (mode == "help") return MagentoMagerunQueries.help()
        return withContext(Dispatchers.IO) {
            val context = currentCoroutineContext()
            MagentoMcpToolsetSupport.withProjectAction(requireSmartMode = false) {
                MagentoMagerunQueries.execute(it,
                    MagerunRequest(mode, command, search, offset, limit, refresh, include, exclude)) { context.ensureActive() }
            }
        }
    }
}
