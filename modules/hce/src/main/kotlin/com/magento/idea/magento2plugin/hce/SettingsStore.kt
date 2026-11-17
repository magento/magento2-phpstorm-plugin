/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.hce

import com.magento.idea.magento2plugin.project.MagentoCliConfiguration
import org.json.JSONArray
import org.json.JSONObject
import java.nio.channels.FileChannel
import java.nio.channels.OverlappingFileLockException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.WRITE
import java.security.MessageDigest

data class ProjectSettings(
    val projectPath: String,
    val enabled: Boolean = false,
    val magentoPath: String? = null,
    val cliToolCandidates: List<String> = MagentoCliConfiguration.parseCandidates(null),
    val revision: Long = 0
) {
    fun json(): JSONObject = JSONObject().put("schemaVersion", 1).put("projectPath", projectPath)
        .put("enabled", enabled).put("magentoPath", magentoPath ?: JSONObject.NULL)
        .put("cliToolCandidates", JSONArray(cliToolCandidates)).put("revision", revision)
}

/** Durable host settings, independent of HCE's disposable analysis Project objects. */
class SettingsStore(private val directory: Path) {
    private companion object {
        const val MAX_SETTINGS_BYTES = 65_536
    }

    fun projectRoot(project: Path): Path = project.toRealPath().also {
        require(Files.isDirectory(it)) { "The project root must be an existing directory." }
    }

    fun settingsFile(project: Path): Path {
        val identity = projectRoot(project).toString()
        val key = MessageDigest.getInstance("SHA-256").digest(identity.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        return directory.resolve("$key.json")
    }

    fun load(project: Path): ProjectSettings {
        val root = projectRoot(project)
        val file = settingsFile(root)
        if (!Files.exists(file)) return ProjectSettings(root.toString())
        try {
            val bytes = Files.newInputStream(file).use { it.readNBytes(MAX_SETTINGS_BYTES + 1) }
            require(bytes.size <= MAX_SETTINGS_BYTES)
            val json = JSONObject(bytes.toString(Charsets.UTF_8))
            require(json.get("schemaVersion") == 1 && json.getString("projectPath") == root.toString())
            val revision = json.get("revision")
            require(json.get("enabled") is Boolean && (revision is Int || revision is Long))
            require(json.getLong("revision") in 0 until Long.MAX_VALUE)
            val candidates = json.getJSONArray("cliToolCandidates")
            require(candidates.length() <= 100)
            val values = (0 until candidates.length()).map { candidates.get(it).also { value -> require(value is String) } as String }
            return ProjectSettings(root.toString(), json.getBoolean("enabled"),
                if (json.isNull("magentoPath")) null else json.getString("magentoPath"), values, json.getLong("revision"))
        } catch (_: RuntimeException) {
            throw IllegalStateException("Stored Magento settings are invalid or incompatible: $file")
        }
    }

    /** Lock the read/update/write transaction, including calls in different host classloaders or processes. */
    fun update(project: Path, transform: (ProjectSettings) -> ProjectSettings): ProjectSettings {
        val file = settingsFile(project)
        Files.createDirectories(directory)
        FileChannel.open(file.resolveSibling(file.fileName.toString() + ".lock"), CREATE, WRITE).use { channel ->
            val deadline = System.nanoTime() + 5_000_000_000L
            while (true) {
                val lock = try { channel.tryLock() } catch (_: OverlappingFileLockException) { null }
                if (lock != null) {
                    lock.use {
                        val before = load(project)
                        val after = transform(before).copy(revision = before.revision + 1)
                        require(after.projectPath == before.projectPath) { "Settings cannot change project identity." }
                        val bytes = (after.json().toString(2) + "\n").toByteArray(Charsets.UTF_8)
                        require(bytes.size <= MAX_SETTINGS_BYTES) {
                            "Magento settings must not exceed $MAX_SETTINGS_BYTES UTF-8 bytes."
                        }
                        val temporary = Files.createTempFile(directory, ".magento-", ".tmp")
                        try {
                            Files.write(temporary, bytes)
                            try { Files.move(temporary, file, ATOMIC_MOVE, REPLACE_EXISTING) }
                            catch (_: AtomicMoveNotSupportedException) { Files.move(temporary, file, REPLACE_EXISTING) }
                        } finally { Files.deleteIfExists(temporary) }
                        return after
                    }
                }
                check(System.nanoTime() < deadline) { "Magento settings are busy; retry the request." }
                Thread.sleep(10)
            }
        }
    }
}
