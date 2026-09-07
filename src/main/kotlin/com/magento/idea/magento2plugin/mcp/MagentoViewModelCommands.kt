/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.magento.idea.magento2plugin.actions.generation.data.ViewModelFileData
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleViewModelClassGenerator
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN

internal object MagentoViewModelCommands {
    private const val ACTION_NAME = "Magento MCP Create View Model"
    private const val VIEW_MODEL_NAMESPACE_SEGMENT = "\\ViewModel\\"

    fun createMagentoViewModel(project: Project, moduleName: String, viewModelClassFqn: String): String {
        val request = try {
            MagentoMcpCreateSupport.runReadAction {
                resolveRequest(project, moduleName, viewModelClassFqn)
            }
        } catch (_: ReadAction.CannotReadException) {
            return MagentoMcpReadActionSupport.cancellationMessage()
        } catch (exception: ViewModelValidationException) {
            return exception.message ?: "View model creation request is invalid."
        }

        val transaction = MagentoMcpCreateSupport.FileTransaction(project, request.moduleContext.moduleDirectory)
        return try {
            transaction.track(request.viewModelRelativePath)
            val viewModelFile = ModuleViewModelClassGenerator(
                ViewModelFileData(
                    request.viewModelDirectory,
                    request.viewModelClassName,
                    request.moduleName,
                    request.viewModelNamespace
                ),
                project,
                false
            ).generate(ACTION_NAME, false) ?: run {
                transaction.rollback()
                return "Magento view model class \"${request.viewModelClassFqn}\" could not be created. Partial changes were rolled back."
            }

            buildSuccessMessage(project, request, viewModelFile)
        } catch (exception: Throwable) {
            transaction.rollback()
            "Magento view model class \"${request.viewModelClassFqn}\" could not be created. Partial changes were rolled back. Reason: ${exception.message ?: exception::class.java.simpleName}"
        }
    }

    private fun resolveRequest(
        project: Project,
        moduleName: String,
        viewModelClassFqn: String
    ): ViewModelCreationRequest {
        val moduleContext = MagentoMcpCreateSupport.requireEditableModule(project, moduleName) {
            throw ViewModelValidationException(it)
        }
        val viewModelClassSpec = MagentoMcpCreateSupport.requireModuleClassSpec(
            moduleContext = moduleContext,
            classFqn = viewModelClassFqn,
            argumentName = "viewModelClassFqn",
            exampleClassFqn = "${moduleContext.moduleNamespace}\\ViewModel\\SampleViewModel"
        ) {
            throw ViewModelValidationException(it)
        }

        val expectedPrefix = "${moduleContext.moduleNamespace}$VIEW_MODEL_NAMESPACE_SEGMENT"
        if (!viewModelClassSpec.classFqn.startsWith(expectedPrefix)) {
            throw ViewModelValidationException(
                "viewModelClassFqn must be inside the module ViewModel namespace \"$expectedPrefix\"."
            )
        }

        if (GetPhpClassByFQN.getInstance(project).execute(viewModelClassSpec.classFqn) != null) {
            throw ViewModelValidationException("View model class \"${viewModelClassSpec.classFqn}\" already exists.")
        }

        return ViewModelCreationRequest(
            moduleContext = moduleContext,
            viewModelClassFqn = viewModelClassSpec.classFqn,
            viewModelClassName = viewModelClassSpec.className,
            viewModelDirectory = viewModelClassSpec.directory,
            viewModelNamespace = viewModelClassSpec.namespace
        )
    }

    private fun buildSuccessMessage(
        project: Project,
        request: ViewModelCreationRequest,
        viewModelFile: PsiFile
    ): String {
        val lines = mutableListOf(
            "Created Magento view model class \"${request.viewModelClassFqn}\".",
            "module: ${request.moduleName}",
            "files:"
        )
        lines += MagentoMcpSupport.relativePath(project, viewModelFile.virtualFile)
        return lines.joinToString("\n")
    }

    private class ViewModelValidationException(message: String) : RuntimeException(message)

    private data class ViewModelCreationRequest(
        val moduleContext: MagentoMcpCreateSupport.EditableModuleContext,
        val viewModelClassFqn: String,
        val viewModelClassName: String,
        val viewModelDirectory: String,
        val viewModelNamespace: String
    ) {
        val moduleName: String
            get() = moduleContext.moduleName

        val viewModelRelativePath: String
            get() = MagentoMcpCreateSupport.relativePath(viewModelDirectory, "$viewModelClassName.php")
    }
}
