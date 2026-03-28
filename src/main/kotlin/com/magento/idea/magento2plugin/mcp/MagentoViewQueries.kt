/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlFile
import com.intellij.util.indexing.FileBasedIndex
import com.magento.idea.magento2plugin.indexes.LayoutIndex
import com.magento.idea.magento2plugin.indexes.UIComponentIndex
import com.magento.idea.magento2plugin.magento.files.ModuleMenuXml
import com.magento.idea.magento2plugin.stubs.indexes.BlockNameIndex
import com.magento.idea.magento2plugin.stubs.indexes.ContainerNameIndex
import com.magento.idea.magento2plugin.stubs.indexes.xml.AclResourceIndex
import com.magento.idea.magento2plugin.stubs.indexes.xml.MenuIndex
import com.magento.idea.magento2plugin.util.magento.GetAclResourcesListUtil
import com.magento.idea.magento2plugin.util.magento.GetAclResourcesTreeUtil

internal object MagentoViewQueries {
    /**
     * Searches across layout handles, block declarations, and container declarations for the supplied name.
     */
    fun findLayoutEntities(project: Project, name: String): String {
        val query = name.trim()
        if (query.isEmpty()) {
            return "Provide a layout handle, block name, or container name."
        }

        val blockMatches = MagentoMcpSupport.prioritizeMatches(
            LayoutIndex.getAllKeys(BlockNameIndex.KEY, project),
            query
        )
        val containerMatches = MagentoMcpSupport.prioritizeMatches(
            LayoutIndex.getAllKeys(ContainerNameIndex.KEY, project),
            query
        )
        val handleMatches = LayoutIndex.getLayoutFiles(project)
            .filter { MagentoMcpSupport.fuzzyMatch(it.virtualFile.nameWithoutExtension, query) }
            .sortedBy { it.virtualFile.nameWithoutExtension }

        val lines = mutableListOf<String>()
        if (handleMatches.isNotEmpty()) {
            lines += "layout handles"
            for (file in handleMatches.take(MagentoMcpSupport.MAX_MATCHES)) {
                lines += "${file.virtualFile.nameWithoutExtension} -> ${MagentoMcpSupport.relativePath(project, file.virtualFile)}"
            }
        }

        if (blockMatches.isNotEmpty()) {
            if (lines.isNotEmpty()) {
                lines += ""
            }
            lines += "blocks"
            for (blockName in blockMatches.take(MagentoMcpSupport.MAX_MATCHES)) {
                for (tag in LayoutIndex.getBlockDeclarations(blockName, project).take(MagentoMcpSupport.MAX_FILE_MATCHES)) {
                    val file = tag.containingFile?.virtualFile ?: continue
                    val blockClass = tag.getAttributeValue("class") ?: "-"
                    val template = tag.getAttributeValue("template") ?: "-"
                    lines += "$blockName -> ${MagentoMcpSupport.relativePath(project, file)} class=$blockClass template=$template"
                }
            }
        }

        if (containerMatches.isNotEmpty()) {
            if (lines.isNotEmpty()) {
                lines += ""
            }
            lines += "containers"
            for (containerName in containerMatches.take(MagentoMcpSupport.MAX_MATCHES)) {
                for (tag in LayoutIndex.getContainerDeclarations(containerName, project).take(MagentoMcpSupport.MAX_FILE_MATCHES)) {
                    val file = tag.containingFile?.virtualFile ?: continue
                    val htmlTag = tag.getAttributeValue("htmlTag") ?: "-"
                    val htmlClass = tag.getAttributeValue("htmlClass") ?: "-"
                    lines += "$containerName -> ${MagentoMcpSupport.relativePath(project, file)} htmlTag=$htmlTag htmlClass=$htmlClass"
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
            .filter { MagentoMcpSupport.fuzzyMatch(it.virtualFile.nameWithoutExtension, query) }
            .sortedBy { it.virtualFile.nameWithoutExtension }

        if (files.isEmpty()) {
            return "No UI components matched \"$query\"."
        }

        val lines = mutableListOf("Found ${files.size} UI component match(es) for \"$query\".")
        for (file in files.take(MagentoMcpSupport.MAX_MATCHES)) {
            lines += ""
            lines += file.virtualFile.nameWithoutExtension
            lines += "file: ${MagentoMcpSupport.relativePath(project, file.virtualFile)}"
            lines += "rootTag: ${file.rootTag?.name ?: "-"}"
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

        val aclMatches = MagentoMcpSupport.prioritizeMatches(GetAclResourcesListUtil.execute(project), query)
        val menuMatches = MagentoMcpSupport.prioritizeMatches(
            FileBasedIndex.getInstance().getAllKeys(MenuIndex.KEY, project),
            query
        )
        val lines = mutableListOf<String>()

        if (aclMatches.isNotEmpty()) {
            lines += "acl resources"
            for (aclResource in aclMatches.take(MagentoMcpSupport.MAX_MATCHES)) {
                val titles = FileBasedIndex.getInstance()
                    .getValues(AclResourceIndex.KEY, aclResource, GlobalSearchScope.allScope(project))
                    .distinct()
                val files = FileBasedIndex.getInstance()
                    .getContainingFiles(AclResourceIndex.KEY, aclResource, GlobalSearchScope.allScope(project))
                val tree = GetAclResourcesTreeUtil.execute(project, aclResource)
                    ?.joinToString(" > ") { "${it.resourceId}(${it.resourceTitle})" }
                    ?: "-"

                lines += "$aclResource -> title=${titles.joinToString(", ").ifEmpty { "-" }}"
                lines += "tree: $tree"
                for (file in files.take(MagentoMcpSupport.MAX_FILE_MATCHES)) {
                    lines += "file: ${MagentoMcpSupport.relativePath(project, file)}"
                }
            }
        }

        if (menuMatches.isNotEmpty()) {
            if (lines.isNotEmpty()) {
                lines += ""
            }
            lines += "menu entries"
            for (menuId in menuMatches.take(MagentoMcpSupport.MAX_MATCHES)) {
                val files = FileBasedIndex.getInstance()
                    .getContainingFiles(MenuIndex.KEY, menuId, GlobalSearchScope.allScope(project))
                for (file in files.take(MagentoMcpSupport.MAX_FILE_MATCHES)) {
                    val xmlFile = PsiManager.getInstance(project).findFile(file) as? XmlFile ?: continue
                    val rootTag = xmlFile.rootTag ?: continue
                    val menuTag = rootTag.findFirstSubTag(ModuleMenuXml.menuTag) ?: continue
                    for (addTag in menuTag.findSubTags(ModuleMenuXml.addTag)) {
                        if (addTag.getAttributeValue(ModuleMenuXml.idTagAttribute) != menuId) {
                            continue
                        }
                        val title = addTag.getAttributeValue(ModuleMenuXml.titleTagAttribute) ?: "-"
                        val resource = addTag.getAttributeValue(ModuleMenuXml.resourceTagAttribute) ?: "-"
                        val action = addTag.getAttributeValue(ModuleMenuXml.actionTagAttribute) ?: "-"
                        val parent = addTag.getAttributeValue(ModuleMenuXml.parentTagAttribute) ?: "-"
                        lines += "$menuId -> title=$title resource=$resource parent=$parent action=$action"
                        lines += "file: ${MagentoMcpSupport.relativePath(project, file)}"
                    }
                }
            }
        }

        if (lines.isEmpty()) {
            return "No ACL resources or menu entries matched \"$query\"."
        }

        return "ACL and menu matches for \"$query\"\n\n${lines.joinToString("\n")}"
    }
}
