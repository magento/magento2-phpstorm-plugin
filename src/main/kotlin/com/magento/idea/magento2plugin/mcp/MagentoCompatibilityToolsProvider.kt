/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.mcpserver.McpTool
import com.intellij.mcpserver.McpToolCallResult
import com.intellij.mcpserver.McpToolCategory
import com.intellij.mcpserver.McpToolDescriptor
import com.intellij.mcpserver.McpToolSchema
import com.intellij.mcpserver.McpToolsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.json.JSONException
import org.json.JSONObject

/** Keeps a typed discovery schema while validating raw arguments inside the plugin. */
class MagentoCompatibilityToolsProvider : McpToolsProvider {
    override fun getTools(): List<McpTool> = listOf(CompatibilityTool())

    private class CompatibilityTool : McpTool {
        override val descriptor = McpToolDescriptor(
            name = "magento_compatibility",
            description = DESCRIPTION,
            category = McpToolCategory("MagentoMcpToolset", MagentoMcpToolset::class.java.name, false, false),
            fullyQualifiedName = "${MagentoMcpToolset::class.java.name}.magentoCompatibility",
            inputSchema = McpToolSchema(propertiesSchema = schema(), requiredProperties = emptySet(), definitions = emptyMap())
        )

        override suspend fun call(args: JsonObject): McpToolCallResult {
            val parameters = JSONObject(args.toString())
            val rawMode = parameters.remove("mode") ?: "status"
            if (rawMode !is String || rawMode.isBlank()) return MagentoCompatibilityResponse.wire(
                MagentoCompatibilityResponse.failure("unknown", "invalid_request", "mode must be a non-empty string.", "mode"))
            val mode = rawMode.trim().lowercase().replace('-', '_')
            val projectPath = parameters.remove("projectPath")
            if (projectPath != null && (projectPath !is String || projectPath.isBlank())) return MagentoCompatibilityResponse.wire(
                MagentoCompatibilityResponse.failure(mode, "invalid_request", "projectPath must be a non-empty string.", "projectPath"))
            // Projectless documentation remains available, but a supplied path must be validated.
            if (mode in setOf("help", "detailed_schema") && projectPath == null) {
                if (parameters.length() != 0) return MagentoCompatibilityResponse.wire(
                    MagentoCompatibilityResponse.failure(mode, "invalid_request", "Documentation accepts only mode and projectPath."))
                val documentation = if (mode == "help") MagentoCompatibilityCommands.help() else MagentoCompatibilityCommands.detailedSchema()
                return MagentoCompatibilityResponse.wire(MagentoCompatibilityResponse.envelope(JSONObject().put("documentation", documentation), mode))
            }
            val response = withContext(if (mode in setOf("releases", "upgrade")) Dispatchers.IO else currentCoroutineContext()) {
                MagentoMcpToolsetSupport.withProjectAction(validateProject = false) { project ->
                    // Normally consumed by the host; also validate it for direct provider callers.
                    if (projectPath != null && projectPath != project.basePath) {
                        MagentoCompatibilityResponse.failure(mode, "invalid_request", "projectPath must match the connected project: ${project.basePath}", "projectPath").toString()
                    } else {
                        val body = JSONObject(MagentoCompatibilityCommands.execute(project, mode, parameters))
                        if (mode in setOf("help", "detailed_schema")) {
                            body.put("projectPath", project.basePath).put("pathBase", project.basePath)
                        }
                        body.toString()
                    }
                }
            }
            val body = try { JSONObject(response) } catch (_: JSONException) {
                MagentoCompatibilityResponse.failure(mode, "unavailable", response)
            }
            return MagentoCompatibilityResponse.wire(MagentoCompatibilityResponse.envelope(body, mode))
        }
    }

    companion object {
        const val DESCRIPTION = "Analyze Magento upgrade compatibility using built-in PHP/XML inspections, including PHTML/HTML. Always send projectPath (the IDE project directory), including for help and detailed_schema. For an upgrade check, use mode=upgrade with exactly one path/moduleName: it resolves the next published feature release when targetVersion is omitted, defaults ignoreCurrentVersion/explainSuppressed to true, and returns continuations through readiness, preparation, and analysis with the full request preserved. Follow nextStep/nextCall until done. Start with status to verify the project and composer.lock baseline; for a relative target use releases, which returns nextMinorRelease without the full catalog by default (includeReleases=true opts in). Use status with the exact targetVersion and follow nextCall to prepare missing official Magento Open Source data. Preserve preparation continuation arguments. Analyze exactly one path or moduleName once readiness.ready=true. Every plugin reply, including help and errors, is one JSON text object; parse content[0].text. Failed requests/jobs set isError=true. Invalid argument types are validated by the plugin; JetBrains project-routing errors occur before it and can still be plain text. Missing coverage returns coverage_required, state=blocked, error.recoverable=true and a preparation nextCall; invalid_request is terminal. Use nextStep to continue: poll waits retryAfterMs then follows nextCall; call follows nextCall immediately, including blocked coverage, completed preparation and pagination. analyze requires readiness.ready=true; select_target needs an exact target; resolve_error requires correcting the error; enable_support requires Magento support; done has no continuation. Always follow a non-null nextCall even when terminal=true or pollRequired=false. successful and complete describe this response/job, not readiness or the whole upgrade workflow. Terminal jobs are automatically exported to JSON in the IDE system cache; report contains the path and retention. Results remain accessible by runId after memory eviction or IDE restart for up to 30 days / 100 archived runs per operation. Cached preparation has runId=null and a status continuation. Default findings include baseline issues and component tests; ignoreCurrentVersion=true suppresses baseline issues, explainSuppressed adds counts. pathBase is the IDE project directory: for a nested Magento root use src/app/code/Vendor/Module, not app/code/Vendor/Module. Invalid paths include pathBase and, when possible, suggestedPath. Prepared data excludes proprietary Commerce and third-party dependencies; zero findings does not certify an upgrade. Use detailed_schema for constraints."

        private fun schema(): JsonObject {
            val properties = JSONObject()
            fun field(name: String, type: String, description: String, minimum: Int? = null, maximum: Int? = null) {
                properties.put(name, JSONObject().put("type", type).put("description", description).apply {
                    if (minimum != null) put("minimum", minimum)
                    if (maximum != null) put("maximum", maximum)
                })
            }
            field("projectPath", "string", "Absolute IDE project directory used for host routing. Always send when known, including help and detailed_schema; use the project directory, not a nested Magento root.")
            field("mode", "string", "status (default), upgrade, releases, prepare, analyze, results, cancel, help, detailed_schema.")
            field("path", "string", "Upgrade/analyze or continuation: PHP/PHTML/XML/HTML file or component directory, absolute or relative to pathBase (IDE project directory). For Magento in src/, use src/app/code/Vendor/Module. Exactly one of path/moduleName.")
            field("moduleName", "string", "Upgrade/analyze or continuation: exact Vendor_Module. Exactly one of path/moduleName.")
            field("targetVersion", "string", "Exact Magento release. Upgrade: omit to select the next published feature release. Prepare builds this release's indexes.")
            field("currentVersion", "string", "Explicit baseline, otherwise composer.lock. Preserve in preparation continuations.")
            field("minimumSeverity", "string", "Scan/continuation: warning (default), error or critical.")
            field("ignoreCurrentVersion", "boolean", "Suppress baseline issues; upgrade defaults true, other modes false. Preserve in continuations.")
            field("explainSuppressed", "boolean", "Scan/continuation: include raw suppressedDiagnostics; upgrade defaults to ignoreCurrentVersion. Requires ignoreCurrentVersion=true.")
            field("includeReleases", "boolean", "Releases: include the full dated catalog; default false, returning only nextMinorRelease and metadata.")
            field("runId", "string", "Results/cancel: exact returned ID; archived terminal runs remain accessible within report retention.")
            field("offset", "integer", "Results: zero-based offset, default 0. Numeric strings are accepted; fractions/overflow return JSON validation errors.", 0, Int.MAX_VALUE)
            field("limit", "integer", "Results: page size, default 100.", 1, 500)
            field("analysisTargetVersion", "string", "Preparation/results/cancel: original upgrade target, distinct from prepared release. Preserve all nextCall arguments, including scope and scan options.")
            return Json.parseToJsonElement(properties.toString()).jsonObject
        }
    }
}
