/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.updates

import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

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
    private data class PublicationDate(val date: LocalDate, val instant: Instant? = null) {
        fun describe(now: Instant): String {
            val relative = if (instant == null) {
                val days = ChronoUnit.DAYS.between(date, now.atZone(ZoneOffset.UTC).toLocalDate())
                if (days == 0L) "today" else ago(days, "day")
            } else {
                val seconds = Duration.between(instant, now).seconds
                when {
                    seconds < 60 -> "just now"
                    seconds < 3600 -> ago(seconds / 60, "minute")
                    seconds < 86400 -> ago(seconds / 3600, "hour")
                    else -> ago(seconds / 86400, "day")
                }
            }
            return "Published $relative"
        }

        private fun ago(amount: Long, unit: String) = "$amount $unit${if (amount == 1L) "" else "s"} ago"
    }

    private data class PublishedRelease(val version: String, val parsed: ReleaseVersion, val publishedAt: PublicationDate)

    fun evaluate(installed: InstalledMagento, catalogBody: String, securityBody: String?, now: Instant,
                 localPatches: LocalSecurityPatches = LocalSecurityPatches.EMPTY,
                 window: UpdateCheckWindow = UpdateCheckWindow.MONTH): UpdateAssessment {
        val data = JSONObject(catalogBody).getJSONObject("data")
        require(data.length() in 1..10000) { "Invalid release catalog." }
        val current = ReleaseVersion.parse(installed.version) ?: error("Unsupported installed version.")
        val today = now.atZone(ZoneOffset.UTC).toLocalDate()
        val since = window.since(now)
        val sinceDate = since.atZone(ZoneOffset.UTC).toLocalDate()
        fun recent(publication: PublicationDate?): Boolean = publication != null &&
            if (publication.instant != null) !publication.instant.isBefore(since) && !publication.instant.isAfter(now)
            else publication.date >= sinceDate && publication.date <= today
        val versions = data.keySet().mapNotNull { key ->
            val item = data.getJSONObject(key)
            val version = item.getString("version")
            require(version == key) { "Release catalog identifier does not match its version." }
            val parsed = ReleaseVersion.parse(version) ?: return@mapNotNull null
            val publication = publicationDate(item, "releaseDate") ?: return@mapNotNull null
            if (!parsed.stable || !recent(publication) || item.optBoolean("isFutureVersion")) null
            else PublishedRelease(version, parsed, publication)
        }
        val updates = versions.filter { it.parsed > current }
        val latestRelease = updates.maxByOrNull { it.parsed }
        val patchRelease = updates.filter { it.parsed.line == current.line }.maxByOrNull { it.parsed }
        val latest = latestRelease?.version
        val patch = patchRelease?.version
        val currentMetadata = data.optJSONObject(installed.version)
        val eolPublication = currentMetadata?.let { publicationDate(it, "eolDate") }
        val eolDate = eolPublication?.date
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
        val recentAdvisories = (0 until rows.length()).map { rows.getJSONObject(it) }
            .filter { recent(publicationDate(it, "publishedDate")) }
            .distinctBy { it.getString("id") }.sortedBy { it.getString("id") }
        val advisories = recentAdvisories.filterNot { localPatches.covers(it, installed) }
        val coveredCount = recentAdvisories.size - advisories.size
        val severity = advisories.maxOfOrNull { UpdateSeverity.from(it) } ?: UpdateSeverity.UNKNOWN
        val urgent = advisories.any { it.optInt("priority") == 1 || it.optBoolean("knownExploited") }
        val identity = "${installed.distribution.displayName} ${installed.version}"
        val findings = mutableListOf<UpdateFinding>()
        fun finding(kind: UpdateKind, body: String, details: String) {
            val message = if (kind == UpdateKind.SUPPORT) "$identity\n$body" else "$body\n$identity"
            findings.add(UpdateFinding(kind, message, digest("${installed.distribution.apiId}|${installed.version}|$kind|$details")))
        }
        val releases = buildList {
            patchRelease?.let { add("${it.publishedAt.describe(now)}\nPatch release available: ${it.version}.") }
            if (latestRelease != null && latest != patch) {
                add("${latestRelease.publishedAt.describe(now)}\nNewer release available: $latest.")
            }
        }.joinToString("\n")
        if (latest != null) finding(UpdateKind.RELEASE, releases, "$latest|$patch")
        val securityMessage = if (advisories.isNotEmpty()) {
            buildList {
                advisories.take(5).forEach { advisory ->
                    val publication = requireNotNull(publicationDate(advisory, "publishedDate")).describe(now)
                    add("$publication\n${UpdateSeverity.from(advisory).label} security advisory: ${advisory.getString("id")}.")
                }
                if (advisories.size > 5) add("(+${advisories.size - 5} more security advisories).")
                if (advisories.any { advisory ->
                    val patches = advisory.optJSONArray("isolatedPatches") ?: JSONArray()
                    (0 until patches.length()).any { index ->
                        val appliesTo = patches.getJSONObject(index).optJSONArray("appliesTo") ?: JSONArray()
                        (0 until appliesTo.length()).any { appliesTo.optString(it) == installed.version }
                    }
                }) {
                    add("Isolated security patches are available for this version.")
                }
                add("No complete local patch coverage was found for these advisories. Review the security report before choosing a fix.")
            }.joinToString("\n").also { finding(UpdateKind.SECURITY, it, advisories.joinToString { canonical(it) }) }
        } else null
        if (eol && recent(eolPublication)) finding(UpdateKind.SUPPORT, "This release is past its published end of support.", "$eolDate")
        val message = buildList {
            add(identity)
            if (releases.isNotEmpty()) add(releases)
            securityMessage?.let(::add)
            if (eol) add("This release is past its published end of support.")
            if (!known) add("Security coverage is unknown; no verified security assessment is available.")
            else if (advisories.isEmpty() && coveredCount == 0) add("No security advisories published in ${window.period} were reported for this version. Older advisories may still apply.")
            if (coveredCount > 0) add("Recent security advisories covered by installed fixes or local patch configuration: $coveredCount. Deployment status has not been verified.")
            if (latest == null) add("No newer stable releases published in ${window.period} were found.")
        }.joinToString("\n")
        val fingerprint = digest(findings.joinToString { it.fingerprint })
        return UpdateAssessment(installed, patch, latest, severity, urgent, eol, advisories.size, known, now, message, fingerprint, findings)
    }

    private fun publicationDate(item: JSONObject, key: String): PublicationDate? {
        val value = item.optString(key).takeIf { it.isNotBlank() && it != "null" } ?: return null
        return try {
            PublicationDate(LocalDate.parse(value))
        } catch (_: DateTimeParseException) {
            try {
                val instant = OffsetDateTime.parse(value).toInstant()
                PublicationDate(instant.atZone(ZoneOffset.UTC).toLocalDate(), instant)
            } catch (_: DateTimeParseException) {
                null
            }
        }
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
