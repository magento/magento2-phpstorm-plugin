/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64

/** Thrown for a request that cannot succeed as sent; the reply names the parameter and rule. */
internal class InvalidInspectionRequest(message: String) : RuntimeException(message)

/** Shared options of `magento_inspect` queries: format, page size, continuation and filters. */
internal data class InspectionOptions(
    val json: Boolean,
    val limit: Int,
    val cursor: String?,
    val module: String?,
    val area: String?,
    val path: String?
) {
    fun scope(): JSONObject = JSONObject().put("module", module ?: JSONObject.NULL)
        .put("area", area ?: JSONObject.NULL).put("path", path ?: JSONObject.NULL)
}

/**
 * One declaration. [key] is the matched value, such as an event name, shared by nested records. [module] is set
 * when the record knows its module itself, such as a module record; otherwise the owner of [path] is used.
 */
internal data class InspectionRecord(
    val kind: String,
    val key: String,
    val path: String?,
    val line: Int?,
    val area: String?,
    val fields: Map<String, Any?>,
    /** Module of a file outside any module directory, such as a theme override. */
    val moduleHint: String? = null,
    /** Module that owns the record regardless of its path. */
    val module: String? = null
)

internal object MagentoInspectionResults {
    const val DEFAULT_LIMIT = 20
    const val MAX_LIMIT = 100
    const val SCHEMA_VERSION = 1
    val OPTION_KEYS = setOf("format", "limit", "cursor", "module", "area", "path")
    const val TEXT_TRUNCATED_HINT = "More results exist. Add \"format\":\"json\" to parametersJson and follow " +
        "nextCursor to read every record; detailed_schema lists the filters of this queryType."

    fun isJson(parameters: JSONObject): Boolean = parameters.optString("format").trim().lowercase() == "json"

    /** Reads the shared options; [filters] are the filters this query type supports. */
    fun options(parameters: JSONObject, filters: Set<String>): InspectionOptions {
        val format = parameters.optString("format", "text").trim().lowercase()
        if (format !in setOf("text", "json")) invalid("format must be `text` or `json`.")
        val json = format == "json"
        val given = OPTION_KEYS.filter { parameters.has(it) && it != "format" }
        if (!json && given.isNotEmpty()) {
            invalid("${given.joinToString(", ") { "`$it`" }} require `\"format\":\"json\"`.")
        }
        for (filter in listOf("module", "area", "path")) {
            if (parameters.has(filter) && filter !in filters) {
                invalid("Filter `$filter` is not supported for this queryType. Supported filters: " +
                    (filters.sorted().joinToString(", ") { "`$it`" }.ifEmpty { "none" }) + ".")
            }
        }
        val limit = if (parameters.has("limit")) {
            val value = parameters.opt("limit")
            (value as? Int ?: (value as? String)?.toIntOrNull())?.takeIf { it in 1..MAX_LIMIT }
                ?: invalid("limit must be an integer from 1 to $MAX_LIMIT.")
        } else {
            DEFAULT_LIMIT
        }
        return InspectionOptions(json, limit, text(parameters, "cursor"), text(parameters, "module"),
            text(parameters, "area")?.lowercase(), text(parameters, "path")?.let(::normalizePath))
    }

    /** Applies filters, pages the ordered [records] and renders the JSON reply. */
    fun respond(
        project: Project,
        queryType: String,
        query: String,
        records: List<InspectionRecord>,
        options: InspectionOptions
    ): String {
        val owners = if (records.isEmpty()) emptyMap() else MagentoMcpSnapshots.moduleOwners(project)
        // Records of a module at the project root are filtered out anyway when another module is requested.
        val rootModule = owners[""]?.takeIf { options.module == null || options.module == it }
            ?.let { RootModuleFiles(project, it) }
        val owned = records.map { it to (it.module ?: ownerOf(it.path, owners, rootModule) ?: it.moduleHint) }
        val matching = owned.filter { (record, module) ->
            (options.module == null || module == options.module) &&
                (options.area == null || record.area == options.area) &&
                (options.path == null || record.path == options.path || record.path?.startsWith(options.path + "/") == true)
        }
        val fingerprint = fingerprint(matching)
        val offset = options.cursor?.let { decodeCursor(it, fingerprint) } ?: 0
        if (offset > matching.size) invalid("cursor points past the end of the result. Start again without cursor.")
        val page = matching.drop(offset).take(options.limit)
        val next = offset + page.size
        return envelope(queryType, query, options, if (matching.isEmpty()) "no_matches" else "ok")
            .put("total", matching.size)
            .put("offset", offset)
            .put("returned", page.size)
            .put("complete", next >= matching.size)
            .put("nextCursor", if (next < matching.size) encodeCursor(next, fingerprint) else JSONObject.NULL)
            .put("records", JSONArray(page.map { (record, module) -> json(record, module) }))
            .toString(2)
    }

    /** A reply without records, such as `indexing`, `unavailable`, `invalid_request` or `stale_cursor`. */
    fun status(queryType: String, query: String, options: InspectionOptions?, status: String, message: String): String =
        envelope(queryType, query, options, status).put("message", message).put("total", 0).put("offset", 0)
            .put("returned", 0).put("complete", false).put("nextCursor", JSONObject.NULL)
            .put("records", JSONArray()).toString(2)

    private fun envelope(queryType: String, query: String, options: InspectionOptions?, status: String): JSONObject =
        JSONObject().put("schemaVersion", SCHEMA_VERSION).put("queryType", queryType).put("query", query)
            .put("scope", options?.scope() ?: JSONObject.NULL).put("status", status)

    private fun json(record: InspectionRecord, module: String?): JSONObject = JSONObject()
        .put("kind", record.kind).put("key", record.key).put("path", record.path ?: JSONObject.NULL)
        .put("line", record.line ?: JSONObject.NULL).put("module", module ?: JSONObject.NULL)
        .put("area", record.area ?: JSONObject.NULL)
        .apply { record.fields.forEach { (name, value) -> put(name, value ?: JSONObject.NULL) } }

    /** The nearest enclosing module directory, so a nested module owns its own files. */
    private fun ownerOf(path: String?, owners: Map<String, String>, rootModule: RootModuleFiles?): String? {
        val file = path ?: return null
        var directory = file
        while (directory.isNotEmpty()) {
            owners[directory]?.let { return it }
            directory = directory.substringBeforeLast('/', "")
        }
        // A module at the project root has the empty relative path.
        return rootModule?.owner(file)
    }

    /**
     * Files of a module at the project root, decided once per directory for one reply. Installed packages in the
     * Composer vendor directory, the application configuration in `app/etc/` and any directory that is a package of
     * its own are not part of it. A path must name a file under the project root: [MagentoMcpSupport.relativePath]
     * falls back to the project base path, and such a path does not resolve under the root.
     */
    private class RootModuleFiles(project: Project, private val module: String) {
        private val root = MagentoMcpSupport.projectRoot(project)
        private val excluded = listOf(vendorDirectory(root), "app/etc")
        private val directories = hashMapOf<VirtualFile, Boolean>()

        fun owner(path: String): String? {
            val root = root ?: return null
            val ignoreCase = !SystemInfo.isFileSystemCaseSensitive
            if (path.startsWith("/") || DRIVE.containsMatchIn(path) || path.substringBefore('/') == ".." ||
                excluded.any { path.startsWith("$it/", ignoreCase) }) {
                return null
            }
            val directory = root.findFileByRelativePath(path)?.parent ?: return null
            return module.takeIf { inside(directory, root) }
        }

        private fun inside(directory: VirtualFile, root: VirtualFile): Boolean = directories.getOrPut(directory) {
            directory == root || directory.findChild("composer.json") == null &&
                directory.findChild("registration.php") == null && directory.parent?.let { inside(it, root) } == true
        }

        /** The `config.vendor-dir` of the root composer.json, `vendor` by default. */
        private fun vendorDirectory(root: VirtualFile?): String {
            val composer = root?.findChild("composer.json") ?: return "vendor"
            val configured = try {
                JSONObject(VfsUtilCore.loadText(composer)).optJSONObject("config")?.optString("vendor-dir")
            } catch (_: IOException) {
                null
            } catch (_: JSONException) {
                null
            }
            return configured?.replace('\\', '/')?.trim()?.removePrefix("./")?.trim('/')?.ifEmpty { null } ?: "vendor"
        }
    }

    private fun fingerprint(records: List<Pair<InspectionRecord, String?>>): String {
        val digest = MessageDigest.getInstance("SHA-256")
        for ((record, module) in records) {
            digest.update(("${record.kind}\u0000${record.key}\u0000${record.path}\u0000${record.line}\u0000" +
                "${record.area}\u0000$module\u0000${record.fields}\n").toByteArray(StandardCharsets.UTF_8))
        }
        return digest.digest().take(8).joinToString("") { "%02x".format(it) }
    }

    private fun encodeCursor(offset: Int, fingerprint: String): String = Base64.getUrlEncoder().withoutPadding()
        .encodeToString("v1:$offset:$fingerprint".toByteArray(StandardCharsets.UTF_8))

    private fun decodeCursor(cursor: String, fingerprint: String): Int {
        val parts = try {
            String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8).split(':')
        } catch (_: IllegalArgumentException) {
            emptyList()
        }
        val offset = parts.getOrNull(1)?.toIntOrNull()
        if (parts.size != 3 || parts[0] != "v1" || offset == null || offset < 0) {
            invalid("cursor is not a nextCursor value from this tool.")
        }
        if (parts[2] != fingerprint) {
            throw StaleCursor()
        }
        return offset
    }

    private fun normalizePath(value: String): String {
        val raw = value.replace('\\', '/').trim()
        val segments = raw.split('/').filter { it.isNotEmpty() && it != "." }
        if (raw.startsWith("/") || DRIVE.containsMatchIn(raw) || segments.isEmpty() || ".." in segments) {
            invalid("path must be a project-relative directory or file, such as `app/code/Vendor/Module`.")
        }
        return segments.joinToString("/")
    }

    private val DRIVE = Regex("^[A-Za-z]:")

    private fun text(parameters: JSONObject, name: String): String? =
        parameters.optString(name).trim().takeIf { it.isNotEmpty() }

    private fun invalid(message: String): Nothing = throw InvalidInspectionRequest(message)

    /** The declarations changed between pages; the caller must start again. */
    class StaleCursor : RuntimeException("The declarations changed since this cursor was issued. Start again without cursor.")
}
