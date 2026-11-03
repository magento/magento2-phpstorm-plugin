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
    @McpDescription(
        "Return the Magento root path configured for the current IDE project.\n\n" +

        "Use this when a tool or shell command needs an absolute project path. The result is the " +
        "plugin's resolved Magento root directory, not a filesystem search result."
    )
    suspend fun getMagentoRootPath(): String = MagentoMcpToolsetSupport.withProjectAction(
        validateProject = false
    ) {
        MagentoProjectQueries.getMagentoRootPath(it)
    }

    /**
     * Detects project-local CLI wrappers such as Mark Shust Docker scripts and explains how to invoke them.
     */
    @McpTool(name = "describe_magento_cli_environment")
    @McpDescription(
        "Inspect executable Magento CLI and environment wrappers, including bin/magento, " +
        "bin/n98-magerun2, bin/php, bin/composer, bin/start, bin/stop, and bin/restart. Call this " +
        "before running Magento, PHP, Composer, n98-magerun, or environment commands.\n\n" +

        "Project wrappers:\n" +
        "- Prefer scripts beside the nearest Docker Compose configuration found from the Magento " +
        "directory up to the project root; never search outside the project.\n" +
        "- Magento-directory scripts are fallbacks, so ./bin/magento takes precedence over " +
        "./src/bin/magento when Docker lives at the project root. Without an in-project Compose " +
        "configuration, use the Magento directory.\n" +
        "- The result marks preferred and fallback scripts and gives each working directory. Run " +
        "examples from that working directory; do not switch to a fallback merely because a command " +
        "fails.\n\n" +

        "DDEV:\n" +
        "- For DDEV, parse .ddev/config.yaml and ordered config.*.y*ml overrides to report project " +
        "name, type, document/Composer roots, runtime versions and container working directories with " +
        "source files.\n" +
        "- The general execPrefix includes the configured project and web working directory; append " +
        "the intended command as separate arguments and run from cwd. This does not require Magerun.\n\n" +

        "Execution guidance:\n" +
        "- The result includes Terminal guidance for interactive permission prompts.\n" +
        "- Edits still belong under the configured Magento root."
    )
    suspend fun describeMagentoCliEnvironment(): String = MagentoMcpToolsetSupport.withProjectAction(
        requireSmartMode = false
    ) {
        MagentoCliToolQueries.describeCliEnvironment(it)
    }

    @McpTool(name = "magento_magerun")
    @McpDescription(
        "Discover and inspect the project's installed n98-magerun2.\n\n" +

        "Modes:\n" +
        "- mode=help explains the tool.\n" +
        "- mode=status only reads local files and cached metadata.\n" +
        "- mode=commands probes the selected runtime and returns a searchable page (search, offset, " +
        "limit 1..100).\n" +
        "- mode=command_help requires one exact command name and returns its arguments/options.\n" +
        "- mode=mcp_config generates native stdio MCP settings when supported, with optional " +
        "include/exclude filters; it does not start a server or change client settings.\n\n" +

        "Cache:\n" +
        "- refresh=true invalidates the five-minute metadata cache.\n\n" +

        "Runtime selection:\n" +
        "- Docker-directory wrappers take priority; DDEV is selected when no project-local magerun " +
        "candidate exists and .ddev/config.yaml is present.\n" +
        "- Magerun uses the shared DDEV CLI environment, including parsed project and container " +
        "working-directory settings. The host ddev executable is resolved from the IDE PATH.\n" +
        "- Commands never fall back to another runtime on failure.\n" +
        "- For interactive permissions, suggest the returned terminalGuidance command in PhpStorm's " +
        "Terminal (IDE console).\n\n" +

        "Probe limits:\n" +
        "- Probes only check installation, version, command list and help; no installation, download, " +
        "service start, or application command execution.\n" +
        "- Unsupported shell wrappers are reported without executing them."
    )
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

    /**
     * Provides one low-noise entry point for Magento project inspections.
     */
    @McpTool(name = "magento_inspect")
    @McpDescription(
        "Magento inspection library with three context-conscious modes.\n\n" +

        "Workflow:\n" +
        "- Use mode `help` first to get only a compact catalog of queryType values and short " +
        "descriptions.\n" +
        "- Use mode `detailed_schema` with one queryType to load detailed required parameters, " +
        "accepted values, and example parametersJson only for that inspection.\n" +
        "- Use mode `query` with queryType and parametersJson to run the inspection in the currently " +
        "opened Magento project.\n" +
        "- Prefer this single tool over individual finder tools because it keeps the agent context " +
        "small: list, inspect one schema, then query.\n\n" +

        "Available inspections:\n" +
        "- Shared queryType values are `module`, `di_config`, `observers_for_event`, " +
        "`layout_entities`, `ui_component`, and `acl_or_menu`.\n" +
        "- `plugins_for_method` additionally requires the JetBrains PHP plugin; `help` reports the " +
        "capabilities available in this IDE.\n\n" +

        "Coverage:\n" +
        "- XML results describe declarations, not resolved PHP semantics."
    )
    suspend fun magentoInspect(
        mode: String,
        queryType: String,
        parametersJson: String
    ): String {
        return when (mode.trim().lowercase().replace('-', '_').replace(' ', '_')) {
            "help" -> MagentoInspectionCommands.help()
            "schema", "detailed_schema" -> MagentoInspectionCommands.detailedSchema(queryType)
            "query" -> MagentoMcpToolsetSupport.withProjectReadAction(
                requireSmartMode = MagentoInspectionCommands.requiresSmartMode(queryType)
            ) {
                MagentoInspectionCommands.query(it, queryType, parametersJson)
            }
            else -> "mode must be `help`, `detailed_schema`, or `query`. Use `help` to inspect the compact Magento inspection catalog."
        }
    }
}
