/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project
import com.magento.idea.magento2plugin.indexes.ModuleIndex
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil

internal object MagentoModuleQueries {
    /**
     * Returns module metadata for exact or fuzzy Magento module name matches.
     */
    fun findMagentoModule(project: Project, moduleName: String): String {
        val query = moduleName.trim()
        if (query.isEmpty()) {
            return "Provide a Magento module name, for example Magento_Catalog."
        }

        val moduleIndex = ModuleIndex(project)
        val matches = MagentoMcpSupport.prioritizeMatches(moduleIndex.moduleNames, query)
        if (matches.isEmpty()) {
            return "No Magento modules matched \"$query\"."
        }

        val lines = mutableListOf("Found ${matches.size} Magento module match(es) for \"$query\".")
        for (name in matches.take(MagentoMcpSupport.MAX_MATCHES)) {
            val directory = moduleIndex.getModuleDirectoryByModuleName(name)
            val path = directory?.virtualFile?.let { MagentoMcpSupport.relativePath(project, it) } ?: "<unresolved>"
            val editable = directory?.let(GetMagentoModuleUtil::isDirectoryInEditableModule) ?: false
            val etcPath = directory?.findSubdirectory("etc")?.virtualFile?.let {
                MagentoMcpSupport.relativePath(project, it)
            } ?: "-"
            val viewPath = directory?.findSubdirectory("view")?.virtualFile?.let {
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
}
