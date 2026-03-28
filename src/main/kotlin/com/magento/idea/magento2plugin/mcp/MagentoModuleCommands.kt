/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDirectory
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

internal object MagentoModuleCommands {
    private const val ACTION_NAME = "Magento MCP Create Module"
    private const val DEFAULT_MODULE_VERSION = "1.0.0"
    private val MODULE_PART_PATTERN = Regex("[A-Za-z0-9]+")

    /**
     * Creates a minimal Magento module under the configured Magento root.
     */
    fun createMagentoModule(project: Project, packageName: String, moduleName: String): String {
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
        val appCodeDirectory = DirectoryGenerator.getInstance()
            .findOrCreateSubdirectories(magentoRootDirectory, Package.packagesRoot)

        val moduleFullName = "${normalizedPackage}_${normalizedModule}"
        if (ModuleIndex(project).getModuleDirectoryByModuleName(moduleFullName) != null) {
            return "Magento module \"$moduleFullName\" already exists."
        }

        val existingModuleDirectory = appCodeDirectory.findSubdirectory(normalizedPackage)
            ?.findSubdirectory(normalizedModule)
        if (existingModuleDirectory != null) {
            return "Target directory already exists: ${MagentoMcpSupport.relativePath(project, existingModuleDirectory.virtualFile)}"
        }

        val composerPackageName = buildComposerPackageName(normalizedPackage, normalizedModule)
        val moduleDescription = "Magento 2 module $moduleFullName"
        val moduleLicense = listOf(Settings.getDefaultLicenseName(project) ?: Settings.DEFAULT_LICENSE)

        val createdFiles = listOfNotNull(
            ModuleComposerJsonGenerator(
                ModuleComposerJsonData(
                    normalizedPackage,
                    normalizedModule,
                    appCodeDirectory,
                    moduleDescription,
                    composerPackageName,
                    DEFAULT_MODULE_VERSION,
                    moduleLicense,
                    emptyList<String>(),
                    true
                ),
                project
            ).generate(ACTION_NAME),
            ModuleRegistrationPhpGenerator(
                ModuleRegistrationPhpData(
                    normalizedPackage,
                    normalizedModule,
                    appCodeDirectory,
                    true
                ),
                project
            ).generate(ACTION_NAME),
            ModuleXmlGenerator(
                ModuleXmlData(
                    normalizedPackage,
                    normalizedModule,
                    null,
                    appCodeDirectory,
                    emptyList<String>(),
                    true
                ),
                project
            ).generate(ACTION_NAME, false)
        )

        if (createdFiles.size != 3) {
            val createdPaths = createdFiles.joinToString(", ") {
                MagentoMcpSupport.relativePath(project, it.virtualFile)
            }.ifEmpty { "none" }
            return "Magento module \"$moduleFullName\" was only partially created. Files created: $createdPaths"
        }

        val lines = mutableListOf(
            "Created Magento module \"$moduleFullName\".",
            "root: $configuredRoot",
            "composerPackage: $composerPackageName",
            "files:"
        )
        createdFiles.forEach { file ->
            lines += MagentoMcpSupport.relativePath(project, file.virtualFile)
        }
        return lines.joinToString("\n")
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

    private fun resolveMagentoRootDirectory(project: Project, configuredRoot: String): PsiDirectory? {
        val fileSystem = LocalFileSystem.getInstance()
        val candidates = linkedSetOf(configuredRoot)
        val basePath = project.basePath
        if (basePath != null) {
            candidates += Paths.get(basePath, configuredRoot.removePrefix("/")).normalize().toString()
            candidates += Paths.get(basePath, configuredRoot).normalize().toString()
        }

        val virtualFile = candidates.asSequence()
            .mapNotNull { path -> fileSystem.refreshAndFindFileByPath(path) ?: fileSystem.findFileByPath(path) }
            .firstOrNull { it.isDirectory }
        if (virtualFile != null) {
            return PsiManager.getInstance(project).findDirectory(virtualFile)
        }

        val configuredPrefix = configuredRoot.trimEnd('/') + "/" + Package.packagesRoot + "/"
        val moduleIndex = ModuleIndex(project)
        for (moduleName in moduleIndex.moduleNames) {
            val moduleDirectory = moduleIndex.getModuleDirectoryByModuleName(moduleName) ?: continue
            val modulePath = moduleDirectory.virtualFile.path.replace('\\', '/')
            if (!modulePath.startsWith(configuredPrefix)) {
                continue
            }

            return moduleDirectory.parentDirectory(levels = 4)
        }

        return null
    }

    private fun PsiDirectory.parentDirectory(levels: Int): PsiDirectory? {
        var current: PsiDirectory? = this
        repeat(levels) {
            current = current?.parentDirectory
        }
        return current
    }
}
