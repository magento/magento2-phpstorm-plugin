/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlFile
import java.io.IOException
import java.nio.file.Files

/** A declaration retains absent attributes so a partial override can inherit them. */
internal data class ConfigDeclaration(
    val subject: String, val name: String, val file: String, val area: String, val module: String?,
    val instance: String?, val disabled: Boolean?, val sortOrder: Int? = null, val line: Int? = null
)

internal data class ConfigDecision(
    val source: ConfigDeclaration, val status: String, val decidedBy: String? = null,
    val instance: String? = source.instance, val sortOrder: Int? = source.sortOrder,
    val instanceFile: String? = source.file.takeIf { source.instance != null }
) {
    fun record(kind: String, area: String, extra: Map<String, Any?>): InspectionRecord =
        InspectionRecord(kind, source.subject, source.file, source.line, area, linkedMapOf(
            "target" to source.subject,
            "name" to source.name,
            "status" to status,
            "decidedBy" to decidedBy,
            "instance" to instance,
            "instanceFile" to instanceFile,
            "declaredDisabled" to source.disabled,
            "sortOrder" to sortOrder
        ) + extra, source.module)
}

internal class MagentoEffectiveConfiguration(private val project: Project) {
    private val projectRoot = MagentoMcpSupport.projectRoot(project)
    private val fs = projectRoot?.fileSystem ?: VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL)
    private val rootPath = MagentoMcpProjectContext.magentoRoot(project)
    private val root = rootPath?.let { fs.findFileByPath(FileUtil.toSystemIndependentName(it.toString())) }
    private val owners = mutableMapOf<String, Pair<String, String?>>()
    val modules by lazy {
        val document = root?.findFileByRelativePath(CONFIG_PATH)
            ?.let { FileDocumentManager.getInstance().getDocument(it) }
        (document?.text ?: readConfigFile())?.let(MagentoModuleLoadOrder::parse)
    }

    // Hosts without a PHP document can still read the literal module list from the configured root.
    private fun readConfigFile(): String? {
        val file = rootPath?.let { MagentoCliEnvironment.resolveProjectPath(it, CONFIG_PATH) } ?: return null
        return try {
            if (!Files.isRegularFile(file)) return null
            Files.newInputStream(file).use { input ->
                input.readNBytes(MAX_CONFIG_BYTES + 1).takeIf { it.size <= MAX_CONFIG_BYTES }
                    ?.decodeToString(throwOnInvalidSequence = true)
            }
        } catch (_: IOException) {
            null
        }
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
        private const val CONFIG_PATH = "app/etc/config.php"
        private const val MAX_CONFIG_BYTES = 1024 * 1024

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

        fun result(rows: List<InspectionRecord>, area: String?, modulesKnown: Boolean): InspectionQueryResult {
            if (area == null) return InspectionQueryResult(rows, "declarations",
                "Declaration-only results. Specify effectiveArea to resolve overrides.")
            val ordered = rows.sortedWith(compareBy(
                { it.fields["status"] != "applies" }, { it.fields["sortOrder"] as? Int ?: 0 }
            ))
            return if (modulesKnown) InspectionQueryResult(ordered, "effective", "Configuration resolved for area $area.")
            else InspectionQueryResult(ordered, "candidates", "Cannot establish module load order from app/etc/config.php.")
        }

        fun renderText(result: InspectionQueryResult, fileArea: String?): String {
            val rows = result.records.filter { fileArea == null || it.area == fileArea }
            val shown = rows.take(200)
            return buildString {
                append(result.message.orEmpty())
                for (row in shown) {
                    val fields = row.fields + mapOf("file" to row.path, "area" to row.area, "module" to (row.module ?: "null"))
                    append("\n").append(fields.toSortedMap().entries.filter { it.value != null }
                        .joinToString(" ") { (key, value) -> "$key=$value" })
                }
                if (shown.size < rows.size) {
                    append("\nShowing ${shown.size} of ${rows.size} records.\n")
                    append(MagentoInspectionResults.TEXT_TRUNCATED_HINT)
                }
            }
        }
    }
}
