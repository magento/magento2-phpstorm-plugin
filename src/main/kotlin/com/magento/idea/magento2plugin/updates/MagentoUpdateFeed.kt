/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.updates

import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.util.io.HttpRequests
import kotlinx.coroutines.CancellationException
import java.time.Instant

data class UpdateFeedResult(val assessment: UpdateAssessment, val stale: Boolean = false, val retryNeeded: Boolean = false)

internal class UpdateFeedUnavailableException(cause: Exception) : Exception("Magento update feed is unavailable.", cause)

/** Application cache avoids downloading the same public metadata for every open project. */
@Service(Service.Level.APP)
class MagentoUpdateFeed internal constructor(private val fetch: (String) -> String, private val now: () -> Instant) {
    constructor() : this(::download, Instant::now)
    private data class CachedFeed(val catalog: String, val security: String, val checkedAt: Instant) {
        fun assess(installed: InstalledMagento, now: Instant, localPatches: LocalSecurityPatches): UpdateAssessment =
            UpdateEvaluator.evaluate(installed, catalog, security, now, localPatches).copy(checkedAt = checkedAt)
    }
    private val cache = linkedMapOf<String, CachedFeed>()

    fun check(feedUrl: String, installed: InstalledMagento, force: Boolean = false): UpdateFeedResult =
        check(feedUrl, installed, force, LocalSecurityPatches.EMPTY)

    @Synchronized
    internal fun check(feedUrl: String, installed: InstalledMagento, force: Boolean, localPatches: LocalSecurityPatches): UpdateFeedResult {
        require(MagentoUpdateSettings.isValidFeedUrl(feedUrl)) { "Updates feed URL must use HTTPS (HTTP is allowed on localhost)." }
        val base = MagentoUpdateSettings.normalizeFeedUrl(feedUrl)
        val key = "$base/${installed.distribution.apiId}/${installed.version}"
        val previous = cache[key]
        val time = now()
        if (!force && previous != null && time.isBefore(previous.checkedAt.plusSeconds(3600))) {
            return UpdateFeedResult(previous.assess(installed, time, localPatches))
        }
        try {
            val catalog = fetch("$base/${installed.distribution.apiId}/versions")
            val security = try {
                fetch("$base/${installed.distribution.apiId}/versions/${installed.version}/security")
            } catch (exception: Exception) {
                rethrowCancellation(exception)
                null
            }
            val assessment = UpdateEvaluator.evaluate(installed, catalog, security, time, localPatches)
            if (security != null) {
                if (cache.size >= 100) cache.remove(cache.keys.first())
                cache[key] = CachedFeed(catalog, security, time)
            }
            return UpdateFeedResult(assessment, retryNeeded = security == null)
        } catch (exception: Exception) {
            rethrowCancellation(exception)
            if (previous != null) return UpdateFeedResult(previous.assess(installed, time, localPatches), stale = true, retryNeeded = true)
            throw UpdateFeedUnavailableException(exception)
        }
    }

    companion object {
        private const val MAX_BYTES = 4 * 1024 * 1024
        private fun download(url: String): String = HttpRequests.request(url)
            .userAgent("Magento-PhpStorm-Plugin").accept("application/json")
            .connectTimeout(10000).readTimeout(10000).connect { request ->
                request.inputStream.use {
                    val bytes = it.readNBytes(MAX_BYTES + 1)
                    require(bytes.size <= MAX_BYTES) { "Updates feed response is too large." }
                    bytes.toString(Charsets.UTF_8)
                }
            }

        internal fun rethrowCancellation(exception: Exception) {
            if (exception is CancellationException || exception is ProcessCanceledException) throw exception
        }
    }
}
