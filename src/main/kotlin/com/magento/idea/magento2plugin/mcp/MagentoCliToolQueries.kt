/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.magento.idea.magento2plugin.project.Settings

internal object MagentoCliToolQueries {
    private const val MAX_CLI_TOOLS_TO_REPORT = 100

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
        ),
        "start" to ToolKind(
            description = "environment wrapper",
            usage = "Use this to start the local project environment or stack when needed.",
            exampleSuffix = ""
        ),
        "stop" to ToolKind(
            description = "environment wrapper",
            usage = "Use this to stop the local project environment or stack when needed.",
            exampleSuffix = ""
        ),
        "up" to ToolKind(
            description = "environment wrapper",
            usage = "Use this to bring the local project environment up when needed.",
            exampleSuffix = ""
        ),
        "down" to ToolKind(
            description = "environment wrapper",
            usage = "Use this to bring the local project environment down when needed.",
            exampleSuffix = ""
        ),
        "status" to ToolKind(
            description = "environment wrapper",
            usage = "Use this to inspect local project environment status when needed.",
            exampleSuffix = ""
        )
    )

    fun describeCliEnvironment(project: Project): String {
        val configuredCandidates = Settings.getMcpCliToolCandidates(project)
        val configuredMagentoRootRelative = resolveConfiguredMagentoRootRelativePath(project)
        val searchRoots = buildSearchRoots(configuredMagentoRootRelative)
        val detectedTools = discoverTools(project, searchRoots, configuredCandidates)
        val fallbackTools = buildConfiguredFallbackTools(project, searchRoots, configuredCandidates)
        val toolsToReport = if (detectedTools.isEmpty()) fallbackTools else detectedTools
        val wrappersOutsideMagentoRoot = if (configuredMagentoRootRelative == null) {
            emptyList()
        } else {
            toolsToReport.filter { tool ->
                !isUnderRoot(tool.command.removePrefix("./"), configuredMagentoRootRelative)
            }
        }
        val wrapperCommandsOutsideMagentoRoot = wrappersOutsideMagentoRoot.map { it.command }.toSet()
        val lines = mutableListOf<String>()

        lines += "Magento CLI environment"
        lines += "Prefer project-local wrappers over global binaries for Magento, Docker, PHP, Composer, n98-magerun, and stack lifecycle commands."
        lines += "Configured wrapper candidates: ${configuredCandidates.joinToString(", ")}"

        if (searchRoots.isNotEmpty()) {
            lines += "Search roots: ${searchRoots.joinToString(", ")}"
        }
        if (configuredMagentoRootRelative != null) {
            lines += "Configured Magento root: ./$configuredMagentoRootRelative"
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
        for (tool in toolsToReport.take(MAX_CLI_TOOLS_TO_REPORT)) {
            lines += tool.command
            lines += "  type: ${tool.kind.description}"
            lines += "  use: ${tool.kind.usage}"
            if (configuredMagentoRootRelative != null && tool.command in wrapperCommandsOutsideMagentoRoot) {
                lines += "  location: outside configured Magento root `./$configuredMagentoRootRelative`"
            }
            lines += "  example: ${exampleCommand(tool)}"
        }

        if (detectedTools.isEmpty()) {
            lines += "  note: existence could not be verified through the current project index; these commands come from the configured wrapper candidates."
        }

        lines += ""
        lines += "Agent guidance:"
        val guidance = mutableListOf<String>()
        guidance += "Call this tool before running shell commands that normally use Magento, n98-magerun, or project environment wrappers such as `./bin/start`, `./bin/stop`, or `./bin/restart`."
        guidance += "Use the returned wrapper path exactly instead of a global binary or `php bin/...` fallback."
        if (configuredMagentoRootRelative != null) {
            guidance += "Create and edit Magento files under `./$configuredMagentoRootRelative`; that is the configured Magento root."
        }
        if (configuredMagentoRootRelative != null && wrappersOutsideMagentoRoot.isNotEmpty()) {
            guidance += if (detectedTools.isEmpty()) {
                "If these configured wrapper paths exist outside `./$configuredMagentoRootRelative`, that is valid for a nested Magento root. Run the wrapper from the returned project-relative path and do not rewrite it under the Magento root."
            } else {
                "If a detected wrapper is outside `./$configuredMagentoRootRelative`, that is valid for a nested Magento root. Run the wrapper from the returned project-relative path and do not rewrite it under the Magento root."
            }
        }
        guidance += "Mark Shust Docker projects usually route these wrappers into containers, so the wrapper is the correct entrypoint."
        guidance.forEachIndexed { index, entry ->
            lines += "${index + 1}. $entry"
        }

        return lines.joinToString("\n")
    }

    private fun discoverTools(
        project: Project,
        searchRoots: List<String>,
        configuredCandidates: List<String>
    ): List<DetectedTool> {
        val detected = LinkedHashMap<String, DetectedTool>()
        val projectFiles = collectProjectFiles(project)

        for (root in searchRoots) {
            for (candidate in configuredCandidates) {
                val expectedRelativePath = normalizePath(
                    expectedProjectRelativePath(root, candidate)
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

            projectFiles
                .asSequence()
                .filter { file -> isDirectBinChild(project, file, root) }
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

    private fun resolveConfiguredMagentoRootRelativePath(project: Project): String? {
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

        val projectRootName = (project.baseDir ?: project.projectFile?.parent)?.name
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

    private fun buildSearchRoots(configuredMagentoRootRelative: String?): List<String> {
        val roots = linkedSetOf(".")
        if (!configuredMagentoRootRelative.isNullOrEmpty()) {
            roots += configuredMagentoRootRelative
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

    private fun expectedProjectRelativePath(searchRoot: String, candidate: String): String {
        val normalizedCandidate = normalizePath(candidate)
        val rootRelativePath = normalizePath(searchRoot)
        return if (rootRelativePath.isEmpty() || rootRelativePath == ".") {
            normalizedCandidate
        } else {
            "$rootRelativePath/$normalizedCandidate"
        }
    }

    private fun buildConfiguredFallbackTools(
        project: Project,
        searchRoots: List<String>,
        configuredCandidates: List<String>
    ): List<DetectedTool> {
        val primaryRoot = searchRoots.firstOrNull()

        return configuredCandidates.map { candidate ->
            val relativePath = if (primaryRoot == null) {
                normalizePath(candidate)
            } else {
                expectedProjectRelativePath(primaryRoot, candidate)
            }
            buildDetectedTool(relativePath, candidate.substringAfterLast('/'))
        }
    }

    private fun collectProjectFiles(project: Project): List<VirtualFile> {
        val projectRoot = project.baseDir ?: project.projectFile?.parent ?: return emptyList()
        val files = ArrayList<VirtualFile>()
        val pending = ArrayDeque<VirtualFile>()
        pending += projectRoot

        while (pending.isNotEmpty()) {
            val file = pending.removeLast()
            if (file.isDirectory) {
                file.children.forEach { child -> pending += child }
            } else {
                files += file
            }
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

    private fun isUnderRoot(relativePath: String, rootRelativePath: String): Boolean {
        val normalizedPath = normalizePath(relativePath)
        val normalizedRoot = normalizePath(rootRelativePath)
        return normalizedPath == normalizedRoot || normalizedPath.startsWith("$normalizedRoot/")
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
