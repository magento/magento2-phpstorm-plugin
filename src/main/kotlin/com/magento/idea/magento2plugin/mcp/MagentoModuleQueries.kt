/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.magento.idea.magento2plugin.indexes.ModuleIndex
import com.magento.idea.magento2plugin.stubs.indexes.xml.ModuleXmlIndex

internal object MagentoModuleQueries {
    /**
     * Returns module metadata for exact or fuzzy Magento module name matches.
     */
    fun findMagentoModule(project: Project, moduleName: String, indexKeys: MagentoIndexKeys? = null): String {
        val query = moduleName.trim()
        if (query.isEmpty()) {
            return "Provide a Magento module name, for example Magento_Catalog."
        }

        return runWithReadyIndexes(project) {
            findMagentoModuleWithReadyIndexes(project, query, indexKeys)
        }
    }

    private fun findMagentoModuleWithReadyIndexes(project: Project, query: String, indexKeys: MagentoIndexKeys?): String {
        val matches = moduleRecords(project, query, indexKeys)
        if (matches.isEmpty()) {
            return "No Magento modules matched \"$query\"."
        }

        val names = matches.map { it.key }.distinct()
        val shown = names.take(MagentoMcpSupport.MAX_MATCHES).toSet()
        val lines = mutableListOf("Found ${names.size} Magento module match(es) for \"$query\".")
        for (record in matches.filter { it.key in shown }) {
            lines += ""
            lines += record.key
            lines += "path: ${record.path ?: "<unresolved>"}"
            if (matches.count { it.key == record.key } > 1) {
                lines += "preferred: ${if (record.fields["preferred"] == true) "yes" else "no"}"
            }
            lines += "editable: ${if (record.fields["editable"] == true) "yes" else "no"}"
            lines += "etc: ${record.fields["etc"] ?: "-"}"
            lines += "view: ${record.fields["view"] ?: "-"}"
        }

        if (names.size > MagentoMcpSupport.MAX_MATCHES) {
            lines += ""
            lines += MagentoInspectionResults.TEXT_TRUNCATED_HINT
        }
        return lines.joinToString("\n")
    }

    /**
     * Every indexed copy of every matching module, in match order. The copy that other features use comes first and
     * is marked preferred. The record path is the module directory, `.` at the project root.
     */
    fun moduleRecords(project: Project, moduleName: String, indexKeys: MagentoIndexKeys? = null): List<InspectionRecord> {
        val query = moduleName.trim()
        val moduleIndex = ModuleIndex(project)
        val candidateNames = linkedSetOf<String>()
        candidateNames += if (indexKeys == null) moduleIndex.moduleNames
            else moduleIndex.getModuleNames(indexKeys.keys(ModuleXmlIndex.KEY, project))
        if (indexKeys == null && moduleIndex.getModuleDirectoryByModuleName(query) != null) {
            candidateNames += query
        }
        return MagentoMcpSupport.prioritizeMatches(candidateNames, query).flatMap { name ->
            val preferred = moduleIndex.getModuleDirectoryVirtualFileByModuleName(name)
            val copies = moduleIndex.getIndexedModuleDirectories(name).filter { it != preferred }
                .sortedBy { MagentoMcpSupport.relativePath(project, it) }
            (listOf(preferred) + copies).map { moduleRecord(project, name, it, it == preferred) }
        }
    }

    private fun moduleRecord(project: Project, name: String, directory: VirtualFile?, preferred: Boolean): InspectionRecord {
        val relative = directory?.let { MagentoMcpSupport.relativePath(project, it) }
        return InspectionRecord("module", name, relative?.ifEmpty { "." }, null, null, linkedMapOf(
            "name" to name,
            // A module at the project root is the project being edited.
            "editable" to (relative == "" || directory?.let(::isEditableModuleDirectory) == true),
            "etc" to directory?.findChild("etc")?.let { MagentoMcpSupport.relativePath(project, it) },
            "view" to directory?.findChild("view")?.let { MagentoMcpSupport.relativePath(project, it) },
            "preferred" to preferred
        ), module = name)
    }

    private fun runWithReadyIndexes(project: Project, action: () -> String): String {
        if (DumbService.getInstance(project).isDumb) {
            return MagentoMcpSupport.INDEXES_NOT_READY_MESSAGE
        }
        return try {
            action()
        } catch (_: IndexNotReadyException) {
            // Do not wait for indexing while the caller holds a read action.
            MagentoMcpSupport.INDEXES_NOT_READY_MESSAGE
        }
    }

    private fun isEditableModuleDirectory(directory: VirtualFile): Boolean {
        return directory.path.replace('\\', '/').contains("/app/code/")
    }
}
