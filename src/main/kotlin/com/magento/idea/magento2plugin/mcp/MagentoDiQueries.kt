/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project
import com.jetbrains.php.PhpIndex

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

    /**
     * Resolves the target class hierarchy and reports plugin methods that match the requested method name.
     */
    fun findPluginsForMethod(project: Project, className: String, methodName: String): String {
        val targetClassName = MagentoMcpSupport.normalizeFqn(className)
        val targetMethodName = methodName.trim()
        if (targetClassName.isEmpty() || targetMethodName.isEmpty()) {
            return "Provide both a target class name and method name."
        }

        val phpIndex = PhpIndex.getInstance(project)
        val targetClasses = MagentoMcpSupport.resolveTargetClasses(phpIndex, targetClassName)
        if (targetClasses.isEmpty()) {
            return "Could not resolve target class \"$targetClassName\"."
        }

        val allTargetFqns = linkedSetOf(targetClassName)
        targetClasses.forEach { MagentoMcpSupport.collectClassHierarchy(it, allTargetFqns) }
        val diSnapshot = MagentoMcpSnapshots.diSnapshot(project)
        val pluginCandidates = mutableListOf<MagentoMcpSupport.PluginMethodMatch>()

        for (targetFqn in allTargetFqns) {
            for (pluginDeclaration in diSnapshot.pluginDeclarations[targetFqn].orEmpty()) {
                if (pluginDeclaration.disabled) {
                    continue
                }
                val pluginType = pluginDeclaration.pluginType ?: continue
                for (pluginClass in phpIndex.getClassesByFQN(pluginType)) {
                    for (method in pluginClass.methods) {
                        val pluginKind = MagentoMcpSupport.getPluginKind(method.name, targetMethodName) ?: continue
                        if (!method.access.isPublic) {
                            continue
                        }
                        pluginCandidates += MagentoMcpSupport.PluginMethodMatch(
                            targetFqn,
                            pluginDeclaration,
                            pluginClass,
                            method,
                            pluginKind
                        )
                    }
                }
            }
        }

        if (pluginCandidates.isEmpty()) {
            return "No plugins matched ${targetClassName}::${targetMethodName}()."
        }

        val sorted = pluginCandidates
            .distinctBy { "${it.targetFqn}:${it.pluginClass.fqn}:${it.method.name}:${it.scope}:${it.sortOrder}" }
            .sortedWith(
                compareBy<MagentoMcpSupport.PluginMethodMatch>(
                    { MagentoMcpSupport.pluginKindOrder(it.kind) },
                    { MagentoMcpSupport.configScopeOrder(it.scope) },
                    { it.sortOrder },
                    { it.pluginClass.presentableFQN },
                    { it.method.name }
                )
            )

        val lines = mutableListOf(
            "Found ${sorted.size} plugin method match(es) for ${targetClassName}::${targetMethodName}()."
        )
        for (match in sorted.take(MagentoMcpSupport.MAX_MATCHES)) {
            lines += ""
            lines += "${match.kind} plugin"
            lines += "target: ${match.targetFqn}::${targetMethodName}()"
            lines += "pluginClass: ${match.pluginClass.presentableFQN}"
            lines += "pluginMethod: ${match.method.name}()"
            lines += "scope: ${match.scope}"
            lines += "sortOrder: ${match.sortOrder}"
            lines += "file: ${MagentoMcpSupport.relativePath(project, match.method.containingFile.virtualFile)}"
        }

        return lines.joinToString("\n")
    }
}
