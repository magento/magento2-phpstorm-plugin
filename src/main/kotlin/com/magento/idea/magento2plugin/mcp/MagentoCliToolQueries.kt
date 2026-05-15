/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project
import com.magento.idea.magento2plugin.project.Settings
import java.nio.file.Files
import java.nio.file.Path

internal object MagentoCliToolQueries {
    private const val MAX_CLI_TOOLS_TO_REPORT = 100
    private val N98_MAGERUN_CAPABILITY_GROUPS = listOf(
        CapabilityGroup(
            name = "admin",
            description = "Commands for managing Magento admin user accounts and related settings.",
            exampleCommands = listOf(
                "admin:user:list",
                "admin:user:create",
                "admin:user:change-password",
                "admin:notifications"
            )
        ),
        CapabilityGroup(
            name = "cache",
            description = "Commands for interacting with and managing Magento's various cache systems.",
            exampleCommands = listOf(
                "cache:clean",
                "cache:disable",
                "cache:enable",
                "cache:flush",
                "cache:list"
            )
        ),
        CapabilityGroup(
            name = "config",
            description = "Commands for managing Magento store configurations and environment settings.",
            exampleCommands = listOf(
                "config:store:get",
                "config:store:set",
                "config:env:set",
                "config:search"
            )
        ),
        CapabilityGroup(
            name = "composer",
            description = "Commands for managing Composer-related tasks and package deployment.",
            exampleCommands = listOf("composer:redeploy-base-packages")
        ),
        CapabilityGroup(
            name = "customer",
            description = "Commands for managing Magento customer accounts.",
            exampleCommands = listOf(
                "customer:create",
                "customer:list",
                "customer:info",
                "customer:change-password"
            )
        ),
        CapabilityGroup(
            name = "db",
            description = "Commands for database operations such as dumps, imports, and queries.",
            exampleCommands = listOf(
                "db:dump",
                "db:import",
                "db:query",
                "db:create",
                "db:info"
            )
        ),
        CapabilityGroup(
            name = "dev",
            description = "Commands tailored for Magento developers, including code generation and debugging tools.",
            exampleCommands = listOf(
                "dev:module:create",
                "dev:console",
                "dev:translate:admin",
                "dev:theme:list"
            )
        ),
        CapabilityGroup(
            name = "eav",
            description = "Commands for managing EAV (Entity-Attribute-Value) attributes.",
            exampleCommands = listOf(
                "eav:attribute:list",
                "eav:attribute:view",
                "eav:attribute:remove"
            )
        ),
        CapabilityGroup(
            name = "giftcard",
            description = "Commands for managing Magento gift cards.",
            exampleCommands = listOf(
                "giftcard:pool:generate",
                "giftcard:create",
                "giftcard:info",
                "giftcard:remove"
            )
        ),
        CapabilityGroup(
            name = "generation",
            description = "Commands related to Magento's code generation processes.",
            exampleCommands = listOf("generation:flush")
        ),
        CapabilityGroup(
            name = "index",
            description = "Commands for managing Magento's indexers.",
            exampleCommands = listOf(
                "index:list",
                "index:trigger:recreate"
            )
        ),
        CapabilityGroup(
            name = "install",
            description = "Command for installing Magento.",
            exampleCommands = listOf("installer")
        ),
        CapabilityGroup(
            name = "integration",
            description = "Command for integrations to Magento.",
            exampleCommands = listOf(
                "integration:list",
                "integration:show",
                "integration:delete"
            )
        ),
        CapabilityGroup(
            name = "magerun",
            description = "Commands for working with n98-magerun2 config and internal tools.",
            exampleCommands = listOf(
                "magerun:config:info",
                "magerun:config:dump"
            )
        ),
        CapabilityGroup(
            name = "routes",
            description = "Commands for managing and viewing Magento routes.",
            exampleCommands = listOf("routes:list")
        ),
        CapabilityGroup(
            name = "script",
            description = "Command for running sequences of n98-magerun2 commands from a file.",
            exampleCommands = listOf("script")
        ),
        CapabilityGroup(
            name = "sys",
            description = "Commands for system-level information, checks, and maintenance tasks.",
            exampleCommands = listOf(
                "sys:info",
                "sys:check",
                "sys:maintenance",
                "sys:cron:list",
                "sys:store:list"
            )
        )
    )
    private val N98_MAGERUN_TOOL_KIND = ToolKind(
        description = "n98-magerun wrapper",
        usage = "Use this for n98-magerun commands in the project runtime.",
        exampleSuffix = "sys:info",
        capabilityGroups = N98_MAGERUN_CAPABILITY_GROUPS
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
        "n98-magerun" to N98_MAGERUN_TOOL_KIND,
        "magerun" to N98_MAGERUN_TOOL_KIND,
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
        val configuredMagentoRootRelative = resolveConfiguredMagentoRootRelativePath(project)
        val searchRoots = buildSearchRoots(configuredMagentoRootRelative)
        val detectedTools = discoverTools(project, searchRoots, configuredCandidates)
        val wrappersOutsideMagentoRoot = if (configuredMagentoRootRelative == null) {
            emptyList()
        } else {
            detectedTools.filter { tool ->
                !isUnderRoot(tool.command.removePrefix("./"), configuredMagentoRootRelative)
            }
        }
        val wrapperCommandsOutsideMagentoRoot = wrappersOutsideMagentoRoot.map { it.command }.toSet()
        val lines = mutableListOf<String>()
        val hasN98MagerunWrapper = detectedTools.any { it.kind.capabilityGroups.isNotEmpty() }
        val hasGruntWrapper = detectedTools.any { it.kind == GRUNT_TOOL_KIND }

        lines += "Magento CLI environment"
        lines += "Prefer project-local wrappers over global binaries for Magento, Docker, PHP, Composer, n98-magerun, and stack lifecycle commands."
        lines += "Configured wrapper candidates: ${configuredCandidates.joinToString(", ")}"

        if (searchRoots.isNotEmpty()) {
            lines += "Search roots: ${searchRoots.joinToString(", ")}"
        }
        if (configuredMagentoRootRelative != null) {
            lines += "Configured Magento root: ./$configuredMagentoRootRelative"
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
            lines += "  use: ${tool.kind.usage}"
            if (configuredMagentoRootRelative != null && tool.command in wrapperCommandsOutsideMagentoRoot) {
                lines += "  location: outside configured Magento root `./$configuredMagentoRootRelative`"
            }
            lines += "  example: ${exampleCommand(tool)}"
        }

        if (hasN98MagerunWrapper) {
            lines += ""
            lines += "Known n98-magerun capability groups:"
            for (group in N98_MAGERUN_CAPABILITY_GROUPS) {
                lines += "${group.name}: ${group.description}"
                lines += "  examples: ${group.exampleCommands.joinToString(", ")}"
            }
            lines += "This is a built-in capability snapshot. Use the detected n98 wrapper with `list` or `--help` to confirm the exact command surface and any project-specific custom commands."
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
            guidance += "If a detected wrapper is outside `./$configuredMagentoRootRelative`, that is valid for a nested Magento root. Run the wrapper from the returned project-relative path and do not rewrite it under the Magento root."
        }
        if (hasN98MagerunWrapper) {
            guidance += "For exact n98 command discovery, follow up with the detected wrapper and `list` or a targeted `--help` call such as `sys:info --help`."
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

    private fun discoverTools(
        project: Project,
        searchRoots: List<String>,
        configuredCandidates: List<String>
    ): List<DetectedTool> {
        val detected = LinkedHashMap<String, DetectedTool>()
        val projectRoot = projectRootPath(project) ?: return emptyList()

        for (root in searchRoots) {
            for (candidate in configuredCandidates) {
                val expectedRelativePath = normalizePath(
                    expectedProjectRelativePath(root, candidate)
                )
                val candidatePath = resolveProjectPath(projectRoot, expectedRelativePath)
                if (candidatePath != null && Files.exists(candidatePath) && !Files.isDirectory(candidatePath)) {
                    val relativePath = relativeProjectPath(projectRoot, candidatePath)
                    detected.putIfAbsent(
                        relativePath,
                        buildDetectedTool(relativePath, candidatePath.fileName.toString())
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
                        .filter { !Files.isDirectory(it) }
                        .sorted(
                            compareBy<Path> { toolSortOrder(it.fileName.toString(), configuredCandidates) }
                                .thenBy { it.fileName.toString().lowercase() }
                        )
                        .forEach { file ->
                            val relativePath = relativeProjectPath(projectRoot, file)
                            detected.putIfAbsent(
                                relativePath,
                                buildDetectedTool(relativePath, file.fileName.toString())
                            )
                        }
                }
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

    private fun isUnderRoot(relativePath: String, rootRelativePath: String): Boolean {
        val normalizedPath = normalizePath(relativePath)
        val normalizedRoot = normalizePath(rootRelativePath)
        return normalizedPath == normalizedRoot || normalizedPath.startsWith("$normalizedRoot/")
    }

    private fun projectRootPath(project: Project): Path? {
        val projectBasePath = project.basePath ?: project.baseDir?.path ?: return null
        return Path.of(projectBasePath).toAbsolutePath().normalize()
    }

    private fun resolveProjectPath(projectRoot: Path, relativePath: String): Path? {
        val normalizedRelativePath = normalizePath(relativePath)
        if (normalizedRelativePath.isEmpty()) {
            return projectRoot
        }

        val resolvedPath = projectRoot.resolve(normalizedRelativePath).toAbsolutePath().normalize()
        return if (resolvedPath.startsWith(projectRoot)) {
            resolvedPath
        } else {
            null
        }
    }

    private fun relativeProjectPath(projectRoot: Path, path: Path): String {
        return projectRoot.relativize(path.toAbsolutePath().normalize()).toString().replace('\\', '/')
    }

    private data class DetectedTool(
        val command: String,
        val kind: ToolKind
    )

    private data class ToolKind(
        val description: String,
        val usage: String,
        val exampleSuffix: String,
        val capabilityGroups: List<CapabilityGroup> = emptyList()
    )

    private data class CapabilityGroup(
        val name: String,
        val description: String,
        val exampleCommands: List<String>
    )
}
