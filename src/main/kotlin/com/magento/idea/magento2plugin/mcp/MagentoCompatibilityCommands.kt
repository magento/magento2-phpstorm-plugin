/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.magento.idea.magento2plugin.indexes.ModuleIndex
import com.magento.idea.magento2plugin.project.Settings
import com.magento.idea.magento2plugin.util.magento.MagentoVersionUtil
import com.magento.idea.magento2uct.analysis.UctAnalysisRequest
import com.magento.idea.magento2uct.analysis.UctAnalysisRuns
import com.magento.idea.magento2uct.analysis.UctAnalysisService
import com.magento.idea.magento2uct.analysis.UctIndexCatalog
import com.magento.idea.magento2uct.analysis.UctReleasePreparation
import com.magento.idea.magento2uct.analysis.UctReleaseSource
import com.magento.idea.magento2uct.analysis.UctVersions
import com.magento.idea.magento2uct.analysis.UctPublishedReleases
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
        Magento backward / upgrade compatibility analysis using the built-in UCT PHP/XML inspections, including PHTML and HTML templates.
        Modes: `help`, `detailed_schema`, `status`, `releases`, `prepare`, `analyze`, `results`, `cancel`.
        Start with `status` and targetVersion to verify the connected project, detected baseline, IDE readiness, and release-data readiness.
        Follow nextCall to prepare missing release data, poll progress, and return to status with the original target and baseline options.
        Pass ordinary typed tool arguments; no scripts, shell commands, or JSON string encoding are needed.
        `prepare` downloads the exact official Magento Open Source tag and builds all three indexes in the IDE cache. `analyze` starts a background scan. Follow nextCall with `results` until completed, failed, or cancelled, then retrieve remaining pages.
        Use `detailed_schema` only for additional constraints. Never substitute an older target when the requested release is unsupported.
        Parse JSON from text content when structuredContent is absent. Failed requests/jobs set isError=true; JetBrains omits structuredContent for errors. Host argument-type and project-routing errors can be plain text.
        Running/cancelled jobs are incomplete. Cached preparation has runId=null and needs no polling.
        supportedVersions describes cached compatibility coverage, not a published-release catalog. Use releases to verify the intended published release; prepare verifies the exact official tag and can fail if it does not exist.
        Default findings include baseline issues; use ignoreCurrentVersion=true to suppress them when both releases are covered.
        Analysis uses committed IDE documents and does not save files or change UCT settings.
    """.trimIndent()

    fun detailedSchema(): String = """
        Use typed tool arguments. mode defaults to status; all other arguments are optional until required by a mode.
        status: optional targetVersion, currentVersion, ignoreCurrentVersion. Returns projectPath, magentoRoot, pluginVersion, ideVersion, detectedMagentoVersion and its local source,
          pluginEnabled, uctEnabled, indexing, supportedVersions, preparedVersions, activeRunIds, and effective defaults including targetVersion and ignoreCurrentVersion.
          readiness separates ideReady and releaseDataReady, lists missingVersions, and reports ready for the requested analysis.
          Status does not query published releases. supportedVersions/latestSupportedVersion describe local index coverage only; missing coverage does not establish whether a release exists.
          Follow nextCall to prepare a missing release or observe an active job; preparation continuations preserve the original status options.
        releases: optional currentVersion (otherwise composer.lock). Fetches stable published releases from Published releases URL in Magento settings (default: official magento/magento2 GitHub releases API).
          The URL can point to a GitHub-compatible catalog mirror; changing it invalidates cached discovery data.
          Returns edition, sourceUrl, fetchedAt, expiresAt, cacheHit, dated releases and nextMinorRelease (next x.y.z feature release, excluding -p patches).
          Uses a one-hour cache; failed or partial fetches are errors, never an empty success. Separate from local supportedVersions; status never performs network discovery.
        prepare: targetVersion required. Downloads that exact official Magento Open Source GitHub tag, verifies its metadata,
          and reuses the UCT processors to build existence, API, and deprecation snapshots in the IDE system cache.
          Returns a background runId with operation=prepare and phase/byte/file progress. results and cancel work for both operations.
          Repeating preparation returns the active job or cached ready release. Cached responses are completed with runId=null; follow their status nextCall without polling.
          analysisTargetVersion: optional original upgrade target for the follow-up status call, defaulting to the prepared release for standalone prepare calls.
          currentVersion and ignoreCurrentVersion: optional baseline status options. nextCall preserves all three options through preparation/results/cancel.
          Cache survives restart; failed/cancelled work is never ready. Failed/cancelled preparation has no polling nextCall; inspect its state-specific nextAction before retrying.
          No PHP execution, Composer installation, project writes, or extra IDE instance is needed.
          Adobe Commerce proprietary extensions and third-party dependencies are outside this source coverage.
          Verify projectPath matches the requested project. A mismatch requires correcting the MCP connection, not creating another IDE instance or a script.
        analyze: exactly one of path or moduleName. path is a project-relative or absolute PHP, PHTML, XML, or HTML file recognized by IDE PHP/XML PSI, module/theme directory, or directory containing components; moduleName is exact Vendor_Module.
          targetVersion: required; prepare missing compatibility data first. A prepared target also requires a covered explicit/detected baseline when one is provided.
          currentVersion: optional; defaults to Magento detected from composer.lock in magentoRoot. No configured-version or composer.json fallback. Must not exceed targetVersion.
          minimumSeverity: warning (default), error, or critical.
          ignoreCurrentVersion: false (default). When true, currentVersion must also have complete index coverage.
          explainSuppressed: false (default); requires ignoreCurrentVersion=true. Returns baseline suppression totals by rule code and severity, counted before per-element severity filtering.
          Explicit analysis works even when editor UCT inspections are disabled. Magento project support must be enabled and indexing finished.
          Directory scans exclude bundled Magento components and symlink directories, but include test/fixture files inside custom components. processedFiles/totalFiles count supported IDE PHP/XML PSI files, including PHTML/HTML.
          pathBase is the IDE project directory (not necessarily magentoRoot). Completed results include fileTypeCounts and testFiles, counted by Test/Tests/Fixture/Fixtures/_files path segments.
          analysisIdentity identifies the exact current/target states by SHA-256, source kind, archive checksum (prepared data) and inspection engine revision.
          Unsupported single files are rejected before a job starts; empty/unsupported directory scopes fail during background discovery. Analyze acceptance is not scan completion.
          Example tool arguments: {"mode":"analyze","moduleName":"Foo_Bar","targetVersion":"<requested-version>","minimumSeverity":"warning"}
          Replace <requested-version> with the user's exact target release. Use status and prepare to obtain missing data for that release.
        results: runId required; offset is an integer 0..2147483647 (default 0); limit is an integer 1..500 (default 100). Send JSON integers. The JetBrains host also coerces numeric strings to integers; fractions and overflow are rejected before the tool runs. Pages are available after completion.
          Returns state, progress, version options, totals by severity, findings with project-relative filePath, 1-based line/column, code, severity, and message, plus nextOffset and hasMore.
          Follow nextCall.arguments while running or when hasMore is true; retryAfterMs gives the polling interval.
          For preparation runs, preserve analysisTargetVersion, currentVersion, and ignoreCurrentVersion from nextCall; completion returns the original status call.
          Example tool arguments: {"mode":"results","runId":"<returned runId>","offset":0,"limit":100}
        cancel: runId required. Idempotent; cancelled scans never report complete results. Preparation accepts the same status continuation options as results.
        Plugin responses always include JSON text; structuredContent, when delivered, contains the same JSON. Parse text when structuredContent is absent. JetBrains omits structuredContent on isError=true responses and may disable it for all responses.
        Failed requests/jobs return MCP isError=true; successful status, running, completed, and cancelled responses return false. Host argument deserialization and project routing happen before the plugin and may return plain-text errors. Help/detailed_schema are text only.
        Validation errors include parameter/parameters and acceptedValues where applicable, and nextCall=null. Correct the original request; nextCall is reserved for workflow progress such as preparation, indexing and pagination.
        One running analysis and one running preparation per project; the latest five runs of each operation are retained until project close. Retrieve/save findings before starting more analyses. A scan is limited to 10000 files and 50000 findings; exceeding either fails with a narrower-scope request.
        Coverage uses bundled history and prepared Magento Open Source snapshots with existing PHP/XML rules. Prepared snapshots report observed states, not the first release introducing a change; zero findings does not certify all upgrade behavior.
    """.trimIndent()

    fun execute(project: Project, mode: String, parameters: JSONObject): String = try {
        val result = when (mode) {
            "status" -> {
                validateKeys(parameters, setOf("targetVersion", "currentVersion", "ignoreCurrentVersion"))
                status(project, parameters)
            }
            "releases" -> {
                validateKeys(parameters, setOf("currentVersion"))
                val current = optionalString(parameters, "currentVersion") ?: detectedVersion(project)
                current?.let { validateVersion("currentVersion", it) }
                val releaseData = try {
                    project.getService(UctPublishedReleases::class.java).discover(current)
                } catch (exception: com.intellij.openapi.progress.ProcessCanceledException) {
                    throw exception
                } catch (exception: Exception) {
                    throw IOException("Published release discovery failed: ${exception.message}", exception)
                }
                MagentoMcpProjectContext.identity(project).apply {
                    releaseData.keySet().forEach { key -> put(key, releaseData.get(key)) }
                    put("pathBase", project.basePath ?: JSONObject.NULL)
                    put("nextCall", releaseData.optJSONObject("nextMinorRelease")?.let {
                        nextCall(project, "status", JSONObject().put("targetVersion", it.getString("version")).apply {
                            if (current != null) put("currentVersion", current)
                        })
                    } ?: JSONObject.NULL)
                }
            }
            "prepare" -> {
                validateKeys(parameters, setOf("targetVersion", "analysisTargetVersion", "currentVersion", "ignoreCurrentVersion"))
                require(Settings.isEnabled(project)) { "Magento plugin support is disabled for this project." }
                val version = requiredString(parameters, "targetVersion")
                validateVersion("targetVersion", version)
                val statusParameters = preparationStatusParameters(parameters, version)
                preparationView(project, project.getService(UctReleasePreparation::class.java).start(version), statusParameters)
            }
            "analyze" -> {
                require(Settings.isEnabled(project)) { "Magento plugin support is disabled for this project." }
                require(!DumbService.isDumb(project)) { "Indexes are not ready. Wait for indexing to finish and retry." }
                val request = request(project, parameters)
                // Coverage checks never download data implicitly. Preparation is an explicit MCP operation.
                val catalog = project.getService(UctReleasePreparation::class.java).catalog()
                when {
                    request.targetVersion !in catalog.supportedVersions -> coverageError(project, request, "unsupported_target_version",
                        "Compatibility indexes are unavailable for targetVersion `${request.targetVersion}`.")
                    catalog.requiresCurrentCoverage(request.currentVersion, request.targetVersion, request.ignoreCurrentVersion) && request.currentVersion !in catalog.supportedVersions -> coverageError(project, request, "unsupported_current_version",
                        "This analysis requires compatibility data for currentVersion. Prepare the baseline release first.")
                    else -> {
                        catalog.validateVersions(request.currentVersion, request.targetVersion, request.ignoreCurrentVersion)
                        view(project, project.getService(UctAnalysisRuns::class.java).start(request), 0, 100)
                    }
                }
            }
            "results" -> {
                validateKeys(parameters, setOf("runId", "offset", "limit", "analysisTargetVersion", "currentVersion", "ignoreCurrentVersion"))
                val offset = integer(parameters, "offset", 0, 0, Int.MAX_VALUE)
                val limit = integer(parameters, "limit", 100, 1, 500)
                val runId = requiredString(parameters, "runId")
                val preparation = project.getService(UctReleasePreparation::class.java)
                if (preparation.owns(runId)) {
                    validateKeys(parameters, setOf("runId", "analysisTargetVersion", "currentVersion", "ignoreCurrentVersion"))
                    val view = preparation.results(runId)
                    preparationView(project, view, preparationStatusParameters(parameters, view.version))
                } else {
                    validateKeys(parameters, setOf("runId", "offset", "limit"))
                    view(project, project.getService(UctAnalysisRuns::class.java).results(runId), offset, limit)
                }
            }
            "cancel" -> {
                validateKeys(parameters, setOf("runId", "analysisTargetVersion", "currentVersion", "ignoreCurrentVersion"))
                val runId = requiredString(parameters, "runId")
                val preparation = project.getService(UctReleasePreparation::class.java)
                if (preparation.owns(runId)) {
                    val statusParameters = preparationStatusParameters(parameters, preparation.results(runId).version)
                    preparationView(project, preparation.cancel(runId), statusParameters)
                } else {
                    validateKeys(parameters, setOf("runId"))
                    view(project, project.getService(UctAnalysisRuns::class.java).cancel(runId), 0, 100)
                }
            }
            else -> invalid("mode", "Unknown mode.", listOf("help", "detailed_schema", "status", "releases", "prepare", "analyze", "results", "cancel"))
        }
        result.toString()
    } catch (exception: ParameterException) {
        error(project, "invalid_request", exception.message.orEmpty(), exception.details)
    } catch (exception: JSONException) {
        error(project, "invalid_parameters", "Invalid parameter type. ${exception.message}")
    } catch (exception: IllegalArgumentException) {
        error(project, "invalid_request", exception.message.orEmpty())
    } catch (exception: IllegalStateException) {
        error(project, "unavailable", exception.message.orEmpty())
    } catch (exception: IOException) {
        error(project, "unavailable", exception.message.orEmpty())
    }

    private fun status(project: Project, parameters: JSONObject): JSONObject {
        val settings = UctSettingsService.getInstance(project)
        val detected = detectedVersion(project)
        val preparation = project.getService(UctReleasePreparation::class.java)
        val catalog = preparation.catalog()
        val versions = catalog.supportedVersions
        val current = optionalString(parameters, "currentVersion") ?: detected
        val target = optionalString(parameters, "targetVersion")
        current?.let { validateVersion("currentVersion", it) }
        target?.let { validateVersion("targetVersion", it) }
        if (current != null && target != null) validate(UctVersions.compare(current, target) <= 0, "currentVersion", "currentVersion must not exceed targetVersion.", listOf("<= $target"))
        val ignore = boolean(parameters, "ignoreCurrentVersion", false)
        validate(!ignore || current != null, "ignoreCurrentVersion", "ignoreCurrentVersion requires a detected or explicit currentVersion.", listOf("false", "true with a detected or explicit currentVersion"))
        val missing = if (target == null) emptyList() else missingVersions(catalog, current, target, ignore)
        val active = preparation.activeRunIds()
        val next = when {
            active.isNotEmpty() -> nextCall(project, "results", JSONObject().put("runId", active.first()).apply {
                if (target != null) putPreparationStatusOptions(this, statusOptions(current, target, ignore))
            })
            missing.isNotEmpty() && Settings.isEnabled(project) -> nextCall(project, "prepare",
                putPreparationStatusOptions(JSONObject().put("targetVersion", missing.first()), statusOptions(current, target!!, ignore)))
            target != null && DumbService.isDumb(project) -> nextCall(project, "status", parameters)
            else -> JSONObject.NULL
        }
        return MagentoMcpProjectContext.identity(project)
            .put("pathBase", project.basePath ?: JSONObject.NULL)
            .put("analysisIdentity", if (target != null && missing.isEmpty()) catalog.identity(current, target, ignore) else JSONObject.NULL)
            .put("detectedMagentoVersion", detected ?: JSONObject.NULL)
            .put("magentoVersionSource", if (detected != null) "composer.lock" else JSONObject.NULL)
            .put("versionDetectionWarning", if (detected == null) "Magento version could not be read from composer.lock. Provide currentVersion explicitly when needed." else JSONObject.NULL)
            .put("pluginEnabled", Settings.isEnabled(project))
            .put("uctEnabled", settings.isEnabled)
            .put("indexing", DumbService.isDumb(project))
            .put("supportedVersions", JSONArray(versions))
            .put("latestSupportedVersion", versions.lastOrNull() ?: JSONObject.NULL)
            .put("currentVersionCovered", current != null && current in versions)
            .put("indexSource", if (preparation.preparedVersions().isEmpty()) "bundled" else "bundled_and_prepared")
            .put("preparedVersions", JSONArray(preparation.preparedVersions()))
            .put("cacheWarnings", JSONObject(preparation.warnings()))
            .put("coverage", "Bundled history and verified Magento Open Source release snapshots; proprietary Commerce extensions and third-party dependencies are excluded from prepared data.")
            .put("releaseAvailabilityNote", "Status reports local compatibility coverage, not published releases. Missing data does not mean a release exists or is unavailable; prepare verifies the exact official tag and may fail if it does not exist.")
            .put("activeRunIds", JSONArray(project.getService(UctAnalysisRuns::class.java).activeRunIds() + active))
            .put("readiness", JSONObject().put("targetVersion", target ?: JSONObject.NULL)
                .put("ideReady", !DumbService.isDumb(project)).put("releaseDataReady", target != null && missing.isEmpty())
                .put("ready", Settings.isEnabled(project) && !DumbService.isDumb(project) && target != null && missing.isEmpty())
                .put("missingVersions", JSONArray(missing)))
            .put("nextCall", next)
            .put("retryAfterMs", if (target != null && DumbService.isDumb(project) && missing.isEmpty()) 500 else JSONObject.NULL)
            .put("defaults", JSONObject()
                .put("currentVersion", current ?: JSONObject.NULL)
                .put("currentVersionSource", if (parameters.has("currentVersion")) "explicit" else if (detected != null) "composer.lock" else JSONObject.NULL)
                .put("targetVersion", target ?: JSONObject.NULL)
                .put("ignoreCurrentVersion", ignore))
            .put("nextAction", when {
                !Settings.isEnabled(project) -> "Enable Magento project support before preparation or analysis."
                target == null -> "Call status with the requested targetVersion to obtain analysis readiness and the next preparation call."
                missing.isNotEmpty() -> "Follow nextCall to prepare missing release data. The official tag has not been verified by this status call; preparation can fail if it does not exist. Repeat this status request after preparation completes."
                DumbService.isDumb(project) -> "IDE indexing is running. Follow nextCall until readiness.ready is true."
                else -> "Release data and IDE indexes are ready. Call analyze with path or moduleName and these version options."
            })
    }

    internal fun request(project: Project, parameters: JSONObject): UctAnalysisRequest {
        validateKeys(parameters, setOf("path", "moduleName", "currentVersion", "targetVersion", "minimumSeverity", "ignoreCurrentVersion", "explainSuppressed"))
        val moduleName = optionalString(parameters, "moduleName")
        val path = optionalString(parameters, "path")
        if ((moduleName == null) == (path == null)) throw ParameterException(
            "Provide exactly one of path or moduleName.", JSONObject().put("parameters", JSONArray(listOf("path", "moduleName")))
                .put("acceptedValues", JSONArray(listOf("exactly one of path or moduleName"))))
        val root = Path.of(requireNotNull(project.basePath) { "Project directory is unavailable." }).toRealPath()
        val resolved = if (moduleName != null) {
            validate(Regex("[A-Za-z][A-Za-z0-9]*_[A-Za-z][A-Za-z0-9_]*").matches(moduleName), "moduleName", "moduleName must use Vendor_Module format.", listOf("Vendor_Module"))
            ReadAction.computeBlocking<Path, RuntimeException> {
                val directory = ModuleIndex(project).getModuleDirectoryVirtualFileByModuleName(moduleName)
                if (directory == null) invalid("moduleName", "Magento module `$moduleName` was not found.", listOf("an installed Vendor_Module name"))
                Path.of(directory.path)
            }
        } else root.resolve(path!!)
        validate(Files.exists(resolved), "path", "Analysis path does not exist: $resolved", listOf("existing project-relative or absolute path"))
        val canonical = resolved.toRealPath()
        validate(canonical.startsWith(root), "path", "Analysis path must stay inside the current project.", listOf("path inside $root"))
        if (!Files.isDirectory(canonical)) {
            validate(Files.isRegularFile(canonical), "path", "Analysis path must be a regular file or component directory.", listOf("regular file", "component directory"))
            ReadAction.computeBlocking<Unit, RuntimeException> {
                val file = requireNotNull(LocalFileSystem.getInstance().findFileByNioFile(canonical)) {
                    "Analysis file is unavailable in the IDE: $canonical. Wait for the IDE to refresh files and retry."
                }
                validate(UctAnalysisService.isSupportedFile(project, file), "path",
                    "Unsupported analysis file: $canonical. Select a PHP, PHTML, XML, or HTML file recognized by IDE PHP/XML PSI. No scan was started.",
                    listOf("PHP", "PHTML", "XML", "HTML"))
            }
        }
        val target = requiredString(parameters, "targetVersion")
        val severity = optionalString(parameters, "minimumSeverity") ?: "warning"
        validate(severity in setOf("warning", "error", "critical"), "minimumSeverity", "minimumSeverity must be warning, error, or critical.", listOf("warning", "error", "critical"))
        val ignore = boolean(parameters, "ignoreCurrentVersion", false)
        val current = optionalString(parameters, "currentVersion") ?: detectedVersion(project)
        validateVersion("targetVersion", target)
        current?.let { validateVersion("currentVersion", it) }
        if (current != null) validate(UctVersions.compare(current, target) <= 0, "currentVersion", "currentVersion must not exceed targetVersion.", listOf("<= $target"))
        validate(!ignore || current != null, "ignoreCurrentVersion", "ignoreCurrentVersion requires a detected or explicit currentVersion.", listOf("false", "true with a detected or explicit currentVersion"))
        val explain = boolean(parameters, "explainSuppressed", false)
        validate(!explain || ignore, "explainSuppressed", "explainSuppressed requires ignoreCurrentVersion=true.", listOf("false", "true with ignoreCurrentVersion=true"))
        return UctAnalysisRequest(listOf(canonical.toString()), current, target, IssueSeverityLevel.valueOf(severity.uppercase()), ignore, explain)
    }

    private fun view(project: Project, view: UctAnalysisRuns.View, offset: Int, limit: Int): JSONObject {
        val result = view.result
        val findings = result?.findings.orEmpty()
        val end = (offset.toLong() + limit).coerceAtMost(findings.size.toLong()).toInt()
        val page = if (offset >= findings.size) emptyList() else findings.subList(offset, end)
        val root = Path.of(project.basePath!!)
        return MagentoMcpProjectContext.identity(project).put("operation", "analyze").put("runId", view.runId).put("state", view.state)
            .put("complete", view.state == "completed")
            .put("paths", JSONArray(view.request.paths.map { root.relativize(Path.of(it)).toString().replace('\\', '/').ifEmpty { "." } }))
            .put("currentVersion", view.request.currentVersion ?: JSONObject.NULL)
            .put("targetVersion", view.request.targetVersion)
            .put("minimumSeverity", view.request.minimumSeverity.name.lowercase())
            .put("ignoreCurrentVersion", view.request.ignoreCurrentVersion)
            .put("explainSuppressed", view.request.explainSuppressed)
            .put("pathBase", project.basePath ?: JSONObject.NULL)
            .put("analysisIdentity", result?.analysisIdentity?.let(::JSONObject) ?: JSONObject.NULL)
            .put("scope", if (result == null) JSONObject.NULL else JSONObject()
                .put("fileTypeCounts", JSONObject(result.fileTypeCounts)).put("testFiles", result.testFiles)
                .put("testFileClassification", "Case-insensitive Test/Tests/Fixture/Fixtures/_files path segments relative to pathBase.")
                .put("includesTests", true))
            .put("suppression", result?.suppressedByRule?.let { rules -> JSONObject()
                .put("reason", "already_present_in_current_version")
                .put("total", rules.values.sum()).put("byRule", JSONObject(rules.mapKeys { it.key.toString() }))
                .put("bySeverity", JSONObject(result.suppressedBySeverity.orEmpty()))
                .put("counting", "Distinct rule diagnostics before per-element severity filtering; totals need not equal the difference between displayed finding counts.")
            } ?: JSONObject.NULL)
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
            .put("nextCall", when {
                view.state == "running" -> nextCall(project, "results", JSONObject().put("runId", view.runId).put("offset", offset).put("limit", limit))
                view.state == "completed" && end < findings.size -> nextCall(project, "results", JSONObject().put("runId", view.runId).put("offset", end).put("limit", limit))
                else -> JSONObject.NULL
            })
            .put("retryAfterMs", if (view.state == "running") 500 else JSONObject.NULL)
    }

    private fun optionalString(parameters: JSONObject, key: String): String? {
        if (!parameters.has(key)) return null
        validate(parameters.get(key) is String, key, "$key must be a string.", listOf("string"))
        val value = parameters.getString(key).trim()
        validate(value.isNotEmpty(), key, "$key must be a non-empty string.", listOf("non-empty string"))
        return value
    }
    private fun requiredString(parameters: JSONObject, key: String): String =
        optionalString(parameters, key) ?: invalid(key, "Missing required parameter `$key`.", listOf("non-empty string"))

    private fun integer(parameters: JSONObject, key: String, default: Int, min: Int, max: Int): Int {
        if (!parameters.has(key)) return default
        val value = parameters.get(key)
        validate((value is Int || value is Long) && (value as Number).toLong() in min.toLong()..max.toLong(),
            key, "$key must be an integer in $min..$max.", listOf("integer $min..$max"))
        return (value as Number).toInt()
    }
    private fun validateKeys(parameters: JSONObject, allowed: Set<String>) {
        val unknown = parameters.keySet() - allowed
        if (unknown.isNotEmpty()) throw ParameterException("Unknown parameters: ${unknown.sorted().joinToString()}. Use detailed_schema.",
            JSONObject().put("parameters", JSONArray(unknown.sorted())).put("acceptedValues", JSONArray(allowed.sorted())))
    }
    private fun nextCall(project: Project, mode: String, arguments: JSONObject = JSONObject()): JSONObject = JSONObject()
        .put("tool", "magento_compatibility")
        .put("arguments", JSONObject(arguments.toString()).put("mode", mode).put("projectPath", project.basePath ?: JSONObject.NULL))

    private fun statusOptions(current: String?, target: String, ignore: Boolean): JSONObject =
        JSONObject().put("targetVersion", target).put("ignoreCurrentVersion", ignore).apply {
            if (current != null) put("currentVersion", current)
        }

    private fun preparationStatusParameters(parameters: JSONObject, preparedVersion: String): JSONObject {
        val target = optionalString(parameters, "analysisTargetVersion") ?: preparedVersion
        validateVersion(if (parameters.has("analysisTargetVersion")) "analysisTargetVersion" else "targetVersion", target)
        val current = optionalString(parameters, "currentVersion")
        current?.let { validateVersion("currentVersion", it) }
        if (current != null) validate(UctVersions.compare(current, target) <= 0, "currentVersion", "currentVersion must not exceed analysisTargetVersion.", listOf("<= $target"))
        return statusOptions(current, target, boolean(parameters, "ignoreCurrentVersion", false))
    }

    private fun putPreparationStatusOptions(arguments: JSONObject, statusParameters: JSONObject): JSONObject = arguments
        .put("analysisTargetVersion", statusParameters.getString("targetVersion"))
        .put("ignoreCurrentVersion", statusParameters.getBoolean("ignoreCurrentVersion"))
        .apply { if (statusParameters.has("currentVersion")) put("currentVersion", statusParameters.getString("currentVersion")) }

    private fun missingVersions(catalog: UctIndexCatalog, current: String?, target: String, ignore: Boolean): List<String> =
        buildList {
            if (catalog.requiresCurrentCoverage(current, target, ignore) && current != null && current !in catalog.supportedVersions) add(current)
            if (target !in catalog.supportedVersions) add(target)
        }.distinct()

    private fun preparationView(project: Project, view: UctReleasePreparation.View, statusParameters: JSONObject): JSONObject =
        MagentoMcpProjectContext.identity(project).put("operation", "prepare")
            .put("runId", view.runId ?: JSONObject.NULL).put("targetVersion", view.version)
            .put("state", view.state).put("complete", view.state == "completed")
            .put("releaseDataReady", view.state == "completed").put("sourceUrl", UctReleaseSource.url(view.version))
            .put("sourceScope", "Magento Open Source core; proprietary Commerce extensions and third-party dependencies excluded")
            .put("phase", view.progress.phase).put("downloadedBytes", view.progress.downloadedBytes)
            .put("totalBytes", view.progress.totalBytes ?: JSONObject.NULL)
            .put("processedFiles", view.progress.processedFiles).put("totalFiles", view.progress.totalFiles)
            .put("summary", view.index?.let { JSONObject().put("existenceSymbols", it.existence.size)
                .put("apiSymbols", it.api.size).put("deprecationSymbols", it.deprecation.size)
                .put("archiveSha256", it.archiveSha256) } ?: JSONObject.NULL)
            .put("error", view.error?.let { JSONObject().put("code", "preparation_failed").put("message", it)
                .put("action", "Verify the requested official release tag exists and resolve the download or parsing error before retrying prepare. No release data was published.") } ?: JSONObject.NULL)
            .put("nextCall", when (view.state) {
                "running" -> nextCall(project, "results", putPreparationStatusOptions(JSONObject().put("runId", view.runId), statusParameters))
                "completed" -> nextCall(project, "status", statusParameters)
                else -> JSONObject.NULL
            })
            .put("retryAfterMs", if (view.state == "running") 500 else JSONObject.NULL)
            .put("nextAction", when (view.state) {
                "running" -> "Follow nextCall to poll preparation. Its arguments preserve the original analysis target and baseline options."
                "completed" -> "Preparation is complete. Follow nextCall to check readiness for the original analysis target and baseline options; no further preparation polling is needed."
                "cancelled" -> "Preparation was cancelled; no release data was published. Start prepare again if this release is still needed."
                else -> "Preparation failed; no release data was published. Verify the requested official release tag and resolve the reported error before retrying prepare."
            })

    private fun boolean(parameters: JSONObject, key: String, default: Boolean): Boolean {
        if (!parameters.has(key)) return default
        validate(parameters.get(key) is Boolean, key, "$key must be a boolean.", listOf("true", "false"))
        return parameters.getBoolean(key)
    }

    private fun coverageError(project: Project, request: UctAnalysisRequest, kind: String, message: String): String {
        val catalog = project.getService(UctReleasePreparation::class.java).catalog()
        val missing = missingVersions(catalog, request.currentVersion, request.targetVersion, request.ignoreCurrentVersion)
        val action = "Follow nextCall to prepare missing Magento Open Source release data through MCP. Missing coverage does not establish whether the official tag exists; preparation verifies it and can fail. No scan was started. Retry the original analysis after readiness.ready is true."
        return JSONObject(error(project, "invalid_request", message))
            .put("currentVersion", request.currentVersion ?: JSONObject.NULL).put("targetVersion", request.targetVersion)
            .put("coverage", JSONObject().put("supportedVersions", JSONArray(catalog.supportedVersions))
                .put("missingVersions", JSONArray(missing))
                .put("currentVersionCovered", request.currentVersion in catalog.supportedVersions)
                .put("targetVersionCovered", request.targetVersion in catalog.supportedVersions))
            .put("nextCall", if (missing.isNotEmpty()) nextCall(project, "prepare",
                putPreparationStatusOptions(JSONObject().put("targetVersion", missing.first()),
                    statusOptions(request.currentVersion, request.targetVersion, request.ignoreCurrentVersion)))
                else nextCall(project, "status", JSONObject().put("targetVersion", request.targetVersion)))
            .put("nextAction", action)
            .apply { getJSONObject("error").put("kind", kind).put("action", action) }
            .toString()
    }

    private fun detectedVersion(project: Project): String? = MagentoMcpProjectContext.magentoRoot(project)?.let {
        MagentoVersionUtil.get(project, it.toString()).takeUnless { version -> version == MagentoVersionUtil.DEFAULT_VERSION }
    }

    private class ParameterException(message: String, val details: JSONObject) : IllegalArgumentException(message)

    private fun invalid(parameter: String, message: String, accepted: List<String>): Nothing =
        throw ParameterException(message, JSONObject().put("parameter", parameter).put("acceptedValues", JSONArray(accepted)))

    private fun validate(condition: Boolean, parameter: String, message: String, accepted: List<String>) {
        if (!condition) invalid(parameter, message, accepted)
    }

    private fun validateVersion(parameter: String, version: String) {
        try { UctVersions.compare(version, version) }
        catch (_: IllegalArgumentException) { invalid(parameter, "Invalid Magento version: $version", listOf("x.y.z", "x.y.z-pN", "x.y.z-alphaN", "x.y.z-betaN", "x.y.z-rcN")) }
    }

    private fun error(project: Project, code: String, message: String, details: JSONObject? = null): String {
        val error = JSONObject().put("code", code).put("message", message)
        details?.keySet()?.forEach { error.put(it, details.get(it)) }
        // A readiness request cannot correct invalid arguments or recover an expired run.
        if (details == null && message.contains("runId")) {
            error.put("parameter", "runId").put("acceptedValues", JSONArray(listOf("a retained runId from this project")))
        }
        return MagentoMcpProjectContext.identity(project)
            .put("state", "failed").put("complete", false).put("pathBase", project.basePath ?: JSONObject.NULL)
            .put("error", error).put("nextCall", JSONObject.NULL)
            .put("nextAction", if (code == "unavailable") "Resolve the reported availability error before retrying the original request."
                else "Correct the rejected parameters and retry the original request. Use detailed_schema for constraints.")
            .toString()
    }
}
