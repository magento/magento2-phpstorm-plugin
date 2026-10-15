/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.updates

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.replaceService
import com.magento.idea.magento2plugin.PhysicalMagentoTestCase
import com.magento.idea.magento2plugin.project.Settings
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Delay
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.io.IOException
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

class MagentoUpdateServiceTest : PhysicalMagentoTestCase() {
    private val notifications = CopyOnWriteArrayList<Notification>()
    private val requests = AtomicInteger()
    private var now = Instant.parse("2026-10-15T12:00:00Z")
    private var unavailable = false
    private var securityUnavailable = false
    private var catalog = """{"data":{"2.4.8-p5":{"version":"2.4.8-p5","releaseDate":"2026-05-12"}}}"""
    private var security = """{"data":{"distribution":"magento-community","version":"2.4.8-p5","status":"vulnerable","vulnerableTo":[{"id":"APSB-TEST","severity":"critical","publishedDate":"2026-10-01","isolatedPatches":[{"id":"PATCH-1","appliesTo":["2.4.8-p5"]}]}]}}"""

    override fun setUp() {
        super.setUp()
        myFixture.addFileToProject("composer.lock", """{"packages":[{"name":"magento/product-community-edition","version":"2.4.8-p5"}]}""")
        Settings.getInstance(project).apply { pluginEnabled = true; magentoPath = project.basePath }
        ApplicationManager.getApplication().replaceService(MagentoUpdateFeed::class.java, MagentoUpdateFeed({ url ->
            requests.incrementAndGet()
            if (unavailable || (securityUnavailable && url.endsWith("/security"))) throw IOException("HTTP 404")
            if (url.endsWith("/security")) security else catalog
        }, { now }), testRootDisposable)
        project.messageBus.connect(testRootDisposable).subscribe(Notifications.TOPIC, object : Notifications {
            override fun notify(notification: Notification) {
                if (notification.groupId == "Magento Updates") notifications.add(notification)
            }
        })
    }

    override fun tearDown() {
        try { notifications.forEach(Notification::expire) } finally { super.tearDown() }
    }

    private fun check(manual: Boolean = false) {
        val job = project.getService(MagentoUpdateService::class.java).checkForUpdates(manual) ?: return
        PlatformTestUtil.waitWithEventsDispatching("Magento update check", { job.isCompleted }, 15)
    }

    private fun controlRetry(): RetryDispatcher {
        val dispatcher = RetryDispatcher()
        val scope = CoroutineScope(SupervisorJob() + dispatcher)
        Disposer.register(testRootDisposable) { scope.cancel() }
        project.replaceService(MagentoUpdateService::class.java, MagentoUpdateService(project, scope), testRootDisposable)
        return dispatcher
    }

    fun testUnavailableFeedIsSilentAndRecoversAfterTwoMinutes() {
        val dispatcher = controlRetry()
        unavailable = true
        check()
        assertTrue(notifications.isEmpty())
        assertEquals(1, requests.get())
        assertEquals(120000L, dispatcher.pending.single().first)
        unavailable = false
        dispatcher.advance()
        PlatformTestUtil.waitWithEventsDispatching("retried update notification", { notifications.size == 1 }, 15)
        assertEquals(3, requests.get())
        assertTrue(dispatcher.pending.isEmpty())
    }

    fun testRepeated404StopsAfterOneRetryAndManualCheckIsInformational() {
        val dispatcher = controlRetry()
        unavailable = true
        check(manual = true)
        assertEquals(NotificationType.INFORMATION, notifications.single().type)
        assertTrue(notifications.single().content.contains("two minutes"))
        dispatcher.advance()
        PlatformTestUtil.waitWithEventsDispatching("failed retry status", { notifications.size == 2 }, 15)
        assertEquals(2, requests.get())
        assertEquals(NotificationType.INFORMATION, notifications.last().type)
        assertFalse(notifications.last().content.contains("two minutes"))
        assertTrue(dispatcher.pending.isEmpty())
    }

    fun testDisablingChecksCancelsThePendingRetry() {
        val dispatcher = controlRetry()
        unavailable = true
        check()
        project.getService(MagentoUpdateSettings::class.java).state.enabled = false
        dispatcher.advance()
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        assertEquals(1, requests.get())
        assertTrue(notifications.isEmpty())
    }

    fun testMissingSecurityEndpointRetriesWithoutRepeatingTheReleaseNotification() {
        val dispatcher = controlRetry()
        catalog = """{"data":{"2.4.9":{"version":"2.4.9","releaseDate":"2026-10-01"}}}"""
        securityUnavailable = true
        check()
        assertEquals(NotificationType.INFORMATION, notifications.single().type)
        assertEquals(120000L, dispatcher.pending.single().first)
        securityUnavailable = false
        dispatcher.advance()
        PlatformTestUtil.waitWithEventsDispatching("security report recovery", { notifications.size == 2 }, 15)
        assertEquals(4, requests.get())
        assertEquals(NotificationType.ERROR, notifications.last().type)
    }

    fun testMissingSecurityEndpointKeepsAnExistingSecurityWarningVisible() {
        val dispatcher = controlRetry()
        check()
        val securityNotification = notifications.single()
        securityUnavailable = true
        now = now.plusSeconds(3600)
        check()
        assertFalse(securityNotification.isExpired)
        assertEquals(1, notifications.size)
        assertEquals(120000L, dispatcher.pending.single().first)
    }

    fun testNewReleaseAndApplicableSecurityPatchHaveSeparateNotificationsAndSeverities() {
        catalog = """{"data":{"2.4.8-p5":{"version":"2.4.8-p5","releaseDate":"2026-05-12"},"2.4.9":{"version":"2.4.9","releaseDate":"2026-10-01"}}}"""
        check()
        assertEquals(2, notifications.size)
        val release = notifications.single { it.content.contains("Newer release available") }
        val patch = notifications.single { it.content.contains("APSB-TEST") }
        assertEquals(NotificationType.INFORMATION, release.type)
        assertEquals(NotificationType.ERROR, patch.type)
        assertFalse(release.content.contains("APSB-TEST"))
        assertFalse(patch.content.contains("Newer release available"))
        assertFalse(release.isExpired)
        assertFalse(patch.isExpired)
        check()
        assertEquals(2, notifications.size)
    }

    fun testNewReleaseWithUnknownSecurityCoverageIsInformational() {
        catalog = """{"data":{"2.4.9":{"version":"2.4.9","releaseDate":"2026-10-01"}}}"""
        security = """{"data":{"distribution":"magento-community","version":"2.4.8-p5","status":"unknown","vulnerableTo":[]}}"""
        check()
        assertEquals(NotificationType.INFORMATION, notifications.single().type)
    }

    fun testAppliedComposerPatchIsSilentAutomaticallyAndManualCheckExplainsCoverage() {
        myFixture.addFileToProject("vendor/composer/installed.json", """{"packages":[{"name":"magento/framework","extra":{
            "patches_applied":{"Security fix":"https://example.test/PATCH-1.patch"}}}]}""")
        check()
        assertTrue(notifications.isEmpty())
        check(manual = true)
        assertEquals(NotificationType.INFORMATION, notifications.single().type)
        assertTrue(notifications.single().content.contains("covered by installed fixes or local patch configuration"))
    }

    fun testCloudQualityPatchConfigurationSuppressesTheSecurityNotification() {
        myFixture.addFileToProject(".magento.env.yaml", "stage: {build: {QUALITY_PATCHES: [PATCH-1]}}")
        check()
        assertTrue(notifications.isEmpty())
    }

    fun testComposerConfigurationSuppressesSecurityButKeepsIndependentReleaseUpdates() {
        catalog = """{"data":{"2.4.9":{"version":"2.4.9","releaseDate":"2026-10-01"}}}"""
        myFixture.addFileToProject("composer.json", """{"require":{"cweagans/composer-patches":"*"},"extra":{
            "patches":{"magento/framework":{"Security fix":"https://example.test/PATCH-1.patch"}}}}""")
        check()
        assertEquals(NotificationType.INFORMATION, notifications.single().type)
        assertTrue(notifications.single().content.contains("Newer release available"))
    }

    fun testAddingAndRemovingAPatchDismissesAndRestoresTheWarningWithTheSameCachedFeed() {
        check()
        val original = notifications.single()
        myFixture.addFileToProject(".magento.env.yaml", "stage: {build: {QUALITY_PATCHES: [PATCH-1]}}")
        check()
        assertTrue(original.isExpired)
        assertEquals(1, notifications.size)
        assertEquals(2, requests.get())
        myFixture.addFileToProject(".magento.env.yaml", "stage: {build: {QUALITY_PATCHES: []}}")
        check()
        assertEquals(2, requests.get())
        assertEquals(2, notifications.size)
        assertEquals(NotificationType.ERROR, notifications.last().type)
        assertFalse(notifications.last().isExpired)
    }

    fun testPatchInADifferentMagentoRootCannotSuppressThisProjectWarning() {
        myFixture.addFileToProject("other-store/.magento.env.yaml", "stage: {build: {QUALITY_PATCHES: [PATCH-1]}}")
        check()
        assertEquals(NotificationType.ERROR, notifications.single().type)
    }

    fun testLocalFixDismissesAnExistingWarningWhileTheFeedIsOffline() {
        check()
        val original = notifications.single()
        myFixture.addFileToProject(".magento.env.yaml", "stage: {build: {QUALITY_PATCHES: [PATCH-1]}}")
        unavailable = true
        now = now.plusSeconds(3600)
        check()
        assertTrue(original.isExpired)
        assertEquals(1, notifications.size)
    }

    fun testSnoozingAReleaseDoesNotHideARevisedSecurityFindingOrRepeatTheRelease() {
        catalog = """{"data":{"2.4.9":{"version":"2.4.9","releaseDate":"2026-10-01"}}}"""
        check()
        val release = notifications.single { it.type == NotificationType.INFORMATION }
        val patch = notifications.single { it.type == NotificationType.ERROR }
        Notification.fire(release, release.actions.single { it.templateText == "Snooze for 1 day" }, null)
        security = security.replace("PATCH-1", "PATCH-2")
        now = now.plusSeconds(3600)
        check()
        assertEquals(3, notifications.size)
        assertEquals(NotificationType.ERROR, notifications.last().type)
        assertTrue(patch.isExpired)
        project.getService(MagentoUpdateSettings::class.java).state.snoozedUntil.clear()
        check()
        assertEquals(4, notifications.size)
        assertEquals(NotificationType.INFORMATION, notifications.last().type)
    }

    fun testNewReleaseDoesNotRepeatUnchangedSecurityFinding() {
        check()
        catalog = """{"data":{"2.4.9":{"version":"2.4.9","releaseDate":"2026-10-01"}}}"""
        now = now.plusSeconds(3600)
        check()
        assertEquals(2, notifications.size)
        assertEquals(NotificationType.INFORMATION, notifications.last().type)
        assertFalse(notifications.first().isExpired)
    }

    fun testOldFindingsAreSilentAutomaticallyAndManualCheckExplainsTheWindow() {
        catalog = """{"data":{"2.4.9":{"version":"2.4.9","releaseDate":"2026-09-14"}}}"""
        security = security.replace("2026-10-01", "2026-09-14")
        check()
        assertTrue(notifications.isEmpty())
        check(manual = true)
        val notification = notifications.single()
        assertEquals(NotificationType.INFORMATION, notification.type)
        assertTrue(notification.content.contains("past month"))
        assertFalse(notification.content.contains("APSB-TEST"))
        assertFalse(notification.content.contains("No open advisories"))
    }

    fun testAutomaticChecksDeduplicateWhileManualChecksRefresh() {
        check()
        assertEquals(1, notifications.size)
        assertEquals(2, requests.get())
        check()
        assertEquals(1, notifications.size)
        assertEquals(2, requests.get())
        check(manual = true)
        assertEquals(2, notifications.size)
        assertEquals(4, requests.get())
        assertEquals(listOf("View security report", "Snooze for 1 day", "Disable automatic checks"), notifications.last().actions.map { it.templateText })
    }

    fun testDisabledAutomaticChecksDoNotFetchButManualChecksStillWork() {
        project.getService(MagentoUpdateSettings::class.java).state.enabled = false
        check()
        assertEquals(0, requests.get())
        check(manual = true)
        assertEquals(2, requests.get())
        assertEquals(1, notifications.size)
        Settings.getInstance(project).pluginEnabled = false
        check(manual = true)
        assertEquals(2, requests.get())
    }

    fun testSnoozeExpiresWithoutPermanentlyAcknowledgingTheFinding() {
        check()
        val notification = notifications.single()
        Notification.fire(notification, notification.actions.single { it.templateText == "Snooze for 1 day" }, null)
        check()
        assertEquals(1, notifications.size)
        project.getService(MagentoUpdateSettings::class.java).state.snoozedUntil.clear()
        check()
        assertEquals(2, notifications.size)
    }

    fun testSourceCheckoutUsesSharedVersionDetectionWithoutFetchingUpdates() {
        myFixture.addFileToProject("composer.json", """{"name":"magento/magento2ce","version":"2.4.8"}""")
        myFixture.addFileToProject("app/code/Magento/Store/registration.php", "<?php")
        myFixture.addFileToProject("lib/internal/Magento/Framework/registration.php", "<?php")
        check()
        assertEquals(0, requests.get())
        assertTrue(notifications.isEmpty())
        check(manual = true)
        assertEquals(0, requests.get())
        assertTrue(notifications.single().content.contains("source checkout"))
        assertTrue(notifications.single().content.contains("2.4.8"))
    }

    fun testDevelopmentPackageVersionSkipsUpdates() {
        myFixture.addFileToProject("composer.lock", """{"packages":[{"name":"magento/product-community-edition","version":"2.4.x-dev"}]}""")
        check()
        check(manual = true)
        assertEquals(0, requests.get())
        assertTrue(notifications.single().content.contains("development version"))
    }

    fun testSwitchingToDevelopmentVersionDismissesPreviousSecurityWarnings() {
        check()
        val previous = notifications.single()
        myFixture.addFileToProject("composer.lock", """{"packages":[{"name":"magento/product-community-edition","version":"2.4.x-dev"}]}""")
        check()
        assertEquals(2, requests.get())
        assertTrue(previous.isExpired)
    }
}

/** Runs scheduled retry delays under test control without waiting two real minutes. */
@OptIn(InternalCoroutinesApi::class)
private class RetryDispatcher : CoroutineDispatcher(), Delay {
    val pending = CopyOnWriteArrayList<Pair<Long, CancellableContinuation<Unit>>>()
    override fun dispatch(context: CoroutineContext, block: Runnable) = block.run()
    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        pending.add(timeMillis to continuation)
    }
    fun advance() {
        val scheduled = pending.toList()
        pending.clear()
        scheduled.forEach { (_, continuation) -> if (continuation.isActive) continuation.resume(Unit) }
    }
}
