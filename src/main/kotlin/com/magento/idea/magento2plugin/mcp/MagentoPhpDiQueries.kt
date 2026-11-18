/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlFile
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml
import com.magento.idea.magento2plugin.util.PhpNameUtil
import org.json.JSONObject

internal object MagentoPhpDiQueries {
    /**
     * Resolves the target class hierarchy and reports plugin methods that match the requested method name.
     */
    fun findPluginsForMethod(project: Project, className: String, methodName: String,
                             effectiveArea: String? = null, json: Boolean = false, fileArea: String? = null): String {
        val targetClassName = MagentoMcpSupport.normalizeFqn(className)
        val targetMethodName = methodName.trim()
        if (targetClassName.isEmpty() || targetMethodName.isEmpty()) {
            return "Provide both a target class name and method name."
        }

        val phpIndex = PhpIndex.getInstance(project)
        val targetClasses = MagentoPhpInspectionSupport.resolveTargetClasses(phpIndex, targetClassName)
        if (targetClasses.isEmpty()) {
            val message = "Could not resolve target class \"$targetClassName\"."
            return if (json) JSONObject().put("mode", "unresolved_target").put("message", message)
                .put("records", emptyList<String>()).toString() else message
        }

        if (effectiveArea != null) {
            return resolveEffective(project, targetClasses, targetMethodName, effectiveArea, json, fileArea)
        }
        val allTargetFqns = linkedSetOf(targetClassName)
        targetClasses.forEach { MagentoPhpInspectionSupport.collectClassHierarchy(it, allTargetFqns) }
        if (json) {
            return renderConfiguration(project, allTargetFqns, targetMethodName, area = null, json = true, fileArea = fileArea)
        }
        val pluginDeclarations = findPluginDeclarations(project, allTargetFqns)
        val pluginCandidates = mutableListOf<MagentoPhpInspectionSupport.PluginMethodMatch>()

        for (targetFqn in allTargetFqns) {
            for (pluginDeclaration in pluginDeclarations[targetFqn].orEmpty()) {
                if (pluginDeclaration.disabled == true || (fileArea != null && pluginDeclaration.scope != fileArea)) {
                    continue
                }
                val pluginType = pluginDeclaration.pluginType ?: continue
                for (pluginClass in phpIndex.getClassesByFQN(pluginType)) {
                    for (method in pluginClass.methods) {
                        val pluginKind = PhpNameUtil.interceptorKind(method.name, targetMethodName) ?: continue
                        if (!method.access.isPublic) {
                            continue
                        }
                        pluginCandidates += MagentoPhpInspectionSupport.PluginMethodMatch(
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
            return "Declaration-only results. No plugins matched ${targetClassName}::${targetMethodName}()."
        }

        val sorted = pluginCandidates
            .distinctBy { "${it.targetFqn}:${it.pluginClass.fqn}:${it.method.name}:${it.scope}:${it.sortOrder}" }
            .sortedWith(
                compareBy<MagentoPhpInspectionSupport.PluginMethodMatch>(
                    { MagentoMcpSupport.pluginKindOrder(it.kind) },
                    { MagentoMcpSupport.configScopeOrder(it.scope) },
                    { it.sortOrder },
                    { it.pluginClass.presentableFQN },
                    { it.method.name }
                )
            )

        val lines = mutableListOf(
            "Declaration-only results. Specify effectiveArea to resolve overrides.",
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

    private fun resolveEffective(
        project: Project, targets: List<PhpClass>, method: String, area: String, json: Boolean, fileArea: String?
    ): String {
        val hierarchy = linkedSetOf<String>()
        val interfaces = linkedSetOf<String>()
        val visitedClasses = mutableSetOf<String>()
        fun addInterfaces(types: List<PhpClass>) {
            val added = types.filter { interfaces.add(it.presentableFQN) }
            added.forEach { hierarchy += it.presentableFQN }
            added.forEach { parent -> addInterfaces(parent.supers.filter { it.isInterface }) }
        }
        fun visit(type: PhpClass) {
            if (!visitedClasses.add(type.fqn)) return
            if (type.isInterface) {
                // Magento's runtime relation reader calls class_exists() before inspecting parents.
                interfaces += type.presentableFQN
            } else {
                type.superClass?.let(::visit)
                addInterfaces(type.implementedInterfaces.toList())
            }
            hierarchy += type.presentableFQN
        }
        targets.forEach(::visit)
        return renderConfiguration(project, hierarchy, method, area, json, fileArea, interfaces)
    }

    private fun renderConfiguration(
        project: Project, hierarchy: Set<String>, method: String, area: String?, json: Boolean,
        fileArea: String?, interfaces: Set<String> = emptySet()
    ): String {
        val context = MagentoEffectiveConfiguration(project)
        val modules = if (area != null) context.modules else null
        val snapshot = MagentoMcpSnapshots.diSnapshot(project)
        val declarations = hierarchy.flatMap { type ->
            snapshot.pluginDeclarations[type].orEmpty().map { plugin ->
                val (scope, module) = context.location(plugin.filePath)
                ConfigDeclaration(type, plugin.pluginName, plugin.filePath, scope, module,
                    plugin.pluginType, plugin.disabled, plugin.sortOrder)
            }
        }.filter { fileArea == null || it.area == fileArea }
        val phpIndex = PhpIndex.getInstance(project)
        val hooks = mutableMapOf<String, List<String>?>()
        fun hooksFor(instance: String): List<String>? {
            if (instance !in hooks) {
                val classes = phpIndex.getClassesByFQN(instance)
                hooks[instance] = classes.takeIf { it.isNotEmpty() }?.flatMap { plugin ->
                    plugin.methods.filter { it.access.isPublic && PhpNameUtil.interceptorKind(it.name, method) != null }
                        .map { it.name }
                }?.distinct()?.sorted()
            }
            return hooks[instance]
        }

        val records = declarations.groupBy { it.name }.values.flatMap { group ->
            ProgressManager.checkCanceled()
            val decisions = MagentoEffectiveConfiguration.resolve(group, area, modules, observer = false, interfaces = interfaces)
            // Keep suppression evidence even when the replacing class has no hook for this method.
            if (group.mapNotNull { it.instance }.all { hooksFor(it)?.isEmpty() == true }) return@flatMap emptyList()
            decisions.map { decision ->
                val methods = decision.instance?.let(::hooksFor)
                val row = decision.record().put("method", method).put("pluginMethods", methods)
                if (decision.status == "applies") {
                    if (methods == null) row.put("status", "unresolved_class")
                    else if (methods.isEmpty()) row.put("status", "no_interceptor")
                }
                row
            }
        }
        return MagentoEffectiveConfiguration.render(records, area, modules != null, json)
    }

    private fun findPluginDeclarations(
        project: Project,
        targetFqns: Set<String>
    ): Map<String, List<PluginDeclarationRecord>> {
        val declarationsByTarget = linkedMapOf<String, MutableList<PluginDeclarationRecord>>()
        val fileBasedIndex = FileBasedIndex.getInstance()
        val psiManager = PsiManager.getInstance(project)
        val searchScope = GlobalSearchScope.allScope(project)

        for (targetFqn in targetFqns) {
            ProgressManager.checkCanceled()

            val virtualFiles = fileBasedIndex.getContainingFiles(
                com.magento.idea.magento2plugin.stubs.indexes.PluginIndex.KEY,
                targetFqn,
                searchScope
            )
            for (virtualFile in virtualFiles) {
                ProgressManager.checkCanceled()

                val xmlFile = psiManager.findFile(virtualFile) as? XmlFile ?: continue
                val filePath = MagentoMcpSupport.relativePath(project, virtualFile)
                val scope = MagentoMcpSupport.determineConfigScope(filePath, ModuleDiXml.FILE_NAME)
                collectPluginDeclarationsForTarget(
                    xmlFile = xmlFile,
                    targetFqn = targetFqn,
                    filePath = filePath,
                    scope = scope,
                    declarations = declarationsByTarget
                )
            }
        }

        return declarationsByTarget.mapValues { (_, records) ->
            records
                .distinct()
                .sortedWith(compareBy<PluginDeclarationRecord>({ it.scope }, { it.sortOrder }, { it.filePath }))
        }
    }

    private fun collectPluginDeclarationsForTarget(
        xmlFile: XmlFile,
        targetFqn: String,
        filePath: String,
        scope: String,
        declarations: MutableMap<String, MutableList<PluginDeclarationRecord>>
    ) {
        val rootTag = xmlFile.rootTag ?: return
        for (typeTag in rootTag.findSubTags(ModuleDiXml.TYPE_TAG)) {
            val typeName = MagentoMcpSupport.presentableFqn(typeTag.getAttributeValue(ModuleDiXml.NAME_ATTR)) ?: continue
            if (typeName != targetFqn) {
                continue
            }

            for (pluginTag in typeTag.findSubTags(ModuleDiXml.PLUGIN_TAG_NAME)) {
                declarations.getOrPut(typeName, ::mutableListOf) +=
                    PluginDeclarationRecord.fromTag(pluginTag, typeName, filePath, scope)
            }
        }
    }
}
