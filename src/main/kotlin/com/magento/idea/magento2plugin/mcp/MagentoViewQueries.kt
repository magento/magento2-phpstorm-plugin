/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.magento.idea.magento2plugin.indexes.LayoutIndex
import com.magento.idea.magento2plugin.indexes.UIComponentIndex
import com.magento.idea.magento2plugin.stubs.indexes.BlockNameIndex
import com.magento.idea.magento2plugin.stubs.indexes.ContainerNameIndex

internal object MagentoViewQueries {
    /**
     * Searches across layout handles, block declarations, and container declarations for the supplied name.
     */
    fun findLayoutEntities(project: Project, name: String): String {
        val query = name.trim()
        if (query.isEmpty()) {
            return "Provide a layout handle, block name, or container name."
        }

        val handleMatches = findLayoutHandles(project, query)
        val blockMatches = findBlockMatches(project, query)
        val containerMatches = findContainerMatches(project, query)

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
            for ((blockName, records) in blockMatches.take(MagentoMcpSupport.MAX_MATCHES)) {
                for (record in records.take(MagentoMcpSupport.MAX_FILE_MATCHES)) {
                    lines += "$blockName -> ${record.filePath} class=${record.blockClass ?: "-"} template=${record.template ?: "-"}"
                }
            }
        }

        if (containerMatches.isNotEmpty()) {
            if (lines.isNotEmpty()) {
                lines += ""
            }
            lines += "containers"
            for ((containerName, records) in containerMatches.take(MagentoMcpSupport.MAX_MATCHES)) {
                for (record in records.take(MagentoMcpSupport.MAX_FILE_MATCHES)) {
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

        val files = UIComponentIndex.getUiComponentFiles(project)
            .asSequence()
            .mapNotNull { xmlFile ->
                ProgressManager.checkCanceled()
                val virtualFile = xmlFile.virtualFile ?: return@mapNotNull null
                val componentName = virtualFile.nameWithoutExtension
                if (!MagentoMcpSupport.fuzzyMatch(componentName, query)) {
                    return@mapNotNull null
                }

                UiComponentRecord(
                    name = componentName,
                    filePath = MagentoMcpSupport.relativePath(project, virtualFile),
                    rootTag = xmlFile.rootTag?.name ?: "-"
                )
            }
            .sortedWith(compareBy<UiComponentRecord>({ it.name }, { it.filePath }))
            .toList()

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

    private fun findLayoutHandles(project: Project, query: String): List<LayoutHandleRecord> {
        return FilenameIndex.getAllFilesByExt(project, "xml")
            .asSequence()
            .mapNotNull { virtualFile ->
                ProgressManager.checkCanceled()
                if (!LayoutIndex.isLayoutFile(virtualFile)) {
                    return@mapNotNull null
                }

                val handleName = virtualFile.nameWithoutExtension
                if (!MagentoMcpSupport.fuzzyMatch(handleName, query)) {
                    return@mapNotNull null
                }

                LayoutHandleRecord(
                    name = handleName,
                    filePath = MagentoMcpSupport.relativePath(project, virtualFile)
                )
            }
            .sortedBy(LayoutHandleRecord::name)
            .toList()
    }

    private fun findBlockMatches(project: Project, query: String): List<Pair<String, List<BlockRecord>>> {
        val blockNames = MagentoMcpSupport.prioritizeMatches(
            LayoutIndex.getAllKeys(BlockNameIndex.KEY, project),
            query
        )

        return blockNames.map { blockName ->
            blockName to LayoutIndex.getBlockDeclarations(blockName, project)
                .mapNotNull { tag -> tag.toBlockRecord(project) }
                .distinct()
                .sortedWith(compareBy<BlockRecord>({ it.filePath }, { it.blockClass ?: "" }, { it.template ?: "" }))
        }
    }

    private fun findContainerMatches(project: Project, query: String): List<Pair<String, List<ContainerRecord>>> {
        val containerNames = MagentoMcpSupport.prioritizeMatches(
            LayoutIndex.getAllKeys(ContainerNameIndex.KEY, project),
            query
        )

        return containerNames.map { containerName ->
            containerName to LayoutIndex.getContainerDeclarations(containerName, project)
                .mapNotNull { tag -> tag.toContainerRecord(project) }
                .distinct()
                .sortedWith(compareBy<ContainerRecord>({ it.filePath }, { it.htmlTag ?: "" }, { it.htmlClass ?: "" }))
        }
    }

    private fun XmlTag.toBlockRecord(project: Project): BlockRecord? {
        val filePath = containingXmlFilePath(project) ?: return null
        return BlockRecord(
            filePath = filePath,
            blockClass = getAttributeValue("class"),
            template = getAttributeValue("template")
        )
    }

    private fun XmlTag.toContainerRecord(project: Project): ContainerRecord? {
        val filePath = containingXmlFilePath(project) ?: return null
        return ContainerRecord(
            filePath = filePath,
            htmlTag = getAttributeValue("htmlTag"),
            htmlClass = getAttributeValue("htmlClass")
        )
    }

    private fun XmlTag.containingXmlFilePath(project: Project): String? {
        val xmlFile = containingFile as? XmlFile ?: return null
        val virtualFile = xmlFile.virtualFile ?: return null
        return MagentoMcpSupport.relativePath(project, virtualFile)
    }
}
