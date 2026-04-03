/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.magento.idea.magento2plugin.actions.generation.data.ModuleComposerJsonData
import com.magento.idea.magento2plugin.actions.generation.data.ModuleRegistrationPhpData
import com.magento.idea.magento2plugin.actions.generation.data.ModuleXmlData
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleComposerJsonGenerator
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleRegistrationPhpGenerator
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleXmlGenerator
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator
import com.magento.idea.magento2plugin.indexes.ModuleIndex
import com.magento.idea.magento2plugin.magento.packages.Package
import com.magento.idea.magento2plugin.project.Settings
import com.magento.idea.magento2plugin.util.CamelCaseToHyphen
import java.nio.file.Paths
import java.nio.file.InvalidPathException
import java.util.concurrent.atomic.AtomicReference

internal object MagentoModuleCommands {
    private const val ACTION_NAME = "Magento MCP Create Module"
    private const val DEFAULT_MODULE_VERSION = "1.0.0"
    private val MODULE_PART_PATTERN = Regex("[A-Za-z0-9]+")

    internal data class ModuleCreationContext(
        val project: Project,
        val packageName: String,
        val moduleName: String,
        val moduleFullName: String,
        val composerPackageName: String,
        val moduleDescription: String,
        val moduleLicense: List<String>,
        val moduleDirectory: PsiDirectory
    )

    internal typealias ModuleArtifactWriter = (ModuleCreationContext) -> List<PsiFile>

    /**
     * Creates a minimal Magento module under the configured Magento root.
     */
    fun createMagentoModule(project: Project, packageName: String, moduleName: String): String =
        createMagentoModule(project, packageName, moduleName, ::writeModuleArtifacts)

    internal fun createMagentoModule(
        project: Project,
        packageName: String,
        moduleName: String,
        artifactWriter: ModuleArtifactWriter
    ): String {
        val normalizedPackage = packageName.trim()
        val normalizedModule = moduleName.trim()
        validateModulePart(normalizedPackage, "packageName")?.let { return it }
        validateModulePart(normalizedModule, "moduleName")?.let { return it }

        val configuredRoot = Settings.getMagentoPath(project)?.trim().orEmpty()
        if (configuredRoot.isEmpty()) {
            return "Magento root path is not configured for this project."
        }

        val magentoRootDirectory = resolveMagentoRootDirectory(project, configuredRoot)
            ?: return "Configured Magento root path \"$configuredRoot\" could not be resolved."

        val moduleFullName = "${normalizedPackage}_${normalizedModule}"

        val existingModuleDirectory = ReadAction.compute<PsiDirectory?, RuntimeException> {
            magentoRootDirectory.findSubdirectory("app")
                ?.findSubdirectory("code")
                ?.findSubdirectory(normalizedPackage)
                ?.findSubdirectory(normalizedModule)
        }
        if (existingModuleDirectory != null) {
            return "Target directory already exists: ${MagentoMcpSupport.relativePath(project, existingModuleDirectory.virtualFile)}"
        }

        val composerPackageName = buildComposerPackageName(normalizedPackage, normalizedModule)
        val moduleDescription = "Magento 2 module $moduleFullName"
        val moduleLicense = listOf(Settings.getDefaultLicenseName(project) ?: Settings.DEFAULT_LICENSE)

        val directoryGenerator = DirectoryGenerator.getInstance()
        val appDirectoryResult = findOrCreateDirectory(magentoRootDirectory, "app", directoryGenerator)
        val codeDirectoryResult = findOrCreateDirectory(appDirectoryResult.directory, "code", directoryGenerator)
        val packageDirectoryResult = findOrCreateDirectory(
            codeDirectoryResult.directory,
            normalizedPackage,
            directoryGenerator
        )
        val moduleDirectoryResult = findOrCreateDirectory(
            packageDirectoryResult.directory,
            normalizedModule,
            directoryGenerator
        )

        val creationContext = ModuleCreationContext(
            project = project,
            packageName = normalizedPackage,
            moduleName = normalizedModule,
            moduleFullName = moduleFullName,
            composerPackageName = composerPackageName,
            moduleDescription = moduleDescription,
            moduleLicense = moduleLicense,
            moduleDirectory = moduleDirectoryResult.directory
        )

        val createdFiles = try {
            artifactWriter(creationContext)
        } catch (exception: Exception) {
            rollbackModuleTree(
                moduleDirectoryResult,
                packageDirectoryResult,
                codeDirectoryResult,
                appDirectoryResult
            )
            return "Magento module \"$moduleFullName\" could not be created. Rolled back partial changes. Reason: ${exception.message ?: exception::class.java.simpleName}"
        }

        if (createdFiles.size != 3) {
            rollbackModuleTree(
                moduleDirectoryResult,
                packageDirectoryResult,
                codeDirectoryResult,
                appDirectoryResult
            )
            return "Magento module \"$moduleFullName\" could not be created completely. Partial changes were rolled back."
        }

        refreshCreatedModuleTree(magentoRootDirectory, moduleDirectoryResult.directory)

        val lines = mutableListOf(
            "Created Magento module \"$moduleFullName\".",
            "moduleName: $moduleFullName",
            "root: $configuredRoot",
            "modulePath: ${MagentoMcpSupport.relativePath(project, moduleDirectoryResult.directory.virtualFile)}",
            "composerPackage: $composerPackageName",
            "files:"
        )
        createdFiles.forEach { file ->
            lines += MagentoMcpSupport.relativePath(project, file.virtualFile)
        }
        return lines.joinToString("\n")
    }

    private fun writeModuleArtifacts(context: ModuleCreationContext): List<PsiFile> {
        return listOfNotNull(
            ModuleComposerJsonGenerator(
                ModuleComposerJsonData(
                    context.packageName,
                    context.moduleName,
                    context.moduleDirectory,
                    context.moduleDescription,
                    context.composerPackageName,
                    DEFAULT_MODULE_VERSION,
                    context.moduleLicense,
                    emptyList<String>(),
                    false
                ),
                context.project
            ).generate(ACTION_NAME),
            ModuleRegistrationPhpGenerator(
                ModuleRegistrationPhpData(
                    context.packageName,
                    context.moduleName,
                    context.moduleDirectory,
                    false
                ),
                context.project
            ).generate(ACTION_NAME),
            ModuleXmlGenerator(
                ModuleXmlData(
                    context.packageName,
                    context.moduleName,
                    null,
                    context.moduleDirectory,
                    emptyList<String>(),
                    false
                ),
                context.project
            ).generate(ACTION_NAME, false)
        )
    }

    private fun rollbackModuleTree(vararg directoryResults: DirectoryCreationResult) {
        val rollbackAction = {
            directoryResults.forEach { result ->
                if (!result.created || !result.directory.isValid) {
                    return@forEach
                }

                if (result.directory.files.isEmpty() && result.directory.subdirectories.isEmpty()) {
                    result.directory.delete()
                    return@forEach
                }

                if (result.name == "app" || result.name == "code" || result.name == "etc") {
                    return@forEach
                }

                result.directory.delete()
            }
        }

        val application = ApplicationManager.getApplication()
        if (application.isDispatchThread) {
            WriteAction.run<RuntimeException>(rollbackAction)
            return
        }

        val failure = AtomicReference<RuntimeException?>()
        application.invokeAndWait {
            try {
                WriteAction.run<RuntimeException>(rollbackAction)
            } catch (exception: RuntimeException) {
                failure.set(exception)
            }
        }
        failure.get()?.let { throw it }
    }

    private fun findOrCreateDirectory(
        parent: PsiDirectory,
        name: String,
        directoryGenerator: DirectoryGenerator
    ): DirectoryCreationResult {
        val existing = ReadAction.compute<PsiDirectory?, RuntimeException> {
            parent.findSubdirectory(name)
        }
        if (existing != null) {
            return DirectoryCreationResult(existing, created = false, name = name)
        }

        return DirectoryCreationResult(
            directory = directoryGenerator.findOrCreateSubdirectory(parent, name),
            created = true,
            name = name
        )
    }

    private fun validateModulePart(value: String, argumentName: String): String? {
        if (value.isEmpty()) {
            return "Provide a non-empty $argumentName value."
        }
        if (!MODULE_PART_PATTERN.matches(value)) {
            return "$argumentName must contain only alphanumeric characters."
        }
        if (!value.first().isUpperCase() && !value.first().isDigit()) {
            return "$argumentName must start with an uppercase letter or digit."
        }
        return null
    }

    private fun buildComposerPackageName(packageName: String, moduleName: String): String {
        val camelCaseToHyphen = CamelCaseToHyphen.getInstance()
        return "${camelCaseToHyphen.convert(packageName)}/module-${camelCaseToHyphen.convert(moduleName)}"
    }

    private fun refreshCreatedModuleTree(vararg directories: PsiDirectory) {
        val virtualFiles = directories.map { it.virtualFile }
            .filter { it.isValid }
            .toTypedArray()
        VfsUtil.markDirtyAndRefresh(false, true, true, *virtualFiles)
        virtualFiles.forEach { it.refresh(false, true) }
    }

    private fun resolveMagentoRootDirectory(project: Project, configuredRoot: String): PsiDirectory? {
        return ReadAction.compute<PsiDirectory?, RuntimeException> {
            val fileSystem = LocalFileSystem.getInstance()
            val normalizedConfiguredRoot = FileUtil.toSystemIndependentName(configuredRoot.trim())

            if (isAbsolutePath(normalizedConfiguredRoot)) {
                val absoluteVirtualFile = fileSystem.refreshAndFindFileByPath(normalizedConfiguredRoot)
                    ?: fileSystem.findFileByPath(normalizedConfiguredRoot)
                if (absoluteVirtualFile != null && absoluteVirtualFile.isDirectory) {
                    return@compute PsiManager.getInstance(project).findDirectory(absoluteVirtualFile)
                }
            }

            val moduleIndex = ModuleIndex(project)
            val configuredRootTrimmed = normalizedConfiguredRoot.trim('/').replace('\\', '/')
            val configuredPrefix = normalizedConfiguredRoot.trimEnd('/') + "/" + Package.packagesRoot + "/"
            for (moduleName in moduleIndex.moduleNames) {
                val moduleDirectory = moduleIndex.getModuleDirectoryByModuleName(moduleName) ?: continue
                val modulePath = moduleDirectory.virtualFile.path.replace('\\', '/')

                val resolvedRoot = moduleDirectory.parentDirectory(levels = 4) ?: continue
                val resolvedRootPath = resolvedRoot.virtualFile.path.replace('\\', '/')
                if (modulePath.startsWith(configuredPrefix)) {
                    return@compute resolvedRoot
                }

                if (!isAbsolutePath(normalizedConfiguredRoot) &&
                    configuredRootTrimmed.isNotEmpty() &&
                    resolvedRootPath.endsWith("/$configuredRootTrimmed")
                ) {
                    return@compute resolvedRoot
                }
            }

            val candidates = linkedSetOf(normalizedConfiguredRoot)
            val basePath = project.basePath
            if (basePath != null) {
                candidates += Paths.get(basePath, normalizedConfiguredRoot.removePrefix("/")).normalize().toString()
                candidates += Paths.get(basePath, normalizedConfiguredRoot).normalize().toString()
            }

            val virtualFile = candidates.asSequence()
                .map(FileUtil::toSystemIndependentName)
                .mapNotNull { path -> fileSystem.refreshAndFindFileByPath(path) ?: fileSystem.findFileByPath(path) }
                .firstOrNull { it.isDirectory }
            if (virtualFile != null) {
                return@compute PsiManager.getInstance(project).findDirectory(virtualFile)
            }

            null
        }
    }

    private fun isAbsolutePath(path: String): Boolean {
        return try {
            Paths.get(path).isAbsolute
        } catch (_: InvalidPathException) {
            false
        }
    }

    private fun PsiDirectory.parentDirectory(levels: Int): PsiDirectory? {
        var current: PsiDirectory? = this
        repeat(levels) {
            current = current?.parentDirectory
        }
        return current
    }

    private data class DirectoryCreationResult(
        val directory: PsiDirectory,
        val created: Boolean,
        val name: String
    )
}
