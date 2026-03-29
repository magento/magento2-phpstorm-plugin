/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.magento.idea.magento2plugin.actions.generation.data.CLICommandClassData
import com.magento.idea.magento2plugin.actions.generation.data.CLICommandXmlData
import com.magento.idea.magento2plugin.actions.generation.generator.CLICommandClassGenerator
import com.magento.idea.magento2plugin.actions.generation.generator.CLICommandDiXmlGenerator
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml
import com.magento.idea.magento2plugin.magento.packages.Areas
import com.magento.idea.magento2plugin.util.CamelCaseToSnakeCase
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN
import com.magento.idea.magento2plugin.util.RegExUtil
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil
import java.util.Locale

internal object MagentoCliCommandCommands {
    private const val ACTION_NAME = "Magento MCP Create CLI Command"

    private val CLI_COMMAND_NAME_PATTERN = Regex(RegExUtil.CLI_COMMAND_NAME)

    fun createMagentoCliCommand(
        project: Project,
        moduleName: String,
        commandClassFqn: String,
        commandName: String,
        commandDescription: String
    ): String {
        val request = try {
            MagentoMcpCreateSupport.runReadAction {
                resolveRequest(
                    project = project,
                    moduleName = moduleName,
                    commandClassFqn = commandClassFqn,
                    commandName = commandName,
                    commandDescription = commandDescription
                )
            }
        } catch (_: ReadAction.CannotReadException) {
            return "The request was cancelled by a pending write action. Retry."
        } catch (exception: CliCommandValidationException) {
            return exception.message ?: "CLI command creation request is invalid."
        }

        val transaction = MagentoMcpCreateSupport.FileTransaction(project, request.moduleContext.moduleDirectory)
        return try {
            transaction.track(request.commandRelativePath)
            val commandClassFile = CLICommandClassGenerator(
                project,
                CLICommandClassData(
                    request.commandClassName,
                    request.commandDirectory,
                    request.commandName,
                    request.commandDescription,
                    request.commandNamespace,
                    request.moduleName
                )
            ).generate(ACTION_NAME, false) ?: run {
                transaction.rollback()
                return "Magento CLI command class \"${request.commandClassFqn}\" could not be created. Partial changes were rolled back."
            }

            transaction.track(request.diXmlRelativePath)
            val diXmlFile = CLICommandDiXmlGenerator(
                project,
                CLICommandXmlData(
                    request.moduleName,
                    request.commandClassFqn,
                    request.diXmlItemName
                )
            ).generate(ACTION_NAME) ?: run {
                transaction.rollback()
                return "Magento CLI command \"${request.commandName}\" could not be added to di.xml. Partial changes were rolled back."
            }

            buildSuccessMessage(project, request, commandClassFile, diXmlFile)
        } catch (exception: Throwable) {
            transaction.rollback()
            "Magento CLI command \"${request.commandName}\" could not be created. Partial changes were rolled back. Reason: ${exception.message ?: exception::class.java.simpleName}"
        }
    }

    private fun resolveRequest(
        project: Project,
        moduleName: String,
        commandClassFqn: String,
        commandName: String,
        commandDescription: String
    ): CliCommandCreationRequest {
        val moduleContext = MagentoMcpCreateSupport.requireEditableModule(project, moduleName) {
            throw CliCommandValidationException(it)
        }
        val commandClassSpec = MagentoMcpCreateSupport.requireModuleClassSpec(
            moduleContext = moduleContext,
            classFqn = commandClassFqn,
            argumentName = "commandClassFqn",
            exampleClassFqn = "${moduleContext.moduleNamespace}\\Console\\Command\\SampleCommand"
        ) {
            throw CliCommandValidationException(it)
        }

        val normalizedCommandName = commandName.trim()
        if (normalizedCommandName.isEmpty()) {
            throw CliCommandValidationException("Provide a non-empty commandName value.")
        }
        if (!CLI_COMMAND_NAME_PATTERN.matches(normalizedCommandName)) {
            throw CliCommandValidationException(
                "commandName may contain only letters, numbers, underscores, hyphens, and colons."
            )
        }

        val normalizedCommandDescription = commandDescription.trim()
        if (normalizedCommandDescription.isEmpty()) {
            throw CliCommandValidationException("Provide a non-empty commandDescription value.")
        }

        if (GetPhpClassByFQN.getInstance(project).execute(commandClassSpec.classFqn) != null) {
            throw CliCommandValidationException("CLI command class \"${commandClassSpec.classFqn}\" already exists.")
        }

        val diXmlItemName = buildDiXmlItemName(moduleContext.moduleName, commandClassSpec.className)
        val diXmlFile = FileBasedIndexUtil.findModuleConfigFile(
            ModuleDiXml.FILE_NAME,
            Areas.base,
            moduleContext.moduleName,
            project
        ) as? XmlFile
        if (diXmlFile != null) {
            val duplicateReason = findDuplicateCliDeclaration(
                diXmlFile = diXmlFile,
                diXmlItemName = diXmlItemName,
                commandClassFqn = commandClassSpec.classFqn
            )
            if (duplicateReason != null) {
                throw CliCommandValidationException(
                    "$duplicateReason in ${MagentoMcpSupport.relativePath(project, diXmlFile.virtualFile)}."
                )
            }
        }

        return CliCommandCreationRequest(
            moduleContext = moduleContext,
            commandClassFqn = commandClassSpec.classFqn,
            commandClassName = commandClassSpec.className,
            commandDirectory = commandClassSpec.directory,
            commandNamespace = commandClassSpec.namespace,
            commandName = normalizedCommandName,
            commandDescription = normalizedCommandDescription,
            diXmlItemName = diXmlItemName
        )
    }

    private fun buildDiXmlItemName(moduleName: String, className: String): String {
        return moduleName.lowercase(Locale.ROOT) +
            "_" +
            CamelCaseToSnakeCase.getInstance().convert(className)
    }

    private fun findDuplicateCliDeclaration(
        diXmlFile: XmlFile,
        diXmlItemName: String,
        commandClassFqn: String
    ): String? {
        val rootTag = diXmlFile.rootTag ?: return null
        for (typeTag in rootTag.findSubTags(ModuleDiXml.TYPE_TAG)) {
            if (typeTag.getAttributeValue(ModuleDiXml.NAME_ATTR) != ModuleDiXml.CLI_COMMAND_INTERFACE) {
                continue
            }

            val commandsArgumentTag = findCommandsArgumentTag(typeTag) ?: continue
            for (itemTag in commandsArgumentTag.findSubTags(ModuleDiXml.ITEM_TAG)) {
                if (itemTag.getAttributeValue(ModuleDiXml.NAME_ATTR) == diXmlItemName) {
                    return "CLI command DI item \"$diXmlItemName\" is already declared"
                }

                val declaredClass = MagentoMcpSupport.normalizeFqn(itemTag.value.text.trim())
                if (declaredClass == commandClassFqn) {
                    return "CLI command class \"$commandClassFqn\" is already declared"
                }
            }
        }
        return null
    }

    private fun findCommandsArgumentTag(typeTag: XmlTag): XmlTag? {
        for (argumentsTag in typeTag.findSubTags(ModuleDiXml.ARGUMENTS_TAG)) {
            for (argumentTag in argumentsTag.findSubTags(ModuleDiXml.ARGUMENT_TAG)) {
                if (argumentTag.getAttributeValue(ModuleDiXml.NAME_TAG) == ModuleDiXml.CLI_COMMAND_ATTR_COMMANDS) {
                    return argumentTag
                }
            }
        }
        return null
    }

    private fun buildSuccessMessage(
        project: Project,
        request: CliCommandCreationRequest,
        commandClassFile: PsiFile,
        diXmlFile: PsiFile
    ): String {
        val lines = mutableListOf(
            "Created Magento CLI command \"${request.commandName}\".",
            "module: ${request.moduleName}",
            "commandClass: ${request.commandClassFqn}",
            "diXmlItemName: ${request.diXmlItemName}",
            "files:"
        )
        lines += MagentoMcpSupport.relativePath(project, commandClassFile.virtualFile)
        lines += MagentoMcpSupport.relativePath(project, diXmlFile.virtualFile)
        return lines.joinToString("\n")
    }

    private class CliCommandValidationException(message: String) : RuntimeException(message)

    private data class CliCommandCreationRequest(
        val moduleContext: MagentoMcpCreateSupport.EditableModuleContext,
        val commandClassFqn: String,
        val commandClassName: String,
        val commandDirectory: String,
        val commandNamespace: String,
        val commandName: String,
        val commandDescription: String,
        val diXmlItemName: String
    ) {
        val moduleName: String
            get() = moduleContext.moduleName

        val commandRelativePath: String
            get() = MagentoMcpCreateSupport.relativePath(commandDirectory, "$commandClassName.php")

        val diXmlRelativePath: String
            get() = MagentoMcpCreateSupport.configFileRelativePath(Areas.base.toString(), ModuleDiXml.FILE_NAME)
    }
}
