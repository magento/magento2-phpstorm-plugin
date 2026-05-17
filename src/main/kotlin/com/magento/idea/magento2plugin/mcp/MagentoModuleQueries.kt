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

        return lines.joinToString("\n")
    }

    private fun runWithReadyIndexes(project: Project, action: () -> String): String {
        val dumbService = DumbService.getInstance(project)
        repeat(2) {
            if (dumbService.isDumb) {
                dumbService.waitForSmartMode()
            }
            try {
                return action()
            } catch (_: IndexNotReadyException) {
                // Indexing can start between the smart-mode wait and the index query.
            }
        }

        dumbService.waitForSmartMode()
        return action()
    }

    private fun isEditableModuleDirectory(directory: VirtualFile): Boolean {
        return directory.path.replace('\\', '/').contains("/app/code/")
    }
}
