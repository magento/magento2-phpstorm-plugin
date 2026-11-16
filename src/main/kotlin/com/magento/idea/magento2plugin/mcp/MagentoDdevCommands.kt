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

/**
 * Project custom commands in `.ddev/commands/<service>`, read with DDEV's own file rules and `## Key: value`
 * directives. Files are read, never run.
 */
internal object MagentoDdevCommands {
    private const val MAX_COMMANDS = 100
    private const val MAX_BYTES = 262_144
    private val CONDITIONS = listOf("OSTypes", "HostBinaryExists", "DBTypes")

    data class Command(val name: String, val service: String, val path: Path, val directives: Map<String, String>) {
        fun json(projectRoot: Path): JSONObject = JSONObject()
            .put("name", name).put("invocation", "ddev $name").put("service", service)
            .put("runsOn", if (service == "host") "host" else "container")
            .put("path", projectRoot.relativize(path).toString().replace('\\', '/'))
            .put("description", directives["Description"] ?: name)
            .put("usage", directives["Usage"] ?: "$name [flags] [args]")
            .apply {
                directives["Example"]?.let { put("example", it.replace("\\n", "\n")) }
                directives["Aliases"]?.let { value ->
                    put("aliases", JSONArray(value.split(',').map { it.trim() }.filter { it.isNotEmpty() }))
                }
                val conditions = CONDITIONS.mapNotNull { key -> directives[key]?.let { key to it } }.toMap()
                if (conditions.isNotEmpty()) put("unevaluatedConditions", JSONObject(conditions))
            }
    }

    data class Result(val commands: List<Command>, val truncated: Boolean, val skipped: List<String>)

    fun read(projectRoot: Path, ddevDirectory: Path, projectType: String?): Result {
        val commands = mutableListOf<Command>()
        val skipped = mutableListOf<String>()
        val names = hashSetOf<String>()
        var truncated = false
        val base = MagentoCliToolQueries.resolveProjectPath(projectRoot, ddevDirectory.resolve("commands").toString())
            ?.takeIf { Files.isDirectory(it) } ?: return Result(emptyList(), false, emptyList())
        for (serviceDirectory in list(base).filter { Files.isDirectory(it) && !it.fileName.toString().startsWith(".") }) {
            val service = serviceDirectory.fileName.toString()
            for (file in list(serviceDirectory)) {
                val name = file.fileName.toString()
                // DDEV ignores these files, and the first file with a name wins.
                if (name.endsWith(".example") || name.startsWith("README") || name.startsWith(".") ||
                    Files.isDirectory(file) || name in names) continue
                val checked = MagentoCliToolQueries.resolveProjectPath(projectRoot, file.toString())
                if (checked == null || !Files.isRegularFile(checked) || !Files.isReadable(checked)) {
                    skipped += "$service/$name: unreadable or outside the project"
                    continue
                }
                val bytes = try {
                    Files.newInputStream(checked).use { it.readNBytes(MAX_BYTES + 1) }
                } catch (_: IOException) {
                    skipped += "$service/$name: unreadable"
                    continue
                }
                if (bytes.size > MAX_BYTES) {
                    skipped += "$service/$name: larger than $MAX_BYTES bytes"
                    continue
                }
                val text = bytes.toString(Charsets.UTF_8)
                if (text.contains("\r\n")) {
                    skipped += "$service/$name: CRLF line endings, which DDEV skips"
                    continue
                }
                val directives = directives(text)
                val types = directives["ProjectTypes"]
                if (!types.isNullOrEmpty() && (projectType == null || !types.contains(projectType))) continue
                if (commands.size == MAX_COMMANDS) {
                    truncated = true
                    break
                }
                names += name
                commands += Command(name, service, file, directives)
            }
        }
        return Result(commands, truncated, skipped)
    }

    fun json(projectRoot: Path, result: Result): JSONObject = JSONObject()
        .put("commands", JSONArray(result.commands.map { it.json(projectRoot) }))
        .put("truncated", result.truncated)
        .put("skipped", JSONArray(result.skipped))
        .put("coverage", "Project commands from .ddev/commands with DDEV's file rules and ProjectTypes. Global " +
            "~/.ddev/commands, add-on commands installed elsewhere, name clashes with built-in DDEV commands and " +
            "unevaluatedConditions are not checked. Run a command as its invocation from cwd; container commands " +
            "need a running project, and ddev starts a stopped one.")

    // Same rule as DDEV's findDirectivesInScriptCommand: "## Key: value" lines, the last one wins.
    private fun directives(text: String): Map<String, String> = buildMap {
        for (line in text.lineSequence()) {
            if (!line.startsWith("## ") || !line.contains(':')) continue
            val key = line.removePrefix("## ").substringBefore(':')
            val value = line.substringAfter(':')
            put(key, if (key == "Example") value.trim(' ') else value.trim(' ', '"', '\''))
        }
    }

    private fun list(directory: Path): List<Path> = try {
        Files.list(directory).use { paths -> paths.sorted().toList() }
    } catch (_: IOException) {
        emptyList()
    }
}
