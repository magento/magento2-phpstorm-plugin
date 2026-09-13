/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.magento.idea.magento2plugin.indexes.ModuleIndex
import com.magento.idea.magento2plugin.project.Settings
import com.magento.idea.magento2uct.analysis.UctAnalysisRequest
import com.magento.idea.magento2uct.analysis.UctAnalysisRuns
import com.magento.idea.magento2uct.analysis.UctIndexCatalog
import com.magento.idea.magento2uct.packages.IssueSeverityLevel
import com.magento.idea.magento2uct.settings.UctSettingsService
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.nio.file.Files
import java.nio.file.Path
import java.io.IOException

internal object MagentoCompatibilityCommands {
    fun help(): String = """
        Magento backward / upgrade compatibility analysis using the built-in UCT PHP/XML inspections.
        Modes: `help`, `detailed_schema`, `status`, `analyze`, `results`, `cancel`.
        Read `detailed_schema`, then `status` to discover bundled index coverage before choosing versions.
        `analyze` starts a background run. Use its runId with `results` until completed, failed, or cancelled.
        Analysis uses committed IDE documents and does not save files or change UCT settings.
    """.trimIndent()

    fun detailedSchema(): String = """
        All calls use mode and parametersJson (a JSON object string; use {} when no parameters are needed).
        status: no parameters. Returns pluginEnabled, uctEnabled, indexing, supportedVersions, activeRunIds, and configured defaults.
        analyze: exactly one of path or moduleName. path is a project-relative or absolute PHP/XML file, module/theme directory, or directory containing components; moduleName is exact Vendor_Module.
          targetVersion: required unless configured in UCT settings; must be in status.supportedVersions.
          currentVersion: optional; defaults to configured UCT current version. Must not exceed targetVersion.
          minimumSeverity: warning (default), error, or critical.
          ignoreCurrentVersion: false (default). When true, currentVersion must also have complete index coverage.
          Explicit analysis works even when editor UCT inspections are disabled. Magento project support must be enabled and indexing finished.
          Directory scans exclude bundled Magento components and symlink directories. No components/files is an error.
          Example parametersJson: {"moduleName":"Foo_Bar","targetVersion":"2.4.3","minimumSeverity":"warning"}
          The example version is illustrative: use status.supportedVersions for the installed plugin's coverage.
        results: runId required; offset defaults to 0; limit defaults to 100 (1..500). Pages are available after completion.
          Returns state, progress, version options, totals by severity, findings with project-relative filePath, 1-based line/column, code, severity, and message, plus nextOffset and hasMore.
          Example parametersJson: {"runId":"<returned runId>","offset":0,"limit":100}
        cancel: runId required. Idempotent; cancelled scans never report complete results.
        One running analysis per project; the latest five runs are retained until project close. A scan is limited to 10000 files and 50000 findings; exceeding either fails with a narrower-scope request.
        Coverage is limited to the shipped historical UCT indexes and existing PHP/XML rules; zero findings does not certify all upgrade behavior.
    """.trimIndent()

    fun execute(project: Project, mode: String, parametersJson: String): String = try {
        val parameters = JSONObject(parametersJson.ifBlank { "{}" })
        val result = when (mode) {
            "status" -> {
                validateKeys(parameters, emptySet())
                status(project)
            }
            "analyze" -> {
                require(Settings.isEnabled(project)) { "Magento plugin support is disabled for this project." }
                require(!DumbService.isDumb(project)) { "Indexes are not ready. Wait for indexing to finish and retry." }
                val request = request(project, parameters)
                // Validate coverage before accepting a job. The catalog is cached and reads no remote data.
                UctIndexCatalog.bundled().validateVersions(request.currentVersion, request.targetVersion, request.ignoreCurrentVersion)
                view(project, project.getService(UctAnalysisRuns::class.java).start(request), 0, 100)
            }
            "results" -> {
                validateKeys(parameters, setOf("runId", "offset", "limit"))
                val offset = integer(parameters, "offset", 0, 0, Int.MAX_VALUE)
                val limit = integer(parameters, "limit", 100, 1, 500)
                view(project, project.getService(UctAnalysisRuns::class.java).results(requiredString(parameters, "runId")), offset, limit)
            }
            "cancel" -> {
                validateKeys(parameters, setOf("runId"))
                view(project, project.getService(UctAnalysisRuns::class.java).cancel(requiredString(parameters, "runId")), 0, 100)
            }
            else -> throw IllegalArgumentException("Unknown mode. Use help, detailed_schema, status, analyze, results, or cancel.")
        }
        result.toString()
    } catch (exception: JSONException) {
        error("invalid_parameters", "parametersJson must be a JSON object with the documented parameter types. ${exception.message}")
    } catch (exception: IllegalArgumentException) {
        error("invalid_request", exception.message.orEmpty())
    } catch (exception: IllegalStateException) {
        error("unavailable", exception.message.orEmpty())
    } catch (exception: IOException) {
        error("unavailable", exception.message.orEmpty())
    }

    private fun status(project: Project): JSONObject {
        val settings = UctSettingsService.getInstance(project)
        return JSONObject()
            .put("pluginEnabled", Settings.isEnabled(project))
            .put("uctEnabled", settings.isEnabled)
            .put("indexing", DumbService.isDumb(project))
            .put("supportedVersions", JSONArray(UctIndexCatalog.bundled().supportedVersions))
            .put("indexSource", "bundled")
            .put("coverage", "Historical PHP/XML UCT indexes; only listed versions have all three index families.")
            .put("activeRunIds", JSONArray(project.getService(UctAnalysisRuns::class.java).activeRunIds()))
            .put("defaults", JSONObject()
                .put("currentVersion", settings.configuredCurrentVersion ?: JSONObject.NULL)
                .put("targetVersion", settings.configuredTargetVersion ?: JSONObject.NULL))
    }

    internal fun request(project: Project, parameters: JSONObject): UctAnalysisRequest {
        validateKeys(parameters, setOf("path", "moduleName", "currentVersion", "targetVersion", "minimumSeverity", "ignoreCurrentVersion"))
        val moduleName = optionalString(parameters, "moduleName")
        val path = optionalString(parameters, "path")
        require((moduleName == null) != (path == null)) { "Provide exactly one of path or moduleName." }
        val root = Path.of(requireNotNull(project.basePath) { "Project directory is unavailable." }).toRealPath()
        val resolved = if (moduleName != null) {
            require(Regex("[A-Za-z][A-Za-z0-9]*_[A-Za-z][A-Za-z0-9_]*").matches(moduleName)) { "moduleName must use Vendor_Module format." }
            ReadAction.computeBlocking<Path, RuntimeException> {
                val directory = ModuleIndex(project).getModuleDirectoryVirtualFileByModuleName(moduleName)
                requireNotNull(directory) { "Magento module `$moduleName` was not found." }
                Path.of(directory.path)
            }
        } else root.resolve(path!!)
        require(Files.exists(resolved)) { "Analysis path does not exist: $resolved" }
        val canonical = resolved.toRealPath()
        require(canonical.startsWith(root)) { "Analysis path must stay inside the current project." }
        val settings = UctSettingsService.getInstance(project)
        val target = optionalString(parameters, "targetVersion") ?: settings.configuredTargetVersion
        require(!target.isNullOrBlank()) { "Provide targetVersion or configure a UCT target version." }
        val severity = optionalString(parameters, "minimumSeverity") ?: "warning"
        require(severity in setOf("warning", "error", "critical")) { "minimumSeverity must be warning, error, or critical." }
        val ignore = if (parameters.has("ignoreCurrentVersion")) {
            require(parameters.get("ignoreCurrentVersion") is Boolean) { "ignoreCurrentVersion must be a boolean." }
            parameters.getBoolean("ignoreCurrentVersion")
        } else false
        return UctAnalysisRequest(
            listOf(canonical.toString()), optionalString(parameters, "currentVersion") ?: settings.configuredCurrentVersion,
            target, IssueSeverityLevel.valueOf(severity.uppercase()), ignore
        )
    }

    private fun view(project: Project, view: UctAnalysisRuns.View, offset: Int, limit: Int): JSONObject {
        val result = view.result
        val findings = result?.findings.orEmpty()
        val end = (offset.toLong() + limit).coerceAtMost(findings.size.toLong()).toInt()
        val page = if (offset >= findings.size) emptyList() else findings.subList(offset, end)
        val root = Path.of(project.basePath!!)
        return JSONObject().put("runId", view.runId).put("state", view.state)
            .put("complete", view.state == "completed")
            .put("paths", JSONArray(view.request.paths.map { root.relativize(Path.of(it)).toString().replace('\\', '/').ifEmpty { "." } }))
            .put("currentVersion", view.request.currentVersion ?: JSONObject.NULL)
            .put("targetVersion", view.request.targetVersion)
            .put("minimumSeverity", view.request.minimumSeverity.name.lowercase())
            .put("ignoreCurrentVersion", view.request.ignoreCurrentVersion)
            .put("processedFiles", view.progress.processedFiles).put("totalFiles", view.progress.totalFiles)
            .put("error", if (view.error == null) JSONObject.NULL else JSONObject().put("code", "analysis_failed").put("message", view.error))
            .put("summary", if (result == null) JSONObject.NULL else JSONObject()
                .put("totalIssues", findings.size).put("modules", result.modules).put("themes", result.themes)
                .put("warning", findings.count { it.issue.level == IssueSeverityLevel.WARNING })
                .put("error", findings.count { it.issue.level == IssueSeverityLevel.ERROR })
                .put("critical", findings.count { it.issue.level == IssueSeverityLevel.CRITICAL }))
            .put("findings", JSONArray(page.map { finding ->
                JSONObject().put("filePath", root.relativize(Path.of(finding.filePath)).toString().replace('\\', '/'))
                    .put("line", finding.line).put("column", finding.column).put("code", finding.issue.code)
                    .put("severity", finding.issue.level.name.lowercase()).put("message", finding.message)
            }))
            .put("hasMore", end < findings.size)
            .put("nextOffset", if (end < findings.size) end else JSONObject.NULL)
    }

    private fun optionalString(parameters: JSONObject, key: String): String? {
        if (!parameters.has(key)) return null
        val value = parameters.getString(key).trim()
        require(value.isNotEmpty()) { "$key must be a non-empty string." }
        return value
    }
    private fun requiredString(parameters: JSONObject, key: String): String =
        requireNotNull(optionalString(parameters, key)) { "Missing required parameter `$key`." }

    private fun integer(parameters: JSONObject, key: String, default: Int, min: Int, max: Int): Int {
        if (!parameters.has(key)) return default
        val value = parameters.get(key)
        require((value is Int || value is Long) && (value as Number).toLong() in min.toLong()..max.toLong()) {
            "$key must be an integer in $min..$max."
        }
        return (value as Number).toInt()
    }
    private fun validateKeys(parameters: JSONObject, allowed: Set<String>) {
        val unknown = parameters.keySet() - allowed
        require(unknown.isEmpty()) { "Unknown parameters: ${unknown.sorted().joinToString()}. Use detailed_schema." }
    }
    private fun error(code: String, message: String): String = JSONObject()
        .put("state", "failed").put("complete", false)
        .put("error", JSONObject().put("code", code).put("message", message)).toString()
}
