/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.analysis

import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.util.io.HttpRequests
import com.magento.idea.magento2plugin.project.Settings
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.Instant

/** Publication is independent of prepared compatibility coverage. No tags or guessed versions. */
@Service(Service.Level.PROJECT)
internal class UctPublishedReleases internal constructor(
    private val source: () -> String,
    private val fetch: (String) -> String,
    private val now: () -> Instant
) {
    constructor(project: Project) : this(
        { Settings.getInstance(project).getPublishedReleasesUrl() }, ::download, Instant::now
    )

    private data class Catalog(val sourceUrl: String, val fetchedAt: Instant, val releases: List<Release>)
    private data class Release(val version: String, val publishedAt: String, val url: String) {
        fun json() = JSONObject().put("version", version).put("publishedAt", publishedAt).put("url", url)
    }
    private var cached: Catalog? = null

    @Synchronized
    fun discover(currentVersion: String?): JSONObject {
        currentVersion?.let { UctVersions.compare(it, it) }
        val sourceUrl = Settings.normalizePublishedReleasesUrl(source())
        require(Settings.isValidPublishedReleasesUrl(sourceUrl)) {
            "Invalid Published releases URL. Set an absolute HTTP or HTTPS URL without a fragment in Magento settings."
        }
        val time = now()
        val existing = cached?.takeIf {
            it.sourceUrl == sourceUrl && time.isBefore(it.fetchedAt.plusSeconds(CACHE_SECONDS))
        }
        val catalog = existing ?: load(sourceUrl, time).also { cached = it }
        val next = currentVersion?.let { current -> catalog.releases.firstOrNull {
            !it.version.contains('-') && UctVersions.compare(it.version, current.substringBefore('-')) > 0
        } }
        return JSONObject().put("operation", "releases").put("complete", true)
            .put("edition", "Magento Open Source").put("sourceUrl", catalog.sourceUrl)
            .put("fetchedAt", catalog.fetchedAt.toString()).put("expiresAt", catalog.fetchedAt.plusSeconds(CACHE_SECONDS).toString())
            .put("cacheHit", existing != null).put("currentVersion", currentVersion ?: JSONObject.NULL)
            .put("releases", JSONArray(catalog.releases.map { it.json() }))
            .put("nextMinorRelease", next?.json() ?: JSONObject.NULL)
            .put("selectionRule", "Next stable x.y.z feature release after the installed release line; excludes prereleases and -p security patches.")
            .put("coverageNote", "Published releases from the configured catalog, not local compatibility coverage. Use status for analysis readiness.")
    }

    private fun load(sourceUrl: String, time: Instant): Catalog {
        val releases = linkedMapOf<String, Release>()
        for (page in 1..MAX_PAGES) {
            ProgressManager.checkCanceled()
            val separator = when {
                sourceUrl.endsWith('?') || sourceUrl.endsWith('&') -> ""
                sourceUrl.contains('?') -> "&"
                else -> "?"
            }
            val body = fetch("$sourceUrl${separator}per_page=$PAGE_SIZE&page=$page")
            require(body.length <= MAX_PAGE_BYTES) { "Published release response is too large." }
            val items = JSONArray(body)
            require(items.length() <= PAGE_SIZE) { "Invalid published release page size." }
            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val version = item.getString("tag_name")
                if (item.getBoolean("draft") || item.getBoolean("prerelease") || !STABLE.matches(version)) continue
                val date = item.getString("published_at")
                if (Instant.parse(date).isAfter(time)) continue
                val url = item.getString("html_url")
                require(url.isNotBlank() && Settings.isValidPublishedReleasesUrl(url)) {
                    "Invalid published release link in the configured catalog."
                }
                releases[version] = Release(version, date, url)
            }
            if (items.length() < PAGE_SIZE) {
                require(releases.isNotEmpty()) { "No stable Magento Open Source releases returned by the configured catalog." }
                return Catalog(sourceUrl, time, releases.values.sortedWith { a, b -> UctVersions.compare(a.version, b.version) })
            }
        }
        throw IOException("Published release catalog exceeded $MAX_PAGES pages; no partial catalog was cached. Retry later.")
    }

    companion object {
        internal const val CACHE_SECONDS = 3600L
        private const val PAGE_SIZE = 100
        private const val MAX_PAGES = 10
        private const val MAX_PAGE_BYTES = 8 * 1024 * 1024
        private val STABLE = Regex("\\d+\\.\\d+\\.\\d+(?:-p\\d+)?")

        private fun download(url: String): String = HttpRequests.request(url)
            .userAgent("Magento-PhpStorm-Plugin").accept("application/vnd.github+json")
            .connectTimeout(10000).readTimeout(10000).connect { request ->
                request.inputStream.use { input ->
                    val bytes = input.readNBytes(MAX_PAGE_BYTES + 1)
                    require(bytes.size <= MAX_PAGE_BYTES) { "Published release response is too large." }
                    String(bytes, Charsets.UTF_8)
                }
            }
    }
}
