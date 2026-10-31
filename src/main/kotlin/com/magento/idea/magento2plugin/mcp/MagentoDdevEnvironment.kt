/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.util.SystemInfoRt
import com.intellij.util.EnvironmentUtil
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path

/** Shared DDEV discovery and command planning; never launches a process. */
internal object MagentoDdevEnvironment {
    fun discover(projectRoot: Path, magentoRoot: Path?, findExecutable: () -> Path? = ::findExecutable): Runtime? {
        var directory = magentoRoot ?: return null
        while (directory.startsWith(projectRoot)) {
            val config = MagentoCliToolQueries.resolveProjectPath(projectRoot,
                directory.resolve(".ddev/config.yaml").toString())
            if (config != null && Files.isRegularFile(config) && Files.isReadable(config)) {
                val executable = findExecutable()?.takeIf { it.isAbsolute && Files.isRegularFile(it) && Files.isExecutable(it) }
                return Runtime(config, magentoRoot, executable, MagentoDdevConfiguration.read(projectRoot, config))
            }
            if (directory == projectRoot) break
            directory = directory.parent ?: break
        }
        return null
    }

    fun findExecutable(): Path? = findExecutable(EnvironmentUtil.getValue("PATH"))

    internal fun findExecutable(path: String?): Path? {
        val name = if (SystemInfoRt.isWindows) "ddev.exe" else "ddev"
        for (entry in path.orEmpty().split(File.pathSeparatorChar)) {
            val directory = if (SystemInfoRt.isWindows) entry.removeSurrounding("\"") else entry
            if (directory.isEmpty()) continue
            val candidate = try {
                Path.of(directory).resolve(name)
            } catch (_: InvalidPathException) {
                continue
            }
            if (candidate.isAbsolute && Files.isRegularFile(candidate) && Files.isExecutable(candidate)) {
                return candidate
            }
        }
        return null
    }

    data class Runtime(val config: Path, val cwd: Path, val executable: Path?,
                       val configuration: MagentoDdevConfiguration) {
        /** Keep each configured value as one argument; no shell interpretation. */
        fun execArguments(command: List<String>, service: String = "web"): List<String> = buildList {
            addAll(listOf("exec", "--raw", "--service=$service"))
            configuration.string("name")?.let { add("--project=$it") }
            configuration.string("working_dir.$service")?.let { add("--dir=$it") }
            add("--")
            addAll(command)
        }

        fun example(command: List<String>): String = (listOf("ddev") + execArguments(command))
            .joinToString(" ", transform = MagentoCliToolQueries::shellQuote)

        fun terminalGuidance(command: List<String> = listOf("php", "-v")): JSONObject = JSONObject()
            .put("command", example(command)).put("cwd", cwd.toString())
            .put("message", "If execution needs interactive permissions, suggest running this command in PhpStorm's Terminal (IDE console) from the reported working directory and handling permission prompts there. The IDE process must also have access to DDEV and Docker. Retry the intended operation after resolving access.")

        fun json(): JSONObject = JSONObject().put("runtime", "ddev")
            .put("status", when {
                configuration.error != null -> "invalid_configuration"
                executable == null -> "missing_launcher"
                else -> "configured"
            })
            .put("configuration", configuration.json())
            .put("projectRoot", config.parent.parent.toString()).put("cwd", cwd.toString())
            .put("executable", executable?.toString() ?: JSONObject.NULL)
            .put("runtimeProbed", false)
            .apply {
                if (configuration.error == null) {
                    put("execPrefix", JSONArray(listOf(executable?.toString() ?: "ddev") + execArguments(emptyList())))
                    put("terminalGuidance", terminalGuidance())
                    put("usage", "Append the intended command and each argument to execPrefix; run from cwd. The default service is web. working_dir.web is inside the container; docroot is the web document root, and composer_root controls ddev composer. Without working_dir.web, DDEV selects its project-type default. This is static configuration, not proof that DDEV or a command is running or installed.")
                }
            }
    }
}
