/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.hce

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import com.intellij.mcpserver.annotations.McpToolHints
import com.intellij.mcpserver.annotations.McpToolHintValue.TRUE
import com.intellij.mcpserver.project
import com.magento.idea.magento2plugin.mcp.MagentoXmlDiagnostics
import com.magento.idea.magento2plugin.mcp.MagentoInspectionCommands
import com.magento.idea.magento2plugin.mcp.MagentoMcpToolsetSupport
import com.magento.idea.magento2plugin.project.Settings
import kotlinx.coroutines.currentCoroutineContext
import java.nio.file.Path
import java.nio.file.Files

/** HCE lifecycle adaptation only; catalog, validation, PSI traversal and results are shared. */
class AnalysisToolset : McpToolset {
    override fun alwaysIncluded() = true
    override fun isExperimental() = false

    private val queries = setOf("module", "layout_entities", "di_config", "observers_for_event", "ui_component", "acl_or_menu")

    @McpTool(name = "magento_diagnostics")
    @McpToolHints(readOnlyHint = TRUE)
    @McpDescription("Run selected existing Magento XML inspections on one project-relative file. mode=help discovers rule IDs and limits; mode=check requires filePath. Reports severity, explanation, source range, truncation and readiness. Default severities, no editor profiles or quick-fix application. No PHP semantics or full-project validation.")
    suspend fun diagnostics(mode: String = "help", filePath: String = "", ruleIds: List<String> = emptyList(), limit: Int = 25): String {
        if (mode == "help") return MagentoXmlDiagnostics.help()
        if (mode != "check") return MagentoXmlDiagnostics.failure("invalid_request", "mode must be help or check.").toString()
        val project = currentCoroutineContext().project
        val root = project.basePath ?: return MagentoXmlDiagnostics.failure("project_unavailable", "Project path is unavailable.").toString()
        val configured = settingsStore().load(Path.of(root))
        if (!configured.enabled) return MagentoXmlDiagnostics.failure("disabled", "Enable Magento integration with configure_magento_project.").toString()
        val magentoRoot = configured.magentoPath?.let { Path.of(it) }
        if (magentoRoot == null || !Files.isDirectory(magentoRoot) || !magentoRoot.toRealPath().startsWith(Path.of(root).toRealPath())) {
            return MagentoXmlDiagnostics.failure("invalid_configuration", "Configure an existing Magento root inside this project.").toString()
        }
        return MagentoMcpToolsetSupport.withProjectReadAction(validateProject = false) { analysisProject ->
            Settings.getInstance(analysisProject).apply { pluginEnabled = true; magentoPath = configured.magentoPath }
            MagentoXmlDiagnostics.check(analysisProject, filePath, ruleIds, limit).toString()
        }
    }

    @McpTool(name = "magento_inspect")
    @McpToolHints(readOnlyHint = TRUE)
    @McpDescription("Read Magento XML declarations using shared IDE analysis. Modes: help, detailed_schema, query. Query types: module, layout_entities, di_config, observers_for_event, ui_component, acl_or_menu. Returns project-relative source paths. PHP semantics are unavailable; XML declarations do not establish merge order or runtime behavior.")
    suspend fun inspect(mode: String, queryType: String = "", parametersJson: String = ""): String {
        when (mode.trim().lowercase()) {
            "help" -> return MagentoInspectionCommands.help(queries)
            "schema", "detailed_schema" -> return MagentoInspectionCommands.detailedSchema(queryType, queries)
            "query" -> Unit
            else -> return "mode must be help, detailed_schema, or query."
        }
        val project = currentCoroutineContext().project
        val root = project.basePath ?: return "HCE project path is unavailable."
        val configured = settingsStore().load(Path.of(root))
        if (!configured.enabled) return "Magento integration is disabled; use configure_magento_project."
        if (configured.magentoPath == null) return "Magento root is not configured; use configure_magento_project."
        val magentoRoot = Path.of(configured.magentoPath)
        if (!Files.isDirectory(magentoRoot) || !magentoRoot.toRealPath().startsWith(Path.of(root).toRealPath())) {
            return "Configured Magento root is unavailable or outside this project; use configure_magento_project."
        }
        return MagentoMcpToolsetSupport.withProjectReadAction(validateProject = false) { analysisProject ->
            Settings.getInstance(analysisProject).apply {
                pluginEnabled = configured.enabled
                magentoPath = configured.magentoPath
            }
            MagentoInspectionCommands.query(analysisProject, queryType, parametersJson, queries, IndexKeys)
        }
    }
}
