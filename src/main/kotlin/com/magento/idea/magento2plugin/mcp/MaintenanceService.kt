/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.file.Path

internal data class MaintenanceFinding(
    val reasonId: String, val status: String, val item: String, val evidence: String,
    val nextAction: String, val command: JSONObject? = null, val module: String? = null,
    val complete: Boolean = status !in setOf("unknown", "advisory"), val patchStatus: String? = null,
    val cloudEnvironment: String? = null
) {
    fun json(check: String, environment: String) = JSONObject().put("checkId", check).put("reasonId", reasonId)
        .put("status", status).put("item", item).put("evidence", evidence).put("environment", cloudEnvironment?.let { "$it ($environment)" } ?: environment)
        .put("complete", complete)
        .put("nextAction", nextAction).put("suggestedCommand", command).put("module", module).put("patchStatus", patchStatus)
}

internal class MaintenanceService(
    private val project: Path, private val relative: String?, private val candidates: List<String>,
    private val runner: MagerunRunner = MagentoMagerunProcess()
) {
    companion object {
        val checks = listOf("composer_install", "ece_patches", "setup_upgrade", "cache_clean")
        const val description = "Check Magento maintenance: composer_install, ece_patches, setup_upgrade, cache_clean. " +
            "check reads file evidence; verify explicitly runs bounded diagnostic probes in the selected runtime. " +
            "Pass checkIds, changedFiles relative to the Magento root (including deletions), newlyEnabledModules " +
            "from the module-list diff, and devMode=dev or no-dev. Suggested commands are never run."
        fun error(status: String, message: String) = JSONObject().put("status", status).put("complete", false)
            .put("message", message).put("findings", JSONArray())
    }

    fun execute(mode: String, checkIds: List<String>, changedFiles: List<String>, newlyEnabledModules: List<String>,
                devMode: String, cancel: () -> Unit): JSONObject {
        if (mode !in listOf("check", "verify") || devMode !in listOf("dev", "no-dev") ||
            checkIds.any { it !in checks } || changedFiles.size > 512 || newlyEnabledModules.size > 512 ||
            changedFiles.any { !MaintenanceSources.validPath(it) } ||
            newlyEnabledModules.any { !it.matches(Regex("[A-Za-z][A-Za-z0-9]*_[A-Za-z][A-Za-z0-9_]*")) })
            return error("invalid_request", description)
        cancel()
        val runtime = try {
            MaintenanceRuntime.discover(project, relative, candidates, runner, cancel)
        } catch (e: MaintenanceUnavailable) {
            return error(e.reason, e.message.orEmpty())
        } catch (_: IOException) {
            return error("invalid_configuration", "Could not read the configured project or wrappers.")
        }
        val findings = JSONArray()
        var blocked: String? = null
        for (check in checks.filter { checkIds.isEmpty() || it in checkIds }) {
            cancel()
            val rows = if (blocked != null) {
                listOf(MaintenanceFinding("blocked_by_$blocked", "unknown", check,
                    "An earlier $blocked finding needs attention.", "Resolve it and verify again against the resulting code and caches."))
            } else evaluate(check, mode, runtime, changedFiles, newlyEnabledModules, devMode)
            rows.take(500).forEach { findings.put(it.json(check, runtime.label)) }
            if (rows.size > 500) findings.put(MaintenanceFinding("findings_truncated", "unknown", check,
                "Returned 500 of ${rows.size} findings.", "Inspect the underlying tool output.").json(check, runtime.label))
            if (mode == "verify" && blocked == null && rows.any { it.status in setOf("unknown", "required", "advisory") }) {
                blocked = when (check) {
                    "composer_install" -> "composer"
                    "ece_patches" -> "patches"
                    "setup_upgrade" -> "setup"
                    else -> null
                }
            }
        }
        return JSONObject().put("scope", if (mode == "verify") "selected_runtime" else "file_evidence")
            .put("mode", mode).put("checks", JSONArray(checks)).put("environment", runtime.label)
            .put("status", "ok").put("complete", findings.all { (it as JSONObject).getBoolean("complete") })
            .put("findings", findings)
    }

    private fun evaluate(check: String, mode: String, runtime: MaintenanceRuntime, paths: List<String>,
                         modules: List<String>, devMode: String): List<MaintenanceFinding> {
        val sources = try {
            MaintenanceSources.find(check, runtime, paths.distinct(), modules.distinct())
        } catch (_: IOException) {
            return listOf(MaintenanceFinding("file_unreadable", "unknown", check, "Could not read file evidence.", "Check file access and retry."))
        }
        if (mode == "check") return sources.ifEmpty { listOf(MaintenanceFinding("no_file_signal", "not_applicable",
            check, "No supplied change triggers this check.", "Use verify for an observation of the current runtime.")) }

        val observations = try {
            when (check) {
                "composer_install" -> MaintenancePackages.verify(runtime, devMode)
                "ece_patches" -> MaintenancePatches.verify(runtime)
                "setup_upgrade" -> MaintenanceSetup.verify(runtime)
                else -> MaintenanceSources.cache(runtime, sources)
            }
        } catch (e: MaintenanceUnavailable) {
            listOf(MaintenanceFinding(e.reason, "unknown", check, e.message.orEmpty(), "Inspect the selected runtime and retry verification."))
        } catch (_: JSONException) {
            listOf(MaintenanceFinding("invalid_probe_output", "unknown", check, "Incomplete or malformed probe response.",
                "Check the installed tool version and retry."))
        }
        if (check == "cache_clean" && observations.any { it.status == "unknown" }) return observations + sources
        if (check != "setup_upgrade") return observations
        val enablement = sources.filter { it.reasonId in setOf("module_enabled", "module_list_changed") }
        return if (enablement.isEmpty()) observations else observations.filterNot { it.reasonId == "validators_current" } + enablement
    }

}
