/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.maintenance

import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFileManager
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
    MaintenanceStatusBarWidget(), StatusBarWidget.TextPresentation {
    private val changes = MutableStateFlow(Changes(""))
    private var result: JSONObject? = null
    private var observed: Changes? = null
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
        project.messageBus.connect(this).subscribe(VirtualFileManager.VFS_CHANGES,
            MaintenanceChangeListener(::root, ::recordChanges))
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

    private fun recordChanges(batch: MaintenanceFileChanges) {
        changes.update { old ->
            val previous = if (old.root == batch.root) old else Changes(batch.root)
            val files = previous.files.filterNot { it in batch.files } + batch.files
            val enabled = batch.enabledModules?.let {
                previous.enabled.intersect(it) + batch.newlyEnabledModules
            } ?: previous.enabled
            Changes(batch.root, files.takeLast(MAINTENANCE_PATH_LIMIT), enabled, revision = previous.revision + 1,
                limited = previous.limited || batch.limited || files.size > MAINTENANCE_PATH_LIMIT)
        }
    }

    override fun getClickConsumer() = Consumer<MouseEvent> {
        val snapshot = observed ?: return@Consumer
        if (snapshot != changes.value || root() != snapshot.root) return@Consumer
        val rows = result?.optJSONArray("findings")?.map { it as JSONObject }.orEmpty()
        val body = buildString {
            if (snapshot.limited) appendLine("Showing up to $MAINTENANCE_PATH_LIMIT changed paths.")
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
