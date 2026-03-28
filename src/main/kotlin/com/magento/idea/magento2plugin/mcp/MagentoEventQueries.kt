/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project

internal object MagentoEventQueries {
    /**
     * Finds event names via index lookup and expands them to observer declarations from matching events.xml files.
     */
    fun findObserversForEvent(project: Project, eventName: String): String {
        val query = eventName.trim()
        if (query.isEmpty()) {
            return "Provide an event name."
        }

        val snapshot = MagentoMcpSnapshots.eventSnapshot(project)
        val matchedEvents = MagentoMcpSupport.prioritizeMatches(
            snapshot.observersByEvent.keys,
            query
        )

        if (matchedEvents.isEmpty()) {
            return "No Magento events matched \"$query\"."
        }

        val lines = mutableListOf("Found ${matchedEvents.size} event match(es) for \"$query\".")
        for (matchedEvent in matchedEvents.take(MagentoMcpSupport.MAX_MATCHES)) {
            lines += ""
            lines += matchedEvent

            val observers = snapshot.observersByEvent[matchedEvent].orEmpty()
            if (observers.isEmpty()) {
                lines += "observers: none declared in events.xml"
                continue
            }

            for (observer in observers.take(MagentoMcpSupport.MAX_FILE_MATCHES)) {
                lines += "file: ${observer.filePath} observer=${observer.observerName} instance=${observer.observerInstance} disabled=${observer.disabled}"
            }
        }

        return lines.joinToString("\n")
    }
}
