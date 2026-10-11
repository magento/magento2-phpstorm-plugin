/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.analysis

import com.magento.idea.magento2plugin.PhysicalMagentoTestCase
import com.magento.idea.magento2plugin.project.Settings
import java.net.URI
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertThrows
import java.io.IOException
import java.time.Instant

class UctPublishedReleasesTest : PhysicalMagentoTestCase() {
    private val today = Instant.parse("2026-10-10T00:00:00Z")
    private fun service(fetch: (Int) -> String, now: () -> Instant) = UctPublishedReleases(
        { Settings.DEFAULT_PUBLISHED_RELEASES_URL },
        { url -> fetch(URI(url).query.substringAfterLast("page=").toInt()) }, now
    )

    private fun release(version: String, draft: Boolean = false, prerelease: Boolean = false) = JSONObject()
        .put("tag_name", version).put("draft", draft).put("prerelease", prerelease)
        .put("published_at", "2026-05-12T00:00:00Z")
        .put("html_url", "https://github.com/magento/magento2/releases/tag/$version")

    fun testStablePublishedNextMinorExcludesPatchesDraftsPrereleasesAndFutureDates() {
        val rows = JSONArray(listOf(release("2.4.10", draft = true), release("2.4.9"),
            release("2.4.9-beta1", prerelease = true), release("2.4.8-p6"), release("2.4.8"),
            release("2.4.11").put("published_at", "2099-01-01T00:00:00Z")))
        val service = service({ rows.toString() }, { today })
        val result = service.discover("2.4.8-p5", includeReleases = true)
        assertEquals("2.4.9", result.getJSONObject("nextMinorRelease").getString("version"))
        assertEquals(3, result.getJSONArray("releases").length())
        assertEquals(Settings.DEFAULT_PUBLISHED_RELEASES_URL, result.getString("sourceUrl"))
        assertEquals(today.toString(), result.getString("fetchedAt"))
        assertTrue(service.discover("2.4.9").isNull("nextMinorRelease"))
        assertTrue(service.discover(null).isNull("nextMinorRelease"))
    }

    fun testPaginationIsCompleteAndCacheExpires() {
        var time = today
        var requests = 0
        val service = service({ page ->
            requests++
            if (page == 1) JSONArray((1..100).map { release("2.4.8-p$it") }).toString()
            else JSONArray(listOf(release("2.4.9"))).toString()
        }, { time })
        val first = service.discover("2.4.8-p5", includeReleases = true)
        assertEquals(101, first.getJSONArray("releases").length())
        assertFalse(first.getBoolean("cacheHit"))
        assertEquals(2, requests)
        assertTrue(service.discover("2.4.8-p5").getBoolean("cacheHit"))
        assertEquals(2, requests)
        time = time.plusSeconds(UctPublishedReleases.CACHE_SECONDS)
        assertFalse(service.discover("2.4.8-p5").getBoolean("cacheHit"))
        assertEquals(4, requests)
    }

    fun testFailedRefreshDoesNotMasqueradeAsAnEmptyOrFreshCachedCatalog() {
        var time = today
        var failing = false
        var calls = 0
        val service = service({
            calls++
            if (failing) throw IOException("HTTP 429")
            JSONArray(listOf(release("2.4.9"))).toString()
        }, { time })
        service.discover("2.4.8-p5")
        time = time.plusSeconds(UctPublishedReleases.CACHE_SECONDS)
        failing = true
        assertThrows(IOException::class.java) { service.discover("2.4.8-p5") }
        failing = false
        assertFalse(service.discover("2.4.8-p5").getBoolean("cacheHit"))
        assertEquals(3, calls)
    }

    fun testIncompleteAndInvalidCatalogsAreRejected() {
        val fullPage = JSONArray((1..100).map { release("2.4.8-p$it") }).toString()
        val service = service({ fullPage }, { today })
        assertThrows(IOException::class.java) { service.discover("2.4.8-p5") }
        for (body in listOf("[]", JSONArray(listOf(release("2.4.9").put("html_url", "file:///tmp/release"))).toString())) {
            assertThrows(IllegalArgumentException::class.java) { service({ body }, { today }).discover(null) }
        }
    }
    fun testDefaultDiscoveryIsCompactAndCatalogCanBeRequestedWithoutRefetching() {
        var requests = 0
        val service = service({ requests++; JSONArray(listOf(release("2.4.8-p5"), release("2.4.9"))).toString() }, { today })
        val compact = service.discover("2.4.8-p5")
        assertFalse(compact.has("releases"))
        assertFalse(compact.getBoolean("includesReleases"))
        assertEquals(2, compact.getInt("releaseCount"))
        assertEquals("2.4.9", compact.getJSONObject("nextMinorRelease").getString("version"))
        val full = service.discover("2.4.8-p5", includeReleases = true)
        assertEquals(2, full.getJSONArray("releases").length())
        assertTrue(full.getBoolean("cacheHit"))
        assertEquals(1, requests)
    }

}
