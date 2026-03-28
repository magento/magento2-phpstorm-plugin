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
import com.magento.idea.magento2plugin.stubs.indexes.EventNameIndex
import com.magento.idea.magento2plugin.stubs.indexes.EventObserverIndex

internal object MagentoEventQueries {
    /**
     * Finds event names via index lookup and expands them to observer declarations from matching events.xml files.
     */
    fun findObserversForEvent(project: Project, eventName: String): String {
        val query = eventName.trim()
        if (query.isEmpty()) {
            return "Provide an event name."
        }

        val matchedEvents = MagentoMcpSupport.prioritizeMatches(
            FileBasedIndex.getInstance().getAllKeys(EventNameIndex.KEY, project),
            query
        )

        if (matchedEvents.isEmpty()) {
            return "No Magento events matched \"$query\"."
        }

        val lines = mutableListOf("Found ${matchedEvents.size} event match(es) for \"$query\".")
        for (matchedEvent in matchedEvents.take(MagentoMcpSupport.MAX_MATCHES)) {
            lines += ""
            lines += matchedEvent

            val files = FileBasedIndex.getInstance()
                .getContainingFiles(EventObserverIndex.KEY, matchedEvent, GlobalSearchScope.allScope(project))
            if (files.isEmpty()) {
                lines += "observers: none declared in events.xml"
                continue
            }

            for (virtualFile in files.take(MagentoMcpSupport.MAX_FILE_MATCHES)) {
                val xmlFile = PsiManager.getInstance(project).findFile(virtualFile) as? XmlFile ?: continue
                val rootTag = xmlFile.rootTag ?: continue
                for (eventTag in rootTag.findSubTags("event")) {
                    if (eventTag.getAttributeValue("name") != matchedEvent) {
                        continue
                    }
                    for (observerTag in eventTag.findSubTags("observer")) {
                        val observerName = observerTag.getAttributeValue("name") ?: "-"
                        val observerInstance = MagentoMcpSupport.presentableFqn(
                            observerTag.getAttributeValue("instance")
                        ) ?: "-"
                        val disabled = observerTag.getAttributeValue("disabled") ?: "false"
                        lines += "file: ${MagentoMcpSupport.relativePath(project, virtualFile)} observer=$observerName instance=$observerInstance disabled=$disabled"
                    }
                }
            }
        }

        return lines.joinToString("\n")
    }
}
