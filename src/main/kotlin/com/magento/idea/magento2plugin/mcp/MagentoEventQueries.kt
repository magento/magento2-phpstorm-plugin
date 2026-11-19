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
    fun findObserversForEvent(
        project: Project, eventName: String, effectiveArea: String? = null, fileArea: String? = null
    ): String {
        val query = eventName.trim()
        if (query.isEmpty()) {
            return "Provide an event name."
        }

        if (effectiveArea != null) {
            return MagentoEffectiveConfiguration.renderText(observerRecords(project, query, effectiveArea), fileArea)
        }

        val snapshot = MagentoMcpSnapshots.eventSnapshot(project)
        val matchedEvents = MagentoMcpSupport.prioritizeMatches(snapshot.observersByEvent.keys, query)
            .filter { event -> fileArea == null || snapshot.observersByEvent.getValue(event).any {
                sourceArea(it.filePath) == fileArea
            } }

        if (matchedEvents.isEmpty()) {
            return "No Magento events matched \"$query\"."
        }

        val lines = mutableListOf("Found ${matchedEvents.size} event match(es) for \"$query\".")
        var truncated = matchedEvents.size > MagentoMcpSupport.MAX_MATCHES
        for (matchedEvent in matchedEvents.take(MagentoMcpSupport.MAX_MATCHES)) {
            lines += ""
            lines += matchedEvent

            val observers = snapshot.observersByEvent[matchedEvent].orEmpty()
                .filter { fileArea == null || sourceArea(it.filePath) == fileArea }
            if (observers.isEmpty()) {
                lines += "observers: none declared in events.xml"
                continue
            }

            truncated = truncated || observers.size > MagentoMcpSupport.MAX_FILE_MATCHES
            for (observer in observers.take(MagentoMcpSupport.MAX_FILE_MATCHES)) {
                lines += "file: ${observer.filePath} observer=${observer.observerName} instance=${observer.observerInstance} disabled=${observer.disabled ?: false}"
            }
        }

        if (truncated) {
            lines += ""
            lines += MagentoInspectionResults.TEXT_TRUNCATED_HINT
        }
        return lines.joinToString("\n")
    }

    /** Every observer of every matching event, before declaration filters and pagination. */
    fun observerRecords(project: Project, eventName: String, effectiveArea: String?): InspectionQueryResult {
        val snapshot = MagentoMcpSnapshots.eventSnapshot(project)
        val context = if (effectiveArea != null) MagentoEffectiveConfiguration(project) else null
        val modules = context?.modules
        val records = MagentoMcpSupport.prioritizeMatches(snapshot.observersByEvent.keys, eventName.trim()).flatMap { event ->
            ProgressManager.checkCanceled()
            val declarations = snapshot.observersByEvent.getValue(event).map { observer ->
                val scope = sourceArea(observer.filePath)
                val (runtimeScope, module) = context?.location(observer.filePath) ?: (scope to null)
                ConfigDeclaration(event, observer.observerName, observer.filePath, runtimeScope, module,
                    observer.observerInstance.takeUnless { it == "-" }, observer.disabled, line = observer.line)
            }
            val decisions = if (effectiveArea == null) declarations.map { ConfigDecision(it, "declaration") }
                else declarations.groupBy { it.name }.values.flatMap {
                    MagentoEffectiveConfiguration.resolve(it, effectiveArea, modules, observer = true)
                }
            decisions.map { decision ->
                decision.record("observer", sourceArea(decision.source.file), linkedMapOf(
                    "observerName" to decision.source.name,
                    "disabled" to (decision.source.disabled ?: false)
                ))
            }
        }
        return MagentoEffectiveConfiguration.result(records, effectiveArea, modules != null)
    }

    private fun sourceArea(path: String): String = MagentoMcpSupport.determineConfigScope(path, "events.xml")
}
