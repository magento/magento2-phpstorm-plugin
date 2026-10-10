/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.McpToolCallResult
import com.intellij.mcpserver.McpToolCallResultContent
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.json.JSONException
import org.json.JSONObject

/**
 * Exposes Magento-specific MCP tools that require the JetBrains PHP plugin.
 */
class MagentoMcpToolset : McpToolset {
    @McpTool(name = "magento_compatibility")
    @McpDescription("Analyze Magento upgrade compatibility with built-in PHP/XML inspections, including PHTML and HTML templates. Start with status and the exact targetVersion to verify project identity, the composer.lock baseline, and readiness. Use releases for published stable versions, dates and nextMinorRelease; supportedVersions describes local index coverage only. Follow nextCall to prepare missing data from the exact official Magento Open Source tag, which may fail if the tag does not exist. Preserve preparation continuation arguments. Cached preparation has runId=null; follow its status nextCall without polling. Once readiness.ready is true, analyze exactly one path or moduleName with the original targetVersion. Follow nextCall through terminal results and all finding pages; save findings before additional scans because only five runs per operation are retained. Cancel uses the returned runId. Failed requests/jobs set isError=true. Parse JSON text when structuredContent is absent; JetBrains omits structuredContent on errors. Host argument-type and project-routing errors may be plain text. Default findings include baseline issues and component test/fixture files; ignoreCurrentVersion=true suppresses baseline issues. Prepared data excludes proprietary Commerce and third-party dependencies; zero findings does not certify an upgrade. No scripts, Composer installation, or extra IDE instance are needed. Completed analyses expose analysisIdentity and scope counts; explainSuppressed adds baseline suppression counts. pathBase is the IDE project directory. Validation errors have nextCall=null; correct the specified parameters. Use detailed_schema for constraints.")
    suspend fun magentoCompatibility(
        @McpDescription("status (default), releases, prepare, analyze, results, cancel, help, or detailed_schema.") mode: String = "status",
        @McpDescription("Analyze: project-relative or absolute PHP, PHTML, XML, or HTML file or component directory, as recognized by IDE PHP/XML PSI. Use either path or moduleName.") path: String? = null,
        @McpDescription("Analyze: exact Vendor_Module name. Use either moduleName or path.") moduleName: String? = null,
        @McpDescription("Status/prepare/analyze: exact requested Magento release. Prepare obtains missing compatibility data.") targetVersion: String? = null,
        @McpDescription("Status/analyze/releases: explicit baseline; otherwise Magento composer.lock. Preparation/results/cancel: preserved baseline for the follow-up status call.") currentVersion: String? = null,
        @McpDescription("Analyze: warning (default), error, or critical.") minimumSeverity: String? = null,
        @McpDescription("Status/analyze: suppress existing baseline issues; requires covered current and target releases. Default false. Preparation/results/cancel preserve this option for status.") ignoreCurrentVersion: Boolean? = null,
        @McpDescription("Analyze: include baseline suppression counts by rule and severity. Requires ignoreCurrentVersion=true; default false.") explainSuppressed: Boolean? = null,
        @McpDescription("Results/cancel: runId returned by prepare or analyze; belongs to the same project.") runId: String? = null,
        @McpDescription("Results: JSON integer offset 0..2147483647, default 0; use nextCall or nextOffset. Send JSON integers; the host also coerces numeric strings.") offset: Int? = null,
        @McpDescription("Results: JSON integer page size 1..500, default 100. Send JSON integers; the host also coerces numeric strings. Fractions and overflow are rejected before the tool runs.") limit: Int? = null,
        @McpDescription("Preparation/results/cancel: original upgrade target for the follow-up status call, distinct from the release being prepared. Preserve the value returned in nextCall.") analysisTargetVersion: String? = null
    ): McpToolCallResult {
        val normalizedMode = mode.trim().lowercase().replace('-', '_')
        val response = when (normalizedMode) {
            "help" -> MagentoCompatibilityCommands.help()
            "detailed_schema" -> MagentoCompatibilityCommands.detailedSchema()
            else -> withContext(if (normalizedMode == "releases") Dispatchers.IO else currentCoroutineContext()) {
                MagentoMcpToolsetSupport.withProjectAction(validateProject = false) {
                    val parameters = JSONObject()
                    mapOf("path" to path, "moduleName" to moduleName, "targetVersion" to targetVersion,
                        "currentVersion" to currentVersion, "minimumSeverity" to minimumSeverity,
                        "ignoreCurrentVersion" to ignoreCurrentVersion, "explainSuppressed" to explainSuppressed, "runId" to runId,
                        "offset" to offset, "limit" to limit,
                        "analysisTargetVersion" to analysisTargetVersion).forEach { (key, value) ->
                        if (value != null) parameters.put(key, value)
                    }
                    MagentoCompatibilityCommands.execute(it, normalizedMode, parameters)
                }
            }
        }
        val structured = if (normalizedMode in setOf("help", "detailed_schema")) null else try {
            JSONObject(response)
        } catch (_: JSONException) {
            JSONObject().put("state", "failed").put("complete", false)
                .put("error", JSONObject().put("code", "unavailable").put("message", response))
                .put("nextCall", JSONObject.NULL)
        }
        return McpToolCallResult(
            content = arrayOf(McpToolCallResultContent.Text(structured?.toString() ?: response)),
            structuredContent = structured?.let { Json.parseToJsonElement(it.toString()).jsonObject },
            isError = structured?.optString("state") == "failed" || structured?.optJSONObject("error") != null
        )
    }

    /**
     * Provides one low-noise entry point for Magento scaffolding.
     */
    @McpTool(name = "magento_scaffold")
    @McpDescription("Magento scaffold library and renderer with three context-conscious modes. Use mode `help` first to get only a compact catalog of scaffoldType values and short descriptions. Use mode `detailed_schema` with one scaffoldType to load the detailed required parameters, optional parameters, defaults, constraints, and example parametersJson only for that scaffold. Use mode `render` with scaffoldType and parametersJson to create files in the currently opened Magento project. Supported scaffoldType values are `module`, `plugin`, `observer`, `entity_crud`, `controller`, `cli_command`, `block`, `view_model`, `product_eav_attribute`, `category_eav_attribute`, and `customer_eav_attribute`. Prefer this single tool over generator-specific tools because it keeps the agent context small: list, load one schema, then render. Most scaffold types require `moduleName` in Magento `Vendor_Module` format; PHP class names must be FQNs such as `Foo\\Bar\\Block\\Product\\BadgeBlock`; JSON backslashes must be escaped.")
    suspend fun magentoScaffold(
        mode: String,
        scaffoldType: String,
        parametersJson: String
    ): String {
        return when (mode.trim().lowercase().replace('-', '_').replace(' ', '_')) {
            "help" -> MagentoScaffoldCommands.help()
            "schema", "detailed_schema" -> MagentoScaffoldCommands.detailedSchema(scaffoldType)
            "render" -> MagentoMcpToolsetSupport.withProjectEdtAction {
                MagentoScaffoldCommands.render(it, scaffoldType, parametersJson)
            }
            else -> "mode must be `help`, `detailed_schema`, or `render`. Use `help` to view the compact Magento scaffold catalog."
        }
    }

    /**
     * Provides one low-noise entry point for Magento project inspections.
     */
    @McpTool(name = "magento_inspect")
    @McpDescription("Magento inspection library with three context-conscious modes. Use mode `help` first to get only a compact catalog of queryType values and short descriptions. Use mode `detailed_schema` with one queryType to load detailed required parameters, accepted values, and example parametersJson only for that inspection. Use mode `query` with queryType and parametersJson to run the inspection in the currently opened Magento project. Supported queryType values are `module`, `di_config`, `plugins_for_method`, `observers_for_event`, `layout_entities`, `ui_component`, and `acl_or_menu`. Prefer this single tool over individual finder tools because it keeps the agent context small: list, inspect one schema, then query.")
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
