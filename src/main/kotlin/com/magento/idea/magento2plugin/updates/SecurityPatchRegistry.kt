/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.updates

import org.json.JSONObject

/** Adobe CVT's local registry identifies component patches and prerequisites, not applied status. */
internal data class SecurityPatchRegistry(val areas: Map<String, String>, val patches: Map<String, Patch>) {
    data class Patch(val area: String, val versions: Set<String>, val requires: Set<String>)

    fun covers(month: String, installed: InstalledMagento, packages: Map<String, String>, matches: (String) -> Boolean): Boolean? {
        fun applicable(patch: Patch): Boolean {
            val version = when (patch.area) {
                "CE" -> installed.version
                "EE" -> if (installed.distribution == MagentoDistribution.COMMERCE) installed.version else null
                else -> packages[areas[patch.area]]
            }
            return version in patch.versions
        }
        val required = patches.filter { (id, patch) -> id.contains("-$month-") && applicable(patch) }.keys
        if (required.isEmpty()) return null
        fun covered(id: String, visiting: Set<String>): Boolean {
            if (id in visiting || !matches(id)) return false
            val patch = patches[id] ?: return false
            return patch.requires.all { dependency ->
                val prerequisite = patches[dependency] ?: return@all false
                !applicable(prerequisite) || covered(dependency, visiting + id)
            }
        }
        return required.all { covered(it, emptySet()) }
    }

    companion object {
        fun from(json: JSONObject): SecurityPatchRegistry? {
            val definitions = json.optJSONObject("patches") ?: return null
            val areas = json.optJSONObject("_areas") ?: return null
            return SecurityPatchRegistry(
                areas.keySet().associateWith { areas.optJSONObject(it)?.optString("composer_package") ?: "" },
                definitions.keySet().mapNotNull { id ->
                    val patch = definitions.optJSONObject(id) ?: return@mapNotNull null
                    fun strings(key: String): Set<String> {
                        val array = patch.optJSONArray(key) ?: return emptySet()
                        return (0 until array.length()).mapNotNull { array.opt(it) as? String }.toSet()
                    }
                    id to Patch(patch.optString("area"), strings("applies_to"), strings("requires"))
                }.toMap()
            )
        }
    }
}
