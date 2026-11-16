/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml

internal object MagentoDiQueries {
    /**
     * Scans DI XML files for preferences, type declarations, virtual types, and plugins tied to a class name.
     */
    fun findDiConfigForClass(project: Project, className: String): String {
        val query = MagentoMcpSupport.normalizeFqn(className)
        if (query.isEmpty()) {
            return "Provide a PHP class or virtual type name."
        }

        val snapshot = MagentoMcpSnapshots.diSnapshot(project)
        val preferencesFor = snapshot.preferencesFor[query].orEmpty().map { record ->
            "${record.filePath} -> preference for=${record.preferenceFor ?: "-"} type=${record.preferenceType ?: "-"}"
        }
        val preferencesType = snapshot.preferencesType[query].orEmpty().map { record ->
            "${record.filePath} -> preference for=${record.preferenceFor ?: "-"} type=${record.preferenceType ?: "-"}"
        }
        val typeDeclarations = snapshot.typeDeclarations[query].orEmpty().map { record ->
            val argumentSuffix = if (record.argumentNames.isEmpty()) {
                ""
            } else {
                " arguments=${record.argumentNames.joinToString(", ")}"
            }
            "${record.filePath} -> type name=${record.typeName}$argumentSuffix"
        }
        val virtualTypeDeclarations = (
            snapshot.virtualTypesByName[query].orEmpty() + snapshot.virtualTypesByTarget[query].orEmpty()
        ).distinct().map { record ->
            "${record.filePath} -> virtualType name=${record.name ?: "-"} type=${record.targetType ?: "-"}"
        }
        val pluginDeclarations = snapshot.pluginDeclarations[query].orEmpty().map { record ->
            "${record.filePath} -> plugin name=${record.pluginName} type=${record.pluginType ?: "-"} " +
                "sortOrder=${record.sortOrder} disabled=${record.disabled} scope=${record.scope}"
        }

        val lines = mutableListOf<String>()
        MagentoMcpSupport.addSection(lines, "preferences for class", preferencesFor)
        MagentoMcpSupport.addSection(lines, "preferences using class as implementation", preferencesType)
        MagentoMcpSupport.addSection(lines, "type declarations", typeDeclarations)
        MagentoMcpSupport.addSection(lines, "virtual type declarations", virtualTypeDeclarations)
        MagentoMcpSupport.addSection(lines, "plugin declarations", pluginDeclarations)

        if (lines.isEmpty()) {
            return "No DI configuration matched \"$query\"."
        }

        val truncated = listOf(preferencesFor, preferencesType, typeDeclarations, virtualTypeDeclarations, pluginDeclarations)
            .any { it.size > MagentoMcpSupport.MAX_MATCHES }
        val hint = if (truncated) "\n\n${MagentoInspectionResults.TEXT_TRUNCATED_HINT}" else ""
        return "DI configuration for \"$query\"\n\n${lines.joinToString("\n")}$hint"
    }

    /** Every di.xml declaration of the class, ordered by file, line and kind. */
    fun diConfigRecords(project: Project, className: String): List<InspectionRecord> {
        val query = MagentoMcpSupport.normalizeFqn(className)
        val snapshot = MagentoMcpSnapshots.diSnapshot(project)
        fun area(path: String) = MagentoMcpSupport.determineConfigScope(path, ModuleDiXml.FILE_NAME)
        fun preference(role: String, record: PreferenceRecord) = InspectionRecord("preference", query, record.filePath,
            record.line, area(record.filePath), linkedMapOf("role" to role, "for" to record.preferenceFor, "type" to record.preferenceType))
        return snapshot.preferencesFor[query].orEmpty().map { preference("for", it) } +
            snapshot.preferencesType[query].orEmpty().map { preference("type", it) } +
            snapshot.typeDeclarations[query].orEmpty().map {
                InspectionRecord("type", query, it.filePath, it.line, area(it.filePath),
                    linkedMapOf("name" to it.typeName, "arguments" to it.argumentNames))
            } +
            (snapshot.virtualTypesByName[query].orEmpty() + snapshot.virtualTypesByTarget[query].orEmpty()).distinct().map {
                InspectionRecord("virtualType", query, it.filePath, it.line, area(it.filePath),
                    linkedMapOf("name" to it.name, "type" to it.targetType))
            } +
            snapshot.pluginDeclarations[query].orEmpty().map {
                InspectionRecord("plugin", query, it.filePath, it.line, it.scope, linkedMapOf("name" to it.pluginName,
                    "type" to it.pluginType, "sortOrder" to it.sortOrder, "disabled" to it.disabled))
            }.let { records ->
                records.sortedWith(compareBy({ it.path }, { it.line }, { it.kind }, { it.fields["role"]?.toString() }))
            }
    }

    /** Retains the existing query API while dispatching PHP semantics through the optional provider. */
    fun findPluginsForMethod(project: Project, className: String, methodName: String): String {
        val provider = MagentoMethodPluginQueryProvider.EP_NAME.extensionList.firstOrNull()
            ?: return MagentoMethodPluginQueryProvider.UNAVAILABLE_MESSAGE
        return provider.findPluginsForMethod(project, className, methodName)
    }
}
