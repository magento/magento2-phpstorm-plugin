/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project
import com.magento.idea.magento2plugin.project.Settings
import java.nio.file.Path

/** IDE adapter for the shared filesystem-only environment service. */
internal object MagentoCliToolQueries {
    fun describeCliEnvironment(project: Project): String {
        val root = projectRootPath(project) ?: return "The project directory is unavailable."
        return MagentoCliEnvironment.describeCliEnvironment(root, resolveConfiguredMagentoRootRelativePath(project),
            Settings.getMcpCliToolCandidates(project), ::findDdevExecutable, "PhpStorm's Terminal (IDE console)")
    }

    internal fun findDdevExecutable(): Path? =
        MagentoDdevEnvironment.findExecutable(com.intellij.util.EnvironmentUtil.getValue("PATH"))

    internal fun discoverTools(root: Path, searchRoots: List<String>, candidates: List<String>) =
        MagentoCliEnvironment.discoverTools(root, searchRoots, candidates)
    internal fun buildSearchRoots(root: Path, relative: String?) = MagentoCliEnvironment.buildSearchRoots(root, relative)
    internal fun resolveProjectPath(root: Path, path: String) = MagentoCliEnvironment.resolveProjectPath(root, path)
    internal fun shellQuote(value: String) = MagentoCliEnvironment.shellQuote(value)
    private fun normalizePath(value: String?) = MagentoCliEnvironment.normalizePath(value)

    internal fun resolveConfiguredMagentoRootRelativePath(project: Project): String? {
        val configuredMagentoPath = Settings.getMagentoPath(project)?.trim().orEmpty()
        if (configuredMagentoPath.isEmpty()) {
            return null
        }
        val normalizedConfiguredPath = normalizePath(configuredMagentoPath)
        if (normalizedConfiguredPath.isEmpty() || normalizedConfiguredPath == ".") {
            return null
        }

        val projectBasePath = normalizePath(project.basePath)
        if (projectBasePath.isNotEmpty()) {
            if (normalizedConfiguredPath == projectBasePath) {
                return null
            }
            if (normalizedConfiguredPath.startsWith("$projectBasePath/")) {
                return normalizedConfiguredPath.removePrefix("$projectBasePath/").trim('/').ifEmpty { null }
            }
        }

        val projectRootName = project.projectFile?.parent?.name
        val configuredSegments = normalizedConfiguredPath
            .removePrefix("/")
            .removePrefix("./")
            .split('/')
            .filter { it.isNotBlank() && it != "." }
        if (configuredSegments.isEmpty()) {
            return null
        }
        if (projectRootName != null && configuredSegments.first() == projectRootName) {
            return configuredSegments.drop(1).joinToString("/").ifEmpty { null }
        }

        val relativeCandidate = configuredSegments.joinToString("/")
        if (relativeCandidate == projectRootName) {
            return null
        }

        return relativeCandidate.ifEmpty { null }
    }

    internal fun projectRootPath(project: Project): Path? {
        val projectBasePath = project.basePath ?: project.projectFile?.parent?.path ?: return null
        return Path.of(projectBasePath).toAbsolutePath().normalize()
    }

}
