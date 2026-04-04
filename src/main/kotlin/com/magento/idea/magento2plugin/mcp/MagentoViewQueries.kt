/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project

internal object MagentoViewQueries {
    /**
     * Searches across layout handles, block declarations, and container declarations for the supplied name.
     */
    fun findLayoutEntities(project: Project, name: String): String {
        val query = name.trim()
        if (query.isEmpty()) {
            return "Provide a layout handle, block name, or container name."
        }

        val snapshot = MagentoMcpSnapshots.viewSnapshot(project)
        val blockMatches = MagentoMcpSupport.prioritizeMatches(
            snapshot.blocks.keys,
            query
        )
        val containerMatches = MagentoMcpSupport.prioritizeMatches(
            snapshot.containers.keys,
            query
        )
        val handleMatches = snapshot.handles
            .filter { MagentoMcpSupport.fuzzyMatch(it.name, query) }
            .sortedBy(LayoutHandleRecord::name)

        val lines = mutableListOf<String>()
        if (handleMatches.isNotEmpty()) {
            lines += "layout handles"
            for (handle in handleMatches.take(MagentoMcpSupport.MAX_MATCHES)) {
                lines += "${handle.name} -> ${handle.filePath}"
            }
        }

        if (blockMatches.isNotEmpty()) {
            if (lines.isNotEmpty()) {
                lines += ""
            }
            lines += "blocks"
            for (blockName in blockMatches.take(MagentoMcpSupport.MAX_MATCHES)) {
                for (record in snapshot.blocks[blockName].orEmpty().take(MagentoMcpSupport.MAX_FILE_MATCHES)) {
                    lines += "$blockName -> ${record.filePath} class=${record.blockClass ?: "-"} template=${record.template ?: "-"}"
                }
            }
        }

        if (containerMatches.isNotEmpty()) {
            if (lines.isNotEmpty()) {
                lines += ""
            }
            lines += "containers"
            for (containerName in containerMatches.take(MagentoMcpSupport.MAX_MATCHES)) {
                for (record in snapshot.containers[containerName].orEmpty().take(MagentoMcpSupport.MAX_FILE_MATCHES)) {
                    lines += "$containerName -> ${record.filePath} htmlTag=${record.htmlTag ?: "-"} htmlClass=${record.htmlClass ?: "-"}"
                }
            }
        }

        if (lines.isEmpty()) {
            return "No layout handles, blocks, or containers matched \"$query\"."
        }

        return "Layout entities for \"$query\"\n\n${lines.joinToString("\n")}"
    }

    /**
     * Finds UI component XML files whose component file names match the query.
     */
    fun findUiComponent(project: Project, name: String): String {
        val query = name.trim()
        if (query.isEmpty()) {
            return "Provide a UI component name."
        }

        val files = MagentoMcpSnapshots.viewSnapshot(project).uiComponents
            .filter { MagentoMcpSupport.fuzzyMatch(it.name, query) }
            .sortedBy(UiComponentRecord::name)

        if (files.isEmpty()) {
            return "No UI components matched \"$query\"."
        }

        val lines = mutableListOf("Found ${files.size} UI component match(es) for \"$query\".")
        for (file in files.take(MagentoMcpSupport.MAX_MATCHES)) {
            lines += ""
            lines += file.name
            lines += "file: ${file.filePath}"
            lines += "rootTag: ${file.rootTag}"
        }
        return lines.joinToString("\n")
    }

    /**
     * Combines ACL resource and admin menu lookups because Magento often shares identifiers between them.
     */
    fun findAclOrMenu(project: Project, identifier: String): String {
        val query = identifier.trim()
        if (query.isEmpty()) {
            return "Provide an ACL resource ID or admin menu ID."
        }

        val snapshot = MagentoMcpSnapshots.viewSnapshot(project)
        val aclMatches = MagentoMcpSupport.prioritizeMatches(snapshot.aclResources.keys, query)
        val menuMatches = MagentoMcpSupport.prioritizeMatches(
            snapshot.menuEntries.keys,
            query
        )
        val lines = mutableListOf<String>()

        if (aclMatches.isNotEmpty()) {
            lines += "acl resources"
            for (aclResource in aclMatches.take(MagentoMcpSupport.MAX_MATCHES)) {
                val records = snapshot.aclResources[aclResource].orEmpty()
                val titles = records.mapNotNull(AclResourceRecord::title).distinct()
                val tree = records.firstOrNull()
                    ?.tree
                    ?.joinToString(" > ") { "${it.resourceId}(${it.resourceTitle ?: "-"})" }
                    ?: "-"

                lines += "$aclResource -> title=${titles.joinToString(", ").ifEmpty { "-" }}"
                lines += "tree: $tree"
                for (record in records.take(MagentoMcpSupport.MAX_FILE_MATCHES)) {
                    lines += "file: ${record.filePath}"
                }
            }
        }

        if (menuMatches.isNotEmpty()) {
            if (lines.isNotEmpty()) {
                lines += ""
            }
            lines += "menu entries"
            for (menuId in menuMatches.take(MagentoMcpSupport.MAX_MATCHES)) {
                for (record in snapshot.menuEntries[menuId].orEmpty().take(MagentoMcpSupport.MAX_FILE_MATCHES)) {
                    lines += "$menuId -> title=${record.title} resource=${record.resource} parent=${record.parent} action=${record.action}"
                    lines += "file: ${record.filePath}"
                }
            }
        }

        if (lines.isEmpty()) {
            return "No ACL resources or menu entries matched \"$query\"."
        }

        return "ACL and menu matches for \"$query\"\n\n${lines.joinToString("\n")}"
    }
}
