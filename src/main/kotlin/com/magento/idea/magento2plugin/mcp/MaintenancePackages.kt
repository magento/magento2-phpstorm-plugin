/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import org.json.JSONArray
import org.json.JSONObject
import java.nio.file.Files

internal object MaintenancePackages {
    fun missingLock() = MaintenanceFinding("lock_missing", "advisory", "composer.lock",
        "No composer.lock in the configured checkout.", "Restore or establish the intended lock before installing.", complete = true)

    fun verify(runtime: MaintenanceRuntime, devMode: String): List<MaintenanceFinding> {
        if (Files.notExists(runtime.root.resolve("composer.lock"))) return listOf(missingLock())
        val validation = runtime.validateLock()
        val diagnostic = validation.stdout + "\n" + validation.stderr
        if ("lock file is not up to date" in diagnostic.lowercase()) return listOf(MaintenanceFinding(
            "lock_stale", "advisory", "composer.lock", "Composer reports the lock is stale relative to composer.json.",
            "Reconcile the manifest and lock with the intended dependency set.", complete = true))
        if (validation.exitCode != 0) return listOf(MaintenanceFinding("lock_validation_failed", "unknown", "composer.lock",
            "composer validate --check-lock exited ${validation.exitCode}.", "Resolve the validation errors before comparing packages."))
        val vendor = runtime.composerPath("vendor-dir")
        val inventory = JSONObject(runtime.php("inventory", vendor))
        val options = if (devMode == "no-dev") arrayOf("--no-dev") else emptyArray()
        val install = runtime.suggestion("composer", "install", *options)
        val action = "Install the locked dependencies in the selected runtime and verify again."
        if (inventory.optBoolean("missing")) return listOf(MaintenanceFinding("vendor_missing", "required", vendor,
            "Missing ${inventory.optString("missingFile", "composer/installed.json")} under the selected vendor directory $vendor.", action, install))
        val locked = packages(inventory.getJSONArray("locked"))
        if (devMode == "dev") locked.putAll(packages(inventory.getJSONArray("lockedDev")))
        val installed = packages(inventory.getJSONArray("installed"))
        val findings = mutableListOf<MaintenanceFinding>()
        for (name in (locked.keys + installed.keys).sorted()) {
            val want = locked[name]
            val have = installed[name]
            val reason = when {
                have == null -> "package_missing"
                want == null -> "package_extra"
                want != have -> "package_mismatch"
                else -> continue
            }
            val evidence = "Lock ($devMode): ${want ?: "absent"}. Installed ($vendor): ${have ?: "absent"}."
            findings += MaintenanceFinding(reason, "required", name, evidence, action, install)
        }
        return findings.ifEmpty { listOf(MaintenanceFinding("packages_match", "clear", vendor,
            "${installed.size} installed package records match the current lock ($devMode).", "No Composer action.")) }
    }

    private data class Identity(val version: String, val source: String?, val dist: String?)

    private fun packages(rows: JSONArray): MutableMap<String, Identity> {
        val result = mutableMapOf<String, Identity>()
        for (i in 0 until rows.length()) {
            val row = rows.getJSONObject(i)
            fun reference(type: String) = row.optJSONObject(type)?.let {
                if (it.isNull("reference")) null else it.getString("reference")
            }
            if (result.put(row.getString("name"), Identity(row.getString("version"), reference("source"), reference("dist"))) != null)
                throw MaintenanceUnavailable("inventory_ambiguous", "Composer returned duplicate package names.")
        }
        return result
    }
}
