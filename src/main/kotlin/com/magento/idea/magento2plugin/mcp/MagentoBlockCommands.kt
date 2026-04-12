/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.magento.idea.magento2plugin.actions.generation.data.BlockFileData
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleBlockClassGenerator
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN

internal object MagentoBlockCommands {
    private const val ACTION_NAME = "Magento MCP Create Block"
    private const val BLOCK_NAMESPACE_SEGMENT = "\\Block\\"

    fun createMagentoBlock(project: Project, moduleName: String, blockClassFqn: String): String {
        val request = try {
            MagentoMcpCreateSupport.runReadAction {
                resolveRequest(project, moduleName, blockClassFqn)
            }
        } catch (_: ReadAction.CannotReadException) {
            return MagentoMcpReadActionSupport.cancellationMessage()
        } catch (exception: BlockValidationException) {
            return exception.message ?: "Block creation request is invalid."
        }

        val transaction = MagentoMcpCreateSupport.FileTransaction(project, request.moduleContext.moduleDirectory)
        return try {
            transaction.track(request.blockRelativePath)
            val blockFile = ModuleBlockClassGenerator(
                BlockFileData(
                    request.blockDirectory,
                    request.blockClassName,
                    request.moduleName,
                    request.blockNamespace
                ),
                project,
                false
            ).generate(ACTION_NAME, false) ?: run {
                transaction.rollback()
                return "Magento block class \"${request.blockClassFqn}\" could not be created. Partial changes were rolled back."
            }

            buildSuccessMessage(project, request, blockFile)
        } catch (exception: Throwable) {
            transaction.rollback()
            "Magento block class \"${request.blockClassFqn}\" could not be created. Partial changes were rolled back. Reason: ${exception.message ?: exception::class.java.simpleName}"
        }
    }

    private fun resolveRequest(project: Project, moduleName: String, blockClassFqn: String): BlockCreationRequest {
        val moduleContext = MagentoMcpCreateSupport.requireEditableModule(project, moduleName) {
            throw BlockValidationException(it)
        }
        val blockClassSpec = MagentoMcpCreateSupport.requireModuleClassSpec(
            moduleContext = moduleContext,
            classFqn = blockClassFqn,
            argumentName = "blockClassFqn",
            exampleClassFqn = "${moduleContext.moduleNamespace}\\Block\\SampleBlock"
        ) {
            throw BlockValidationException(it)
        }

        val expectedPrefix = "${moduleContext.moduleNamespace}$BLOCK_NAMESPACE_SEGMENT"
        if (!blockClassSpec.classFqn.startsWith(expectedPrefix)) {
            throw BlockValidationException("blockClassFqn must be inside the module Block namespace \"$expectedPrefix\".")
        }

        if (GetPhpClassByFQN.getInstance(project).execute(blockClassSpec.classFqn) != null) {
            throw BlockValidationException("Block class \"${blockClassSpec.classFqn}\" already exists.")
        }

        return BlockCreationRequest(
            moduleContext = moduleContext,
            blockClassFqn = blockClassSpec.classFqn,
            blockClassName = blockClassSpec.className,
            blockDirectory = blockClassSpec.directory,
            blockNamespace = blockClassSpec.namespace
        )
    }

    private fun buildSuccessMessage(project: Project, request: BlockCreationRequest, blockFile: PsiFile): String {
        val lines = mutableListOf(
            "Created Magento block class \"${request.blockClassFqn}\".",
            "module: ${request.moduleName}",
            "files:"
        )
        lines += MagentoMcpSupport.relativePath(project, blockFile.virtualFile)
        return lines.joinToString("\n")
    }

    private class BlockValidationException(message: String) : RuntimeException(message)

    private data class BlockCreationRequest(
        val moduleContext: MagentoMcpCreateSupport.EditableModuleContext,
        val blockClassFqn: String,
        val blockClassName: String,
        val blockDirectory: String,
        val blockNamespace: String
    ) {
        val moduleName: String
            get() = moduleContext.moduleName

        val blockRelativePath: String
            get() = MagentoMcpCreateSupport.relativePath(blockDirectory, "$blockClassName.php")
    }
}
