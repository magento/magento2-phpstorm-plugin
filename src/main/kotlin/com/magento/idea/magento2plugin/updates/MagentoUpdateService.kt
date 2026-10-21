/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.updates

import com.intellij.ide.BrowserUtil
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.magento.idea.magento2plugin.project.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.Instant
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicLong

@Service(Service.Level.PROJECT)
class MagentoUpdateService(private val project: Project, private val scope: CoroutineScope) {
    private val mutex = Mutex()
    private val generation = AtomicLong()
    private var started = false
    private var debounce: Job? = null
    private var retryJob: Job? = null
    private val notifications = mutableMapOf<String, Notification>()
    private var notificationContext: String? = null
    @Volatile private var patchPaths: Set<String> = emptySet()

    /** Called on the EDT; requests also work before the startup activity has run. */
    @JvmOverloads
    fun checkForUpdates(manual: Boolean = false): Job? = checkForUpdates(manual, allowRetry = true)

    private fun checkForUpdates(manual: Boolean, allowRetry: Boolean): Job? {
        retryJob?.cancel()
        retryJob = null
        val requestId = generation.incrementAndGet()
        val settings = Settings.getInstance(project)
        val options = project.getService(MagentoUpdateSettings::class.java).state
        if (!settings.pluginEnabled || (!manual && !options.enabled)) return null
        val root = Settings.getMagentoPath(project)?.takeIf { it.isNotBlank() } ?: return null
        val feedUrl = MagentoUpdateSettings.normalizeFeedUrl(options.feedUrl)
        val window = options.checkWindow
        return scope.launch(Dispatchers.IO) {
            mutex.withLock {
                if (requestId != generation.get()) return@withLock
                try {
                    val installed = InstalledMagento.detect(project, root)
                    if (installed.development) {
                        withContext(Dispatchers.EDT) {
                            if (isCurrent(requestId, root, feedUrl, manual, window)) {
                                notifications.values.forEach(Notification::expire)
                                notifications.clear()
                                if (manual) publish("Magento update checks skipped",
                                    "${installed.distribution.displayName} ${installed.version}: development version or source checkout. Release and security checks are skipped.",
                                    NotificationType.INFORMATION).notify(project)
                            }
                        }
                        return@withLock
                    }
                    val localPatches = LocalSecurityPatches.inspect(Path.of(root))
                    patchPaths = localPatches.watchedPaths
                    val result = ApplicationManager.getApplication().getService(MagentoUpdateFeed::class.java)
                        .check(feedUrl, installed, manual, localPatches, window)
                    // A checkout or Composer operation during the request invalidates this result.
                    if (InstalledMagento.detect(project, root) != installed ||
                        LocalSecurityPatches.inspect(Path.of(root)) != localPatches) return@withLock
                    withContext(Dispatchers.EDT) {
                        if (isCurrent(requestId, root, feedUrl, manual, window)) {
                            val retryScheduled = result.retryNeeded && allowRetry
                            if (retryScheduled) scheduleRetry(requestId, root, feedUrl, manual, window)
                            show(result, root, feedUrl, manual, retryScheduled)
                        }
                    }
                } catch (exception: Exception) {
                    MagentoUpdateFeed.rethrowCancellation(exception)
                    LOG.debug("Magento update check failed", exception)
                    withContext(Dispatchers.EDT) {
                        if (isCurrent(requestId, root, feedUrl, manual, window)) {
                            val unavailable = exception is UpdateFeedUnavailableException
                            val retryScheduled = unavailable && allowRetry
                            if (retryScheduled) scheduleRetry(requestId, root, feedUrl, manual, window)
                            if (manual) {
                                val message = when {
                                    retryScheduled -> "Update information is unavailable. Will try again in two minutes."
                                    unavailable -> "Update information is still unavailable. A later check will try again."
                                    else -> "Update information is unavailable. Check composer.lock and the updates feed URL."
                                }
                                publish("Magento update check", message, NotificationType.INFORMATION).notify(project)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun scheduleRetry(id: Long, root: String, feed: String, manual: Boolean, window: UpdateCheckWindow) {
        retryJob?.cancel()
        retryJob = scope.launch {
            delay(2 * 60 * 1000L)
            withContext(Dispatchers.EDT) {
                retryJob = null
                if (isCurrent(id, root, feed, manual, window)) checkForUpdates(manual, allowRetry = false)
            }
        }
    }

    private fun isCurrent(id: Long, root: String, feed: String, manual: Boolean, window: UpdateCheckWindow): Boolean {
        if (project.isDisposed || id != generation.get()) return false
        val options = project.getService(MagentoUpdateSettings::class.java).state
        return Settings.isEnabled(project) && (manual || options.enabled) && Settings.getMagentoPath(project) == root &&
            MagentoUpdateSettings.normalizeFeedUrl(options.feedUrl) == feed && options.checkWindow == window
    }

    private fun show(result: UpdateFeedResult, root: String, feed: String, manual: Boolean, retryScheduled: Boolean) {
        val assessment = result.assessment
        val options = project.getService(MagentoUpdateSettings::class.java).state
        // Keep independent cards so publishing a release cannot replace a security warning.
        val context = "$root|$feed|${assessment.installed}"
        if (notificationContext != context) {
            notifications.values.forEach(Notification::expire)
            notifications.clear()
            notificationContext = context
        }
        val activeKinds = assessment.findings.map { it.kind.name }.toSet()
        if (assessment.securityKnown && UpdateKind.SECURITY.name !in activeKinds) {
            // Reverting a fix must make the same advisory eligible for notification again.
            options.lastNotifiedFingerprints.remove(UpdateKind.SECURITY.name)
        }
        notifications.keys.toList().filter {
            it !in activeKinds && (it != UpdateKind.SECURITY.name || assessment.securityKnown)
        }.forEach { notifications.remove(it)?.expire() }
        // Local fixes can dismiss an old warning even while the public feed is unavailable.
        if (!manual && result.stale) return
        val freshness = (if (result.stale) "Cached result; refresh failed. Last successful check: ${assessment.checkedAt}."
            else "Checked: ${assessment.checkedAt}.") +
            if (retryScheduled) " Some update information is unavailable. Will try again in two minutes." else ""
        if (manual && !assessment.actionable) {
            publish("${assessment.installed.distribution.displayName} updates", "${assessment.message}\n$freshness", NotificationType.INFORMATION).notify(project)
        }
        for (finding in assessment.findings) {
            val kind = finding.kind.name
            val fingerprint = UpdateEvaluator.digest("$root|$feed|${finding.fingerprint}")
            if (!manual && (options.lastNotifiedFingerprints[kind] == fingerprint ||
                    (options.snoozedFingerprints[kind] == fingerprint && (options.snoozedUntil[kind] ?: 0) > Instant.now().epochSecond))) continue
            val (title, type) = when (finding.kind) {
                UpdateKind.RELEASE -> "release updates" to NotificationType.INFORMATION
                UpdateKind.SECURITY -> "security updates" to NotificationType.ERROR
                UpdateKind.SUPPORT -> "end of support" to NotificationType.WARNING
            }
            val identity = assessment.installed
            val current = publish("${identity.distribution.displayName} $title", "${finding.message}\n$freshness", type, kind)
            // Use a fixed public report URL rather than executing URLs or commands supplied by a feed.
            current.addAction(NotificationAction.createSimple(if (finding.kind == UpdateKind.SECURITY) "View security report" else "View release details") {
                val version = if (finding.kind == UpdateKind.RELEASE) assessment.latestVersion else identity.version
                BrowserUtil.browse("https://magento.watch/versions/${identity.distribution.apiId}/$version")
            })
            current.addAction(NotificationAction.createSimpleExpiring("Snooze for 1 day") {
                options.snoozedFingerprints[kind] = fingerprint
                options.snoozedUntil[kind] = Instant.now().plusSeconds(86400).epochSecond
                if (options.lastNotifiedFingerprints[kind] == fingerprint) options.lastNotifiedFingerprints.remove(kind)
            })
            current.addAction(NotificationAction.createSimpleExpiring("Disable automatic checks") { options.enabled = false })
            current.notify(project)
            if (!result.stale) options.lastNotifiedFingerprints[kind] = fingerprint
        }
    }

    private fun publish(title: String, message: String, type: NotificationType, kind: String = "status"): Notification {
        notifications.remove(kind)?.expire()
        return NotificationGroupManager.getInstance().getNotificationGroup("Magento Updates")
            .createNotification(title, StringUtil.escapeXmlEntities(message).replace("\n", "<br/>"), type)
            .also { notifications[kind] = it }
    }

    internal fun start() {
        if (started) return
        started = true
        project.messageBus.connect(scope).subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: List<VFileEvent>) {
                val root = Settings.getMagentoPath(project) ?: return
                if (events.none { event ->
                    val path = event.path.replace('\\', '/')
                    path.startsWith(root.replace('\\', '/').trimEnd('/') + "/") &&
                        (LocalSecurityPatches.isPatchPath(root, path) || path in patchPaths)
                }) return
                generation.incrementAndGet()
                debounce?.cancel()
                debounce = scope.launch {
                    delay(1000)
                    withContext(Dispatchers.EDT) { checkForUpdates() }
                }
            }
        })
        scope.launch {
            while (isActive) {
                withContext(Dispatchers.EDT) { checkForUpdates() }
                delay(60 * 60 * 1000L)
            }
        }
    }

    companion object {
        private val LOG = Logger.getInstance(MagentoUpdateService::class.java)
    }
}

class MagentoUpdateActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        if (!ApplicationManager.getApplication().isUnitTestMode) {
            withContext(Dispatchers.EDT) { project.getService(MagentoUpdateService::class.java).start() }
        }
    }
}
