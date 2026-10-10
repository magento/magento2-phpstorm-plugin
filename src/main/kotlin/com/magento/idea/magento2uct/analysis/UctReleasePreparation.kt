/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.analysis

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import org.json.JSONObject
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.UUID
import java.util.concurrent.Future

/** Project-owned preparation jobs; only validated, atomically published releases are ready. */
@Service(Service.Level.PROJECT)
class UctReleasePreparation internal constructor(
    private val project: Project,
    private val cacheRoot: Path,
    private val source: UctReleaseSource
) : Disposable {
    constructor(project: Project) : this(project,
        Path.of(PathManager.getSystemPath(), "magento-compatibility", "v1", project.locationHash), UctReleaseSource())

    internal data class View(
        val runId: String?, val version: String, val state: String,
        val progress: UctPreparationProgress, val index: UctReleaseIndex?, val error: String?
    )

    private class Run(val version: String) {
        val id = "prepare-" + UUID.randomUUID()
        val indicator = EmptyProgressIndicator()
        var state = "running"
        var progress = UctPreparationProgress()
        var index: UctReleaseIndex? = null
        var error: String? = null
        var future: Future<*>? = null
        fun view() = View(id, version, state, progress, index, error)
    }

    private val runs = linkedMapOf<String, Run>()
    private val releases = linkedMapOf<String, UctReleaseIndex>()
    private val cacheErrors = linkedMapOf<String, String>()
    private var loaded = false

    @Synchronized
    fun catalog(): UctIndexCatalog {
        loadCache()
        return UctIndexCatalog.bundled().withReleases(releases)
    }

    @Synchronized
    internal fun warnings(): Map<String, String> { loadCache(); return cacheErrors.toMap() }

    @Synchronized
    internal fun preparedVersions(): List<String> { loadCache(); return releases.keys.sortedWith(UctVersions) }

    @Synchronized
    internal fun start(version: String): View = start(version) { archive, check, progress ->
        source.prepare(project, version, archive, check, progress)
    }

    @Synchronized
    internal fun start(version: String,
        prepare: (Path, () -> Unit, (UctPreparationProgress) -> Unit) -> UctReleaseIndex): View {
        check(!project.isDisposed) { "The project is closed." }
        UctVersions.compare(version, version)
        loadCache()
        releases[version]?.let { return View(null, version, "completed",
            UctPreparationProgress("ready", processedFiles = it.processedFiles, totalFiles = it.processedFiles), it, null) }
        runs.values.firstOrNull { it.version == version && it.state == "running" }?.let { return it.view() }
        require(runs.values.none { it.state == "running" }) { "Release preparation is already running. Follow its results or cancel it first." }
        while (runs.size >= 5) runs.remove(runs.keys.first())
        val run = Run(version)
        runs[run.id] = run
        run.future = AppExecutorUtil.getAppExecutorService().submit {
            var archive: Path? = null
            var pending: Path? = null
            try {
                ProgressManager.getInstance().runProcess(Runnable {
                    val check = { run.indicator.checkCanceled(); if (project.isDisposed) throw ProcessCanceledException() }
                    check()
                    Files.createDirectories(cacheRoot)
                    archive = Files.createTempFile(cacheRoot, "release-", ".zip")
                    val index = prepare(archive!!, check) { progress ->
                        synchronized(this) { if (run.state == "running") run.progress = progress }
                    }
                    require(index.version == version) { "Prepared release does not match requested version." }
                    check()
                    synchronized(this) { if (run.state == "running") run.progress = run.progress.copy(phase = "publishing") }
                    pending = Files.createTempFile(cacheRoot, "index-", ".tmp")
                    Files.writeString(pending, index.json().toString())
                    synchronized(this) {
                        check()
                        if (run.state == "running") {
                            Files.move(pending!!, cacheRoot.resolve("$version.json"),
                                StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
                            releases[version] = index
                            cacheErrors.remove(version)
                            run.index = index
                            run.progress = run.progress.copy(phase = "ready", processedFiles = index.processedFiles, totalFiles = index.processedFiles)
                            run.state = "completed"
                        }
                    }
                }, run.indicator)
            } catch (_: ProcessCanceledException) {
                synchronized(this) { run.state = "cancelled" }
            } catch (exception: Exception) {
                synchronized(this) {
                    if (run.state == "running") { run.state = "failed"; run.error = exception.message ?: exception.javaClass.simpleName }
                }
            } finally {
                archive?.let { Files.deleteIfExists(it) }
                pending?.let { Files.deleteIfExists(it) }
            }
        }
        return run.view()
    }

    @Synchronized
    internal fun owns(runId: String): Boolean = runId in runs

    @Synchronized
    internal fun results(runId: String): View = requireNotNull(runs[runId]) { "Unknown or expired preparation runId." }.view()

    @Synchronized
    internal fun cancel(runId: String): View {
        val run = requireNotNull(runs[runId]) { "Unknown or expired preparation runId." }
        if (run.state == "running") { run.state = "cancelled"; run.indicator.cancel() }
        return run.view()
    }

    @Synchronized
    internal fun activeRunIds(): List<String> = runs.values.filter { it.state == "running" }.map { it.id }

    private fun loadCache() {
        if (loaded) return
        if (Files.isDirectory(cacheRoot)) Files.list(cacheRoot).use { paths ->
            paths.filter { it.fileName.toString().endsWith(".json") }.forEach { path ->
                val version = path.fileName.toString().removeSuffix(".json")
                try {
                    require(Files.size(path) <= 64L * 1024 * 1024) { "Release index cache is too large." }
                    val index = UctReleaseIndex.read(JSONObject(Files.readString(path)))
                    require(index.version == version) { "Release index cache version mismatch." }
                    releases[version] = index
                } catch (exception: Exception) {
                    cacheErrors[version] = "Cached compatibility data rejected: ${exception.message}. Prepare this release again."
                }
            }
        }
        loaded = true
    }

    @Synchronized
    override fun dispose() {
        runs.values.forEach { it.indicator.cancel(); it.future?.cancel(false) }
        runs.clear()
    }
}
