/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import org.json.JSONArray
import org.json.JSONObject

internal object MaintenancePatches {
    fun verify(runtime: MaintenanceRuntime): List<MaintenanceFinding> {
        val expected = JSONObject(runtime.php("patches", runtime.composerPath("vendor-dir")))
        val cloud = expected.optString("environment").takeIf { it.matches(Regex("[A-Za-z0-9_.-]{1,128}")) }
        val items = expected.getJSONArray("items").map { it as? String ?: throw org.json.JSONException("Invalid patch ID") }.toSortedSet()
        if (!expected.getBoolean("available")) {
            if (!expected.getBoolean("cloud")) return listOf(MaintenanceFinding("no_cloud_patches", "not_applicable",
                "ece-patches", "No installed Cloud patch tool or Cloud project marker in this environment.", "No patch action."))
            return (items + "ece-patches").map { unknown(it, "Cloud patch tool is missing from this environment.").copy(cloudEnvironment = cloud) }
        }
        if (items.isEmpty()) return listOf(MaintenanceFinding("no_expected_patches", "clear", "ece-patches",
            "The installed patch pools and project hotfix directory contain no expected patches.", "No patch action."))
        val bin = runtime.composerPath("bin-dir").trimEnd('/', '\\') + "/ece-patches"
        val observations = try {
            val rows = JSONArray(runtime.output(bin, "status", "--format=json", "--no-interaction"))
            (0 until rows.length()).map(rows::getJSONObject).groupBy { row ->
                val id = row.optString("Id").trim()
                if (id == "N/A") row.optString("Title").filterNot { it == '\n' || it == '\r' }.trim().removePrefix("../") else id
            }
        } catch (e: MaintenanceUnavailable) {
            return items.map { unknown(it, e.message.orEmpty()).copy(cloudEnvironment = cloud) }
        } catch (_: org.json.JSONException) {
            return items.map { unknown(it, "The installed ECE tool did not return recognized JSON status rows.").copy(cloudEnvironment = cloud) }
        }
        return items.map { item ->
            val rows = observations[item].orEmpty()
            val status = rows.singleOrNull()?.optString("Status")?.trim()?.ifEmpty { null }
            when (status) {
                "Applied" -> MaintenanceFinding("patch_applied", "clear", item, "ece-patches status: Applied.", "No patch action.", patchStatus = "applied")
                "Not applied" -> MaintenanceFinding("patch_pending", "required", item, "ece-patches status: Not applied.",
                    "Resolve any unknown patch statuses, apply the expected Cloud patches, and recheck the installed code.", runtime.suggestion(bin, "apply"), patchStatus = "pending")
                else -> unknown(item, when {
                    rows.isEmpty() -> "ECE did not return this expected patch."
                    rows.size > 1 -> "ECE returned ${rows.size} rows for this identity."
                    else -> "ece-patches status: ${status ?: "unspecified"}."
                })
            }.copy(cloudEnvironment = cloud)
        }
    }

    private fun unknown(item: String, evidence: String) = MaintenanceFinding("patch_unknown", "unknown", item, evidence,
        "Inspect patch applicability and conflicts in the selected environment before applying.", patchStatus = "unknown")
}
