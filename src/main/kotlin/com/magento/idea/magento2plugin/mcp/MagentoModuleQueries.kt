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

internal object MagentoModuleQueries {
    /**
     * Returns module metadata for exact or fuzzy Magento module name matches.
     */
    fun findMagentoModule(project: Project, moduleName: String): String {
        val query = moduleName.trim()
        if (query.isEmpty()) {
            return "Provide a Magento module name, for example Magento_Catalog."
        }

        return runWithReadyIndexes(project) {
            findMagentoModuleWithReadyIndexes(project, query)
        }
    }

    private fun findMagentoModuleWithReadyIndexes(project: Project, query: String): String {
        val moduleIndex = ModuleIndex(project)
        val candidateNames = linkedSetOf<String>()
        candidateNames += moduleIndex.moduleNames
        if (moduleIndex.getModuleDirectoryByModuleName(query) != null) {
            candidateNames += query
        }
        val matches = MagentoMcpSupport.prioritizeMatches(candidateNames, query)
        if (matches.isEmpty()) {
            return "No Magento modules matched \"$query\"."
        }

        val lines = mutableListOf("Found ${matches.size} Magento module match(es) for \"$query\".")
        for (name in matches.take(MagentoMcpSupport.MAX_MATCHES)) {
            val directory = moduleIndex.getModuleDirectoryVirtualFileByModuleName(name)
            val path = directory?.let { MagentoMcpSupport.relativePath(project, it) } ?: "<unresolved>"
            val editable = directory?.let(::isEditableModuleDirectory) ?: false
            val etcPath = directory?.findChild("etc")?.let {
                MagentoMcpSupport.relativePath(project, it)
            } ?: "-"
            val viewPath = directory?.findChild("view")?.let {
                MagentoMcpSupport.relativePath(project, it)
            } ?: "-"

            lines += ""
            lines += name
            lines += "path: $path"
            lines += "editable: ${if (editable) "yes" else "no"}"
            lines += "etc: $etcPath"
            lines += "view: $viewPath"
        }

        if (matches.size > MagentoMcpSupport.MAX_MATCHES) {
            lines += ""
            lines += MagentoInspectionResults.TEXT_TRUNCATED_HINT
        }
        return lines.joinToString("\n")
    }

    /** Every matching module, in match order; the record path is the module directory. */
    fun moduleRecords(project: Project, moduleName: String): List<InspectionRecord> {
        val query = moduleName.trim()
        val moduleIndex = ModuleIndex(project)
        val candidateNames = linkedSetOf<String>()
        candidateNames += moduleIndex.moduleNames
        if (moduleIndex.getModuleDirectoryByModuleName(query) != null) {
            candidateNames += query
        }
        return MagentoMcpSupport.prioritizeMatches(candidateNames, query).map { name ->
            val directory = moduleIndex.getModuleDirectoryVirtualFileByModuleName(name)
            InspectionRecord("module", name, directory?.let { MagentoMcpSupport.relativePath(project, it) }, null, null, linkedMapOf(
                "name" to name,
                "editable" to (directory?.let(::isEditableModuleDirectory) ?: false),
                "etc" to directory?.findChild("etc")?.let { MagentoMcpSupport.relativePath(project, it) },
                "view" to directory?.findChild("view")?.let { MagentoMcpSupport.relativePath(project, it) }
            ))
        }
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
