/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.roots.ProjectRootManager
import com.magento.idea.magento2plugin.project.Settings

internal object MagentoCliToolQueries {
    private val knownToolTypes = linkedMapOf(
        "magento" to ToolKind(
            description = "Magento CLI wrapper",
            usage = "Use this for Magento CLI commands in the project runtime.",
            exampleSuffix = "cache:flush"
        ),
        "n98-magerun2" to ToolKind(
            description = "n98-magerun wrapper",
            usage = "Use this for n98-magerun commands in the project runtime.",
            exampleSuffix = "sys:info"
        ),
        "n98-magerun" to ToolKind(
            description = "n98-magerun wrapper",
            usage = "Use this for n98-magerun commands in the project runtime.",
            exampleSuffix = "sys:info"
        ),
        "magerun" to ToolKind(
            description = "n98-magerun wrapper",
            usage = "Use this for n98-magerun commands in the project runtime.",
            exampleSuffix = "sys:info"
        ),
        "composer" to ToolKind(
            description = "Composer wrapper",
            usage = "Use this for Composer commands in the project runtime.",
            exampleSuffix = "install"
        ),
        "php" to ToolKind(
            description = "PHP wrapper",
            usage = "Use this for ad hoc PHP commands in the project runtime.",
            exampleSuffix = "-v"
        ),
        "cli" to ToolKind(
            description = "project CLI wrapper",
            usage = "Use this wrapper when the project documents a custom CLI entrypoint.",
            exampleSuffix = "list"
        ),
        "console" to ToolKind(
            description = "project CLI wrapper",
            usage = "Use this wrapper when the project documents a custom console entrypoint.",
            exampleSuffix = "list"
        ),
        "restart" to ToolKind(
            description = "environment wrapper",
            usage = "Use this to restart the local project environment when needed.",
            exampleSuffix = ""
        )
    )

    fun describeCliEnvironment(project: Project): String {
        val configuredCandidates = Settings.getMcpCliToolCandidates(project)
        val searchRoots = resolveSearchRoots(project)
        val detectedTools = discoverTools(project, searchRoots, configuredCandidates)
        val fallbackTools = buildConfiguredFallbackTools(project, searchRoots, configuredCandidates)
        val toolsToReport = if (detectedTools.isEmpty()) fallbackTools else detectedTools
        val lines = mutableListOf<String>()

        lines += "Magento CLI environment"
        lines += "Prefer project-local wrappers over global binaries for Magento, Docker, PHP, Composer, and n98-magerun commands."
        lines += "Configured wrapper candidates: ${configuredCandidates.joinToString(", ")}"

        if (searchRoots.isNotEmpty()) {
            lines += "Search roots: ${searchRoots.joinToString(", ") { relativeToProject(project, it) }}"
        }

        if (toolsToReport.isEmpty()) {
            lines += ""
            lines += "No project-local CLI wrappers were detected."
            lines += "If this project uses custom wrappers, update `MCP CLI wrapper candidates` in Magento settings."
            return lines.joinToString("\n")
        }

        lines += ""
        lines += if (detectedTools.isEmpty()) {
            "Configured wrappers:"
        } else {
            "Detected wrappers:"
        }
        for (tool in toolsToReport.take(MagentoMcpSupport.MAX_MATCHES)) {
            lines += tool.command
            lines += "  type: ${tool.kind.description}"
            lines += "  use: ${tool.kind.usage}"
            lines += "  example: ${exampleCommand(tool)}"
        }

        if (detectedTools.isEmpty()) {
            lines += "  note: existence could not be verified through the current project index; these commands come from the configured wrapper candidates."
        }

        lines += ""
        lines += "Agent guidance:"
        lines += "1. Call this tool before running shell commands that normally use Magento or n98-magerun."
        lines += "2. Use the returned wrapper path exactly instead of a global binary or `php bin/...` fallback."
        lines += "3. Mark Shust Docker projects usually route these wrappers into containers, so the wrapper is the correct entrypoint."

        return lines.joinToString("\n")
    }

    private fun discoverTools(
        project: Project,
        searchRoots: List<VirtualFile>,
        configuredCandidates: List<String>
    ): List<DetectedTool> {
        val detected = LinkedHashMap<String, DetectedTool>()
        val projectFiles = collectProjectFiles(project)

        for (root in searchRoots) {
            for (candidate in configuredCandidates) {
                val expectedRelativePath = normalizePath(
                    expectedProjectRelativePath(project, root, candidate)
                )
                val candidateFile = projectFiles.firstOrNull { file ->
                    normalizePath(MagentoMcpSupport.relativePath(project, file)) == expectedRelativePath
                }
                if (candidateFile != null && !candidateFile.isDirectory) {
                    val relativePath = MagentoMcpSupport.relativePath(project, candidateFile)
                    detected.putIfAbsent(
                        relativePath,
                        buildDetectedTool(relativePath, candidateFile.name)
                    )
                }
            }

            val rootPrefix = normalizePath(relativeToProject(project, root))
            projectFiles
                .asSequence()
                .filter { file -> isDirectBinChild(project, file, rootPrefix) }
                .sortedWith(
                    compareBy<VirtualFile> { toolSortOrder(it.name, configuredCandidates) }
                        .thenBy { it.name.lowercase() }
                )
                .forEach { file ->
                    val relativePath = MagentoMcpSupport.relativePath(project, file)
                    detected.putIfAbsent(
                        relativePath,
                        buildDetectedTool(relativePath, file.name)
                    )
                }
        }

        return detected.values.toList()
    }

    private fun resolveSearchRoots(project: Project): List<VirtualFile> {
        val roots = LinkedHashSet<VirtualFile>()
        val projectRoot = project.baseDir ?: project.projectFile?.parent

        if (projectRoot != null) {
            roots.add(projectRoot)
        }

        val configuredMagentoPath = Settings.getMagentoPath(project)?.trim().orEmpty()
        if (configuredMagentoPath.isEmpty()) {
            return roots.toList()
        }

        val configuredLocalRoot = LocalFileSystem.getInstance().findFileByPath(configuredMagentoPath)
        if (configuredLocalRoot != null && configuredLocalRoot.isDirectory) {
            roots.add(configuredLocalRoot)
            return roots.toList()
        }

        if (projectRoot == null) {
            return roots.toList()
        }

        val normalizedConfiguredPath = normalizePath(configuredMagentoPath)
        val normalizedProjectBasePath = normalizePath(project.basePath)

        if (normalizedConfiguredPath == normalizedProjectBasePath) {
            return roots.toList()
        }

        val relativeCandidate = configuredMagentoPath
            .replace('\\', '/')
            .removePrefix("/")
            .removePrefix("./")
            .trim('/')
        if (relativeCandidate.isEmpty()) {
            return roots.toList()
        }

        val nestedRoot = findRelativeFile(projectRoot, relativeCandidate)
        if (nestedRoot != null && nestedRoot.isDirectory) {
            roots.add(nestedRoot)
        }

        return roots.toList()
    }

    private fun buildDetectedTool(relativePath: String, fileName: String): DetectedTool {
        val normalizedName = fileName.lowercase()
        val toolKind = knownToolTypes[normalizedName] ?: ToolKind(
            description = "project wrapper",
            usage = "Use this wrapper instead of a global command when the project expects it.",
            exampleSuffix = "--help"
        )

        return DetectedTool(
            command = "./$relativePath",
            kind = toolKind
        )
    }

    private fun exampleCommand(tool: DetectedTool): String {
        return if (tool.kind.exampleSuffix.isEmpty()) {
            tool.command
        } else {
            "${tool.command} ${tool.kind.exampleSuffix}"
        }
    }

    private fun relativeToProject(project: Project, root: VirtualFile): String {
        val relativePath = MagentoMcpSupport.relativePath(project, root)
        return if (relativePath.isEmpty()) "." else relativePath
    }

    private fun toolSortOrder(fileName: String, configuredCandidates: List<String>): Int {
        val configuredIndex = configuredCandidates.indexOfFirst { candidate ->
            candidate.substringAfterLast('/').equals(fileName, ignoreCase = true)
        }
        if (configuredIndex >= 0) {
            return configuredIndex
        }

        val knownIndex = knownToolTypes.keys.indexOfFirst { it.equals(fileName, ignoreCase = true) }
        return if (knownIndex >= 0) {
            configuredCandidates.size + knownIndex
        } else {
            configuredCandidates.size + knownToolTypes.size + 1
        }
    }

    private fun normalizePath(value: String?): String {
        return value?.trim()?.replace('\\', '/')?.trimEnd('/') ?: ""
    }

    private fun expectedProjectRelativePath(
        project: Project,
        searchRoot: VirtualFile,
        candidate: String
    ): String {
        val normalizedCandidate = normalizePath(candidate)
        val rootRelativePath = normalizePath(relativeToProject(project, searchRoot))
        return if (rootRelativePath.isEmpty() || rootRelativePath == ".") {
            normalizedCandidate
        } else {
            "$rootRelativePath/$normalizedCandidate"
        }
    }

    private fun buildConfiguredFallbackTools(
        project: Project,
        searchRoots: List<VirtualFile>,
        configuredCandidates: List<String>
    ): List<DetectedTool> {
        val primaryRoot = searchRoots.firstOrNull()

        return configuredCandidates.map { candidate ->
            val relativePath = if (primaryRoot == null) {
                normalizePath(candidate)
            } else {
                expectedProjectRelativePath(project, primaryRoot, candidate)
            }
            buildDetectedTool(relativePath, candidate.substringAfterLast('/'))
        }
    }

    private fun collectProjectFiles(project: Project): List<VirtualFile> {
        val files = ArrayList<VirtualFile>()
        ProjectRootManager.getInstance(project).fileIndex.iterateContent { file ->
            if (!file.isDirectory) {
                files += file
            }
            true
        }
        return files
    }

    private fun isDirectBinChild(project: Project, file: VirtualFile, rootPrefix: String): Boolean {
        val relativePath = normalizePath(MagentoMcpSupport.relativePath(project, file))
        val binPrefix = if (rootPrefix.isEmpty() || rootPrefix == ".") {
            "bin/"
        } else {
            "$rootPrefix/bin/"
        }

        if (!relativePath.startsWith(binPrefix)) {
            return false
        }

        return !relativePath.removePrefix(binPrefix).contains('/')
    }

    private fun findRelativeFile(root: VirtualFile, relativePath: String): VirtualFile? {
        var current: VirtualFile = root
        val segments = relativePath
            .replace('\\', '/')
            .split('/')
            .filter { it.isNotBlank() && it != "." }

        for (segment in segments) {
            current = current.findChild(segment) ?: return null
        }

        return current
    }

    private data class DetectedTool(
        val command: String,
        val kind: ToolKind
    )

    private data class ToolKind(
        val description: String,
        val usage: String,
        val exampleSuffix: String
    )
}
