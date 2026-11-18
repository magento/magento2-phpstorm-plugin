/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.maintenance

import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextArea
import com.intellij.util.Consumer
import com.magento.idea.magento2plugin.mcp.MagentoCliToolQueries
import com.magento.idea.magento2plugin.mcp.MaintenanceDesktop
import com.magento.idea.magento2plugin.mcp.MaintenanceSources
import com.magento.idea.magento2plugin.project.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.awt.event.MouseEvent
import java.io.IOException
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JPanel
import java.awt.BorderLayout

internal class MaintenanceWidgetFactory : StatusBarWidgetFactory {
    override fun getId() = "MagentoMaintenance"
    override fun getDisplayName() = "Magento maintenance"
    override fun createWidget(project: Project, scope: CoroutineScope): StatusBarWidget = MaintenanceWidget(project, scope)
}

private data class Changes(val root: String, val files: List<String> = emptyList(), val enabled: Set<String> = emptySet(),
                           val verify: Boolean = false, val devMode: String = "dev", val revision: Long = 0, val limited: Boolean = false)

private class MaintenanceWidget(private val project: Project, private val scope: CoroutineScope) :
    StatusBarWidget, StatusBarWidget.TextPresentation, BulkFileListener {
    private val changes = MutableStateFlow(Changes(""))
    private var result: JSONObject? = null
    private var observed: Changes? = null
    private var beforeModules: Set<String>? = null
    private var refresh: kotlinx.coroutines.Job? = null

    override fun ID() = "MagentoMaintenance"
    override fun getPresentation() = this
    override fun getAlignment() = 0.5f
    override fun getTooltipText() = "Review Magento maintenance evidence"
    override fun getText(): String {
        val rows = result?.optJSONArray("findings") ?: return ""
        val count = rows.map { it as JSONObject }.filter { it.getString("status") !in setOf("clear", "not_applicable") }
            .distinctBy { it.getString("checkId") to it.getString("item") }.size
        return if (count == 0 || !Settings.isEnabled(project)) "" else "Magento maintenance ($count)"
    }

    override fun install(statusBar: StatusBar) {
        project.messageBus.connect(this).subscribe(VirtualFileManager.VFS_CHANGES, this)
        refresh = scope.launch(Dispatchers.IO) {
            changes.collectLatest { snapshot ->
                delay(200)
                val context = currentCoroutineContext()
                val checks = snapshot.files.mapNotNull(MaintenanceSources::checkId).distinct()
                val value = if (checks.isEmpty() || root() != snapshot.root) null else MaintenanceDesktop.execute(
                    project, if (snapshot.verify) "verify" else "check", checks, snapshot.files, snapshot.enabled.toList(),
                    snapshot.devMode) { context.ensureActive() }
                withContext(Dispatchers.EDT) {
                    result = value
                    observed = snapshot
                    statusBar.updateWidget(ID())
                }
            }
        }
    }

    override fun dispose() { refresh?.cancel() }

    private fun root(): String? {
        if (!Settings.isEnabled(project)) return null
        val base = MagentoCliToolQueries.projectRootPath(project) ?: return null
        return base.resolve(MagentoCliToolQueries.resolveConfiguredMagentoRootRelativePath(project) ?: ".")
            .normalize().toString().replace('\\', '/')
    }

    override fun before(events: List<VFileEvent>) {
        beforeModules = null
        val root = root() ?: return
        val old = events.firstOrNull { it.path == "$root/app/etc/config.php" }?.file
        beforeModules = old?.let(::enabledModules)
    }

    override fun after(events: List<VFileEvent>) {
        val root = root() ?: return
        val paths = events.asSequence().flatMap { event -> when (event) {
            is VFileMoveEvent -> sequenceOf(event.oldPath, event.newPath)
            is VFilePropertyChangeEvent -> sequenceOf(event.oldPath, event.newPath)
            else -> sequenceOf(event.path)
        } }.filter { it.startsWith("$root/") }.map { it.removePrefix("$root/") }
            .filter { MaintenanceSources.checkId(it) != null }.toSet()
        if (paths.isEmpty()) return
        val currentModules = if ("app/etc/config.php" in paths) events.firstOrNull {
            it.file?.path == "$root/app/etc/config.php" && it.file?.isValid == true
        }?.file?.let(::enabledModules) else null
        val additions = beforeModules?.let { before -> currentModules?.minus(before) }.orEmpty()
        changes.update { old ->
            val previous = if (old.root == root) old else Changes(root)
            val files = (previous.files.filterNot { it in paths } + paths)
            val enabled = if ("app/etc/config.php" !in paths) previous.enabled
                else previous.enabled.intersect(currentModules.orEmpty()) + additions
            Changes(root, files.takeLast(512), enabled, revision = previous.revision + 1,
                limited = previous.limited || files.size > 512)
        }
    }

    private fun enabledModules(file: VirtualFile): Set<String>? {
        if (file.isDirectory || file.length > 1024 * 1024) return null
        return try {
            MaintenanceSources.enabledModules(file.contentsToByteArray().toString(Charsets.UTF_8))
        } catch (_: IOException) { null }
    }

    override fun getClickConsumer() = Consumer<MouseEvent> {
        val snapshot = observed ?: return@Consumer
        if (snapshot != changes.value || root() != snapshot.root) return@Consumer
        val rows = result?.optJSONArray("findings")?.map { it as JSONObject }.orEmpty()
        val body = buildString {
            if (snapshot.limited) appendLine("Showing the most recent 512 changed paths.")
            for ((check, group) in rows.groupBy { it.getString("checkId") }) {
                appendLine(check)
                for ((item, findings) in group.groupBy { it.getString("item") }) {
                    appendLine("  $item")
                    for (finding in findings) {
                        appendLine("    ${finding.getString("status")} · ${finding.getString("evidence")}")
                        appendLine("    ${finding.getString("nextAction")}")
                        finding.optJSONObject("suggestedCommand")?.let { cmd ->
                            appendLine("    ${cmd.getString("cwd")}  ${cmd.getJSONArray("argv")}")
                        }
                    }
                }
                appendLine()
            }
        }
        val includeDev = JBCheckBox("Include Composer development dependencies", snapshot.devMode == "dev")
        val dialog = object : DialogWrapper(project) {
            init {
                title = "Magento Maintenance"
                setOKButtonText("Verify runtime")
                init()
            }
            override fun createCenterPanel(): JComponent = JPanel(BorderLayout()).apply {
                add(JBScrollPane(JBTextArea(body, 16, 76).apply {
                    isEditable = false
                    lineWrap = true
                    wrapStyleWord = true
                    caretPosition = 0
                }), BorderLayout.CENTER)
                if (rows.any { it.getString("checkId") == "composer_install" }) add(includeDev, BorderLayout.SOUTH)
            }
            override fun createActions(): Array<Action> = arrayOf(okAction, object : DialogWrapperAction("Mark reviewed") {
                override fun doAction(e: java.awt.event.ActionEvent) {
                    changes.compareAndSet(snapshot, Changes(snapshot.root))
                    close(CANCEL_EXIT_CODE)
                }
            }, cancelAction)
        }
        if (dialog.showAndGet()) changes.compareAndSet(snapshot, snapshot.copy(verify = true,
            devMode = if (includeDev.isSelected) "dev" else "no-dev", revision = snapshot.revision + 1))
    }
}
