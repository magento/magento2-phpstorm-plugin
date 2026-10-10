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
    private val scanOptions = setOf("path", "moduleName", "minimumSeverity", "explainSuppressed")
    private val analysisOptions = scanOptions + setOf("targetVersion", "currentVersion", "ignoreCurrentVersion")
    private val preparationOptions = scanOptions + setOf("analysisTargetVersion", "currentVersion", "ignoreCurrentVersion")

    fun help(): String = """
        Magento backward / upgrade compatibility analysis using the built-in UCT PHP/XML inspections, including PHTML and HTML templates.
        Modes: `help`, `detailed_schema`, `status`, `releases`, `upgrade`, `prepare`, `analyze`, `results`, `cancel`.
        For an upgrade check, call `upgrade` with exactly one path/moduleName and optional exact targetVersion. It selects the next published feature release when omitted, defaults baseline suppression/explanation on, and preserves the full scan request in nextCall. Follow nextStep/nextCall through preparation and analysis.
        Start with `status` and targetVersion to verify the connected project, detected baseline, IDE readiness, and release-data readiness.
        Follow nextCall to prepare missing release data, poll progress, and return to status with the original target and baseline options.
        Always send projectPath (the IDE project directory), including help and detailed_schema.
        Pass ordinary typed tool arguments; no scripts, shell commands, or JSON string encoding are needed.
        `prepare` downloads the exact official Magento Open Source tag and builds all three indexes in the IDE cache. `analyze` starts a background scan. Follow nextCall with `results` until completed, failed, or cancelled, then retrieve remaining pages.
        Use `detailed_schema` only for additional constraints. Never substitute an older target when the requested release is unsupported.
        Every plugin reply is one JSON text object in content[0].text, including documentation and errors; structuredContent is consistently omitted. Failed requests/jobs set isError=true. The plugin validates argument types. JetBrains project-routing errors happen before the plugin and can still be plain text.
        Use nextStep: poll waits retryAfterMs then follows nextCall; call follows nextCall immediately (including missing coverage, completed preparation, and pagination). Always follow a non-null nextCall even if terminal=true or pollRequired=false. analyze means status is ready for a scope; select_target needs an exact target; resolve_error needs corrected parameters or availability; enable_support needs Magento support; done has no continuation.
        successful/complete describe this response or job, not analysis readiness or completion of the whole workflow. Check readiness.ready before analyzing.
        Poll only while pollRequired=true. terminal=true includes completed, failed and cancelled; successful and legacy complete indicate success only. Cached preparation has runId=null and needs no polling. Terminal job reports are exported automatically and remain readable by runId after memory eviction/restart for 30 days / 100 runs per operation.
        supportedVersions describes cached compatibility coverage, not a published-release catalog. Use releases to verify the intended published release; prepare verifies the exact official tag and can fail if it does not exist.
        Default findings include baseline issues; use ignoreCurrentVersion=true to suppress them when both releases are covered.
        Resolve relative paths from pathBase, the IDE project directory; with Magento in src/, use src/app/code/Vendor/Module. Invalid paths return pathBase and, when detected, suggestedPath.
        Analysis uses committed IDE documents and does not save files or change UCT settings.
    """.trimIndent()

    fun detailedSchema(): String = """
        Always send projectPath with the absolute IDE project directory for host routing, including help and detailed_schema.
        Use typed tool arguments. mode defaults to status; all other arguments are optional until required by a mode.
        upgrade: exactly one path/moduleName; optional exact targetVersion (otherwise next published feature release), currentVersion, minimumSeverity, ignoreCurrentVersion (default true), explainSuppressed (default same as ignoreCurrentVersion).
          Requires a detected/explicit baseline. Returns ordinary status/prepare/analyze continuations with the complete request frozen, including the selected exact release and baseline. No newer release returns outcome=no_newer_release and no scan.
          Example: {"projectPath":"<IDE-project-directory>","mode":"upgrade","moduleName":"Application_Blog"}. Follow nextStep/nextCall until done.
        status: optional targetVersion, currentVersion, ignoreCurrentVersion, and scan scope/options (path/moduleName, minimumSeverity, explainSuppressed). A scoped ready status returns an analyze nextCall; unscoped status asks for a scope. Returns projectPath, magentoRoot, pluginVersion, ideVersion, detectedMagentoVersion and its local source,
          pluginEnabled, uctEnabled, indexing, supportedVersions, preparedVersions, activeRunIds, and effective defaults including targetVersion and ignoreCurrentVersion.
          readiness separates ideReady and releaseDataReady, lists missingVersions, and reports ready for the requested analysis. successful/complete indicate that status itself succeeded; only readiness.ready authorizes analyze.
          Status does not query published releases. supportedVersions/latestSupportedVersion describe local index coverage only; missing coverage does not establish whether a release exists.
          Follow nextCall to prepare a missing release or observe an active job; preparation continuations preserve the original status options.
        releases: optional currentVersion (otherwise composer.lock), includeReleases (boolean, default false). Fetches stable published releases from Published releases URL in Magento settings (default: official magento/magento2 GitHub releases API).
          The URL can point to a GitHub-compatible catalog mirror; changing it invalidates cached discovery data.
          Returns compact edition/source/cache metadata, releaseCount and nextMinorRelease (next x.y.z feature release, excluding -p patches). The dated releases array is included only with includeReleases=true.
          Uses a one-hour cache; failed or partial fetches are errors, never an empty success. Separate from local supportedVersions; status never performs network discovery.
        prepare: targetVersion required. Downloads that exact official Magento Open Source GitHub tag, verifies its metadata,
          and reuses the UCT processors to build existence, API, and deprecation snapshots in the IDE system cache.
          Returns a background runId with operation=prepare and phase/byte/file progress. results and cancel work for both operations.
          Repeating preparation returns the active job or cached ready release. Cached responses are completed with runId=null; follow their status nextCall without polling.
          analysisTargetVersion: optional original upgrade target for the follow-up status call, defaulting to the prepared release for standalone prepare calls.
          currentVersion and ignoreCurrentVersion: optional baseline status options. nextCall preserves all version options and optional path/moduleName, minimumSeverity, explainSuppressed through preparation/results/cancel and status into analyze. Keep all continuation arguments.
          Cache survives restart; failed/cancelled work is never ready. Failed/cancelled preparation has no polling nextCall; inspect its state-specific nextAction before retrying.
          No PHP execution, Composer installation, project writes, or extra IDE instance is needed.
          Adobe Commerce proprietary extensions and third-party dependencies are outside this source coverage.
          Verify projectPath matches the requested project. A mismatch requires correcting the MCP connection, not creating another IDE instance or a script.
        analyze: exactly one of path or moduleName. path is a project-relative or absolute PHP, PHTML, XML, or HTML file recognized by IDE PHP/XML PSI, module/theme directory, or directory containing components; moduleName is exact Vendor_Module.
          targetVersion: required; prepare missing compatibility data first. A prepared target also requires a covered explicit/detected baseline when one is provided.
          currentVersion: optional; defaults to Magento detected from composer.lock in magentoRoot. No configured-version or composer.json fallback. Must not exceed targetVersion.
          minimumSeverity: warning (default), error, or critical.
          ignoreCurrentVersion: false (default). When true, currentVersion must also have complete index coverage.
          explainSuppressed: false (default); requires ignoreCurrentVersion=true. Returns suppression.suppressedDiagnostics (legacy total), counted before per-element severity filtering. summary.displayedFindings (legacy totalIssues) counts displayed findings; their difference need not equal suppressedDiagnostics. findingsScope distinguishes upgrade_changes from target_including_baseline.
          Explicit analysis works even when editor UCT inspections are disabled. Magento project support must be enabled and indexing finished.
          Directory scans exclude bundled Magento components and symlink directories, but include test/fixture files inside custom components. processedFiles/totalFiles count supported IDE PHP/XML PSI files, including PHTML/HTML.
          pathBase is the IDE project directory (not necessarily magentoRoot). For Magento under src/, use src/app/code/Vendor/Module. Invalid path errors include pathBase and may include suggestedPath; retry explicitly with that path. Completed results include fileTypeCounts and testFiles, counted by Test/Tests/Fixture/Fixtures/_files path segments.
          analysisIdentity exposes releaseSnapshotSha256 (baseline-independent release state), comparisonSnapshotSha256 (legacy snapshotSha256, includes baseline symbols for removal detection), archiveSha256 (source bytes), and indexRevision (comparison plus suppression options). Target comparison hashes can differ by baseline with unchanged release/archive hashes.
          Unsupported single files are rejected before a job starts; empty/unsupported directory scopes fail during background discovery. Analyze acceptance is not scan completion.
          Example tool arguments: {"projectPath":"<IDE-project-directory>","mode":"analyze","moduleName":"Foo_Bar","targetVersion":"<requested-version>","minimumSeverity":"warning"}
          Replace <requested-version> with the user's exact target release. Use status and prepare to obtain missing data for that release.
        results: runId required; offset is an integer 0..2147483647 (default 0); limit is an integer 1..500 (default 100). Send JSON integers. The plugin also coerces numeric strings to integers; fractions and overflow return the same JSON validation envelope. Pages are available after completion, including archived runs.
          Returns state, progress, version options, totals by severity, findings with project-relative filePath, 1-based line/column, code, severity, and message, plus nextOffset and hasMore.
          Follow nextCall.arguments while pollRequired=true or when hasMore=true; retryAfterMs gives the polling interval. terminal=true means a job stopped, including failure/cancellation. successful and legacy complete are success flags, never polling conditions.
          For preparation runs, preserve all nextCall arguments, including analysisTargetVersion, currentVersion, ignoreCurrentVersion, path/moduleName, minimumSeverity, and explainSuppressed; completion returns the original status call.
          Example tool arguments: {"projectPath":"<IDE-project-directory>","mode":"results","runId":"<returned runId>","offset":0,"limit":100}
        cancel: runId required. Idempotent; cancelled scans never report complete results. Preparation accepts the same status continuation options as results.
        Every plugin response is one JSON object in content[0].text, with schemaVersion, operation, state, terminal, successful, complete, pollRequired, error, nextCall, nextStep and retryAfterMs. structuredContent is consistently omitted. Help/detailed_schema use the same envelope with a documentation field.
        nextStep is the workflow decision: poll = wait retryAfterMs then follow nextCall; call = follow nextCall now, including blocked coverage, completed/cached preparation and remaining pages; analyze = ready status, supply the requested scope; select_target = obtain an exact target via releases or the user request; resolve_error = correct the reported error; enable_support = enable Magento project support; done = no continuation. Always follow a non-null nextCall regardless of successful, terminal or pollRequired.
        Failed requests/jobs and blocked analyses return MCP isError=true; successful status, running, completed, and cancelled responses return false. Raw typed arguments are validated by the plugin, so bad types get JSON errors. Host project routing precedes the tool and may still return plain-text errors.
        invalid_request validation errors are terminal, error.recoverable=false, with parameter/parameters and acceptedValues where applicable, and nextCall=null. Missing release data uses coverage_required, state=blocked, terminal=false, pollRequired=false, error.recoverable=true and a preparation nextCall; follow it through status and the preserved analyze continuation.
        One running analysis and one running preparation per project. Five workers per operation remain in memory, but all terminal jobs are automatically exported outside the project to JSON in the IDE system cache. report exposes path, saved, error, retentionDays=30 and maxRunsPerOperation=100. Full findings are unpaginated in the export; results/cancel can reload archived runIds after eviction or restart. Expired reports are pruned when saving new ones. Export failures are visible in report.error and prevent eviction of unsaved runs. A scan is limited to 10000 files and 50000 findings; exceeding either fails with a narrower-scope request.
        Coverage uses bundled history and prepared Magento Open Source snapshots with existing PHP/XML rules. Prepared snapshots report observed states, not the first release introducing a change; zero findings does not certify all upgrade behavior.
    """.trimIndent()

    fun execute(project: Project, mode: String, parameters: JSONObject): String =
        MagentoCompatibilityResponse.envelope(JSONObject(executeBody(project, mode, parameters)), mode).toString()

    private fun executeBody(project: Project, mode: String, parameters: JSONObject): String = try {
        val result = when (mode) {
            "help", "detailed_schema" -> {
                validateKeys(parameters, emptySet())
                JSONObject().put("documentation", if (mode == "help") help() else detailedSchema())
            }
            "status" -> {
                validateKeys(parameters, analysisOptions)
                status(project, parameters)
            }
            "upgrade" -> upgrade(project, parameters)
            "releases" -> {
                validateKeys(parameters, setOf("currentVersion", "includeReleases"))
                val includeReleases = boolean(parameters, "includeReleases", false)
                val current = optionalString(parameters, "currentVersion") ?: detectedVersion(project)
                current?.let { validateVersion("currentVersion", it) }
                val releaseData = try {
                    project.getService(UctPublishedReleases::class.java).discover(current, includeReleases)
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
                validateKeys(parameters, preparationOptions + "targetVersion")
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
                    request.targetVersion !in catalog.supportedVersions -> coverageError(project, request, parameters, "unsupported_target_version",
                        "Compatibility indexes are unavailable for targetVersion `${request.targetVersion}`.")
                    catalog.requiresCurrentCoverage(request.currentVersion, request.targetVersion, request.ignoreCurrentVersion) && request.currentVersion !in catalog.supportedVersions -> coverageError(project, request, parameters, "unsupported_current_version",
                        "This analysis requires compatibility data for currentVersion. Prepare the baseline release first.")
                    else -> {
                        catalog.validateVersions(request.currentVersion, request.targetVersion, request.ignoreCurrentVersion)
                        view(project, project.getService(UctAnalysisRuns::class.java).start(request), 0, 100)
                    }
                }
            }
            "results" -> {
                validateKeys(parameters, preparationOptions + setOf("runId", "offset", "limit"))
                val offset = integer(parameters, "offset", 0, 0, Int.MAX_VALUE)
                val limit = integer(parameters, "limit", 100, 1, 500)
                val runId = requiredString(parameters, "runId")
                val preparation = project.getService(UctReleasePreparation::class.java)
                if (preparation.owns(runId)) {
                    validateKeys(parameters, preparationOptions + "runId")
                    val view = preparation.results(runId)
                    preparationView(project, view, preparationStatusParameters(parameters, view.version))
                } else {
                    validateKeys(parameters, setOf("runId", "offset", "limit"))
                    view(project, project.getService(UctAnalysisRuns::class.java).results(runId), offset, limit)
                }
            }
            "cancel" -> {
                validateKeys(parameters, preparationOptions + "runId")
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
            else -> invalid("mode", "Unknown mode.", listOf("help", "detailed_schema", "status", "releases", "upgrade", "prepare", "analyze", "results", "cancel"))
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

    /** Resolve the preset once, then use ordinary, fully specified continuations. */
    private fun upgrade(project: Project, parameters: JSONObject): JSONObject {
        validateKeys(parameters, analysisOptions)
        val options = JSONObject(parameters.toString())
        val current = optionalString(options, "currentVersion") ?: detectedVersion(project)
            ?: invalid("currentVersion", "Upgrade checks require a detected or explicit currentVersion.", listOf("installed Magento release"))
        validateVersion("currentVersion", current)
        options.put("currentVersion", current)
        val ignore = boolean(options, "ignoreCurrentVersion", true)
        options.put("ignoreCurrentVersion", ignore)
        options.put("explainSuppressed", boolean(options, "explainSuppressed", ignore))
        validateScanOptions(options, ignore, required = true)
        val explicitTarget = optionalString(options, "targetVersion")
        explicitTarget?.let { validateVersion("targetVersion", it) }
        val published = if (explicitTarget == null) try {
            project.getService(UctPublishedReleases::class.java).discover(current)
        } catch (exception: com.intellij.openapi.progress.ProcessCanceledException) {
            throw exception
        } catch (exception: Exception) {
            throw IOException("Published release discovery failed: ${exception.message}", exception)
        } else null
        val selected = published?.optJSONObject("nextMinorRelease")
        val target = explicitTarget ?: selected?.getString("version")
        if (target == null) return MagentoMcpProjectContext.identity(project)
            .put("operation", "upgrade").put("outcome", "no_newer_release")
            .put("currentVersion", current).put("releaseDiscovery", published)
            .put("nextAction", "No newer stable feature release is published in the configured catalog. No scan was started.")
        options.put("targetVersion", target)
        return status(project, options).put("operation", "upgrade")
            .put("releaseDiscovery", published ?: JSONObject.NULL)
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
        val scoped = validateScanOptions(parameters, ignore)
        val continuation = target?.let { statusOptions(current, it, ignore, parameters) }
        val missing = if (target == null) emptyList() else missingVersions(catalog, current, target, ignore)
        val active = preparation.activeRunIds()
        val next = when {
            !Settings.isEnabled(project) -> JSONObject.NULL
            active.isNotEmpty() -> nextCall(project, "results", JSONObject().put("runId", active.first()).apply {
                if (continuation != null) putPreparationStatusOptions(this, continuation)
            })
            missing.isNotEmpty() && Settings.isEnabled(project) -> nextCall(project, "prepare",
                putPreparationStatusOptions(JSONObject().put("targetVersion", missing.first()), continuation!!))
            target != null && DumbService.isDumb(project) -> nextCall(project, "status", continuation!!)
            target != null && scoped -> nextCall(project, "analyze", continuation!!)
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
            .put("retryAfterMs", if (active.isNotEmpty() || (target != null && DumbService.isDumb(project) && missing.isEmpty())) 500 else JSONObject.NULL)
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
                scoped -> "Release data and IDE indexes are ready. Follow nextCall to analyze the preserved scope and options."
                else -> "Release data and IDE indexes are ready. Call analyze with path or moduleName and these version options."
            })
    }

    internal fun request(project: Project, parameters: JSONObject): UctAnalysisRequest {
        validateKeys(parameters, analysisOptions)
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
        if (!Files.exists(resolved)) {
            val magentoRoot = MagentoMcpProjectContext.magentoRoot(project)
            val suggestion = path?.takeUnless { Path.of(it).isAbsolute }?.let { magentoRoot?.resolve(it)?.normalize() }
                ?.takeIf { Files.exists(it) && it.toRealPath().startsWith(root) }
                ?.let { root.relativize(it.toRealPath()).toString().replace('\\', '/') }
            val details = JSONObject().put("parameter", "path").put("pathBase", root.toString())
                .put("magentoRoot", magentoRoot?.toString() ?: JSONObject.NULL)
                .put("acceptedValues", JSONArray(listOf("existing path relative to pathBase ($root)", "absolute path inside $root")))
            if (suggestion != null) details.put("suggestedPath", suggestion)
            throw ParameterException("Analysis path does not exist: $resolved. Relative paths use pathBase ($root), the IDE project directory." +
                if (suggestion != null) " Retry with path=$suggestion." else "", details)
        }
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
            .put("report", if (view.state == "running") JSONObject.NULL else project.getService(UctAnalysisRuns::class.java).reportInfo(view.runId))
            .put("paths", JSONArray(view.request.paths.map { root.relativize(Path.of(it)).toString().replace('\\', '/').ifEmpty { "." } }))
            .put("currentVersion", view.request.currentVersion ?: JSONObject.NULL)
            .put("targetVersion", view.request.targetVersion)
            .put("minimumSeverity", view.request.minimumSeverity.name.lowercase())
            .put("ignoreCurrentVersion", view.request.ignoreCurrentVersion)
            .put("findingsScope", if (view.request.ignoreCurrentVersion) "upgrade_changes" else "target_including_baseline")
            .put("explainSuppressed", view.request.explainSuppressed)
            .put("pathBase", project.basePath ?: JSONObject.NULL)
            .put("analysisIdentity", result?.analysisIdentity?.let(::JSONObject) ?: JSONObject.NULL)
            .put("scope", if (result == null) JSONObject.NULL else JSONObject()
                .put("fileTypeCounts", JSONObject(result.fileTypeCounts)).put("testFiles", result.testFiles)
                .put("testFileClassification", "Case-insensitive Test/Tests/Fixture/Fixtures/_files path segments relative to pathBase.")
                .put("includesTests", true))
            .put("suppression", result?.suppressedByRule?.let { rules -> JSONObject()
                .put("reason", "already_present_in_current_version")
                .put("suppressedDiagnostics", rules.values.sum()).put("total", rules.values.sum())
                .put("byRule", JSONObject(rules.mapKeys { it.key.toString() }))
                .put("bySeverity", JSONObject(result.suppressedBySeverity.orEmpty()))
                .put("counting", "Distinct rule diagnostics before per-element severity filtering; totals need not equal the difference between displayed finding counts.")
            } ?: JSONObject.NULL)
            .put("processedFiles", view.progress.processedFiles).put("totalFiles", view.progress.totalFiles)
            .put("error", if (view.error == null) JSONObject.NULL else JSONObject().put("code", "analysis_failed").put("message", view.error))
            .put("summary", if (result == null) JSONObject.NULL else JSONObject()
                .put("displayedFindings", findings.size).put("totalIssues", findings.size).put("modules", result.modules).put("themes", result.themes)
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
        val raw = parameters.get(key)
        val value = when (raw) {
            is Int, is Long -> (raw as Number).toLong()
            is String -> raw.toLongOrNull()
            else -> null
        }
        validate(value != null && value in min.toLong()..max.toLong(),
            key, "$key must be an integer in $min..$max.", listOf("integer $min..$max"))
        return value!!.toInt()
    }
    private fun validateKeys(parameters: JSONObject, allowed: Set<String>) {
        val unknown = parameters.keySet() - allowed
        if (unknown.isNotEmpty()) throw ParameterException("Unknown parameters: ${unknown.sorted().joinToString()}. Use detailed_schema.",
            JSONObject().put("parameters", JSONArray(unknown.sorted())).put("acceptedValues", JSONArray(allowed.sorted())))
    }
    private fun nextCall(project: Project, mode: String, arguments: JSONObject = JSONObject()): JSONObject = JSONObject()
        .put("tool", "magento_compatibility")
        .put("arguments", JSONObject(arguments.toString()).put("mode", mode).put("projectPath", project.basePath ?: JSONObject.NULL))

    private fun statusOptions(current: String?, target: String, ignore: Boolean, parameters: JSONObject = JSONObject()): JSONObject =
        JSONObject().put("targetVersion", target).put("ignoreCurrentVersion", ignore).apply {
            if (current != null) put("currentVersion", current)
            scanOptions.filter(parameters::has).forEach { put(it, parameters.get(it)) }
            if (has("path") || has("moduleName")) {
                put("minimumSeverity", optionalString(parameters, "minimumSeverity") ?: "warning")
                put("explainSuppressed", boolean(parameters, "explainSuppressed", false))
            }
        }

    /** Validate continuation options without accessing PSI while the IDE is indexing. */
    private fun validateScanOptions(parameters: JSONObject, ignore: Boolean, required: Boolean = false): Boolean {
        val path = optionalString(parameters, "path")
        val module = optionalString(parameters, "moduleName")
        val scoped = path != null || module != null
        if (required || scoped || scanOptions.any(parameters::has)) {
            validate((path == null) != (module == null), "path", "Provide exactly one of path or moduleName.", listOf("exactly one of path or moduleName"))
        }
        if (module != null) validate(Regex("[A-Za-z][A-Za-z0-9]*_[A-Za-z][A-Za-z0-9_]*").matches(module), "moduleName", "moduleName must use Vendor_Module format.", listOf("Vendor_Module"))
        optionalString(parameters, "minimumSeverity")?.let {
            validate(it in setOf("warning", "error", "critical"), "minimumSeverity", "minimumSeverity must be warning, error, or critical.", listOf("warning", "error", "critical"))
        }
        validate(!boolean(parameters, "explainSuppressed", false) || ignore, "explainSuppressed", "explainSuppressed requires ignoreCurrentVersion=true.", listOf("false", "true with ignoreCurrentVersion=true"))
        return scoped
    }

    private fun preparationStatusParameters(parameters: JSONObject, preparedVersion: String): JSONObject {
        val target = optionalString(parameters, "analysisTargetVersion") ?: preparedVersion
        validateVersion(if (parameters.has("analysisTargetVersion")) "analysisTargetVersion" else "targetVersion", target)
        val current = optionalString(parameters, "currentVersion")
        current?.let { validateVersion("currentVersion", it) }
        if (current != null) validate(UctVersions.compare(current, target) <= 0, "currentVersion", "currentVersion must not exceed analysisTargetVersion.", listOf("<= $target"))
        val ignore = boolean(parameters, "ignoreCurrentVersion", false)
        validateScanOptions(parameters, ignore)
        return statusOptions(current, target, ignore, parameters)
    }

    private fun putPreparationStatusOptions(arguments: JSONObject, statusParameters: JSONObject): JSONObject = arguments
        .put("analysisTargetVersion", statusParameters.getString("targetVersion"))
        .put("ignoreCurrentVersion", statusParameters.getBoolean("ignoreCurrentVersion"))
        .apply {
            if (statusParameters.has("currentVersion")) put("currentVersion", statusParameters.getString("currentVersion"))
            scanOptions.filter(statusParameters::has).forEach { put(it, statusParameters.get(it)) }
        }

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
            .put("summary", view.summary ?: JSONObject.NULL)
            .put("report", if (view.runId == null || view.state == "running") JSONObject.NULL else project.getService(UctReleasePreparation::class.java).reportInfo(view.runId))
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

    private fun coverageError(project: Project, request: UctAnalysisRequest, parameters: JSONObject, kind: String, message: String): String {
        val catalog = project.getService(UctReleasePreparation::class.java).catalog()
        val missing = missingVersions(catalog, request.currentVersion, request.targetVersion, request.ignoreCurrentVersion)
        val action = "Follow nextCall to prepare missing Magento Open Source release data through MCP. Missing coverage does not establish whether the official tag exists; preparation verifies it and can fail. No scan was started. Continue through status and the preserved analyze call once readiness.ready is true."
        return JSONObject(error(project, "coverage_required", message)).put("state", "blocked")
            .put("currentVersion", request.currentVersion ?: JSONObject.NULL).put("targetVersion", request.targetVersion)
            .put("coverage", JSONObject().put("supportedVersions", JSONArray(catalog.supportedVersions))
                .put("missingVersions", JSONArray(missing))
                .put("currentVersionCovered", request.currentVersion in catalog.supportedVersions)
                .put("targetVersionCovered", request.targetVersion in catalog.supportedVersions))
            .put("nextCall", if (missing.isNotEmpty()) nextCall(project, "prepare",
                putPreparationStatusOptions(JSONObject().put("targetVersion", missing.first()),
                    statusOptions(request.currentVersion, request.targetVersion, request.ignoreCurrentVersion, parameters)))
                else nextCall(project, "status", JSONObject().put("targetVersion", request.targetVersion)))
            .put("nextAction", action)
            .apply { getJSONObject("error").put("kind", kind).put("action", action).put("recoverable", true) }
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
        val error = JSONObject().put("code", code).put("message", message).put("recoverable", false)
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
