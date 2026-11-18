/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlFile
import org.json.JSONObject

/** A declaration retains absent attributes so a partial override can inherit them. */
internal data class ConfigDeclaration(
    val subject: String, val name: String, val file: String, val area: String, val module: String?,
    val instance: String?, val disabled: Boolean?, val sortOrder: Int? = null
)

internal data class ConfigDecision(
    val source: ConfigDeclaration, val status: String, val decidedBy: String? = null,
    val instance: String? = source.instance, val sortOrder: Int? = source.sortOrder,
    val instanceFile: String? = source.file.takeIf { source.instance != null }
) {
    fun record(): JSONObject = JSONObject().put("target", source.subject).put("name", source.name)
        .put("file", source.file).put("area", source.area).put("module", source.module ?: JSONObject.NULL)
        .put("instanceFile", instanceFile).put("declaredDisabled", source.disabled)
        .put("status", status).put("decidedBy", decidedBy).put("instance", instance).put("sortOrder", sortOrder)
}

internal class MagentoEffectiveConfiguration(private val project: Project) {
    private val fs = LocalFileSystem.getInstance()
    private val root = MagentoMcpProjectContext.magentoRoot(project)?.let { fs.findFileByNioFile(it) }
    private val projectRoot = MagentoMcpSupport.projectRoot(project)
    private val owners = mutableMapOf<String, Pair<String, String?>>()
    val modules by lazy {
        root?.findFileByRelativePath("app/etc/config.php")
            ?.let { FileDocumentManager.getInstance().getDocument(it)?.text }
            ?.let(MagentoModuleLoadOrder::parse)
    }

    fun location(path: String): Pair<String, String?> = owners.getOrPut(path) {
        val file = projectRoot?.findFileByRelativePath(path) ?: fs.findFileByPath(path)
        val parent = file?.parent ?: return@getOrPut "" to null
        if (root != null && !VfsUtilCore.isAncestor(root, file, false)) return@getOrPut "" to null
        if (file == root?.findFileByRelativePath("app/etc/di.xml")) return@getOrPut "primary" to null
        val etc = if (parent.name == "etc") parent else parent.parent
        val metadata = etc?.takeIf { it.name == "etc" }?.findChild("module.xml")
        val xml = metadata?.let { PsiManager.getInstance(project).findFile(it) as? XmlFile }
        val name = xml?.rootTag?.findFirstSubTag("module")?.getAttributeValue("name")
        (if (etc == parent) "global" else parent.name) to name
    }

    companion object {
        /** Each call merges one name. Subjects arrive in Magento inheritance order, ancestors first. */
        fun resolve(
            rows: List<ConfigDeclaration>, area: String?, modules: Map<String, Boolean>?, observer: Boolean,
            interfaces: Set<String> = emptySet()
        ): List<ConfigDecision> {
            if (area == null) return rows.map { ConfigDecision(it, "declaration") }
            val order = modules?.keys?.withIndex()?.associate { it.value to it.index }.orEmpty()
            val decisions = mutableListOf<ConfigDecision>()
            val loaded = rows.filter { row ->
                val status = when {
                    row.area !in setOf("primary", "global", area) -> "not_loaded"
                    row.area != "primary" && row.module == null -> "not_loaded"
                    modules == null -> "candidate"
                    row.area == "primary" -> null
                    row.module !in modules -> "not_loaded"
                    modules[row.module] == false -> "disabled_module"
                    else -> null
                }
                if (status != null) decisions += ConfigDecision(row, status)
                status == null
            }
            val sorted = mutableListOf<ConfigDeclaration>()
            for ((subject, target) in loaded.groupBy { it.subject }) {
                // PluginListGenerator filters an undeclared plugin out of each parent before inheritance.
                // A standalone interface has no inherited class configuration in Relations/Runtime.
                if (!observer && target.none { it.instance != null } && (subject in interfaces || sorted.isEmpty())) {
                    decisions += target.map { ConfigDecision(it, "not_loaded") }
                    continue
                }
                sorted += target.sortedWith(compareBy(
                    { when (it.area) { "primary" -> 0; "global" -> 1; else -> 2 } }, { order[it.module] ?: -1 }
                ))
            }
            if (sorted.isEmpty()) return decisions
            val instance = sorted.lastOrNull { it.instance != null }
            // Observers convert disabled="false" to an absent key between scopes. In one scope XML
            // attributes merge first. Plugins preserve explicit false when converting a scope.
            val stages = sorted.groupBy { it.subject to it.area }.values
            val disabled = stages.mapNotNull { scope -> scope.lastOrNull { it.disabled != null } }
                .lastOrNull { !observer || it.disabled == true }?.takeIf { it.disabled == true }
            val winner = sorted.last()
            val sortOrder = stages.last().lastOrNull { it.sortOrder != null }?.sortOrder ?: 0
            for ((index, row) in sorted.withIndex()) {
                decisions += when {
                    disabled != null -> ConfigDecision(row, "disabled", disabled.file,
                        instance = row.instance ?: instance?.instance,
                        instanceFile = if (row.instance != null) row.file else instance?.file)
                    row !== winner -> {
                        val replacement = sorted.drop(index + 1).firstOrNull { it.instance != null } ?: winner
                        ConfigDecision(row, "overridden", replacement.file)
                    }
                    instance == null -> ConfigDecision(row, "not_loaded")
                    else -> ConfigDecision(row, "applies", instance = instance.instance, instanceFile = instance.file,
                        sortOrder = if (observer) null else sortOrder)
                }
            }
            return decisions
        }

        fun render(rows: List<JSONObject>, area: String?, modulesKnown: Boolean, json: Boolean): String {
            val mode = when {
                area == null -> "declarations"
                !modulesKnown -> "candidates"
                else -> "effective"
            }
            val note = when (mode) {
                "declarations" -> "Declaration-only results. Specify an area to resolve overrides."
                "candidates" -> "Cannot establish module load order from app/etc/config.php."
                else -> "Configuration resolved for area $area."
            }
            val limit = 200
            val shown = rows.sortedWith(compareBy<JSONObject>(
                { it.optString("status") != "applies" }, { it.optInt("sortOrder", 0) }
            )).take(limit)
            if (json) return JSONObject().put("mode", mode).put("area", area ?: JSONObject.NULL).put("message", note)
                .put("total", rows.size).put("truncated", rows.size > limit).put("records", shown).toString()
            return buildString {
                append(note)
                for (row in shown) append("\n").append(row.keySet().sorted().joinToString(" ") { "$it=${row.get(it)}" })
                if (shown.size < rows.size) append("\nShowing ${shown.size} of ${rows.size} records.")
            }
        }
    }
}
