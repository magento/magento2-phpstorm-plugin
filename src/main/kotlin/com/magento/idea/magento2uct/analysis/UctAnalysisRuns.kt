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
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.CancellationException
import java.util.concurrent.Future

/** Project-owned background scans with bounded memory and durable, unpaginated terminal reports. */
@Service(Service.Level.PROJECT)
class UctAnalysisRuns internal constructor(private val project: Project, historyRoot: Path) : Disposable {
    constructor(project: Project) : this(project, Path.of(PathManager.getSystemPath(), "magento-compatibility", "reports", project.locationHash, "analysis"))
    private val history = UctRunHistory(historyRoot)
    private val runs = linkedMapOf<String, Run>()

    data class View(
        val runId: String,
        val state: String,
        val request: UctAnalysisRequest,
        val progress: UctAnalysisProgress,
        val result: UctAnalysisResult?,
        val error: String?
    )

    private class Run(val request: UctAnalysisRequest) {
        val id = UUID.randomUUID().toString()
        val indicator = EmptyProgressIndicator()
        var state = "running"
        var progress = UctAnalysisProgress(0, 0)
        var result: UctAnalysisResult? = null
        var error: String? = null
        var future: Future<*>? = null
        fun view() = View(id, state, request, progress, result, error)
    }

    @Synchronized
    fun start(request: UctAnalysisRequest): View {
        val analysis = UctAnalysisService(project)
        return start(request) { progress ->
            analysis.analyze(request, Runnable { ProgressManager.checkCanceled() }, progress)
        }
    }

    @Synchronized
    internal fun start(
        request: UctAnalysisRequest,
        analyze: (java.util.function.Consumer<UctAnalysisProgress>) -> UctAnalysisResult
    ): View {
        check(!project.isDisposed) { "The project is closed." }
        require(runs.values.none { it.state == "running" }) { "An analysis is already running for this project. Retrieve its results or cancel it first." }
        while (runs.size >= 5) {
            val oldest = runs.values.first()
            if (!history.info(oldest.id).getBoolean("saved")) history.save(oldest.id, UctAnalysisRunReport.write(oldest.view()))
            check(history.info(oldest.id).getBoolean("saved")) { "Cannot archive previous analysis. Retrieve its findings and resolve report storage before starting another scan." }
            runs.remove(oldest.id)
        }
        val run = Run(request)
        runs[run.id] = run
        run.future = AppExecutorUtil.getAppExecutorService().submit {
            try {
                ProgressManager.getInstance().runProcess(Runnable {
                    run.indicator.checkCanceled()
                    val result = analyze(java.util.function.Consumer { progress ->
                        synchronized(this) { if (run.state == "running") run.progress = progress }
                    })
                    synchronized(this) {
                        if (run.state == "running") {
                            run.result = result
                            run.progress = UctAnalysisProgress(result.processedFiles, result.processedFiles)
                            run.state = "completed"
                            history.save(run.id, UctAnalysisRunReport.write(run.view()))
                        }
                    }
                }, run.indicator)
            } catch (_: ProcessCanceledException) {
                synchronized(this) {
                    if (run.state == "running") run.state = "cancelled"
                    history.save(run.id, UctAnalysisRunReport.write(run.view()))
                }
            } catch (_: CancellationException) {
                synchronized(this) {
                    if (run.state == "running") run.state = "cancelled"
                    history.save(run.id, UctAnalysisRunReport.write(run.view()))
                }
            } catch (exception: Exception) {
                synchronized(this) {
                    if (run.state == "running") {
                        run.error = exception.message ?: exception.javaClass.simpleName
                        run.state = "failed"
                        history.save(run.id, UctAnalysisRunReport.write(run.view()))
                    }
                }
            }
        }
        return run.view()
    }

    @Synchronized
    fun results(runId: String): View = runs[runId]?.view() ?: history.read(runId)?.let(UctAnalysisRunReport::read)
        ?: throw IllegalArgumentException("Unknown or expired runId. Reports belong to this project and are retained for 30 days / 100 runs per operation.")

    internal fun reportInfo(runId: String): JSONObject = history.info(runId)

    @Synchronized
    fun cancel(runId: String): View {
        val run = runs[runId] ?: return results(runId)
        if (run.state == "running") {
            run.state = "cancelled"
            run.indicator.cancel()
            history.save(run.id, UctAnalysisRunReport.write(run.view()))
        }
        return run.view()
    }

    @Synchronized
    fun activeRunIds(): List<String> = runs.values.filter { it.state == "running" }.map { it.id }

    @Synchronized
    override fun dispose() {
        runs.values.forEach { run ->
            run.indicator.cancel()
            run.future?.cancel(false)
        }
        runs.clear()
    }
}
