/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.magento.idea.magento2plugin.indexes.ModuleIndex
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile
import com.magento.idea.magento2plugin.magento.packages.Package
import com.magento.idea.magento2plugin.util.RegExUtil

internal object MagentoMcpCreateSupport {
    private val MODULE_NAME_PATTERN = Regex(RegExUtil.Magento.MODULE_NAME)
    private val PHP_CLASS_PATTERN = Regex(RegExUtil.Magento.PHP_CLASS)
    private val DIRECTORY_PATTERN = Regex(RegExUtil.DIRECTORY)

    fun <T> runReadAction(action: () -> T): T {
        return if (ApplicationManager.getApplication().isDispatchThread) {
            ApplicationManager.getApplication().runReadAction<T> { action() }
        } else {
            ReadAction.computeCancellable<T, RuntimeException>(action)
        }
    }

    fun requireEditableModule(
        project: Project,
        moduleName: String,
        onError: (String) -> Nothing
    ): EditableModuleContext {
        val normalizedModuleName = moduleName.trim()
        if (normalizedModuleName.isEmpty()) {
            onError("Provide a non-empty moduleName value.")
        }
        if (!MODULE_NAME_PATTERN.matches(normalizedModuleName)) {
            onError("moduleName must match the Magento Vendor_Module format.")
        }

        val moduleIndex = ModuleIndex(project)
        val moduleDirectory = moduleIndex.getModuleDirectoryByModuleName(normalizedModuleName)
            ?: onError("Magento module \"$normalizedModuleName\" was not found.")
        if (normalizedModuleName !in moduleIndex.getEditableModuleNames()) {
            onError("Magento module \"$normalizedModuleName\" is not editable.")
        }

        return EditableModuleContext(
            moduleName = normalizedModuleName,
            moduleDirectory = moduleDirectory,
            moduleNamespace = buildModuleNamespace(normalizedModuleName)
        )
    }

    fun requireModuleClassSpec(
        moduleContext: EditableModuleContext,
        classFqn: String,
        argumentName: String,
        exampleClassFqn: String,
        onError: (String) -> Nothing
    ): ModuleClassSpec {
        val normalizedClassFqn = MagentoMcpSupport.normalizeFqn(classFqn)
        if (normalizedClassFqn.isEmpty()) {
            onError("Provide a non-empty $argumentName value.")
        }

        val expectedPrefix = "${moduleContext.moduleNamespace}${Package.fqnSeparator}"
        if (!normalizedClassFqn.startsWith(expectedPrefix)) {
            onError("$argumentName must be inside the module namespace \"$expectedPrefix\".")
        }

        val relativeClassFqn = normalizedClassFqn.removePrefix(expectedPrefix)
        val segments = relativeClassFqn.split(Package.fqnSeparator).filter { it.isNotEmpty() }
        if (segments.size < 2) {
            onError("$argumentName must include at least one sub-namespace and a class name, for example \"$exampleClassFqn\".")
        }

        val className = segments.last()
        if (!PHP_CLASS_PATTERN.matches(className)) {
            onError("$argumentName must end with a valid PHP class name.")
        }

        val directory = segments.dropLast(1).joinToString("/")
        if (!DIRECTORY_PATTERN.matches(directory)) {
            onError("$argumentName contains an invalid target directory.")
        }

        return ModuleClassSpec(
            classFqn = normalizedClassFqn,
            className = className,
            directory = directory,
            namespace = normalizedClassFqn.substringBeforeLast(Package.fqnSeparator)
        )
    }

    fun phpFileRelativePath(file: AbstractPhpFile): String {
        return "${file.directory}/${file.fileName}"
    }

    fun configFileRelativePath(area: String, fileName: String): String {
        return if (area == "base") {
            "etc/$fileName"
        } else {
            "etc/$area/$fileName"
        }
    }

    fun relativePath(directory: String, fileName: String): String {
        return "$directory/$fileName"
    }

    fun buildModuleNamespace(moduleName: String): String {
        return moduleName.replace(Package.vendorModuleNameSeparator, Package.fqnSeparator)
    }

    class FileTransaction(
        private val project: Project,
        moduleDirectory: PsiDirectory
    ) {
        private val moduleRootPath = moduleDirectory.virtualFile.path.trimEnd('/')
        private val snapshots = linkedMapOf<String, FileSnapshot>()

        fun track(relativePath: String) {
            if (relativePath in snapshots) {
                return
            }

            val normalizedRelativePath = relativePath.trimStart('/').replace('\\', '/')
            val absolutePath = "$moduleRootPath/$normalizedRelativePath"
            val virtualFile = LocalFileSystem.getInstance().findFileByPath(absolutePath)
            snapshots[normalizedRelativePath] = FileSnapshot(
                relativePath = normalizedRelativePath,
                existedBefore = virtualFile != null,
                textBefore = virtualFile?.let(::readTextSafely)
            )
        }

        fun rollback() {
            WriteCommandAction.runWriteCommandAction(project, "Magento MCP Rollback Creation", null, Runnable {
                snapshots.values.toList().asReversed().forEach { snapshot ->
                    val absolutePath = "$moduleRootPath/${snapshot.relativePath}"
                    val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(absolutePath)
                        ?: LocalFileSystem.getInstance().findFileByPath(absolutePath)

                    if (snapshot.existedBefore) {
                        if (virtualFile != null && snapshot.textBefore != null) {
                            VfsUtil.saveText(virtualFile, snapshot.textBefore)
                        }
                    } else if (virtualFile != null && virtualFile.isValid) {
                        virtualFile.delete(this)
                    }
                }
            })
        }

        private fun readTextSafely(virtualFile: VirtualFile): String? {
            return try {
                VfsUtil.loadText(virtualFile)
            } catch (_: Throwable) {
                null
            }
        }
    }

    data class EditableModuleContext(
        val moduleName: String,
        val moduleDirectory: PsiDirectory,
        val moduleNamespace: String
    )

    data class ModuleClassSpec(
        val classFqn: String,
        val className: String,
        val directory: String,
        val namespace: String
    )

    private data class FileSnapshot(
        val relativePath: String,
        val existedBefore: Boolean,
        val textBefore: String?
    )
}
