/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.updates

import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeParseException

enum class UpdateSeverity(val label: String) {
    UNKNOWN("Unknown"), LOW("Low"), MEDIUM("Medium"), HIGH("High"), CRITICAL("Critical");

    companion object {
        internal fun from(advisory: JSONObject): UpdateSeverity {
            entries.firstOrNull { it.name.equals(advisory.optString("severity"), true) }?.let { return it }
            val score = advisory.optDouble("cvssScore", Double.NaN)
            return when {
                score in 9.0..10.0 -> CRITICAL
                score >= 7.0 && score < 9.0 -> HIGH
                score >= 4.0 && score < 7.0 -> MEDIUM
                score > 0.0 && score < 4.0 -> LOW
                else -> UNKNOWN
            }
        }
    }
}

enum class UpdateKind { RELEASE, SECURITY, SUPPORT }

data class UpdateFinding(val kind: UpdateKind, val message: String, val fingerprint: String)

data class UpdateAssessment(
    val installed: InstalledMagento,
    val patchVersion: String?,
    val latestVersion: String?,
    val severity: UpdateSeverity,
    val urgent: Boolean,
    val endOfLife: Boolean,
    val advisoryCount: Int,
    val securityKnown: Boolean,
    val checkedAt: Instant,
    val message: String,
    val fingerprint: String,
    val findings: List<UpdateFinding>
) {
    val actionable: Boolean get() = findings.isNotEmpty()
}

internal object UpdateEvaluator {
    fun evaluate(installed: InstalledMagento, catalogBody: String, securityBody: String?, now: Instant): UpdateAssessment {
        val data = JSONObject(catalogBody).getJSONObject("data")
        require(data.length() in 1..10000) { "Invalid release catalog." }
        val current = ReleaseVersion.parse(installed.version) ?: error("Unsupported installed version.")
        val today = now.atZone(ZoneOffset.UTC).toLocalDate()
        val since = today.minusMonths(1)
        fun recent(date: LocalDate?): Boolean = date != null && date >= since && date <= today
        val versions = data.keySet().mapNotNull { key ->
            val item = data.getJSONObject(key)
            val version = item.getString("version")
            require(version == key) { "Release catalog identifier does not match its version." }
            val parsed = ReleaseVersion.parse(version) ?: return@mapNotNull null
            val date = date(item, "releaseDate")
            if (!parsed.stable || !recent(date) || item.optBoolean("isFutureVersion")) null else version to parsed
        }
        val updates = versions.filter { it.second > current }
        val latest = updates.maxByOrNull { it.second }?.first
        val patch = updates.filter { it.second.line == current.line }.maxByOrNull { it.second }?.first
        val currentMetadata = data.optJSONObject(installed.version)
        val eolDate = currentMetadata?.let { date(it, "eolDate") }
        val eol = currentMetadata?.optBoolean("isEOLVersion") == true || (eolDate != null && eolDate <= today)
        val security = securityBody?.let { JSONObject(it).getJSONObject("data") }
        if (security != null) {
            require(security.getString("distribution") == installed.distribution.apiId && security.getString("version") == installed.version) {
                "Security report does not match the installed distribution and version."
            }
        }
        val status = security?.optString("status")
        val known = status in setOf("secure", "vulnerable", "eol") && security?.optJSONArray("vulnerableTo") != null
        val rows = security?.optJSONArray("vulnerableTo") ?: JSONArray()
        require(rows.length() <= 1000) { "Security report is too large." }
        val advisories = (0 until rows.length()).map { rows.getJSONObject(it) }
            .filter { recent(date(it, "publishedDate")) }
            .distinctBy { it.getString("id") }.sortedBy { it.getString("id") }
        val severity = advisories.maxOfOrNull { UpdateSeverity.from(it) } ?: UpdateSeverity.UNKNOWN
        val urgent = advisories.any { it.optInt("priority") == 1 || it.optBoolean("knownExploited") }
        val identity = "${installed.distribution.displayName} ${installed.version}"
        val findings = mutableListOf<UpdateFinding>()
        fun finding(kind: UpdateKind, body: String, details: String) {
            findings.add(UpdateFinding(kind, "$identity\n$body", digest("${installed.distribution.apiId}|${installed.version}|$kind|$details")))
        }
        val releases = buildList {
            patch?.let { add("Patch release available: $it.") }
            if (latest != null && latest != patch) add("Newer release available: $latest.")
        }.joinToString("\n")
        if (latest != null) finding(UpdateKind.RELEASE, releases, "$latest|$patch")
        val securityMessage = if (advisories.isNotEmpty()) {
            buildList {
                add("${severity.label} security advisories: ${advisories.take(5).joinToString { it.getString("id") }}${if (advisories.size > 5) " (+${advisories.size - 5} more)" else ""}.")
                if (advisories.any { advisory ->
                    val patches = advisory.optJSONArray("isolatedPatches") ?: JSONArray()
                    (0 until patches.length()).any { index ->
                        val appliesTo = patches.getJSONObject(index).optJSONArray("appliesTo") ?: JSONArray()
                        (0 until appliesTo.length()).any { appliesTo.optString(it) == installed.version }
                    }
                }) {
                    add("Isolated security patches are available for this version.")
                }
                add("Local patch application has not been verified. Review the security report before choosing a fix.")
            }.joinToString("\n").also { finding(UpdateKind.SECURITY, it, advisories.joinToString { canonical(it) }) }
        } else null
        if (eol && recent(eolDate)) finding(UpdateKind.SUPPORT, "This release is past its published end of support.", "$eolDate")
        val message = buildList {
            add(identity)
            if (releases.isNotEmpty()) add(releases)
            securityMessage?.let(::add)
            if (eol) add("This release is past its published end of support.")
            if (!known) add("Security coverage is unknown; no verified security assessment is available.")
            else if (advisories.isEmpty()) add("No security advisories published in the past month were reported for this version. Older advisories may still apply.")
            if (latest == null) add("No newer stable releases published in the past month were found.")
        }.joinToString("\n")
        val fingerprint = digest(findings.joinToString { it.fingerprint })
        return UpdateAssessment(installed, patch, latest, severity, urgent, eol, advisories.size, known, now, message, fingerprint, findings)
    }

    private fun date(item: JSONObject, key: String): LocalDate? = try {
        item.optString(key).takeIf { it.isNotBlank() && it != "null" }?.let(LocalDate::parse)
    } catch (_: DateTimeParseException) {
        null
    }

    internal fun digest(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray()).joinToString("") { "%02x".format(it) }

    private fun canonical(value: Any?): String = when (value) {
        is JSONObject -> value.keySet().sorted().joinToString(prefix = "{", postfix = "}") { JSONObject.quote(it) + ":" + canonical(value.get(it)) }
        is JSONArray -> (0 until value.length()).joinToString(prefix = "[", postfix = "]") { canonical(value.get(it)) }
        is String -> JSONObject.quote(value)
        else -> value.toString()
    }
}
