/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.magento.idea.magento2plugin.actions.generation.data.ControllerFileData
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleControllerClassGenerator
import com.magento.idea.magento2plugin.magento.packages.Areas
import com.magento.idea.magento2plugin.magento.packages.HttpMethod
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN
import com.magento.idea.magento2plugin.util.RegExUtil

internal object MagentoControllerCommands {
    private const val ACTION_NAME = "Magento MCP Create Controller"
    private const val CONTROLLER_NAMESPACE_SEGMENT = "\\Controller\\"

    private val IDENTIFIER_WITH_COLON_PATTERN = Regex(RegExUtil.IDENTIFIER_WITH_COLON)

    fun createMagentoController(
        project: Project,
        moduleName: String,
        controllerClassFqn: String,
        httpMethod: String,
        inheritClass: Boolean,
        aclResource: String
    ): String {
        val request = try {
            MagentoMcpCreateSupport.runReadAction {
                resolveRequest(
                    project = project,
                    moduleName = moduleName,
                    controllerClassFqn = controllerClassFqn,
                    httpMethod = httpMethod,
                    inheritClass = inheritClass,
                    aclResource = aclResource
                )
            }
        } catch (_: ReadAction.CannotReadException) {
            return MagentoMcpReadActionSupport.cancellationMessage()
        } catch (exception: ControllerValidationException) {
            return exception.message ?: "Controller creation request is invalid."
        }

        val transaction = MagentoMcpCreateSupport.FileTransaction(project, request.moduleContext.moduleDirectory)
        return try {
            transaction.track(request.controllerRelativePath)
            val controllerFile = ModuleControllerClassGenerator(
                ControllerFileData(
                    request.controllerDirectory,
                    request.controllerClassName,
                    request.moduleName,
                    request.area,
                    request.httpMethod,
                    request.aclResource,
                    request.inheritClass,
                    request.controllerNamespace
                ),
                project,
                false
            ).generate(ACTION_NAME, false) ?: run {
                transaction.rollback()
                return "Magento controller class \"${request.controllerClassFqn}\" could not be created. Partial changes were rolled back."
            }

            buildSuccessMessage(project, request, controllerFile)
        } catch (exception: Throwable) {
            transaction.rollback()
            "Magento controller class \"${request.controllerClassFqn}\" could not be created. Partial changes were rolled back. Reason: ${exception.message ?: exception::class.java.simpleName}"
        }
    }

    private fun resolveRequest(
        project: Project,
        moduleName: String,
        controllerClassFqn: String,
        httpMethod: String,
        inheritClass: Boolean,
        aclResource: String
    ): ControllerCreationRequest {
        val moduleContext = MagentoMcpCreateSupport.requireEditableModule(project, moduleName) {
            throw ControllerValidationException(it)
        }
        val controllerClassSpec = MagentoMcpCreateSupport.requireModuleClassSpec(
            moduleContext = moduleContext,
            classFqn = controllerClassFqn,
            argumentName = "controllerClassFqn",
            exampleClassFqn = "${moduleContext.moduleNamespace}\\Controller\\Index\\Index"
        ) {
            throw ControllerValidationException(it)
        }

        val expectedPrefix = "${moduleContext.moduleNamespace}$CONTROLLER_NAMESPACE_SEGMENT"
        if (!controllerClassSpec.classFqn.startsWith(expectedPrefix)) {
            throw ControllerValidationException(
                "controllerClassFqn must be inside the module Controller namespace \"$expectedPrefix\"."
            )
        }

        val relativeSegments = controllerClassSpec.classFqn
            .removePrefix("${moduleContext.moduleNamespace}\\")
            .split("\\")
            .filter { it.isNotEmpty() }
        val area = inferArea(relativeSegments)
        val minimumSegmentCount = if (area == Areas.adminhtml.toString()) 4 else 3
        if (relativeSegments.size < minimumSegmentCount) {
            val exampleFqn = if (area == Areas.adminhtml.toString()) {
                "${moduleContext.moduleNamespace}\\Controller\\Adminhtml\\Index\\Index"
            } else {
                "${moduleContext.moduleNamespace}\\Controller\\Index\\Index"
            }
            throw ControllerValidationException(
                "controllerClassFqn must include a controller path and action class, for example \"$exampleFqn\"."
            )
        }

        val normalizedHttpMethod = httpMethod.trim().uppercase()
        val httpMethodEnum = runCatching { HttpMethod.valueOf(normalizedHttpMethod) }.getOrNull()
            ?: throw ControllerValidationException(
                "httpMethod must be one of: ${HttpMethod.values().joinToString(", ") { it.name }}."
            )

        val normalizedAclResource = aclResource.trim()
        if (normalizedAclResource.isNotEmpty()) {
            if (area != Areas.adminhtml.toString() || !inheritClass) {
                throw ControllerValidationException(
                    "aclResource is only used for adminhtml controllers when inheritClass is true."
                )
            }
            if (!IDENTIFIER_WITH_COLON_PATTERN.matches(normalizedAclResource)) {
                throw ControllerValidationException(
                    "aclResource may contain only letters, numbers, underscores, hyphens, and colons."
                )
            }
        }

        if (GetPhpClassByFQN.getInstance(project).execute(controllerClassSpec.classFqn) != null) {
            throw ControllerValidationException("Controller class \"${controllerClassSpec.classFqn}\" already exists.")
        }

        return ControllerCreationRequest(
            moduleContext = moduleContext,
            controllerClassFqn = controllerClassSpec.classFqn,
            controllerClassName = controllerClassSpec.className,
            controllerDirectory = controllerClassSpec.directory,
            controllerNamespace = controllerClassSpec.namespace,
            area = area,
            httpMethod = httpMethodEnum.name,
            inheritClass = inheritClass,
            aclResource = normalizedAclResource
        )
    }

    private fun inferArea(relativeSegments: List<String>): String {
        if (relativeSegments.firstOrNull() != "Controller") {
            throw ControllerValidationException(
                "controllerClassFqn must start with the module Controller namespace segment."
            )
        }

        return if (relativeSegments.getOrNull(1) == "Adminhtml") {
            Areas.adminhtml.toString()
        } else {
            Areas.frontend.toString()
        }
    }

    private fun buildSuccessMessage(
        project: Project,
        request: ControllerCreationRequest,
        controllerFile: PsiFile
    ): String {
        val lines = mutableListOf(
            "Created Magento controller class \"${request.controllerClassFqn}\".",
            "module: ${request.moduleName}",
            "area: ${request.area}",
            "httpMethod: ${request.httpMethod}",
            "inheritClass: ${request.inheritClass}"
        )
        if (request.aclResource.isNotEmpty()) {
            lines += "aclResource: ${request.aclResource}"
        }
        lines += "files:"
        lines += MagentoMcpSupport.relativePath(project, controllerFile.virtualFile)
        return lines.joinToString("\n")
    }

    private class ControllerValidationException(message: String) : RuntimeException(message)

    private data class ControllerCreationRequest(
        val moduleContext: MagentoMcpCreateSupport.EditableModuleContext,
        val controllerClassFqn: String,
        val controllerClassName: String,
        val controllerDirectory: String,
        val controllerNamespace: String,
        val area: String,
        val httpMethod: String,
        val inheritClass: Boolean,
        val aclResource: String
    ) {
        val moduleName: String
            get() = moduleContext.moduleName

        val controllerRelativePath: String
            get() = MagentoMcpCreateSupport.relativePath(controllerDirectory, "$controllerClassName.php")
    }
}
