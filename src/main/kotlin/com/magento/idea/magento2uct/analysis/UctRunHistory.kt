/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.analysis

import org.json.JSONObject
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.Instant

/** Bounded durable reports, independent of the small in-memory worker cache. */
internal class UctRunHistory(private val root: Path, private val now: () -> Instant = Instant::now) {
    private val errors = mutableMapOf<String, String>()

    @Synchronized
    fun save(id: String, body: JSONObject) {
        val path = path(id) ?: error("Invalid run ID")
        var pending: Path? = null
        try {
            Files.createDirectories(root)
            val report = JSONObject(body.toString()).put("schemaVersion", 1).put("runId", id)
                .put("terminal", true).put("successful", body.getString("state") == "completed")
                .put("storedAt", now().toString())
            pending = Files.createTempFile(root, "report-", ".tmp")
            Files.writeString(pending, report.toString())
            require(Files.size(pending) <= 64L * 1024 * 1024) { "Run report exceeds the 64 MiB archive limit." }
            Files.move(pending, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            errors.remove(id)
            prune(path)
        } catch (exception: Exception) {
            errors[id] = "Automatic report export failed: ${exception.message}"
        } finally {
            pending?.let { Files.deleteIfExists(it) }
        }
    }

    @Synchronized
    fun read(id: String): JSONObject? {
        val path = path(id) ?: return null
        if (!Files.isRegularFile(path) || expired(path)) return null
        require(Files.size(path) <= 64L * 1024 * 1024) { "Archived report is too large." }
        return JSONObject(Files.readString(path)).also {
            require(it.getInt("schemaVersion") == 1 && it.getString("runId") == id && it.getBoolean("terminal")) {
                "Invalid archived run report."
            }
        }
    }

    @Synchronized
    fun info(id: String): JSONObject {
        val path = path(id)
        val saved = path != null && Files.isRegularFile(path) && !expired(path)
        return JSONObject().put("path", if (saved) path.toString() else JSONObject.NULL)
            .put("format", "application/json").put("saved", saved)
            .put("retentionDays", RETENTION_DAYS).put("maxRunsPerOperation", MAX_REPORTS)
            .put("error", errors[id] ?: JSONObject.NULL)
    }

    private fun path(id: String): Path? = if (ID.matches(id)) root.resolve("$id.json") else null
    private fun expired(path: Path): Boolean = Files.getLastModifiedTime(path).toInstant().isBefore(now().minusSeconds(RETENTION_DAYS * 86400L))

    private fun prune(keep: Path) {
        Files.list(root).use { paths ->
            val reports = paths.filter { it != keep && it.fileName.toString().endsWith(".json") && Files.isRegularFile(it) }
                .sorted(compareByDescending<Path> { Files.getLastModifiedTime(it).toMillis() }.thenBy { it.fileName.toString() }).toList()
            reports.forEachIndexed { index, path ->
                if (index >= MAX_REPORTS - 1 || expired(path)) Files.deleteIfExists(path)
            }
        }
    }

    companion object {
        const val RETENTION_DAYS = 30
        const val MAX_REPORTS = 100
        private val ID = Regex("(?:prepare-)?[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
    }
}
