/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project
import com.magento.idea.magento2plugin.project.Settings
import java.io.IOException
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path

internal object MagentoCliToolQueries {
    private const val MAX_CLI_TOOLS_TO_REPORT = 100
    private val DOCKER_COMPOSE_FILES = listOf(
        "compose.yaml", "compose.yml", "docker-compose.yaml", "docker-compose.yml"
    )
    private val N98_MAGERUN_TOOL_KIND = ToolKind(
        description = "n98-magerun2 candidate (version unverified)",
        usage = "Use magento_magerun status, then commands or command_help to inspect this project's installed tool.",
        exampleSuffix = null
    )
    private val LEGACY_MAGERUN_TOOL_KIND = ToolKind(
        description = "magerun candidate (may be Magento 1; version unverified)",
        usage = "Verify with magento_magerun before treating this alias as n98-magerun2.",
        exampleSuffix = null
    )
    private val GRUNT_TOOL_KIND = ToolKind(
        description = "frontend build wrapper",
        usage = "Use this to compile frontend styles and assets in the project runtime.",
        exampleSuffix = "less:THEMENAME"
    )

    private val knownToolTypes = linkedMapOf(
        "magento" to ToolKind(
            description = "Magento CLI wrapper",
            usage = "Use this for Magento CLI commands in the project runtime.",
            exampleSuffix = "cache:flush"
        ),
        "n98-magerun2" to N98_MAGERUN_TOOL_KIND,
        "n98-magerun2.phar" to N98_MAGERUN_TOOL_KIND,
        "magerun2" to N98_MAGERUN_TOOL_KIND,
        "magerun2.phar" to N98_MAGERUN_TOOL_KIND,
        "n98-magerun" to LEGACY_MAGERUN_TOOL_KIND,
        "n98-magerun.phar" to LEGACY_MAGERUN_TOOL_KIND,
        "magerun" to LEGACY_MAGERUN_TOOL_KIND,
        "composer" to ToolKind(
            description = "Composer wrapper",
            usage = "Use this for Composer commands in the project runtime.",
            exampleSuffix = "install"
        ),
        "grunt" to GRUNT_TOOL_KIND,
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
        val projectRoot = projectRootPath(project)
            ?: return "The project directory is unavailable."
        val configuredMagentoRootRelative = resolveConfiguredMagentoRootRelativePath(project)
        val searchRoots = buildSearchRoots(projectRoot, configuredMagentoRootRelative)
        val detectedTools = discoverTools(projectRoot, searchRoots, configuredCandidates)
        val preferredTools = detectedTools.groupBy { it.selectionKey }.mapValues { (_, tools) -> tools.first() }
        val wrappersOutsideMagentoRoot = if (configuredMagentoRootRelative == null) {
            emptyList()
        } else {
            detectedTools.filter { tool ->
                !isUnderRoot(tool.command.removePrefix("./"), configuredMagentoRootRelative)
            }
        }
        val wrapperCommandsOutsideMagentoRoot = wrappersOutsideMagentoRoot.map { it.command }.toSet()
        val lines = mutableListOf<String>()
        val hasN98MagerunWrapper = detectedTools.any { MagentoMagerunSupport.isMagerunName(it.name) }
        val hasGruntWrapper = detectedTools.any { it.kind == GRUNT_TOOL_KIND }

        lines += "Magento CLI environment"
        lines += "Prefer project-local wrappers over global binaries for Magento, Docker, PHP, Composer, n98-magerun, and stack lifecycle commands."
        lines += "Configured wrapper candidates: ${configuredCandidates.joinToString(", ")}"

        if (searchRoots.isNotEmpty()) {
            lines += "Search roots: ${searchRoots.joinToString(", ")}"
            lines += "Preferred CLI directory: ${resolveProjectPath(projectRoot, searchRoots.first())}"
            lines += "Selection: prefer scripts beside the nearest Docker Compose configuration within the project; use Magento-root scripts as fallbacks."
        }
        if (configuredMagentoRootRelative != null) {
            lines += "Configured Magento root: ./$configuredMagentoRootRelative"
        }

        val ddev = MagentoDdevEnvironment.discover(projectRoot,
            resolveProjectPath(projectRoot, configuredMagentoRootRelative ?: "."))
        if (ddev != null) {
            lines += ""
            lines += "DDEV configuration: ${ddev.config}"
            lines += ddev.json().toString(2)
            if (ddev.configuration.error == null) {
                lines += "  example: ${ddev.example(listOf("php", "-v"))}"
                lines += "Use this DDEV execution prefix for commands that belong in the container. Existing project wrappers retain their priority. Discovery does not start DDEV or run a process."
                lines += "Optional Magerun: use magento_magerun to verify installation and inspect commands in this runtime."
            }
        }

        if (detectedTools.isEmpty()) {
            lines += ""
            lines += "No project-local CLI wrappers were detected."
            lines += "If this project uses custom wrappers, update `MCP CLI wrapper candidates` in Magento settings."
            return lines.joinToString("\n")
        }

        lines += ""
        lines += "Detected wrappers:"
        for (tool in detectedTools.take(MAX_CLI_TOOLS_TO_REPORT)) {
            lines += tool.command
            lines += "  type: ${tool.kind.description}"
            val preferredTool = preferredTools.getValue(tool.selectionKey)
            lines += if (preferredTool == tool) {
                "  selection: preferred"
            } else {
                "  selection: fallback for ${preferredTool.command}; use only when the preferred script is unavailable"
            }
            lines += "  working directory: ${resolveProjectPath(projectRoot, tool.workingDirectory)}"
            lines += "  use: ${tool.kind.usage}"
            if (configuredMagentoRootRelative != null && tool.command in wrapperCommandsOutsideMagentoRoot) {
                lines += "  location: outside configured Magento root `./$configuredMagentoRootRelative`"
            }
            exampleCommand(tool)?.let { lines += "  example: $it" }
        }
        if (detectedTools.size > MAX_CLI_TOOLS_TO_REPORT) {
            lines += "${detectedTools.size - MAX_CLI_TOOLS_TO_REPORT} additional scripts omitted."
        }

        if (hasN98MagerunWrapper) {
            lines += ""
            lines += "n98-magerun capabilities are not probed during environment discovery."
            lines += "Use magento_magerun mode=status for detection, commands for the installed command catalogue, command_help for arguments/options, or mcp_config for native MCP settings."
        }

        lines += ""
        lines += "Agent guidance:"
        val guidance = mutableListOf<String>()
        guidance += "Call this tool before running shell commands that normally use Magento, n98-magerun, or project environment wrappers such as `./bin/start`, `./bin/stop`, or `./bin/restart`."
        guidance += "Use the preferred script's example from its working directory instead of a global binary or `php bin/...` fallback."
        guidance += "Choose the preferred script for each command. Run its example from the reported working directory; fallback scripts may use a different runtime. Do not switch to a fallback merely because a command fails."
        if (configuredMagentoRootRelative != null) {
            guidance += "Create and edit Magento files under `./$configuredMagentoRootRelative`; that is the configured Magento root."
        }
        if (configuredMagentoRootRelative != null && wrappersOutsideMagentoRoot.isNotEmpty()) {
            guidance += "If a detected wrapper is outside `./$configuredMagentoRootRelative`, that is valid for a nested Magento root. Keep its reported working directory and invocation; do not rewrite it under the Magento root."
        }
        if (hasN98MagerunWrapper) {
            guidance += "Use magento_magerun for bounded metadata probes through the selected runtime; it verifies n98-magerun2 and does not run install, update, or application commands."
        }
        if (hasGruntWrapper) {
            guidance += "After editing styles, run the detected grunt wrapper right away to rebuild theme assets, usually `./bin/grunt exec:THEMENAME` and `./bin/grunt less:THEMENAME`."
        }
        guidance += "Mark Shust Docker projects usually route these wrappers into containers, so the wrapper is the correct entrypoint."
        guidance.forEachIndexed { index, entry ->
            lines += "${index + 1}. $entry"
        }

        return lines.joinToString("\n")
    }

    internal fun discoverTools(
        projectRoot: Path,
        searchRoots: List<String>,
        configuredCandidates: List<String>
    ): List<DetectedTool> {
        val detected = LinkedHashMap<String, DetectedTool>()
        val candidates = (configuredCandidates + MagentoMagerunSupport.DEFAULT_CANDIDATES).distinct()

        for ((rootOrder, root) in searchRoots.withIndex()) {
            val workingDirectory = resolveProjectPath(projectRoot, root) ?: continue
            for (candidate in candidates) {
                val expectedRelativePath = normalizePath(
                    expectedProjectRelativePath(root, candidate)
                )
                val candidatePath = resolveProjectPath(projectRoot, expectedRelativePath)
                if (candidatePath != null && candidatePath.startsWith(workingDirectory)
                    && isCliFile(candidatePath)) {
                    val owner = searchRoots.lastOrNull { searchRoot ->
                        resolveProjectPath(projectRoot, searchRoot)?.let { candidatePath.startsWith(it) } == true
                    }
                    if (owner != root) {
                        continue
                    }
                    val relativePath = relativeProjectPath(projectRoot, candidatePath)
                    detected.putIfAbsent(
                        relativePath,
                        buildDetectedTool(relativePath, candidatePath, root, workingDirectory, rootOrder)
                    )
                }
            }

            val binDirectory = resolveProjectPath(
                projectRoot,
                expectedProjectRelativePath(root, "bin")
            )
            if (binDirectory != null && Files.isDirectory(binDirectory)) {
                Files.list(binDirectory).use { children ->
                    children
                        .filter { !it.fileName.toString().startsWith(".") }
                        .filter { isCliFile(it) }
                        .filter { resolveProjectPath(projectRoot, it.toString()) != null }
                        .forEach { file ->
                            val relativePath = relativeProjectPath(projectRoot, file)
                            detected.putIfAbsent(
                                relativePath,
                                buildDetectedTool(relativePath, file, root, workingDirectory, rootOrder)
                            )
                        }
                }
            }
        }

        return detected.values.sortedWith(
            compareBy<DetectedTool> { toolSortOrder(it.name, configuredCandidates) }
                .thenBy { it.rootOrder }
                .thenBy { if (MagentoMagerunSupport.isMagerunName(it.name)) MagentoMagerunSupport.candidatePriority(it.command, configuredCandidates) else 0 }
                .thenBy { it.name.lowercase() }
                .thenBy { it.command }
        )
    }

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

    internal fun buildSearchRoots(projectRoot: Path, configuredMagentoRootRelative: String?): List<String> {
        val normalizedProjectRoot = projectRoot.toAbsolutePath().normalize()
        val magentoRoot = resolveProjectPath(normalizedProjectRoot, configuredMagentoRootRelative ?: ".")
            ?: return emptyList()
        val roots = linkedSetOf<String>()
        var directory = magentoRoot
        while (directory.startsWith(normalizedProjectRoot)) {
            if (DOCKER_COMPOSE_FILES.any { name ->
                    resolveProjectPath(normalizedProjectRoot, directory.resolve(name).toString())
                        ?.let { Files.isRegularFile(it) } == true
                }) {
                roots += relativeProjectPath(normalizedProjectRoot, directory).ifEmpty { "." }
                break
            }
            if (directory == normalizedProjectRoot) {
                break
            }
            directory = directory.parent ?: break
        }
        roots += relativeProjectPath(normalizedProjectRoot, magentoRoot).ifEmpty { "." }
        return roots.toList()
    }

    private fun buildDetectedTool(
        relativePath: String,
        path: Path,
        root: String,
        workingDirectory: Path,
        rootOrder: Int
    ): DetectedTool {
        val fileName = path.fileName.toString()
        val normalizedName = fileName.lowercase()
        val toolKind = knownToolTypes[normalizedName] ?: ToolKind(
            description = "project wrapper",
            usage = "Use this wrapper instead of a global command when the project expects it.",
            exampleSuffix = null
        )

        return DetectedTool(
            command = "./$relativePath",
            kind = toolKind,
            name = fileName,
            workingDirectory = root,
            invocation = "./${relativeProjectPath(workingDirectory, path)}",
            rootOrder = rootOrder
        )
    }

    private fun exampleCommand(tool: DetectedTool): String? {
        val suffix = tool.kind.exampleSuffix ?: return null
        val command = shellQuote(tool.invocation)
        return if (suffix.isEmpty()) {
            command
        } else {
            "$command $suffix"
        }
    }

    internal fun shellQuote(value: String): String =
        if (value.matches(Regex("[a-zA-Z0-9_./=:-]+"))) value else "'${value.replace("'", "'\"'\"'")}'"

    private fun isCliFile(path: Path): Boolean = Files.isRegularFile(path) && Files.isReadable(path)
        && (Files.isExecutable(path) || MagentoMagerunSupport.isMagerunName(path.fileName.toString()))

    private fun toolSortOrder(fileName: String, configuredCandidates: List<String>): Int {
        if (MagentoMagerunSupport.isMagerunName(fileName)) {
            val configuredIndex = configuredCandidates.indexOfFirst {
                MagentoMagerunSupport.isMagerunName(it.substringAfterLast('/'))
            }
            return if (configuredIndex >= 0) configuredIndex else configuredCandidates.size + 1
        }
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
        return if (rootRelativePath.isEmpty() || rootRelativePath == "."
            || normalizedCandidate.startsWith("$rootRelativePath/")
            || runCatching { Path.of(normalizedCandidate).isAbsolute }.getOrDefault(false)) {
            normalizedCandidate
        } else {
            "$rootRelativePath/$normalizedCandidate"
        }
    }

    private fun isUnderRoot(relativePath: String, rootRelativePath: String): Boolean {
        val normalizedPath = normalizePath(relativePath)
        val normalizedRoot = normalizePath(rootRelativePath)
        return normalizedPath == normalizedRoot || normalizedPath.startsWith("$normalizedRoot/")
    }

    internal fun projectRootPath(project: Project): Path? {
        val projectBasePath = project.basePath ?: project.projectFile?.parent?.path ?: return null
        return Path.of(projectBasePath).toAbsolutePath().normalize()
    }

    internal fun resolveProjectPath(projectRoot: Path, relativePath: String): Path? {
        val normalizedRelativePath = normalizePath(relativePath)
        if (normalizedRelativePath.isEmpty()) {
            return projectRoot
        }

        return try {
            val resolvedPath = projectRoot.resolve(normalizedRelativePath).toAbsolutePath().normalize()
            if (!resolvedPath.startsWith(projectRoot)) {
                null
            } else if (Files.exists(resolvedPath)
                && !resolvedPath.toRealPath().startsWith(projectRoot.toRealPath())) {
                null
            } else {
                resolvedPath
            }
        } catch (_: IOException) {
            null
        } catch (_: InvalidPathException) {
            null
        }
    }

    private fun relativeProjectPath(projectRoot: Path, path: Path): String {
        return projectRoot.relativize(path.toAbsolutePath().normalize()).toString().replace('\\', '/')
    }

    internal data class DetectedTool(
        val command: String,
        val kind: ToolKind,
        val name: String,
        val workingDirectory: String,
        val invocation: String,
        val rootOrder: Int
    ) {
        val selectionKey: String get() = if (MagentoMagerunSupport.isMagerunName(name)) "magerun_candidate" else name
    }

    internal data class ToolKind(
        val description: String,
        val usage: String,
        val exampleSuffix: String?
    )
}
