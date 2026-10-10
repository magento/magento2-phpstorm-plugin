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
        val progress: UctPreparationProgress, val index: UctReleaseIndex?, val error: String?,
        val summary: JSONObject? = index?.let { JSONObject().put("existenceSymbols", it.existence.size)
            .put("apiSymbols", it.api.size).put("deprecationSymbols", it.deprecation.size).put("archiveSha256", it.archiveSha256) }
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
    private val history = UctRunHistory(cacheRoot.resolve("reports"))
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
        while (runs.size >= 5) {
            val oldest = runs.values.first()
            if (!history.info(oldest.id).getBoolean("saved")) archive(oldest.view())
            check(history.info(oldest.id).getBoolean("saved")) { "Cannot archive previous preparation. Resolve report storage before starting another preparation." }
            runs.remove(oldest.id)
        }
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
                            archive(run.view())
                        }
                    }
                }, run.indicator)
            } catch (_: ProcessCanceledException) {
                synchronized(this) {
                    if (run.state == "running") run.state = "cancelled"
                    archive(run.view())
                }
            } catch (exception: Exception) {
                synchronized(this) {
                    if (run.state == "running") { run.state = "failed"; run.error = exception.message ?: exception.javaClass.simpleName; archive(run.view()) }
                }
            } finally {
                archive?.let { Files.deleteIfExists(it) }
                pending?.let { Files.deleteIfExists(it) }
            }
        }
        return run.view()
    }

    @Synchronized
    internal fun owns(runId: String): Boolean = runId in runs || (runId.startsWith("prepare-") && history.read(runId) != null)

    @Synchronized
    internal fun results(runId: String): View = runs[runId]?.view() ?: history.read(runId)?.let { json ->
        require(json.getString("operation") == "prepare") { "Not a preparation report." }
        View(runId, json.getString("targetVersion"), json.getString("state"),
            UctPreparationProgress(json.getString("phase"), json.getLong("downloadedBytes"),
                if (json.isNull("totalBytes")) null else json.getLong("totalBytes"), json.getInt("processedFiles"), json.getInt("totalFiles")),
            null, if (json.isNull("error")) null else json.getString("error"), json.optJSONObject("summary"))
    } ?: throw IllegalArgumentException("Unknown or expired preparation runId. Reports are retained for 30 days / 100 runs per operation.")

    internal fun reportInfo(runId: String): JSONObject = history.info(runId)

    private fun archive(view: View) {
        history.save(view.runId!!, JSONObject().put("operation", "prepare").put("state", view.state)
            .put("targetVersion", view.version).put("error", view.error ?: JSONObject.NULL)
            .put("phase", view.progress.phase).put("downloadedBytes", view.progress.downloadedBytes)
            .put("totalBytes", view.progress.totalBytes ?: JSONObject.NULL)
            .put("processedFiles", view.progress.processedFiles).put("totalFiles", view.progress.totalFiles)
            .put("summary", view.summary ?: JSONObject.NULL))
    }

    @Synchronized
    internal fun cancel(runId: String): View {
        val run = runs[runId] ?: return results(runId)
        if (run.state == "running") { run.state = "cancelled"; run.indicator.cancel(); archive(run.view()) }
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
