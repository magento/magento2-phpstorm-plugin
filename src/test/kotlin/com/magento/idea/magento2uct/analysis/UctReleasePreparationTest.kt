/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.analysis

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.util.ProgressIndicatorBase
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.PlatformTestUtil
import com.magento.idea.magento2plugin.PhysicalMagentoTestCase
import com.sun.net.httpserver.HttpServer
import org.junit.Assert.assertThrows
import java.net.InetSocketAddress
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class UctReleasePreparationTest : PhysicalMagentoTestCase() {
    private lateinit var cache: Path
    private lateinit var preparation: UctReleasePreparation

    override fun setUp() {
        super.setUp()
        cache = Files.createTempDirectory("uct-preparation-")
        preparation = UctReleasePreparation(project, cache, UctReleaseSource())
        Disposer.register(testRootDisposable, preparation)
    }

    private fun await(id: String) {
        PlatformTestUtil.waitWithEventsDispatching("preparation completion", { preparation.results(id).state != "running" }, 20)
    }

    fun testPublishesACompleteReleasePersistsAcrossStoresAndReusesCacheWithoutNetwork() {
        val index = UctReleaseFixture.index()
        val before = preparation.catalog()
        val started = preparation.start("2.4.9") { _, _, progress ->
            progress(UctPreparationProgress("indexing_release", processedFiles = 1, totalFiles = 2)); index
        }
        await(started.runId!!)
        assertEquals("completed", preparation.results(started.runId).state)
        assertTrue(Files.isRegularFile(cache.resolve("2.4.9.json")))
        assertTrue(preparation.catalog().isPrepared("2.4.9"))
        assertFalse(before.isPrepared("2.4.9"))
        val reopened = UctReleasePreparation(project, cache, UctReleaseSource { error("Unexpected download") })
        try {
            assertTrue(reopened.catalog().isPrepared("2.4.9"))
            val ready = reopened.start("2.4.9")
            assertNull(ready.runId)
            assertEquals("completed", ready.state)
            assertEquals(index, ready.index)
        } finally { reopened.dispose() }
    }

    fun testBlockingPreparationShowsProgressAndPublishesTheRelease() {
        val server = releaseServer()
        val blocking = UctReleasePreparation(project, Files.createTempDirectory("uct-blocking-"), localSource(server, "release"))
        try {
            val indicator = ProgressIndicatorBase()
            assertNull(prepareInBackground(blocking, "2.4.9", indicator))
            assertTrue(blocking.catalog().isPrepared("2.4.9"))
            assertEquals("Preparing Magento 2.4.9 compatibility data", indicator.text)
        } finally { blocking.dispose(); server.stop(0) }
    }

    fun testBlockingPreparationReportsFailureAndCancellation() {
        val server = releaseServer()
        val release = CountDownLatch(1)
        val failing = UctReleasePreparation(project, Files.createTempDirectory("uct-failing-"), localSource(server, "missing"))
        val waiting = UctReleasePreparation(project, Files.createTempDirectory("uct-waiting-"), waitingSource(server, release))
        try {
            assertTrue(prepareInBackground(failing, "2.4.9", ProgressIndicatorBase()) is IllegalStateException)
            assertFalse(failing.catalog().isPrepared("2.4.9"))
            val indicator = ProgressIndicatorBase().apply { start(); cancel() }
            assertTrue(prepareInBackground(waiting, "2.4.9", indicator) is ProcessCanceledException)
            assertFalse(waiting.catalog().isPrepared("2.4.9"))
            assertTrue(waiting.activeRunIds().isEmpty())
        } finally { release.countDown(); failing.dispose(); waiting.dispose(); server.stop(0) }
    }

    private fun releaseServer(): HttpServer {
        val bytes = UctReleaseFixture.archive("2.4.9")
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/missing") { exchange -> exchange.sendResponseHeaders(404, -1); exchange.close() }
        server.createContext("/release") { exchange ->
            exchange.sendResponseHeaders(200, bytes.size.toLong()); exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        return server
    }

    private fun localSource(server: HttpServer, path: String) =
        UctReleaseSource { "http://127.0.0.1:${server.address.port}/$path" }

    private fun waitingSource(server: HttpServer, release: CountDownLatch) =
        UctReleaseSource { release.await(10, TimeUnit.SECONDS); "http://127.0.0.1:${server.address.port}/release" }

    private fun prepareInBackground(preparation: UctReleasePreparation, version: String,
                                    indicator: ProgressIndicatorBase): Throwable? {
        val failure = AtomicReference<Throwable>()
        val done = AtomicBoolean()
        ApplicationManager.getApplication().executeOnPooledThread {
            try { preparation.prepare(version, indicator) } catch (error: Throwable) { failure.set(error) } finally { done.set(true) }
        }
        PlatformTestUtil.waitWithEventsDispatching("blocking preparation", done::get, 20)
        return failure.get()
    }

    fun testEditorStateFollowsTheCatalogAndReportsNothingWithoutData() {
        val removed = "\\Magento\\CloudComponents\\Model\\UrlFixer"
        val framework = "\\Magento\\Framework\\AppInterface"
        val bundled = preparation.editorState("2.4.2", "2.4.4-beta4", false)
        assertFalse(bundled.isExists(removed))
        assertSame(bundled, preparation.editorState("2.4.2", "2.4.4-beta4", false))
        for (state in listOf(preparation.editorState("2.4.2", "2.4.9", false),
            preparation.editorState("2.4.4-beta4", "2.4.2", false), preparation.editorState(null, null, false))) {
            assertTrue(state.isExists(removed))
            assertFalse(state.isPresentInCodebase(framework))
            assertTrue(state.isApi(removed))
        }
        val started = preparation.start("2.4.9") { _, _, _ -> UctReleaseFixture.index() }
        await(started.runId!!)
        assertTrue(preparation.editorState(null, "2.4.9", false).isPresentInCodebase(framework))
    }

    fun testCancellationIsIdempotentAndLateWorkCannotPublishData() {
        val entered = AtomicBoolean()
        val returned = AtomicBoolean()
        val release = CountDownLatch(1)
        try {
            val started = preparation.start("2.4.9") { _, _, _ ->
                entered.set(true); check(release.await(10, TimeUnit.SECONDS)); returned.set(true); UctReleaseFixture.index()
            }
            PlatformTestUtil.waitWithEventsDispatching("preparation start", entered::get, 10)
            assertEquals(started.runId, preparation.start("2.4.9").runId)
            assertThrows(IllegalArgumentException::class.java) { preparation.start("2.4.8") }
            assertFalse(preparation.catalog().isPrepared("2.4.9"))
            assertEquals("cancelled", preparation.cancel(started.runId!!).state)
            assertEquals("cancelled", preparation.cancel(started.runId).state)
            release.countDown()
            PlatformTestUtil.waitWithEventsDispatching("cancelled preparation worker return", returned::get, 10)
            assertFalse(preparation.catalog().isPrepared("2.4.9"))
            assertFalse(Files.exists(cache.resolve("2.4.9.json")))
            assertNull(preparation.results(started.runId).index)
        } finally { release.countDown() }
    }

    fun testFailureDoesNotPublishAndRetryCanSucceed() {
        val failed = preparation.start("2.4.9") { _, _, _ -> error("Release download failed") }
        await(failed.runId!!)
        assertEquals("failed", preparation.results(failed.runId).state)
        assertEquals("Release download failed", preparation.results(failed.runId).error)
        assertFalse(preparation.catalog().isPrepared("2.4.9"))
        val retry = preparation.start("2.4.9") { _, _, _ -> UctReleaseFixture.index() }
        await(retry.runId!!)
        assertEquals("completed", preparation.results(retry.runId).state)
    }

    fun testCorruptMismatchedAndPartialCacheEntriesAreRejectedAndCanBeRepaired() {
        Files.writeString(cache.resolve("2.4.9.json"), "{invalid")
        Files.writeString(cache.resolve("2.4.8.json"), UctReleaseFixture.index().json().toString())
        Files.writeString(cache.resolve("2.4.7.json"), UctReleaseFixture.index("2.4.7").json().put("api", org.json.JSONObject()).toString())
        assertFalse(preparation.catalog().isPrepared("2.4.9"))
        assertEquals(3, preparation.warnings().size)
        val repair = preparation.start("2.4.9") { _, _, _ -> UctReleaseFixture.index() }
        await(repair.runId!!)
        assertTrue(preparation.catalog().isPrepared("2.4.9"))
        assertFalse(preparation.warnings().containsKey("2.4.9"))
    }

    fun testPreparedCatalogResetsFlagsDetectsRemovalAndDoesNotPolluteEarlierSnapshots() {
        val removed = "\\Magento\\Fixture\\Removed"
        val restored = "\\Magento\\Fixture\\Restored"
        val changed = "\\Magento\\Fixture\\Changed"
        val baseline = UctReleaseFixture.index("2.4.8-p5", setOf(removed, changed), deprecated = setOf(changed))
        val target = UctReleaseFixture.index("2.4.9", setOf(changed), api = emptySet())
        val future = UctReleaseFixture.index("2.4.10", setOf(restored))
        val catalog = UctIndexCatalog.bundled().withReleases(mapOf(baseline.version to baseline, target.version to target, future.version to future))
        val snapshot = catalog.snapshot("2.4.8-p5", "2.4.9", false)
        assertFalse(snapshot.isExists(removed))
        assertTrue(snapshot.isPresentInCodebase(removed))
        assertFalse(snapshot.isDeprecated(changed))
        assertFalse(snapshot.isApi(changed))
        assertTrue(snapshot.getRemovedInVersion(removed).contains("first change release unknown"))
        assertFalse(snapshot.isPresentInCodebase(restored))
        assertTrue(catalog.snapshot(null, "2.4.8-p5", false).isExists(removed))
        assertTrue(catalog.snapshot("2.4.8-p5", "2.4.9", true).isExists("Foo\\Unrelated"))
        assertThrows(IllegalArgumentException::class.java) { catalog.snapshot("2.4.8", "2.4.9", false) }
    }

    fun testPublicationFailureCannotReportReady() {
        val started = preparation.start("2.4.9") { _, _, _ ->
            Files.createDirectory(cache.resolve("2.4.9.json"))
            UctReleaseFixture.index()
        }
        await(started.runId!!)
        val result = preparation.results(started.runId)
        assertEquals("failed", result.state)
        assertNotNull(result.error)
        assertNull(result.index)
        assertFalse(preparation.catalog().isPrepared("2.4.9"))
    }

    fun testPreparedBaselineSuppressionAppliesToDeprecationsAndNonApiIssues() {
        val internal = "\\Magento\\Fixture\\Internal"
        val deprecated = "\\Magento\\Fixture\\Deprecated"
        val baseline = UctReleaseFixture.index("2.4.8-p5", setOf(internal, deprecated), api = emptySet(), deprecated = setOf(deprecated))
        val target = UctReleaseFixture.index("2.4.9", setOf(internal, deprecated), api = emptySet(), deprecated = setOf(deprecated))
        val catalog = UctIndexCatalog.bundled().withReleases(mapOf(baseline.version to baseline, target.version to target))
        val all = catalog.snapshot("2.4.8-p5", "2.4.9", false)
        val delta = catalog.snapshot("2.4.8-p5", "2.4.9", true)
        assertFalse(all.isApi(internal))
        assertTrue(all.isDeprecated(deprecated))
        assertTrue(delta.isApi(internal))
        assertFalse(delta.isDeprecated(deprecated))
    }

    fun testArchivedPreparationsSurviveEvictionAndRestartWithinTheirProjectCache() {
        val ids = arrayListOf<String>()
        repeat(6) { number ->
            val version = "2.4.${number + 5}"
            val started = preparation.start(version) { _, _, _ -> UctReleaseFixture.index(version) }
            await(started.runId!!); ids += started.runId
        }
        assertEquals("completed", preparation.results(ids.first()).state)
        assertTrue(preparation.reportInfo(ids.first()).getBoolean("saved"))
        val reopened = UctReleasePreparation(project, cache, UctReleaseSource())
        try {
            assertTrue(reopened.owns(ids.first()))
            assertEquals("completed", reopened.results(ids.first()).state)
            assertEquals(preparation.results(ids.last()).summary.toString(), reopened.results(ids.last()).summary.toString())
            assertEquals("completed", reopened.cancel(ids.first()).state)
        } finally { reopened.dispose() }
        val other = UctReleasePreparation(project, Files.createTempDirectory("other-preparation-reports-"), UctReleaseSource())
        try { assertThrows(IllegalArgumentException::class.java) { other.results(ids.last()) } }
        finally { other.dispose() }
    }
}
