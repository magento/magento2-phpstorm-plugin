/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

internal class MaintenanceUnavailable(val reason: String, message: String) : RuntimeException(message)

internal class MaintenanceRuntime private constructor(
    val root: Path, val cwd: Path, val kind: String, private val prefix: List<String>,
    private val problem: String?, private val localTools: Map<String, String>,
    private val runner: MagerunRunner, private val cancel: () -> Unit
) {
    val label: String get() = "$kind @ $cwd (Magento: $root)"
    private fun argv(command: List<String>): List<String> {
        if (kind != "local") return prefix + command
        val tool = command.first()
        val executable = localTools[tool] ?: tool
        val phpEntry = tool == "bin/magento" || tool.substringAfterLast('/').equals("ece-patches")
        val needsPhp = phpEntry && (java.io.File.separatorChar == '\\' || !Files.isExecutable(cwd.resolve(executable)))
        val interpreter = if (needsPhp) listOf(localTools["php"] ?: "php") else emptyList()
        return interpreter + executable + command.drop(1)
    }

    fun suggestion(vararg command: String): JSONObject? = if (problem != null) null else
        JSONObject().put("argv", JSONArray(argv(command.toList()))).put("cwd", cwd.toString())

    fun probe(vararg command: String): MagerunProcessResult {
        cancel()
        if (problem != null) throw MaintenanceUnavailable("runtime_unavailable", problem)
        val result = try {
            runner.run(argv(command.toList()), cwd, cancel)
        } catch (_: IOException) {
            throw MaintenanceUnavailable("runtime_unavailable", "Could not launch ${command.first()} in the selected runtime.")
        }
        if (result.timedOut || result.outputLimited) throw MaintenanceUnavailable("probe_incomplete",
            if (result.timedOut) "Runtime probe timed out." else "Runtime probe exceeded the output limit.")
        return result
    }

    fun output(vararg command: String): String {
        val result = probe(*command)
        if (result.exitCode != 0) throw MaintenanceUnavailable("probe_failed",
            "${command.first()} exited ${result.exitCode}. Check the selected environment.")
        return result.stdout.trim()
    }

    private val composerFlags = arrayOf("--no-plugins", "--no-scripts", "--no-interaction", "--no-ansi")
    fun validateLock() = probe("composer", *composerFlags, "validate", "--check-lock", "--no-check-publish")
    fun composerPath(key: String): String {
        val path = output("composer", *composerFlags, "config", key, "--absolute")
        if (!(path.startsWith('/') || Regex("^[A-Za-z]:[/\\\\]").containsMatchIn(path)) ||
            path.any { it.isISOControl() }) throw MaintenanceUnavailable("composer_path_unknown", "Invalid Composer $key response.")
        return path
    }
    fun php(name: String, vararg args: String): String {
        val code = requireNotNull(javaClass.getResource("/maintenance/$name.php")).readText()
            .substringAfter("<?php").trim()
        return output("php", "-r", code, "--", *args)
    }

    companion object {
        fun discover(project: Path, relative: String?, candidates: List<String>, runner: MagerunRunner,
                     cancel: () -> Unit): MaintenanceRuntime {
            val root = MagentoCliEnvironment.resolveProjectPath(project, relative ?: ".")
                ?.takeIf(Files::isDirectory) ?: throw MaintenanceUnavailable("invalid_configuration", "Magento root is unavailable.")
            val roots = MagentoCliEnvironment.buildSearchRoots(project, relative)
            val tools = MagentoCliEnvironment.discoverTools(project, roots, candidates)
            val magento = tools.firstOrNull { it.name == "magento" }
            val cwd = magento?.let { project.resolve(it.workingDirectory).normalize() } ?: root
            val launcher = MagentoCliEnvironment.resolveProjectPath(project, cwd.resolve("bin/clinotty").toString())
            val wrapper = magento?.let { project.resolve(it.command) }
            val header = wrapper?.let { file ->
                Files.newInputStream(file).use { it.readNBytes(8192).toString(Charsets.UTF_8) }
            }
            val body = header?.lineSequence()?.map(String::trim)?.filter { it.isNotEmpty() && !it.startsWith("#") }
                ?.singleOrNull()
            val forwarding = body != null && body.removePrefix("exec ").removePrefix("./") in
                listOf("bin/cli bin/magento \"\$@\"", "bin/clinotty bin/magento \"\$@\"")
            if (forwarding || cwd != root) {
                val supported = forwarding && launcher != null && Files.isExecutable(launcher)
                return MaintenanceRuntime(root, cwd, "project_wrapper", listOfNotNull(launcher?.toString()),
                    if (supported) null else "The selected project wrapper has no supported noninteractive launcher.",
                    emptyMap(), runner, cancel)
            }
            val ddev = MagentoDdevEnvironment.discover(project, root, MagentoCliToolQueries::findDdevExecutable)
            if (ddev != null) return MaintenanceRuntime(root, root, "ddev",
                listOf(ddev.executable?.toString() ?: "ddev") + ddev.execArguments(emptyList()),
                ddev.configuration.error ?: if (ddev.executable == null) "DDEV executable unavailable." else null,
                emptyMap(), runner, cancel)
            // Never combine host Composer/PHP with a container-only installation.
            val compose = roots.firstOrNull()?.let { MagentoCliEnvironment.hasComposeConfiguration(project, project.resolve(it)) } == true
            val local = tools.filter { it.workingDirectory == (magento?.workingDirectory ?: roots.lastOrNull()) }
                .associate { it.name to it.invocation }
            val unsupported = header != null && !header.startsWith("<?php") &&
                !Regex("""^#![^\n]*\bphp[0-9.]*\s*\n""").containsMatchIn(header)
            val problem = when {
                compose -> "No supported CLI launcher was found beside Compose."
                unsupported -> "The selected Magento wrapper does not identify a supported runtime."
                else -> null
            }
            return MaintenanceRuntime(root, root, if (compose) "compose" else "local", emptyList(), problem,
                local.filterKeys { it == "php" || it == "composer" } + ("bin/magento" to (magento?.invocation ?: "bin/magento")),
                runner, cancel)
        }
    }
}
