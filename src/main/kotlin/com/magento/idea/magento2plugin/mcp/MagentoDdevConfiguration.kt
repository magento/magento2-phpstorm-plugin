/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import org.json.JSONArray
import org.json.JSONObject
import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.exceptions.YamlEngineException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/** A bounded, static view of DDEV's project-level execution settings. */
internal data class MagentoDdevConfiguration(
    val files: List<Path>,
    val settings: Map<String, Any>,
    val sources: Map<String, Path>,
    val error: String? = null
) {
    fun string(key: String): String? = settings[key]?.toString()?.takeIf { it.isNotEmpty() }

    fun json(): JSONObject = JSONObject()
        .put("status", if (error == null) "parsed" else "invalid_configuration")
        .put("files", JSONArray(files.map { it.toString() }))
        .put("settings", JSONObject(settings))
        .put("sources", JSONObject(sources.mapValues { it.value.toString() }))
        .put("coverage", "Project YAML execution settings, including ordered config.*.y*ml overrides. Global configuration, Docker Compose overrides, hooks, environment values and running-container state are not evaluated.")
        .apply { error?.let { put("message", it) } }

    companion object {
        private const val MAX_FILES = 32
        private const val MAX_BYTES = 262_144
        private const val MAX_TOTAL_BYTES = 1_048_576
        private val STRING_KEYS = setOf("name", "type", "docroot", "composer_root", "php_version",
            "nodejs_version", "webserver_type")
        private val MAP_KEYS = mapOf("working_dir" to setOf("web", "db"), "database" to setOf("type", "version"))

        fun read(projectRoot: Path, config: Path): MagentoDdevConfiguration {
            val files = mutableListOf(config)
            val values = linkedMapOf<String, Any>()
            val sources = linkedMapOf<String, Path>()
            fun failure(message: String) = MagentoDdevConfiguration(files, values, sources, message)
            try {
                Files.list(config.parent).use { children ->
                    files += children.filter { it.fileName.toString().matches(Regex("config\\..*\\.y.*ml")) }
                        .sorted().limit(MAX_FILES.toLong()).toList()
                }
                if (files.size > MAX_FILES) return failure("DDEV configuration exceeds the $MAX_FILES file limit.")
                var total = 0
                for (file in files) {
                    val checked = MagentoCliToolQueries.resolveProjectPath(projectRoot, file.toString())
                    if (checked == null || !Files.isRegularFile(checked) || !Files.isReadable(checked)) {
                        return failure("DDEV configuration is unreadable or outside the project: $file")
                    }
                    val bytes = Files.newInputStream(checked).use { it.readNBytes(MAX_BYTES + 1) }
                    total += bytes.size
                    if (bytes.size > MAX_BYTES || total > MAX_TOTAL_BYTES) {
                        return failure("DDEV configuration exceeds the supported size at $file")
                    }
                    val document = try {
                        Load(LoadSettings.builder().setCodePointLimit(MAX_BYTES).setMaxAliasesForCollections(0)
                            .setAllowDuplicateKeys(false).build()).loadFromString(bytes.toString(Charsets.UTF_8))
                    } catch (_: YamlEngineException) {
                        // Parser exceptions can echo arbitrary environment values from the source.
                        return failure("Invalid or unsupported DDEV YAML in $file")
                    }
                    val map = document as? Map<*, *> ?: if (document == null) emptyMap<Any, Any>()
                        else return failure("DDEV YAML must contain a mapping in $file")
                    if (map.containsKey("<<")) return failure("YAML merge keys are not supported in $file")
                    fun scalar(key: String, value: Any?): Boolean {
                        if (value == null) {
                            values.remove(key)
                            sources.remove(key)
                            return true
                        }
                        if (value !is String && value !is Number) return false
                        val text = value.toString()
                        if (text.length > 4096 || text.any { it.isISOControl() }) return false
                        values[key] = text
                        sources[key] = file
                        return true
                    }
                    for (key in STRING_KEYS) {
                        if (map.containsKey(key) && !scalar(key, map[key])) {
                            return failure("DDEV setting '$key' must be a scalar in $file")
                        }
                    }
                    for ((key, children) in MAP_KEYS) {
                        if (!map.containsKey(key)) continue
                        val nested = map[key]
                        if (nested == null) {
                            children.forEach { scalar("$key.$it", null) }
                            continue
                        }
                        if (nested !is Map<*, *>) return failure("DDEV setting '$key' must be a mapping in $file")
                        if (nested.containsKey("<<")) return failure("YAML merge keys are not supported in $file")
                        for (child in children) {
                            if (nested.containsKey(child) && !scalar("$key.$child", nested[child])) {
                                return failure("DDEV setting '$key.$child' must be a scalar in $file")
                            }
                        }
                    }
                    if (map.containsKey("no_project_mount")) {
                        val value = map["no_project_mount"]
                        if (value !is Boolean) return failure("DDEV setting 'no_project_mount' must be a boolean in $file")
                        values["no_project_mount"] = value
                        sources["no_project_mount"] = file
                    }
                }
            } catch (_: IOException) {
                return failure("DDEV configuration could not be read from ${config.parent}")
            }
            return MagentoDdevConfiguration(files, values, sources)
        }
    }
}
