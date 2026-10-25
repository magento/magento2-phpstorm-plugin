/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project

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

        return "DI configuration for \"$query\"\n\n${lines.joinToString("\n")}"
    }

    /** Retains the existing query API while dispatching PHP semantics through the optional provider. */
    fun findPluginsForMethod(project: Project, className: String, methodName: String): String {
        val provider = MagentoMethodPluginQueryProvider.EP_NAME.extensionList.firstOrNull()
            ?: return MagentoMethodPluginQueryProvider.UNAVAILABLE_MESSAGE
        return provider.findPluginsForMethod(project, className, methodName)
    }
}
