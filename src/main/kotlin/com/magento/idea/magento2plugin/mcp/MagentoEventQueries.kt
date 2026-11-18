/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project

internal object MagentoEventQueries {
    /**
     * Finds event names via index lookup and expands them to observer declarations from matching events.xml files.
     */
    fun findObserversForEvent(project: Project, eventName: String, area: String? = null, json: Boolean = false): String {
        val query = eventName.trim()
        if (query.isEmpty()) {
            return "Provide an event name."
        }

        val snapshot = MagentoMcpSnapshots.eventSnapshot(project)
        val matchedEvents = MagentoMcpSupport.prioritizeMatches(
            snapshot.observersByEvent.keys,
            query
        )

        if (area != null || json) {
            val context = MagentoEffectiveConfiguration(project)
            val records = matchedEvents.flatMap { event ->
                ProgressManager.checkCanceled()
                snapshot.observersByEvent.getValue(event).map { observer ->
                    val (scope, module) = context.location(observer.filePath)
                    ConfigDeclaration(event, observer.observerName, observer.filePath, scope, module,
                        observer.observerInstance.takeUnless { it == "-" }, observer.disabled)
                }.groupBy { it.name }.values.flatMap {
                    MagentoEffectiveConfiguration.resolve(it, area, context.modules, observer = true)
                }.map { it.record() }
            }
            return MagentoEffectiveConfiguration.render(records, area, context.modules != null, json)
        }

        if (matchedEvents.isEmpty()) {
            return "Declaration-only results. No Magento events matched \"$query\"."
        }

        val lines = mutableListOf("Declaration-only results. Specify area to resolve overrides.", "Found ${matchedEvents.size} event match(es) for \"$query\".")
        for (matchedEvent in matchedEvents.take(MagentoMcpSupport.MAX_MATCHES)) {
            lines += ""
            lines += matchedEvent

            val observers = snapshot.observersByEvent[matchedEvent].orEmpty()
            if (observers.isEmpty()) {
                lines += "observers: none declared in events.xml"
                continue
            }

            for (observer in observers.take(MagentoMcpSupport.MAX_FILE_MATCHES)) {
                lines += "file: ${observer.filePath} observer=${observer.observerName} instance=${observer.observerInstance} disabled=${observer.disabled ?: false}"
            }
        }

        return lines.joinToString("\n")
    }
}
