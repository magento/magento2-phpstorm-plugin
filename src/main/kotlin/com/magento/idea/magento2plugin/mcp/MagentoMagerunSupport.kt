/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project
import com.magento.idea.magento2plugin.project.Settings
import org.json.JSONArray
import org.json.JSONObject
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

/** Filesystem-only discovery shared by the environment description and magerun metadata tool. */
internal object MagentoMagerunSupport {
    private val MODERN_NAMES = listOf("n98-magerun2", "magerun2", "n98-magerun2.phar", "magerun2.phar")
    private val LEGACY_NAMES = listOf("n98-magerun", "n98-magerun.phar", "magerun")
    val DEFAULT_CANDIDATES = (MODERN_NAMES + LEGACY_NAMES).flatMap { name ->
        listOf("bin/$name", "vendor/bin/$name", name)
    }

    fun isModernName(name: String): Boolean = name.lowercase() in MODERN_NAMES
    fun isMagerunName(name: String): Boolean = isModernName(name) || name.lowercase() in LEGACY_NAMES

    fun discover(project: Project): Environment? {
        val root = MagentoCliToolQueries.projectRootPath(project) ?: return null
        return discover(root, MagentoCliToolQueries.resolveConfiguredMagentoRootRelativePath(project),
            Settings.getMcpCliToolCandidates(project))
    }

    fun discover(projectRoot: Path, magentoRelative: String?, configured: List<String>): Environment {
        val root = projectRoot.toAbsolutePath().normalize()
        val searchRoots = MagentoCliToolQueries.buildSearchRoots(root, magentoRelative)
        val magentoRoot = MagentoCliToolQueries.resolveProjectPath(root, magentoRelative ?: ".")
        val tools = MagentoCliToolQueries.discoverTools(root, searchRoots, configured)
            .filter { isMagerunName(it.name) }
            .sortedWith(compareBy<MagentoCliToolQueries.DetectedTool> { it.rootOrder }
                .thenBy { candidatePriority(it.command, configured) }
                .thenBy { it.command })
        val candidates = tools.map { tool ->
            Candidate(root.resolve(tool.command.removePrefix("./")).normalize(),
                root.resolve(tool.workingDirectory).normalize(), tool.command, isModernName(tool.name))
        }
        return Environment(root, magentoRoot, searchRoots, candidates)
    }

    internal fun candidatePriority(command: String, configured: List<String>): Int {
        val relative = command.removePrefix("./")
        // Dedicated bin wrappers precede PHAR/Composer installations, including when aliases differ.
        val pathRank = when {
            relative.contains("/vendor/bin/") || relative.startsWith("vendor/bin/") -> 200
            relative.endsWith(".phar") -> 100
            else -> 0
        }
        val configuredIndex = configured.indexOfFirst { it.substringAfterLast('/') == relative.substringAfterLast('/') }
        val nameRank = (MODERN_NAMES + LEGACY_NAMES).indexOf(relative.substringAfterLast('/')).coerceAtLeast(0)
        return pathRank + if (configuredIndex >= 0) configuredIndex.coerceAtMost(50) else 50 + nameRank
    }

    data class Candidate(val path: Path, val cwd: Path, val displayPath: String, val modernName: Boolean) {
        fun json(): JSONObject = JSONObject().put("path", path.toString()).put("cwd", cwd.toString())
            .put("family", if (modernName) "magerun2_candidate" else "unverified_alias")
            .put("identityVerified", false)
    }

    data class Environment(
        val projectRoot: Path,
        val magentoRoot: Path?,
        val searchRoots: List<String>,
        val candidates: List<Candidate>
    ) {
        val selected: Candidate? get() = candidates.firstOrNull()
        fun json(): JSONObject = JSONObject().put("projectRoot", projectRoot.toString())
            .put("magentoRoot", magentoRoot?.toString() ?: JSONObject.NULL)
            .put("searchRoots", JSONArray(searchRoots))
            .put("selected", selected?.json() ?: JSONObject.NULL)
            .put("alternatives", JSONArray(candidates.drop(1).map { it.json() }))
    }

    data class LaunchPlan(
        val argv: List<String>,
        val cwd: Path,
        val runtime: String,
        val source: Path,
        val preflight: List<String>? = null,
        val fingerprintFiles: List<Path> = emptyList()
    ) {
        fun json(): JSONObject = JSONObject().put("argv", JSONArray(argv)).put("cwd", cwd.toString())
            .put("runtime", runtime).put("sourceWrapper", source.toString())
    }

    class Unavailable(val status: String, override val message: String) : RuntimeException(message)

    /** Only known forwarding forms are unwrapped; arbitrary shell scripts are never probed. */
    fun plan(environment: Environment): LaunchPlan {
        val candidate = environment.selected ?: throw Unavailable("not_found", "No project-local magerun candidate was found.")
        val root = environment.magentoRoot
            ?: throw Unavailable("invalid_root", "The configured Magento directory is outside the project.")
        return planFile(environment, candidate.path, candidate.cwd, root, candidate.path, emptySet())
    }

    private fun planFile(environment: Environment, path: Path, cwd: Path, magentoRoot: Path,
                         source: Path, visited: Set<Path>): LaunchPlan {
        val checked = MagentoCliToolQueries.resolveProjectPath(environment.projectRoot, path.toString())
        if (checked == null || !Files.isRegularFile(checked) || !Files.isReadable(checked)) {
            throw Unavailable("missing_installation", "The selected magerun file is missing, unreadable, or outside the project.")
        }
        if (visited.size >= 8 || path in visited) {
            throw Unavailable("unsupported_wrapper", "The selected wrapper has a cyclic or excessively deep forwarding chain.")
        }
        val header = Files.newInputStream(path).use { it.readNBytes(65_536).toString(Charsets.UTF_8) }
        if (header.startsWith("<?php") || Regex("^#![^\n]*\\bphp(?:[0-9.]*)?\\s*\n").containsMatchIn(header)) {
            // Raw PHP has no OS interpreter even if chmod +x is set; Windows cannot use Unix shebangs.
            val needsPhp = header.startsWith("<?php") || java.io.File.separatorChar == '\\' || !Files.isExecutable(path)
            val executable = if (needsPhp) listOf("php", path.toString()) else listOf(path.toString())
            return LaunchPlan(executable + "--root-dir=$magentoRoot", cwd, "local", source,
                fingerprintFiles = (visited + setOf(path)).toList())
        }
        val body = scriptBody(header)
        val forwarding = CONTAINER_FORWARD.matchEntire(body.lastOrNull().orEmpty())
        if (forwarding != null && (body.size == 1 || body == MARK_SHUST_BOOTSTRAP)) {
            val target = forwarding.groupValues[1]
            if (!isMagerunName(target.substringAfterLast('/'))) {
                throw Unavailable("unsupported_wrapper", "The container wrapper does not forward to a recognized magerun filename.")
            }
            val launcher = MagentoCliToolQueries.resolveProjectPath(environment.projectRoot, cwd.resolve("bin/clinotty").toString())
            if (launcher == null || !Files.isRegularFile(launcher) || !Files.isExecutable(launcher)) {
                throw Unavailable("missing_launcher", "The Docker magerun wrapper requires an executable in-project bin/clinotty for metadata and stdio.")
            }
            val prefix = listOf(launcher.toString())
            return LaunchPlan(prefix + listOf("php", target), cwd, "docker_wrapper", source,
                preflight = prefix + listOf("test", "-f", target),
                fingerprintFiles = listOf(path, launcher, cwd.resolve("bin/cli"), cwd.resolve("bin/docker-compose"),
                    cwd.resolve("bin/application-compose"), cwd.resolve("bin/common")))
        }
        if (body.size == 1) {
            val local = LOCAL_FORWARD.matchEntire(body.single())
            if (local != null) {
                val target = cwd.resolve(local.groupValues[1]).normalize()
                return planFile(environment, target, cwd, magentoRoot, source, visited + setOf(path))
            }
        }
        throw Unavailable("unsupported_wrapper", "The selected shell wrapper is not a recognized PHP/Composer entrypoint or simple forwarding wrapper. It was not executed. Keep this runtime; do not fall back to another installation.")
    }

    private fun scriptBody(text: String): List<String> = text.lineSequence().map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }.toList()

    private val CONTAINER_FORWARD = Regex("(?:exec )?(?:\\./)?bin/(?:cli|clinotty) (?:php )?((?:bin|vendor/bin)/[a-zA-Z0-9_.-]+) \"\\$@\"")
    private val LOCAL_FORWARD = Regex("(?:exec )?(?:php )?((?:\\./)?(?:bin|vendor/bin)/[a-zA-Z0-9_.-]+|(?:\\./)?[a-zA-Z0-9_.-]+\\.phar) \"\\$@\"")
    private val MARK_SHUST_BOOTSTRAP = scriptBody("""
        if ! bin/cliq ls bin/n98-magerun2.phar; then
          echo "Downloading n98-magerun2.phar, just a moment..."
          bin/clinotty curl -sS -O https://files.magerun.net/n98-magerun2.phar
          bin/clinotty curl -sS -o n98-magerun2.phar.sha256 https://files.magerun.net/sha256.php?file=n98-magerun2.phar
          bin/clinotty shasum -a 256 -c n98-magerun2.phar.sha256
          [ ${'$'}? != 0 ] && echo "sha256 checksum do not match!" && exit
          bin/cliq chmod +x n98-magerun2.phar
          bin/cliq mkdir -p bin
          bin/cliq mv n98-magerun2.phar bin
        fi
        bin/cli bin/n98-magerun2.phar "${'$'}@"
    """.trimIndent())

    /** No configuration values are read; cache invalidation uses file metadata only. */
    fun fingerprint(environment: Environment, plan: LaunchPlan): String {
        val files = linkedSetOf(plan.source)
        files += plan.fingerprintFiles
        for (directory in listOfNotNull(environment.projectRoot, environment.magentoRoot, plan.cwd).distinct()) {
            for (name in listOf("compose.yaml", "compose.yml", "docker-compose.yaml", "docker-compose.yml", ".env",
                "composer.lock", "composer.json", ".n98-magerun2", ".n98-magerun2.yaml", "n98-magerun2.yaml",
                "app/etc/n98-magerun2.yaml", "app/etc/config.php")) {
                files.add(directory.resolve(name))
            }
        }
        val material = buildString {
            append(environment.projectRoot).append('\n').append(environment.magentoRoot).append('\n')
            append(plan.cwd).append('\n').append(plan.argv.joinToString("\u0000"))
            for (file in files.sortedBy { it.toString() }) {
                append('\n').append(file)
                val checked = MagentoCliToolQueries.resolveProjectPath(environment.projectRoot, file.toString())
                if (checked != null && Files.isRegularFile(checked)) {
                    append(':').append(Files.getLastModifiedTime(file)).append(':').append(Files.size(file))
                } else append(":missing")
            }
        }
        return MessageDigest.getInstance("SHA-256").digest(material.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
