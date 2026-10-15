/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.updates

import org.json.JSONObject
import com.intellij.openapi.util.Pair
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.time.Instant

class MagentoUpdatesTest {
    private val now = Instant.parse("2026-10-15T12:00:00Z")
    private val installed = InstalledMagento(MagentoDistribution.OPEN_SOURCE, "2.4.8-p9")
    @Test fun `does not guess a feed when the shared detector cannot identify an edition`() {
        assertThrows(IllegalArgumentException::class.java) {
            InstalledMagento.fromVersionData(Pair.create("2.4.8", null))
        }
    }

    @Test fun `unknown versions require an exact installed release`() {
        for (version in listOf("any", "2.4.8-2026-oct", "../../security")) {
            assertThrows(IllegalArgumentException::class.java) {
                InstalledMagento.fromVersionData(Pair.create(version, MagentoDistribution.OPEN_SOURCE.displayName))
            }
        }
    }

    @Test fun `development versions are identified for skipping checks`() {
        for (version in listOf("dev-main", "2.4.x-dev")) {
            assertTrue(InstalledMagento.fromVersionData(Pair.create(version, MagentoDistribution.OPEN_SOURCE.displayName)).development)
        }
    }

    private fun catalog() = """{"data":{
      "2.4.8-p9":{"version":"2.4.8-p9","releaseDate":"2026-05-01","isSecureVersion":true},
      "2.4.8-p10":{"version":"2.4.8-p10","releaseDate":"2026-09-15"},
      "2.4.9":{"version":"2.4.9","releaseDate":"2026-10-01"},
      "2.4.10":{"version":"2.4.10","releaseDate":"2027-01-01"},
      "2.4.11-beta1":{"version":"2.4.11-beta1","releaseDate":"2026-07-01"}
    }}"""
    private fun security(status: String = "vulnerable", severity: String = "critical") = """{"data":{
      "distribution":"magento-community","version":"2.4.8-p9","status":"$status",
      "vulnerableTo":[{"id":"APSB-TEST","severity":"$severity","publishedDate":"2026-10-01","cves":["CVE-1"],"fixedIn":null,
        "isolatedPatches":[{"id":"PATCH-1","appliesTo":["2.4.8-p9"]}]}],"recommendedVersion":"2.4.8-p10"
    }}"""

    @Test fun `only findings published within the rolling calendar month are actionable`() {
        val releases = catalog().replace("2026-09-15", "2026-09-14").replace("2026-10-01", "2026-09-14")
        val result = UpdateEvaluator.evaluate(installed, releases, security().replace("2026-10-01", "2026-09-14"), now)
        assertNull(result.latestVersion)
        assertNull(result.patchVersion)
        assertEquals(0, result.advisoryCount)
        assertFalse(result.actionable)
        assertFalse(result.message.contains("No open advisories"))
        assertFalse(result.message.contains("APSB-TEST"))
        assertTrue(result.message.contains("past month"))
    }

    @Test fun `publication window includes its boundary and today but not future or undated findings`() {
        for (date in listOf("2026-09-15", "2026-10-15", "2026-09-14", "2026-10-16", "", "invalid")) {
            val expected = date in setOf("2026-09-15", "2026-10-15")
            val releases = catalog().replace("2026-09-15", date).replace("2026-10-01", date)
            val result = UpdateEvaluator.evaluate(installed, releases, security().replace("2026-10-01", date), now)
            assertEquals(date, expected, result.latestVersion != null)
            assertEquals(date, expected, result.advisoryCount > 0)
        }
    }

    @Test fun `recent low severity advisory is not promoted by an older critical advisory`() {
        val report = JSONObject(security(severity = "low"))
        report.getJSONObject("data").getJSONArray("vulnerableTo").put(JSONObject()
            .put("id", "APSB-OLD").put("severity", "critical").put("publishedDate", "2026-09-14").put("priority", 1))
        val result = UpdateEvaluator.evaluate(installed, catalog(), report.toString(), now)
        assertEquals(1, result.advisoryCount)
        assertEquals(UpdateSeverity.LOW, result.severity)
        assertFalse(result.urgent)
        assertFalse(result.message.contains("APSB-OLD"))
    }

    @Test fun `isolated patch must explicitly apply to installed version`() {
        val result = UpdateEvaluator.evaluate(installed, catalog(), security().replace("\"appliesTo\":[\"2.4.8-p9\"]", "\"appliesTo\":[\"2.4.9\"]"), now)
        assertEquals(1, result.advisoryCount)
        assertFalse(result.message.contains("Isolated security patches are available for this version"))
    }

    @Test fun `older end of support alone does not produce a notification`() {
        val releases = JSONObject(catalog().replace("2026-09-15", "2026-08-01").replace("2026-10-01", "2026-08-01"))
        releases.getJSONObject("data").getJSONObject(installed.version).put("eolDate", "2026-09-14").put("isEOLVersion", true)
        val result = UpdateEvaluator.evaluate(installed, releases.toString(), null, now)
        assertTrue(result.endOfLife)
        assertFalse(result.actionable)
    }

    @Test fun `selects numeric patch and feature updates separately and excludes future releases`() {
        val result = UpdateEvaluator.evaluate(installed, catalog(), security(), now)
        assertEquals("2.4.8-p10", result.patchVersion)
        assertEquals("2.4.9", result.latestVersion)
        assertEquals(UpdateSeverity.CRITICAL, result.severity)
        assertTrue(result.message.contains("not been verified"))
        assertTrue(result.message.contains("APSB-TEST"))
    }

    @Test fun `latest supported version can still require an isolated patch`() {
        val releases = """{"data":{"2.4.8-p9":{"version":"2.4.8-p9","releaseDate":"2026-05-01","isLatestVersion":true,"isSecureVersion":true}}}"""
        val result = UpdateEvaluator.evaluate(installed, releases, security(), now)
        assertNull(result.latestVersion)
        assertTrue(result.actionable)
        assertEquals(UpdateSeverity.CRITICAL, result.severity)
    }

    @Test fun `missing Mage OS coverage is not reported as secure`() {
        val result = UpdateEvaluator.evaluate(InstalledMagento(MagentoDistribution.MAGE_OS, "3.5.0"),
            """{"data":{"3.5.0":{"version":"3.5.0","releaseDate":"2026-09-01"}}}""",
            """{"data":{"distribution":"mage-os","version":"3.5.0","status":"unknown","vulnerableTo":[]}}""", now)
        assertTrue(result.message.contains("Security coverage is unknown"))
        assertFalse(result.actionable)
        assertFalse(result.message.contains("secure"))
    }

    @Test fun `mapped upstream advisories are reported through the Mage OS feed alone`() {
        val requests = mutableListOf<String>()
        val client = MagentoUpdateFeed({ url ->
            requests.add(url)
            if (url.endsWith("/security")) security().replace("magento-community", "mage-os").replace("2.4.8-p9", "3.5.0")
            else """{"data":{"3.5.0":{"version":"3.5.0","releaseDate":"2026-09-01"}}}"""
        }, { now })
        val result = client.check(MagentoUpdateSettings.DEFAULT_FEED_URL, InstalledMagento(MagentoDistribution.MAGE_OS, "3.5.0"))
        assertTrue(result.assessment.actionable)
        assertEquals(UpdateSeverity.CRITICAL, result.assessment.severity)
        assertTrue(result.assessment.message.contains("APSB-TEST"))
        assertTrue(requests.all { it.contains("/mage-os/") })
    }

    @Test fun `severity and urgency remain independent`() {
        for ((score, expected) in listOf(9.0 to UpdateSeverity.CRITICAL, 8.9 to UpdateSeverity.HIGH,
            7.0 to UpdateSeverity.HIGH, 6.9 to UpdateSeverity.MEDIUM, 4.0 to UpdateSeverity.MEDIUM,
            3.9 to UpdateSeverity.LOW, -1.0 to UpdateSeverity.UNKNOWN, 10.1 to UpdateSeverity.UNKNOWN)) {
            assertEquals(expected, UpdateSeverity.from(JSONObject().put("cvssScore", score)))
        }
        val report = JSONObject(security(severity = "medium"))
        report.getJSONObject("data").getJSONArray("vulnerableTo").getJSONObject(0).put("priority", 1)
        val result = UpdateEvaluator.evaluate(installed, catalog(), report.toString(), now)
        assertTrue(result.urgent)
        assertEquals(UpdateSeverity.MEDIUM, result.severity)
        assertFalse(result.message.contains("actively exploited"))
    }

    @Test fun `end of support is separate from vulnerability severity`() {
        val releases = JSONObject(catalog())
        releases.getJSONObject("data").getJSONObject(installed.version).put("eolDate", "2026-10-14")
        val result = UpdateEvaluator.evaluate(installed, releases.toString(), null, now)
        assertTrue(result.endOfLife)
        assertEquals(UpdateSeverity.UNKNOWN, result.severity)
        assertTrue(result.message.contains("end of support"))
    }

    @Test fun `a security endpoint failure still reports release updates without claiming security`() {
        val result = UpdateEvaluator.evaluate(installed, catalog(), null, now)
        assertEquals("2.4.8-p10", result.patchVersion)
        assertTrue(result.message.contains("Security coverage is unknown"))
    }

    @Test fun `unknown severity is preserved and cannot hide an advisory`() {
        val result = UpdateEvaluator.evaluate(installed, catalog(), security(severity = "unrated"), now)
        assertEquals(UpdateSeverity.UNKNOWN, result.severity)
        assertTrue(result.actionable)
    }

    @Test fun `rejects security data for a different installation`() {
        assertThrows(IllegalArgumentException::class.java) {
            UpdateEvaluator.evaluate(installed, catalog(), security().replace("magento-community", "mage-os"), now)
        }
    }

    @Test fun `advisory revisions change the notification fingerprint but fetch dates do not`() {
        val first = UpdateEvaluator.evaluate(installed, catalog(), security(), now)
        assertEquals(first.fingerprint, UpdateEvaluator.evaluate(installed, catalog(), security(), now.plusSeconds(60)).fingerprint)
        assertNotEquals(first.fingerprint, UpdateEvaluator.evaluate(installed, catalog(), security().replace("PATCH-1", "PATCH-2"), now).fingerprint)
    }

    @Test fun `feed cache is shared by identity with manual refresh and stale fallback`() {
        var calls = 0
        var offline = false
        val client = MagentoUpdateFeed({ url ->
            calls++
            if (offline) throw IOException("Offline")
            if (url.endsWith("/security")) security() else catalog()
        }, { now })
        val url = "https://updates.example.test/api/v1"
        assertFalse(client.check(url, installed).stale)
        client.check(url, installed)
        assertEquals(2, calls)
        client.check(url, installed, force = true)
        assertEquals(4, calls)
        offline = true
        val cached = client.check(url, installed, force = true)
        assertTrue(cached.stale)
        assertEquals(now, cached.assessment.checkedAt)
    }

    @Test fun `next hourly refresh drops findings that aged out of the publication window`() {
        var time = Instant.parse("2026-10-15T23:30:00Z")
        val client = MagentoUpdateFeed({ url ->
            if (url.endsWith("/security")) security().replace("2026-10-01", "2026-09-15") else catalog()
        }, { time })
        assertEquals(1, client.check(MagentoUpdateSettings.DEFAULT_FEED_URL, installed).assessment.advisoryCount)
        time = time.plusSeconds(3600)
        assertEquals(0, client.check(MagentoUpdateSettings.DEFAULT_FEED_URL, installed).assessment.advisoryCount)
    }

    @Test fun `stale fallback does not resurrect findings older than one month`() {
        var time = now
        var offline = false
        val client = MagentoUpdateFeed({ url ->
            if (offline) throw IOException("Offline")
            if (url.endsWith("/security")) security() else catalog()
        }, { time })
        assertTrue(client.check(MagentoUpdateSettings.DEFAULT_FEED_URL, installed).assessment.actionable)
        time = Instant.parse("2026-11-16T12:00:00Z")
        offline = true
        val result = client.check(MagentoUpdateSettings.DEFAULT_FEED_URL, installed)
        assertTrue(result.stale)
        assertFalse(result.assessment.actionable)
        assertEquals(now, result.assessment.checkedAt)
    }

    @Test fun `feed URL rejects credentials fragments and non HTTPS remote endpoints`() {
        for (url in listOf("file:///tmp/feed", "https://user:secret@example.test/api", "http://example.test/api", "https://example.test/api#x", "https://example.test/api?x=y")) {
            assertFalse(MagentoUpdateSettings.isValidFeedUrl(url))
        }
        assertTrue(MagentoUpdateSettings.isValidFeedUrl(MagentoUpdateSettings.DEFAULT_FEED_URL))
        assertTrue(MagentoUpdateSettings.isValidFeedUrl("http://127.0.0.1:1234/api/v1"))
    }
}
