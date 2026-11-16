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
    private const val MAX_FILES = 300
    private const val MAX_SKIPPED = 20
    private const val MAX_BYTES = MagentoDdevConfiguration.MAX_BYTES
    private const val MAX_TOTAL_BYTES = MagentoDdevConfiguration.MAX_TOTAL_BYTES
    private val CONDITIONS = listOf("OSTypes", "HostBinaryExists", "DBTypes")

    data class Command(val name: String, val service: String, val path: Path, val directives: Map<String, String>) {
        // DDEV registers the command with Usage as its cobra "Use", so the first word of Usage is what runs it.
        private val usage = directives["Usage"] ?: "$name [flags] [args]"

        fun json(projectRoot: Path): JSONObject = JSONObject()
            .put("name", name).put("invocation", "ddev ${usage.substringBefore(' ').ifEmpty { name }}")
            .put("service", service).put("runsOn", if (service == "host") "host" else "container")
            .put("path", projectRoot.relativize(path).toString().replace('\\', '/'))
            .put("description", directives["Description"] ?: name)
            .put("usage", usage)
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
        var files = 0
        var total = 0
        var truncated = false
        fun skip(entry: String) {
            if (skipped.size < MAX_SKIPPED) skipped += entry else truncated = true
        }
        val base = MagentoCliToolQueries.resolveProjectPath(projectRoot, ddevDirectory.resolve("commands").toString())
            ?.takeIf { Files.isDirectory(it) } ?: return Result(emptyList(), false, emptyList())
        services@ for (serviceDirectory in list(base)) {
            val service = serviceDirectory.fileName.toString()
            if (!Files.isDirectory(serviceDirectory) || service.startsWith(".")) continue
            if (MagentoCliToolQueries.resolveProjectPath(projectRoot, serviceDirectory.toString()) == null) {
                skip("$service: outside the project")
                continue
            }
            for (file in list(serviceDirectory)) {
                val name = file.fileName.toString()
                // DDEV ignores these files, and the first file added under a name wins.
                if (name.endsWith(".example") || name.startsWith("README") || name.startsWith(".") ||
                    Files.isDirectory(file) || name in names) continue
                if (++files > MAX_FILES || commands.size == MAX_COMMANDS) {
                    truncated = true
                    break@services
                }
                val checked = MagentoCliToolQueries.resolveProjectPath(projectRoot, file.toString())
                if (checked == null || !Files.isRegularFile(checked) || !Files.isReadable(checked)) {
                    skip("$service/$name: unreadable or outside the project")
                    continue
                }
                val bytes = try {
                    Files.newInputStream(checked).use { it.readNBytes(MAX_BYTES + 1) }
                } catch (_: IOException) {
                    skip("$service/$name: unreadable")
                    continue
                }
                if (bytes.size > MAX_BYTES) {
                    // DDEV has no size limit and still adds this command under its name.
                    names += name
                    skip("$service/$name: larger than $MAX_BYTES bytes, not read")
                    continue
                }
                total += bytes.size
                if (total > MAX_TOTAL_BYTES) {
                    truncated = true
                    break@services
                }
                val text = bytes.toString(Charsets.UTF_8)
                if (text.contains("\r\n")) {
                    skip("$service/$name: CRLF line endings, which DDEV skips")
                    continue
                }
                val directives = directives(text)
                val types = directives["ProjectTypes"]
                if (!types.isNullOrEmpty() && (projectType == null || !types.contains(projectType))) continue
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
            "~/.ddev/commands, add-on commands installed elsewhere, Usage and alias clashes with built-in DDEV " +
            "commands, unevaluatedConditions, and Windows hosts without bash, where DDEV registers no custom " +
            "commands, are not checked. These are project scripts: run one only when the task needs it and the " +
            "developer agrees, as its invocation from cwd. Container commands need a running project, and ddev " +
            "starts a stopped one.")

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
