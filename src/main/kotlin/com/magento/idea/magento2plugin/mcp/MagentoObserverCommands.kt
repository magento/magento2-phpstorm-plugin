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
import com.magento.idea.magento2plugin.actions.generation.data.ObserverEventsXmlData
import com.magento.idea.magento2plugin.actions.generation.data.ObserverFileData
import com.magento.idea.magento2plugin.actions.generation.generator.ObserverClassGenerator
import com.magento.idea.magento2plugin.actions.generation.generator.ObserverEventsXmlGenerator
import com.magento.idea.magento2plugin.magento.files.ModuleEventsXml
import com.magento.idea.magento2plugin.magento.packages.Areas
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN
import com.magento.idea.magento2plugin.util.RegExUtil
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil

internal object MagentoObserverCommands {
    private const val ACTION_NAME = "Magento MCP Create Observer"

    private val IDENTIFIER_WITH_COLON_PATTERN = Regex(RegExUtil.IDENTIFIER_WITH_COLON)

    fun createMagentoObserver(
        project: Project,
        moduleName: String,
        eventName: String,
        observerName: String,
        observerClassFqn: String,
        area: String
    ): String {
        val request = try {
            MagentoMcpCreateSupport.runReadAction {
                resolveRequest(
                    project = project,
                    moduleName = moduleName,
                    eventName = eventName,
                    observerName = observerName,
                    observerClassFqn = observerClassFqn,
                    area = area
                )
            }
        } catch (_: ReadAction.CannotReadException) {
            return "The request was cancelled by a pending write action. Retry."
        } catch (exception: ObserverValidationException) {
            return exception.message ?: "Observer creation request is invalid."
        }

        val transaction = MagentoMcpCreateSupport.FileTransaction(project, request.moduleContext.moduleDirectory)
        return try {
            transaction.track(request.observerRelativePath)
            val observerFile = ObserverClassGenerator(
                ObserverFileData(
                    request.observerDirectory,
                    request.observerClassName,
                    request.moduleName,
                    request.eventName,
                    request.observerClassFqn,
                    request.observerNamespace
                ),
                project,
                false
            ).generate(ACTION_NAME, false) ?: run {
                transaction.rollback()
                return buildObserverClassFailureMessage(request)
            }

            transaction.track(request.eventsXmlRelativePath)
            val eventsXmlFile = ObserverEventsXmlGenerator(
                ObserverEventsXmlData(
                    request.area,
                    request.moduleName,
                    request.eventName,
                    request.observerName,
                    request.observerClassFqn
                ),
                project
            ).generate(ACTION_NAME) ?: run {
                transaction.rollback()
                return buildEventsXmlFailureMessage(request)
            }

            buildSuccessMessage(project, request, observerFile, eventsXmlFile)
        } catch (exception: Throwable) {
            transaction.rollback()
            "Magento observer \"${request.observerClassFqn}\" could not be created. Partial changes were rolled back. Reason: ${exception.message ?: exception::class.java.simpleName}"
        }
    }

    private fun resolveRequest(
        project: Project,
        moduleName: String,
        eventName: String,
        observerName: String,
        observerClassFqn: String,
        area: String
    ): ObserverCreationRequest {
        val moduleContext = MagentoMcpCreateSupport.requireEditableModule(project, moduleName) {
            throw ObserverValidationException(it)
        }

        val normalizedEventName = eventName.trim()
        if (normalizedEventName.isEmpty()) {
            throw ObserverValidationException("Provide a non-empty eventName value.")
        }
        if (normalizedEventName.any(Char::isWhitespace)) {
            throw ObserverValidationException("eventName must not contain whitespace.")
        }

        val normalizedObserverName = observerName.trim()
        if (normalizedObserverName.isEmpty()) {
            throw ObserverValidationException("Provide a non-empty observerName value.")
        }
        if (!IDENTIFIER_WITH_COLON_PATTERN.matches(normalizedObserverName)) {
            throw ObserverValidationException("observerName may contain only letters, numbers, underscores, hyphens, and colons.")
        }

        val normalizedArea = area.trim().lowercase()
        val areaEnum = Areas.getAreaByString(normalizedArea)
            ?: throw ObserverValidationException(
                "area must be one of: ${Areas.values().joinToString(", ") { it.toString() }}."
            )

        val observerClassSpec = MagentoMcpCreateSupport.requireModuleClassSpec(
            moduleContext = moduleContext,
            classFqn = observerClassFqn,
            argumentName = "observerClassFqn",
            exampleClassFqn = "${moduleContext.moduleNamespace}\\Observer\\SampleObserver"
        ) {
            throw ObserverValidationException(it)
        }

        if (GetPhpClassByFQN.getInstance(project).execute(observerClassSpec.classFqn) != null) {
            throw ObserverValidationException("Observer class \"${observerClassSpec.classFqn}\" already exists.")
        }

        val eventsXmlFile = FileBasedIndexUtil.findModuleConfigFile(
            ModuleEventsXml.FILE_NAME,
            areaEnum,
            moduleContext.moduleName,
            project
        ) as? XmlFile
        if (eventsXmlFile != null) {
            val duplicateReason = findDuplicateObserverDeclaration(
                eventsXmlFile = eventsXmlFile,
                eventName = normalizedEventName,
                observerName = normalizedObserverName,
                observerClassFqn = observerClassSpec.classFqn
            )
            if (duplicateReason != null) {
                throw ObserverValidationException(
                    "$duplicateReason in ${MagentoMcpSupport.relativePath(project, eventsXmlFile.virtualFile)}."
                )
            }
        }

        return ObserverCreationRequest(
            moduleContext = moduleContext,
            eventName = normalizedEventName,
            observerName = normalizedObserverName,
            observerClassFqn = observerClassSpec.classFqn,
            observerClassName = observerClassSpec.className,
            observerDirectory = observerClassSpec.directory,
            observerNamespace = observerClassSpec.namespace,
            area = normalizedArea
        )
    }

    private fun findDuplicateObserverDeclaration(
        eventsXmlFile: XmlFile,
        eventName: String,
        observerName: String,
        observerClassFqn: String
    ): String? {
        val rootTag = eventsXmlFile.rootTag ?: return null
        for (eventTag in rootTag.findSubTags(ModuleEventsXml.EVENT_TAG)) {
            if (eventTag.getAttributeValue(ModuleEventsXml.NAME_ATTRIBUTE) != eventName) {
                continue
            }

            for (observerTag in eventTag.findSubTags(ModuleEventsXml.OBSERVER_TAG)) {
                if (observerTag.getAttributeValue(ModuleEventsXml.NAME_ATTRIBUTE) == observerName) {
                    return "Observer \"$observerName\" is already declared for event \"$eventName\""
                }

                val declaredInstance = observerTag.getAttributeValue(ModuleEventsXml.INSTANCE_ATTRIBUTE)
                    ?.let(PhpLangUtil::toPresentableFQN)
                if (declaredInstance == observerClassFqn) {
                    return "Observer class \"$observerClassFqn\" is already declared for event \"$eventName\""
                }
            }
        }
        return null
    }

    private fun buildObserverClassFailureMessage(request: ObserverCreationRequest): String {
        return "Magento observer class \"${request.observerClassFqn}\" could not be created. Partial changes were rolled back."
    }

    private fun buildEventsXmlFailureMessage(request: ObserverCreationRequest): String {
        return "Magento observer \"${request.observerClassFqn}\" could not be added to events.xml. Partial changes were rolled back."
    }

    private fun buildSuccessMessage(
        project: Project,
        request: ObserverCreationRequest,
        observerFile: PsiFile,
        eventsXmlFile: PsiFile
    ): String {
        val lines = mutableListOf(
            "Created Magento observer \"${request.observerName}\".",
            "module: ${request.moduleName}",
            "event: ${request.eventName}",
            "observerClass: ${request.observerClassFqn}",
            "area: ${request.area}",
            "files:"
        )
        lines += MagentoMcpSupport.relativePath(project, observerFile.virtualFile)
        lines += MagentoMcpSupport.relativePath(project, eventsXmlFile.virtualFile)
        return lines.joinToString("\n")
    }

    private class ObserverValidationException(message: String) : RuntimeException(message)

    private data class ObserverCreationRequest(
        val moduleContext: MagentoMcpCreateSupport.EditableModuleContext,
        val eventName: String,
        val observerName: String,
        val observerClassFqn: String,
        val observerClassName: String,
        val observerDirectory: String,
        val observerNamespace: String,
        val area: String
    ) {
        val moduleName: String
            get() = moduleContext.moduleName

        val observerRelativePath: String
            get() = MagentoMcpCreateSupport.relativePath(observerDirectory, "$observerClassName.php")

        val eventsXmlRelativePath: String
            get() = MagentoMcpCreateSupport.configFileRelativePath(area, ModuleEventsXml.FILE_NAME)
    }
}
