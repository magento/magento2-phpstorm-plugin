/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import org.json.JSONObject
import org.json.JSONTokener

internal object MaintenanceSetup {
    private val validator = Regex("""(?m)^Validator ([\w\\]+) (isUpToDate: (?:true|false)|failed with error:)[^\r\n]*""")
    private val legacyRow = Regex("""(?m)^\s*(\w+_\w+)\s+(schema|data):\s*(\S+)\s+->\s+([\w.\-]+)""")
    private val oldMessages = mapOf("Data patches are not up to date" to "pending_data_patch",
        "Schema patches are not up to date" to "pending_schema_patch", "Declarative Schema is not up to date" to "schema_difference")

    fun verify(runtime: MaintenanceRuntime): List<MaintenanceFinding> {
        val result = runtime.probe("bin/magento", "setup:db:status", "--no-ansi", "--no-interaction")
        val body = result.stdout.replace("\r\n", "\n")
        val headers = validator.findAll(body).toList()
        val failures = headers.filter { it.groupValues[2] == "failed with error:" }
        val failedChecks = failures.map {
            MaintenanceFinding("validator_failed", "unknown", it.groupValues[1],
                "setup:db:status exited ${result.exitCode}. This validator reported an error.",
                "Check database connectivity and the validator error, then retry.")
        }
        if (result.exitCode == 0 && body.lineSequence().any { it.trim() == "All modules are up to date." } &&
            headers.all { it.groupValues[2] == "isUpToDate: true" }) return listOf(
            MaintenanceFinding("validators_current", "clear", "inspected modules", "Magento reports all inspected modules up to date.", "No setup action."))
        val unknown = MaintenanceFinding("db_status_unknown", "unknown", "database",
            "setup:db:status exited ${result.exitCode} without a recognized conclusive result.", "Check the deployment and database connection.")
        if (result.exitCode != 2) return failedChecks.ifEmpty { listOf(unknown) }
        val findings = failedChecks.toMutableList()
        for ((index, header) in headers.withIndex()) {
            if (header.groupValues[2] != "isUpToDate: false") continue
            val name = header.groupValues[1]
            val section = body.substring(header.range.last + 1, headers.getOrNull(index + 1)?.range?.first ?: body.length)
            val details = section.substringAfter("\nDetails:", "").trim().takeIf { it.isNotEmpty() }?.let {
                JSONTokener(it).nextValue() as? JSONObject
            }
            when (name.removeSuffix("\\Interceptor").removeSuffix("\\Proxy").substringAfterLast('\\')) {
                "UpToDateData", "UpToDateSchema" -> {
                    val reason = if ("\\UpToDateData" in name) "pending_data_patch" else "pending_schema_patch"
                    val patches = details?.optJSONArray("unapplied_patches")
                    if (patches == null || patches.length() == 0) findings += pending(runtime, reason, name, "$name reported isUpToDate: false")
                    else for (row in patches) {
                        val patch = row as? JSONObject ?: return listOf(unknown)
                        findings += pending(runtime, reason, patch.getString("patch"), "$name listed this patch as unapplied.", patch.getString("module"))
                    }
                }
                "UpToDateDeclarativeSchema" -> {
                    val tables = details?.optJSONObject("affected_tables")?.keySet().orEmpty()
                    for (item in tables.ifEmpty { setOf("declarative schema") }.sorted()) {
                        findings += pending(runtime, "schema_difference", item, "$name reported a declarative schema difference.")
                    }
                }
                "OldDbValidator" -> {
                    val errors = details?.optJSONArray("version_errors") ?: return listOf(unknown)
                    for (entry in errors) {
                        val row = entry as? JSONObject ?: return listOf(unknown)
                        findings += version(runtime, row.getString("module"), row.getString("current"), row.getString("required"))
                    }
                }
                else -> findings += unknown.copy(item = name)
            }
        }
        if (headers.isEmpty()) {
            for (line in body.lineSequence().map(String::trim)) oldMessages[line]?.let {
                findings += pending(runtime, it, "inspected modules", line)
            }
            legacyRow.findAll(body).forEach { findings += version(runtime, it.groupValues[1], it.groupValues[3], it.groupValues[4]) }
        }
        // A DB-ahead module makes a global setup:upgrade recommendation unsafe.
        if (findings.any { it.reasonId == "database_ahead" || it.status == "unknown" }) {
            return findings.map { if (it.status == "required") it.copy(status = "advisory", command = null,
                complete = false, nextAction = "Resolve deployment alignment and recheck before upgrading.") else it }
        }
        return findings.ifEmpty { listOf(unknown) }
    }

    private fun pending(runtime: MaintenanceRuntime, reason: String, item: String, evidence: String, module: String? = null) =
        MaintenanceFinding(reason, "required", item, evidence, "Run setup:upgrade in this environment and verify again.",
            runtime.suggestion("bin/magento", "setup:upgrade"), module)

    private fun version(runtime: MaintenanceRuntime, module: String, current: String, required: String): MaintenanceFinding {
        val evidence = "Magento reports database version $current and code version $required for $module."
        val pattern = Regex("[0-9]+(?:\\.[0-9]+)*")
        if (current == "none") return pending(runtime, "database_behind", module, evidence, module)
        if (pattern.matches(current) && pattern.matches(required)) {
            val db = current.split('.').map(String::toBigInteger)
            val code = required.split('.').map(String::toBigInteger)
            val direction = (0 until maxOf(db.size, code.size)).map {
                (db.getOrNull(it) ?: java.math.BigInteger.ZERO).compareTo(code.getOrNull(it) ?: java.math.BigInteger.ZERO)
            }.firstOrNull { it != 0 }
            if (direction != null && direction < 0) return pending(runtime, "database_behind", module, evidence, module)
            if (direction != null && direction > 0) return MaintenanceFinding("database_ahead", "advisory", module,
                evidence, "Check deployment alignment and select the matching checkout and database.", module = module, complete = true)
        }
        return MaintenanceFinding("version_direction_unknown", "unknown", module, evidence, "Check the module versions before upgrading.")
    }
}
