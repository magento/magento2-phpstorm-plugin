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
import java.util.Arrays

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
    // Go's bufio.MaxScanTokenSize: DDEV's directive scanner fails on a line this long.
    private const val MAX_LINE_BYTES = 65_536
    private val CONDITIONS = listOf("OSTypes", "HostBinaryExists", "DBTypes")
    // DDEV's root flags, all booleans, so cobra does not take the next word as their value.
    private val ROOT_BOOLEAN_FLAGS = setOf("--json-output", "--skip-hooks", "-j")

    data class Command(
        val name: String,
        val service: String,
        val path: Path,
        val directives: Map<String, String>,
        val aliases: List<String> =
            directives["Aliases"].orEmpty().split(',').map { it.trim() }.filter { it.isNotEmpty() },
    ) {
        // DDEV registers the command with Usage as its cobra "Use", so the first word of Usage is what runs it.
        val usage = directives["Usage"] ?: "$name [flags] [args]"
        val invocation = usage.substringBefore(' ')

        fun json(projectRoot: Path): JSONObject = JSONObject()
            .put("name", name).put("invocation", "ddev $invocation")
            .put("service", service).put("runsOn", if (service == "host") "host" else "container")
            .put("path", projectRoot.relativize(path).toString().replace('\\', '/'))
            .put("description", directives["Description"] ?: name)
            .put("usage", usage)
            .apply {
                directives["Example"]?.let { put("example", it.replace("\\n", "\n")) }
                if (directives.containsKey("Aliases")) put("aliases", JSONArray(aliases))
                val conditions = CONDITIONS.mapNotNull { key -> directives[key]?.let { key to it } }.toMap()
                if (conditions.isNotEmpty()) put("unevaluatedConditions", JSONObject(conditions))
            }
    }

    data class Result(val commands: List<Command>, val truncated: Boolean, val skipped: List<String>)

    fun read(projectRoot: Path, ddevDirectory: Path, projectType: String?): Result {
        val commands = mutableListOf<Command>()
        val skipped = mutableListOf<String>()
        val names = hashSetOf<String>()
        // Names and aliases DDEV has registered so far, with the command that owns each one.
        val registered = hashMapOf<String, String>()
        var files = 0
        var total = 0
        var truncated = false
        fun skip(entry: String) {
            if (skipped.size < MAX_SKIPPED) skipped += entry else truncated = true
        }
        val base = MagentoCliEnvironment.resolveProjectPath(projectRoot, ddevDirectory.resolve("commands").toString())
            ?.takeIf { Files.isDirectory(it) } ?: return Result(emptyList(), false, emptyList())
        services@ for (serviceDirectory in list(base)) {
            val service = serviceDirectory.fileName.toString()
            if (!Files.isDirectory(serviceDirectory) || service.startsWith(".")) continue
            if (MagentoCliEnvironment.resolveProjectPath(projectRoot, serviceDirectory.toString()) == null) {
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
                val checked = MagentoCliEnvironment.resolveProjectPath(projectRoot, file.toString())
                if (checked == null || !Files.isRegularFile(checked) || !Files.isReadable(checked)) {
                    skip("$service/$name: unreadable or outside the project")
                    continue
                }
                val limit = minOf(MAX_BYTES + 1, MAX_TOTAL_BYTES - total)
                val bytes = try {
                    Files.newInputStream(checked).use { it.readNBytes(limit) }
                } catch (_: IOException) {
                    skip("$service/$name: unreadable")
                    continue
                }
                total += bytes.size
                if (bytes.size > MAX_BYTES) {
                    // DDEV has no size limit and still adds this command under its name.
                    names += name
                    registered.putIfAbsent(name, "$service/$name")
                    skip("$service/$name: larger than $MAX_BYTES bytes, not read")
                    continue
                }
                // The budget ran out: the file is complete only when nothing follows what was read.
                if (bytes.size == limit && size(checked) != limit.toLong()) {
                    truncated = true
                    break@services
                }
                val text = bytes.toString(Charsets.UTF_8)
                if (text.contains("\r\n")) {
                    skip("$service/$name: CRLF line endings, which DDEV skips")
                    continue
                }
                val command = Command(name, service, file, directives(bytes))
                // DDEV skips a command whose Usage cobra resolves to an existing command, including the root
                // "ddev" when Usage holds no command word, and drops each alias that resolves.
                val word = commandWord(command.usage.split(' '))
                val owner = if (word == null) "ddev" else registered[word]
                if (owner != null) {
                    skip("$service/$name: '${word ?: command.usage}' is already used by $owner, which DDEV skips")
                    continue
                }
                val types = command.directives["ProjectTypes"]
                if (!types.isNullOrEmpty() && (projectType == null || !types.contains(projectType))) continue
                val (taken, aliases) = command.aliases.partition { it.startsWith("-") || it in registered }
                for (alias in taken) {
                    val aliasOwner = if (alias.startsWith("-")) "ddev" else registered[alias]
                    skip("$service/$name: alias '$alias' is already used by $aliasOwner, which DDEV drops")
                }
                names += name
                for (key in listOf(command.invocation) + aliases) registered.putIfAbsent(key, "$service/$name")
                if (command.invocation.startsWith("-")) {
                    skip("$service/$name: Usage name '${command.invocation}' starts with '-', so DDEV cannot run it")
                    continue
                }
                commands += command.copy(aliases = aliases)
            }
        }
        return Result(commands, truncated, skipped)
    }

    fun json(projectRoot: Path, result: Result): JSONObject = JSONObject()
        .put("commands", JSONArray(result.commands.map { it.json(projectRoot) }))
        .put("truncated", result.truncated)
        .put("skipped", JSONArray(result.skipped))
        .put("coverage", "Project commands from .ddev/commands with DDEV's file rules and ProjectTypes. Usage " +
            "and alias clashes between them follow DDEV's registration order and assume that each earlier command " +
            "passes its unevaluatedConditions. A file skipped as too large is assumed to be registered under its file " +
            "name; clashes with its Usage or aliases cannot be detected. Files are ordered by name bytes as DDEV " +
            "does, but non-ASCII names may order differently: the JDK normalizes them on macOS and cannot decode " +
            "non-UTF-8 names exactly on Linux. Global " +
            "~/.ddev/commands, add-on commands installed elsewhere, Usage and alias clashes with built-in DDEV " +
            "commands, unevaluatedConditions, and Windows hosts without bash, where DDEV registers no custom " +
            "commands, are not checked. These are project scripts: run one only when the task needs it and the " +
            "developer agrees, as its invocation from cwd. Container commands need a running project, and ddev " +
            "starts a stopped one.")

    // Same rule as DDEV's findDirectivesInScriptCommand: "## Key: value" lines split on \n only, the last one wins.
    // When a line is too long for its scanner, DDEV reads no directives at all.
    private fun directives(bytes: ByteArray): Map<String, String> {
        var length = 0
        for (byte in bytes) {
            length = if (byte == '\n'.code.toByte()) 0 else length + 1
            if (length >= MAX_LINE_BYTES) return emptyMap()
        }
        val directives = mutableMapOf<String, String>()
        for (line in bytes.toString(Charsets.UTF_8).split('\n').map { it.removeSuffix("\r") }) {
            if (!line.startsWith("## ") || !line.contains(':')) continue
            val key = line.removePrefix("## ").substringBefore(':')
            val value = line.substringAfter(':')
            directives[key] = if (key == "Example") value.trim(' ') else value.trim(' ', '"', '\'')
        }
        return directives
    }

    // The first command word of cobra's Find after its stripFlags: "--" ends the search, other words starting with
    // "-" are flags, and a "--name" or two-character "-x" flag that is not boolean also takes the next word as its
    // value, unless no word follows that one.
    private fun commandWord(words: List<String>): String? {
        var index = 0
        while (index < words.size) {
            val word = words[index++]
            when {
                word == "--" -> return null
                word.startsWith("-") && !word.contains('=') && (word.startsWith("--") || word.length == 2) &&
                    word !in ROOT_BOOLEAN_FLAGS -> if (words.size - index <= 1) return null else index++
                word.isNotEmpty() && !word.startsWith("-") -> return word
            }
        }
        return null
    }

    private fun size(file: Path): Long = try {
        Files.size(file)
    } catch (_: IOException) {
        -1
    }

    // Go's os.ReadDir order, which DDEV registers in: file name bytes, case-sensitive on every OS.
    private fun list(directory: Path): List<Path> = try {
        Files.list(directory).use { paths -> paths.toList() }.sortedWith { first, second ->
            Arrays.compareUnsigned(first.fileName.toString().toByteArray(), second.fileName.toString().toByteArray())
        }
    } catch (_: IOException) {
        emptyList()
    }
}
