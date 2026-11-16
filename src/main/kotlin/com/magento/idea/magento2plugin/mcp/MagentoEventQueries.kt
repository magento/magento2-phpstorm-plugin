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
        var truncated = matchedEvents.size > MagentoMcpSupport.MAX_MATCHES
        for (matchedEvent in matchedEvents.take(MagentoMcpSupport.MAX_MATCHES)) {
            lines += ""
            lines += matchedEvent

            val observers = snapshot.observersByEvent[matchedEvent].orEmpty()
            if (observers.isEmpty()) {
                lines += "observers: none declared in events.xml"
                continue
            }

            truncated = truncated || observers.size > MagentoMcpSupport.MAX_FILE_MATCHES
            for (observer in observers.take(MagentoMcpSupport.MAX_FILE_MATCHES)) {
                lines += "file: ${observer.filePath} observer=${observer.observerName} instance=${observer.observerInstance} disabled=${observer.disabled}"
            }
        }

        if (truncated) {
            lines += ""
            lines += MagentoInspectionResults.TEXT_TRUNCATED_HINT
        }
        return lines.joinToString("\n")
    }

    /** Every observer of every matching event, one record each, in match order. */
    fun observerRecords(project: Project, eventName: String): List<InspectionRecord> {
        val snapshot = MagentoMcpSnapshots.eventSnapshot(project)
        return MagentoMcpSupport.prioritizeMatches(snapshot.observersByEvent.keys, eventName.trim()).flatMap { event ->
            snapshot.observersByEvent[event].orEmpty().map { observer ->
                InspectionRecord(
                    kind = "observer",
                    key = event,
                    path = observer.filePath,
                    line = observer.line,
                    area = MagentoMcpSupport.determineConfigScope(observer.filePath, "events.xml"),
                    fields = linkedMapOf(
                        "observerName" to observer.observerName,
                        "instance" to observer.observerInstance,
                        "disabled" to observer.disabled
                    )
                )
            }
        }
    }
}
