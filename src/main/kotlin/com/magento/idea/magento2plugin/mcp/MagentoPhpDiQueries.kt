/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlFile
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml
import com.magento.idea.magento2plugin.util.PhpNameUtil

internal object MagentoPhpDiQueries {
    /**
     * Resolves the target class hierarchy and reports plugin methods that match the requested method name.
     */
    fun findPluginsForMethod(project: Project, className: String, methodName: String,
                             effectiveArea: String? = null, fileArea: String? = null): String {
        val targetClassName = MagentoMcpSupport.normalizeFqn(className)
        val targetMethodName = methodName.trim()
        if (targetClassName.isEmpty() || targetMethodName.isEmpty()) {
            return "Provide both a target class name and method name."
        }

        val phpIndex = PhpIndex.getInstance(project)
        val targetClasses = MagentoPhpInspectionSupport.resolveTargetClasses(phpIndex, targetClassName)
        if (targetClasses.isEmpty()) {
            return unresolvedTarget(targetClassName)
        }

        if (effectiveArea != null) {
            return MagentoEffectiveConfiguration.renderText(
                resolveEffective(project, targetClasses, targetMethodName, effectiveArea, includeMethodLocations = false), fileArea
            )
        }
        val allTargetFqns = linkedSetOf(targetClassName)
        targetClasses.forEach { MagentoPhpInspectionSupport.collectClassHierarchy(it, allTargetFqns) }
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

        if (sorted.size > MagentoMcpSupport.MAX_MATCHES) {
            lines += ""
            lines += MagentoInspectionResults.TEXT_TRUNCATED_HINT
        }
        return lines.joinToString("\n")
    }

    fun pluginRecords(project: Project, className: String, methodName: String, effectiveArea: String?): InspectionQueryResult {
        val target = MagentoMcpSupport.normalizeFqn(className)
        val targets = MagentoPhpInspectionSupport.resolveTargetClasses(PhpIndex.getInstance(project), target)
        if (targets.isEmpty()) return InspectionQueryResult(emptyList(), "unresolved_target", unresolvedTarget(target))
        if (effectiveArea != null) return resolveEffective(project, targets, methodName, effectiveArea)

        val hierarchy = linkedSetOf(target)
        targets.forEach { MagentoPhpInspectionSupport.collectClassHierarchy(it, hierarchy) }
        return configurationRecords(project, hierarchy, methodName, area = null)
    }

    private fun unresolvedTarget(target: String) = "Could not resolve target class \"$target\"."

    private fun resolveEffective(
        project: Project, targets: List<PhpClass>, method: String, area: String, includeMethodLocations: Boolean = true
    ): InspectionQueryResult {
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
        return configurationRecords(project, hierarchy, method, area, interfaces, includeMethodLocations)
    }

    private fun configurationRecords(
        project: Project, hierarchy: Set<String>, method: String, area: String?,
        interfaces: Set<String> = emptySet(), includeMethodLocations: Boolean = true
    ): InspectionQueryResult {
        val context = MagentoEffectiveConfiguration(project)
        val modules = if (area != null) context.modules else null
        val snapshot = MagentoMcpSnapshots.diSnapshot(project)
        val declarations = hierarchy.flatMap { type ->
            snapshot.pluginDeclarations[type].orEmpty().map { plugin ->
                val (scope, module) = context.location(plugin.filePath)
                ConfigDeclaration(type, plugin.pluginName, plugin.filePath, scope, module,
                    plugin.pluginType, plugin.disabled, plugin.sortOrder, plugin.line)
            }
        }
        val phpIndex = PhpIndex.getInstance(project)
        val hooks = mutableMapOf<String, PluginHooks?>()
        val lineIndexes = mutableMapOf<PsiFile, MagentoMcpSupport.LineIndex>()
        fun hooksFor(instance: String): PluginHooks? {
            if (instance !in hooks) {
                val classes = phpIndex.getClassesByFQN(instance)
                val matches = classes.takeIf { it.isNotEmpty() }?.flatMap { plugin ->
                    plugin.methods.mapNotNull { hook ->
                        if (!hook.access.isPublic) return@mapNotNull null
                        val kind = PhpNameUtil.interceptorKind(hook.name, method) ?: return@mapNotNull null
                        hook to kind
                    }
                }
                hooks[instance] = matches?.let {
                    val names = it.map { (hook, _) -> hook.name }.distinct().sorted()
                    val locations = if (includeMethodLocations) it.mapNotNull { (hook, kind) ->
                        val file = hook.containingFile ?: return@mapNotNull null
                        val virtualFile = file.virtualFile ?: return@mapNotNull null
                        val lines = lineIndexes.getOrPut(file) { MagentoMcpSupport.LineIndex(file.viewProvider.contents) }
                        PluginMethodLocation(hook.name, kind, MagentoMcpSupport.relativePath(project, virtualFile), lines.lineOf(hook))
                    }.distinct().sortedWith(compareBy({ it.method }, { it.path }, { it.line })) else emptyList()
                    PluginHooks(names, locations.map { location -> location.fields() })
                }
            }
            return hooks[instance]
        }

        val records = declarations.groupBy { it.name }.values.flatMap { group ->
            ProgressManager.checkCanceled()
            val decisions = MagentoEffectiveConfiguration.resolve(group, area, modules, observer = false, interfaces = interfaces)
            // Keep suppression evidence even when the replacing class has no hook for this method.
            if (group.mapNotNull { it.instance }.all { hooksFor(it)?.names?.isEmpty() == true }) return@flatMap emptyList()
            decisions.map { decision ->
                val methods = decision.instance?.let(::hooksFor)
                val status = when {
                    decision.status != "applies" -> decision.status
                    methods == null -> "unresolved_class"
                    methods.names.isEmpty() -> "no_interceptor"
                    else -> decision.status
                }
                val sourceArea = decision.source.area.ifEmpty {
                    MagentoMcpSupport.determineConfigScope(decision.source.file, ModuleDiXml.FILE_NAME)
                }
                val fields = linkedMapOf<String, Any?>("method" to method, "pluginMethods" to methods?.names)
                if (includeMethodLocations) {
                    fields["pluginMethodLocations"] = methods?.locations
                }
                decision.copy(status = status).record("plugin", sourceArea, fields)
            }
        }
        return MagentoEffectiveConfiguration.result(records, area, modules != null)
    }

    private data class PluginHooks(val names: List<String>, val locations: List<Map<String, Any>>)

    private data class PluginMethodLocation(val method: String, val kind: String, val path: String, val line: Int) {
        fun fields(): Map<String, Any> =
            linkedMapOf("method" to method, "pluginKind" to kind, "methodPath" to path, "methodLine" to line)
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
        val lines = MagentoMcpSupport.LineIndex(xmlFile.viewProvider.contents)
        for (typeTag in rootTag.findSubTags(ModuleDiXml.TYPE_TAG)) {
            val typeName = MagentoMcpSupport.presentableFqn(typeTag.getAttributeValue(ModuleDiXml.NAME_ATTR)) ?: continue
            if (typeName != targetFqn) {
                continue
            }

            for (pluginTag in typeTag.findSubTags(ModuleDiXml.PLUGIN_TAG_NAME)) {
                declarations.getOrPut(typeName, ::mutableListOf) +=
                    PluginDeclarationRecord.fromTag(pluginTag, typeName, filePath, scope, lines.lineOf(pluginTag))
            }
        }
    }
}
