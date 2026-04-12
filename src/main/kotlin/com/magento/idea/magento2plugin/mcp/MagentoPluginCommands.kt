/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlFile
import com.jetbrains.php.lang.PhpLangUtil
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.magento.idea.magento2plugin.actions.generation.data.PluginDiXmlData
import com.magento.idea.magento2plugin.actions.generation.data.PluginFileData
import com.magento.idea.magento2plugin.actions.generation.generator.PluginClassGenerator
import com.magento.idea.magento2plugin.actions.generation.generator.PluginDiXmlGenerator
import com.magento.idea.magento2plugin.inspections.php.util.PhpClassImplementsNoninterceptableInterfaceUtil
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml
import com.magento.idea.magento2plugin.magento.files.Plugin
import com.magento.idea.magento2plugin.magento.packages.Areas
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN
import com.magento.idea.magento2plugin.util.RegExUtil
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil
import com.magento.idea.magento2plugin.util.magento.plugin.IsPluginAllowedForMethodUtil

internal object MagentoPluginCommands {
    private const val ACTION_NAME = "Magento MCP Create Plugin"

    private val IDENTIFIER_WITH_COLON_PATTERN = Regex(RegExUtil.IDENTIFIER_WITH_COLON)

    fun createMagentoPlugin(
        project: Project,
        moduleName: String,
        targetClassName: String,
        targetMethodName: String,
        pluginType: String,
        pluginName: String,
        pluginClassFqn: String,
        area: String,
        sortOrder: Int
    ): String {
        val request = try {
            MagentoMcpCreateSupport.runReadAction {
                resolveRequest(
                    project = project,
                    moduleName = moduleName,
                    targetClassName = targetClassName,
                    targetMethodName = targetMethodName,
                    pluginType = pluginType,
                    pluginName = pluginName,
                    pluginClassFqn = pluginClassFqn,
                    area = area,
                    sortOrder = sortOrder
                )
            }
        } catch (_: ReadAction.CannotReadException) {
            return MagentoMcpReadActionSupport.cancellationMessage()
        } catch (exception: PluginValidationException) {
            return exception.message ?: "Plugin creation request is invalid."
        }

        val transaction = MagentoMcpCreateSupport.FileTransaction(project, request.moduleContext.moduleDirectory)
        return try {
            transaction.track(request.pluginRelativePath)
            val pluginFile = PluginClassGenerator(
                PluginFileData(
                    request.pluginDirectory,
                    request.pluginClassName,
                    request.pluginType,
                    request.moduleName,
                    request.targetClass,
                    request.targetMethod,
                    request.pluginClassFqn,
                    request.pluginNamespace
                ),
                project,
                false
            ).generate(ACTION_NAME, false) ?: run {
                transaction.rollback()
                return buildPluginClassFailureMessage(request)
            }

            transaction.track(request.diXmlRelativePath)
            val diXmlFile = PluginDiXmlGenerator(
                PluginDiXmlData(
                    request.area,
                    request.moduleName,
                    request.targetClass,
                    request.sortOrder.toString(),
                    request.pluginName,
                    request.pluginClassFqn
                ),
                project
            ).generate(ACTION_NAME) ?: run {
                transaction.rollback()
                return buildDiXmlFailureMessage(request)
            }

            buildSuccessMessage(project, request, pluginFile, diXmlFile)
        } catch (exception: Throwable) {
            transaction.rollback()
            "Magento plugin \"${request.pluginClassFqn}\" could not be created. Partial changes were rolled back. Reason: ${exception.message ?: exception::class.java.simpleName}"
        }
    }

    private fun resolveRequest(
        project: Project,
        moduleName: String,
        targetClassName: String,
        targetMethodName: String,
        pluginType: String,
        pluginName: String,
        pluginClassFqn: String,
        area: String,
        sortOrder: Int
    ): PluginCreationRequest {
        if (sortOrder < 0) {
            throw PluginValidationException("sortOrder must be zero or greater.")
        }

        val moduleContext = MagentoMcpCreateSupport.requireEditableModule(project, moduleName) {
            throw PluginValidationException(it)
        }

        val normalizedTargetClassName = MagentoMcpSupport.normalizeFqn(targetClassName)
        if (normalizedTargetClassName.isEmpty()) {
            throw PluginValidationException("Provide a non-empty targetClassName value.")
        }

        val targetClass = GetPhpClassByFQN.getInstance(project).execute(normalizedTargetClassName)
            ?: throw PluginValidationException("Target class \"$normalizedTargetClassName\" was not found.")

        if (targetClass.isFinal) {
            throw PluginValidationException("Target class \"$normalizedTargetClassName\" is final and cannot be intercepted.")
        }
        if (PhpClassImplementsNoninterceptableInterfaceUtil.execute(targetClass)) {
            throw PluginValidationException(
                "Target class \"$normalizedTargetClassName\" implements NoninterceptableInterface and cannot be intercepted."
            )
        }

        val normalizedTargetMethodName = targetMethodName.trim()
        if (normalizedTargetMethodName.isEmpty()) {
            throw PluginValidationException("Provide a non-empty targetMethodName value.")
        }

        val targetMethod = targetClass.findMethodByName(normalizedTargetMethodName)
            ?: throw PluginValidationException(
                "Target method \"$normalizedTargetClassName::$normalizedTargetMethodName()\" was not found."
            )
        if (!IsPluginAllowedForMethodUtil.check(targetMethod)) {
            throw PluginValidationException(
                "Target method \"$normalizedTargetClassName::$normalizedTargetMethodName()\" cannot be intercepted."
            )
        }

        val normalizedPluginType = pluginType.trim().lowercase()
        val pluginTypeEnum = Plugin.getPluginTypeByString(normalizedPluginType)
            ?: throw PluginValidationException("pluginType must be one of: before, around, after.")
        if (pluginTypeEnum == Plugin.PluginType.before && targetMethod.parameters.isEmpty()) {
            throw PluginValidationException(
                "The existing generator cannot create a before plugin for a method without parameters."
            )
        }

        val normalizedPluginName = pluginName.trim()
        if (normalizedPluginName.isEmpty()) {
            throw PluginValidationException("Provide a non-empty pluginName value.")
        }
        if (!IDENTIFIER_WITH_COLON_PATTERN.matches(normalizedPluginName)) {
            throw PluginValidationException("pluginName may contain only letters, numbers, underscores, hyphens, and colons.")
        }

        val normalizedArea = area.trim().lowercase()
        val areaEnum = Areas.getAreaByString(normalizedArea)
            ?: throw PluginValidationException(
                "area must be one of: ${Areas.values().joinToString(", ") { it.toString() }}."
            )

        val pluginClassSpec = MagentoMcpCreateSupport.requireModuleClassSpec(
            moduleContext = moduleContext,
            classFqn = pluginClassFqn,
            argumentName = "pluginClassFqn",
            exampleClassFqn = "${moduleContext.moduleNamespace}\\Plugin\\SamplePlugin"
        ) {
            throw PluginValidationException(it)
        }

        val pluginMethodName = buildPluginMethodName(pluginTypeEnum, targetMethod)
        val existingPluginClass = GetPhpClassByFQN.getInstance(project).execute(pluginClassSpec.classFqn)
        if (existingPluginClass?.findMethodByName(pluginMethodName) != null) {
            throw PluginValidationException(
                "Plugin method \"$pluginMethodName()\" already exists in \"${pluginClassSpec.classFqn}\"."
            )
        }

        val diXmlFile = FileBasedIndexUtil.findModuleConfigFile(
            ModuleDiXml.FILE_NAME,
            areaEnum,
            moduleContext.moduleName,
            project
        ) as? XmlFile
        if (diXmlFile != null && isPluginDeclarationAlreadyPresent(
                diXmlFile,
                targetClass.presentableFQN,
                pluginClassSpec.classFqn
            )
        ) {
            throw PluginValidationException(
                "A plugin declaration for \"${pluginClassSpec.classFqn}\" already exists in ${MagentoMcpSupport.relativePath(project, diXmlFile.virtualFile)}."
            )
        }

        return PluginCreationRequest(
            moduleContext = moduleContext,
            targetClass = targetClass,
            targetMethod = targetMethod,
            targetClassName = targetClass.presentableFQN,
            targetMethodName = targetMethod.name,
            pluginMethodName = pluginMethodName,
            pluginType = normalizedPluginType,
            pluginName = normalizedPluginName,
            pluginClassFqn = pluginClassSpec.classFqn,
            pluginClassName = pluginClassSpec.className,
            pluginDirectory = pluginClassSpec.directory,
            pluginNamespace = pluginClassSpec.namespace,
            area = normalizedArea,
            sortOrder = sortOrder
        )
    }

    private fun buildPluginMethodName(pluginType: Plugin.PluginType, targetMethod: Method): String {
        val targetMethodName = targetMethod.name.replaceFirstChar { it.uppercase() }
        return pluginType.toString() + targetMethodName
    }

    private fun isPluginDeclarationAlreadyPresent(
        diXmlFile: XmlFile,
        targetClassFqn: String,
        pluginClassFqn: String
    ): Boolean {
        val rootTag = diXmlFile.rootTag ?: return false
        for (typeTag in rootTag.findSubTags(ModuleDiXml.TYPE_TAG)) {
            val declaredTarget = typeTag.getAttributeValue(ModuleDiXml.NAME_ATTR)
                ?.let(PhpLangUtil::toPresentableFQN)
            if (declaredTarget != targetClassFqn) {
                continue
            }

            for (pluginTag in typeTag.findSubTags(ModuleDiXml.PLUGIN_TAG_NAME)) {
                val declaredPlugin = pluginTag.getAttributeValue(ModuleDiXml.TYPE_ATTR)
                    ?.let(PhpLangUtil::toPresentableFQN)
                if (declaredPlugin == pluginClassFqn) {
                    return true
                }
            }
        }
        return false
    }

    private fun buildPluginClassFailureMessage(request: PluginCreationRequest): String {
        return "Magento plugin class \"${request.pluginClassFqn}\" could not be created. Partial changes were rolled back."
    }

    private fun buildDiXmlFailureMessage(request: PluginCreationRequest): String {
        return "Magento plugin \"${request.pluginClassFqn}\" could not be added to di.xml. Partial changes were rolled back."
    }

    private fun buildSuccessMessage(
        project: Project,
        request: PluginCreationRequest,
        pluginFile: PsiFile,
        diXmlFile: PsiFile
    ): String {
        val lines = mutableListOf(
            "Created Magento plugin \"${request.pluginName}\".",
            "module: ${request.moduleName}",
            "target: ${request.targetClassName}::${request.targetMethodName}()",
            "pluginType: ${request.pluginType}",
            "pluginClass: ${request.pluginClassFqn}",
            "area: ${request.area}",
            "sortOrder: ${request.sortOrder}",
            "files:"
        )
        lines += MagentoMcpSupport.relativePath(project, pluginFile.virtualFile)
        lines += MagentoMcpSupport.relativePath(project, diXmlFile.virtualFile)
        return lines.joinToString("\n")
    }

    private class PluginValidationException(message: String) : RuntimeException(message)

    private data class PluginCreationRequest(
        val moduleContext: MagentoMcpCreateSupport.EditableModuleContext,
        val targetClass: PhpClass,
        val targetMethod: Method,
        val targetClassName: String,
        val targetMethodName: String,
        val pluginMethodName: String,
        val pluginType: String,
        val pluginName: String,
        val pluginClassFqn: String,
        val pluginClassName: String,
        val pluginDirectory: String,
        val pluginNamespace: String,
        val area: String,
        val sortOrder: Int
    ) {
        val moduleName: String
            get() = moduleContext.moduleName

        val pluginRelativePath: String
            get() = MagentoMcpCreateSupport.relativePath(pluginDirectory, "$pluginClassName.php")

        val diXmlRelativePath: String
            get() = MagentoMcpCreateSupport.configFileRelativePath(area, ModuleDiXml.FILE_NAME)
    }
}
