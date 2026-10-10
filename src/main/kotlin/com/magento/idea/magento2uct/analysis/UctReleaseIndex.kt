/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.analysis

import org.json.JSONObject

/** All three full states from one verified Magento Open Source release archive. */
internal data class UctReleaseIndex(
    val version: String,
    val sourceUrl: String,
    val archiveSha256: String,
    val processedFiles: Int,
    val existence: Map<String, Boolean>,
    val api: Map<String, Boolean>,
    val deprecation: Map<String, Boolean>
) {
    init {
        UctVersions.compare(version, version)
        require(sourceUrl == UctReleaseSource.url(version)) { "Invalid release source URL." }
        require(Regex("[0-9a-f]{64}").matches(archiveSha256)) { "Invalid release archive checksum." }
        require(processedFiles > 0 && processedFiles <= 50000) { "Invalid release file count." }
        require(existence["\\Magento\\Framework\\AppInterface"] == true) { "Missing Magento framework index." }
        require(api.isNotEmpty() && api.keys.all { existence[it] == true }) { "Missing or invalid API coverage index." }
        for (family in listOf(existence, api, deprecation)) {
            require(family.size <= 1000000 && family.all { (key, value) -> key.startsWith("\\Magento\\") && value }) {
                "Invalid full release index entries."
            }
        }
    }

    fun json(): JSONObject = JSONObject().put("schemaVersion", 1).put("version", version)
        .put("sourceUrl", sourceUrl).put("archiveSha256", archiveSha256).put("processedFiles", processedFiles)
        .put("existence", JSONObject(existence)).put("api", JSONObject(api)).put("deprecation", JSONObject(deprecation))

    companion object {
        fun read(json: JSONObject): UctReleaseIndex {
            require(json.getInt("schemaVersion") == 1) { "Unsupported release index schema." }
            fun entries(name: String): Map<String, Boolean> {
                val values = json.getJSONObject(name)
                return values.keySet().associateWith { key ->
                    require(values.get(key) is Boolean) { "Invalid $name index value." }
                    values.getBoolean(key)
                }
            }
            return UctReleaseIndex(json.getString("version"), json.getString("sourceUrl"),
                json.getString("archiveSha256"), json.getInt("processedFiles"), entries("existence"),
                entries("api"), entries("deprecation"))
        }
    }
}
