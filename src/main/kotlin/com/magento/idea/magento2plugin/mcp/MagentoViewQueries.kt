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
import com.magento.idea.magento2plugin.magento.files.ModuleAclXml
import com.magento.idea.magento2plugin.magento.files.ModuleMenuXml
import com.magento.idea.magento2plugin.stubs.indexes.BlockNameIndex
import com.magento.idea.magento2plugin.stubs.indexes.ContainerNameIndex
import org.json.JSONObject

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

        val truncated = handleMatches.size > MagentoMcpSupport.MAX_MATCHES ||
            listOf(blockMatches, containerMatches).any { matches ->
                matches.drop(MagentoMcpSupport.MAX_MATCHES).any { it.second.isNotEmpty() } ||
                    matches.take(MagentoMcpSupport.MAX_MATCHES).any { it.second.size > MagentoMcpSupport.MAX_FILE_MATCHES }
            }
        val hint = if (truncated) "\n\n${MagentoInspectionResults.TEXT_TRUNCATED_HINT}" else ""
        return "Layout entities for \"$query\"\n\n${lines.joinToString("\n")}$hint"
    }

    /**
     * Finds UI component XML files whose component file names match the query.
     */
    fun findUiComponent(project: Project, name: String): String {
        val query = name.trim()
        if (query.isEmpty()) {
            return "Provide a UI component name."
        }

        val files = uiComponents(project, query)

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
        if (files.size > MagentoMcpSupport.MAX_MATCHES) {
            lines += ""
            lines += MagentoInspectionResults.TEXT_TRUNCATED_HINT
        }
        return lines.joinToString("\n")
    }

    private fun uiComponents(project: Project, query: String): List<UiComponentRecord> {
        return UIComponentIndex.getUiComponentFiles(project)
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
    }

    /**
     * Combines ACL resource and admin menu lookups because Magento often shares identifiers between them.
     */
    fun findAclOrMenu(project: Project, identifier: String): String {
        val query = identifier.trim()
        if (query.isEmpty()) {
            return "Provide an ACL resource ID or admin menu ID."
        }

        val snapshot = MagentoMcpSnapshots.aclMenuSnapshot(project)
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

        val truncated = aclMatches.size > MagentoMcpSupport.MAX_MATCHES || menuMatches.size > MagentoMcpSupport.MAX_MATCHES ||
            aclMatches.take(MagentoMcpSupport.MAX_MATCHES).any { snapshot.aclResources[it].orEmpty().size > MagentoMcpSupport.MAX_FILE_MATCHES } ||
            menuMatches.take(MagentoMcpSupport.MAX_MATCHES).any { snapshot.menuEntries[it].orEmpty().size > MagentoMcpSupport.MAX_FILE_MATCHES }
        val hint = if (truncated) "\n\n${MagentoInspectionResults.TEXT_TRUNCATED_HINT}" else ""
        return "ACL and menu matches for \"$query\"\n\n${lines.joinToString("\n")}$hint"
    }

    /** Layout handles, then every block and container declaration, each in a fixed order. */
    fun layoutRecords(project: Project, name: String): List<InspectionRecord> {
        val query = name.trim()
        val lineIndexes = hashMapOf<String, MagentoMcpSupport.LineIndex>()
        fun declaration(kind: String, key: String, tag: XmlTag, fields: Map<String, Any?>): InspectionRecord? {
            val xmlFile = tag.containingFile as? XmlFile ?: return null
            val path = MagentoMcpSupport.relativePath(project, xmlFile.virtualFile ?: return null)
            val line = lineIndexes.getOrPut(path) { MagentoMcpSupport.LineIndex(xmlFile.viewProvider.contents) }.lineOf(tag)
            return InspectionRecord(kind, key, path, line, MagentoMcpSupport.viewArea(path), fields,
                MagentoMcpSupport.themeOverrideModule(path))
        }
        fun declarations(kind: String, key: String, tags: Collection<XmlTag>, fields: (XmlTag) -> Map<String, Any?>) =
            tags.sortedWith(compareBy({ it.containingFile?.virtualFile?.path }, { it.textOffset }))
                .mapNotNull { declaration(kind, key, it, fields(it)) }
        val handles = findLayoutHandles(project, query).map {
            InspectionRecord("layout_handle", it.name, it.filePath, null, MagentoMcpSupport.viewArea(it.filePath),
                linkedMapOf("name" to it.name), MagentoMcpSupport.themeOverrideModule(it.filePath))
        }
        val blocks = matchedBlocks(project, query).flatMap { (blockName, tags) ->
            declarations("block", blockName, tags) {
                linkedMapOf("name" to blockName, "class" to it.getAttributeValue("class"), "template" to it.getAttributeValue("template"))
            }
        }
        val containers = matchedContainers(project, query).flatMap { (containerName, tags) ->
            declarations("container", containerName, tags) {
                linkedMapOf("name" to containerName, "htmlTag" to it.getAttributeValue("htmlTag"), "htmlClass" to it.getAttributeValue("htmlClass"))
            }
        }
        return handles + blocks + containers
    }

    fun uiComponentRecords(project: Project, name: String): List<InspectionRecord> = uiComponents(project, name.trim()).map {
        InspectionRecord("ui_component", it.name, it.filePath, null, MagentoMcpSupport.viewArea(it.filePath),
            linkedMapOf("name" to it.name, "rootTag" to it.rootTag), MagentoMcpSupport.themeOverrideModule(it.filePath))
    }

    /** Every ACL resource declaration, then every menu entry, in match order. */
    fun aclOrMenuRecords(project: Project, identifier: String): List<InspectionRecord> {
        val query = identifier.trim()
        val snapshot = MagentoMcpSnapshots.aclMenuSnapshot(project)
        val acl = MagentoMcpSupport.prioritizeMatches(snapshot.aclResources.keys, query).flatMap { id ->
            snapshot.aclResources[id].orEmpty().map { record ->
                InspectionRecord("acl_resource", id, record.filePath, record.line,
                    MagentoMcpSupport.determineConfigScope(record.filePath, ModuleAclXml.FILE_NAME), linkedMapOf(
                        "id" to id,
                        "title" to record.title,
                        "tree" to record.tree.map { mapOf("id" to it.resourceId, "title" to (it.resourceTitle ?: JSONObject.NULL)) }
                    ))
            }
        }
        val menu = MagentoMcpSupport.prioritizeMatches(snapshot.menuEntries.keys, query).flatMap { id ->
            snapshot.menuEntries[id].orEmpty().map { record ->
                InspectionRecord("menu_entry", id, record.filePath, record.line,
                    MagentoMcpSupport.determineConfigScope(record.filePath, ModuleMenuXml.fileName), linkedMapOf(
                        "id" to id, "title" to record.title, "resource" to record.resource,
                        "parent" to record.parent, "action" to record.action
                    ))
            }
        }
        return acl + menu
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
            .sortedWith(compareBy({ it.name }, { it.filePath }))
            .toList()
    }

    private fun findBlockMatches(project: Project, query: String): List<Pair<String, List<BlockRecord>>> =
        matchedBlocks(project, query).map { (blockName, tags) ->
            blockName to tags
                .mapNotNull { tag -> tag.toBlockRecord(project) }
                .distinct()
                .sortedWith(compareBy<BlockRecord>({ it.filePath }, { it.blockClass ?: "" }, { it.template ?: "" }))
        }

    private fun findContainerMatches(project: Project, query: String): List<Pair<String, List<ContainerRecord>>> =
        matchedContainers(project, query).map { (containerName, tags) ->
            containerName to tags
                .mapNotNull { tag -> tag.toContainerRecord(project) }
                .distinct()
                .sortedWith(compareBy<ContainerRecord>({ it.filePath }, { it.htmlTag ?: "" }, { it.htmlClass ?: "" }))
        }

    /** Block names matching the query with their declaration tags; text and structured replies share them. */
    private fun matchedBlocks(project: Project, query: String): List<Pair<String, Collection<XmlTag>>> =
        MagentoMcpSupport.prioritizeMatches(LayoutIndex.getAllKeys(BlockNameIndex.KEY, project), query)
            .map { it to LayoutIndex.getBlockDeclarations(it, project) }

    private fun matchedContainers(project: Project, query: String): List<Pair<String, Collection<XmlTag>>> =
        MagentoMcpSupport.prioritizeMatches(LayoutIndex.getAllKeys(ContainerNameIndex.KEY, project), query)
            .map { it to LayoutIndex.getContainerDeclarations(it, project) }

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
