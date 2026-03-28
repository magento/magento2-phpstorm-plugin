/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.php.PhpIndex
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml
import com.magento.idea.magento2plugin.stubs.indexes.PluginIndex

internal object MagentoDiQueries {
    /**
     * Scans DI XML files for preferences, type declarations, virtual types, and plugins tied to a class name.
     */
    fun findDiConfigForClass(project: Project, className: String): String {
        val query = MagentoMcpSupport.normalizeFqn(className)
        if (query.isEmpty()) {
            return "Provide a PHP class or virtual type name."
        }

        val preferencesFor = mutableListOf<String>()
        val preferencesType = mutableListOf<String>()
        val typeDeclarations = mutableListOf<String>()
        val virtualTypeDeclarations = mutableListOf<String>()
        val pluginDeclarations = mutableListOf<String>()

        for (xmlFile in MagentoMcpSupport.findXmlFilesByName(project, ModuleDiXml.FILE_NAME)) {
            val rootTag = xmlFile.rootTag ?: continue

            for (preferenceTag in rootTag.findSubTags(ModuleDiXml.PREFERENCE_TAG_NAME)) {
                val preferenceFor = MagentoMcpSupport.presentableFqn(
                    preferenceTag.getAttributeValue(ModuleDiXml.PREFERENCE_ATTR_FOR)
                )
                val preferenceType = MagentoMcpSupport.presentableFqn(
                    preferenceTag.getAttributeValue(ModuleDiXml.TYPE_ATTR)
                )
                val filePath = MagentoMcpSupport.relativePath(project, xmlFile.virtualFile)

                if (preferenceFor == query) {
                    preferencesFor += "$filePath -> preference for=$preferenceFor type=${preferenceType ?: "-"}"
                }
                if (preferenceType == query) {
                    preferencesType += "$filePath -> preference for=${preferenceFor ?: "-"} type=$preferenceType"
                }
            }

            for (typeTag in rootTag.findSubTags(ModuleDiXml.TYPE_TAG)) {
                val typeName = MagentoMcpSupport.presentableFqn(typeTag.getAttributeValue(ModuleDiXml.NAME_ATTR))
                if (typeName != query) {
                    continue
                }

                val filePath = MagentoMcpSupport.relativePath(project, xmlFile.virtualFile)
                val argumentNames = typeTag.findFirstSubTag(ModuleDiXml.ARGUMENTS_TAG)
                    ?.findSubTags(ModuleDiXml.ARGUMENT_TAG)
                    ?.mapNotNull { it.getAttributeValue(ModuleDiXml.NAME_ATTR) }
                    .orEmpty()
                val argumentSuffix = if (argumentNames.isEmpty()) "" else " arguments=${argumentNames.joinToString(", ")}"
                typeDeclarations += "$filePath -> type name=$typeName$argumentSuffix"

                for (pluginTag in typeTag.findSubTags(ModuleDiXml.PLUGIN_TAG_NAME)) {
                    val pluginType = MagentoMcpSupport.presentableFqn(pluginTag.getAttributeValue(ModuleDiXml.TYPE_ATTR))
                    val pluginName = pluginTag.getAttributeValue(ModuleDiXml.NAME_ATTR) ?: "-"
                    val sortOrder = pluginTag.getAttributeValue(ModuleDiXml.SORT_ORDER_ATTR) ?: "0"
                    val disabled = pluginTag.getAttributeValue(ModuleDiXml.DISABLED_ATTR_NAME) ?: "false"
                    pluginDeclarations += "$filePath -> plugin name=$pluginName type=${pluginType ?: "-"} sortOrder=$sortOrder disabled=$disabled"
                }
            }

            for (virtualTypeTag in rootTag.findSubTags(ModuleDiXml.VIRTUAL_TYPE_TAG)) {
                val virtualTypeName = MagentoMcpSupport.presentableFqn(
                    virtualTypeTag.getAttributeValue(ModuleDiXml.NAME_ATTR)
                )
                val virtualTypeTarget = MagentoMcpSupport.presentableFqn(
                    virtualTypeTag.getAttributeValue(ModuleDiXml.TYPE_ATTR)
                )
                if (virtualTypeName != query && virtualTypeTarget != query) {
                    continue
                }

                val filePath = MagentoMcpSupport.relativePath(project, xmlFile.virtualFile)
                virtualTypeDeclarations += "$filePath -> virtualType name=${virtualTypeName ?: "-"} type=${virtualTypeTarget ?: "-"}"
            }
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
        val pluginCandidates = mutableListOf<MagentoMcpSupport.PluginMethodMatch>()

        for (targetFqn in allTargetFqns) {
            val pluginSets = FileBasedIndex.getInstance()
                .getValues(PluginIndex.KEY, targetFqn, GlobalSearchScope.allScope(project))
            for (pluginSet in pluginSets) {
                for (pluginData in pluginSet) {
                    for (pluginClass in phpIndex.getClassesByFQN(pluginData.type)) {
                        for (method in pluginClass.methods) {
                            val pluginKind = MagentoMcpSupport.getPluginKind(method.name, targetMethodName) ?: continue
                            if (!method.access.isPublic) {
                                continue
                            }
                            pluginCandidates += MagentoMcpSupport.PluginMethodMatch(
                                targetFqn,
                                pluginData,
                                pluginClass,
                                method,
                                pluginKind
                            )
                        }
                    }
                }
            }
        }

        if (pluginCandidates.isEmpty()) {
            return "No plugins matched ${targetClassName}::${targetMethodName}()."
        }

        val sorted = pluginCandidates
            .distinctBy { "${it.targetFqn}:${it.pluginClass.fqn}:${it.method.name}:${it.sortOrder}" }
            .sortedWith(
                compareBy<MagentoMcpSupport.PluginMethodMatch>(
                    { MagentoMcpSupport.pluginKindOrder(it.kind) },
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
            lines += "sortOrder: ${match.sortOrder}"
            lines += "file: ${MagentoMcpSupport.relativePath(project, match.method.containingFile.virtualFile)}"
        }

        return lines.joinToString("\n")
    }
}
