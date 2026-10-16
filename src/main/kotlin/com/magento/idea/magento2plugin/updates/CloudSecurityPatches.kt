/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.updates

import org.json.JSONObject

/** Cloud Patches mappings select required diffs; only verified post-images establish coverage. */
internal data class CloudSecurityPatches(private val patches: List<Patch>) {
    internal data class Patch(val month: String, val component: String, val constraint: String, val applied: Boolean)

    fun covers(month: String, installed: InstalledMagento, packages: Map<String, String>): Boolean {
        val versions = linkedMapOf(CE to (packages[CE] ?: installed.version))
        if (installed.distribution == MagentoDistribution.COMMERCE) versions[EE] = packages[EE] ?: installed.version
        (packages[B2B] ?: packages["magento/extension-b2b"])?.let { versions[B2B] = it }
        return versions.all { (component, version) ->
            val candidates = patches.filter { it.month == month && it.component == component }
            val applicable = candidates.map { it to matches(version, it.constraint) }
            // Unsupported constraints are uncertain evidence, rather than permission to omit a fix.
            applicable.none { it.second == null } && applicable.any { it.second == true } &&
                applicable.filter { it.second == true }.all { it.first.applied }
        }
    }

    companion object {
        private const val CE = "magento/magento2-base"
        private const val EE = "magento/magento2-ee-base"
        private const val B2B = "magento/magento2-b2b-base"
        val EMPTY = CloudSecurityPatches(emptyList())
        private val months = listOf("january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december")
        private val monthlyRelease = Regex("(?:^|[^a-z])([a-z]+)[ _-]+(20\\d{2})[ _-]+monthly[ _-]+release(?=$|[^a-z])", RegexOption.IGNORE_CASE)

        /** Shape: package -> patch title -> Composer version constraint -> patch filename. */
        fun from(json: JSONObject, verify: (String, String) -> Boolean): CloudSecurityPatches {
            val patches = mutableListOf<Patch>()
            for (component in listOf(CE, EE, B2B)) {
                val definitions = json.optJSONObject(component) ?: continue
                for (title in definitions.keySet()) {
                    val versions = definitions.optJSONObject(title)
                    if (versions == null || versions.length() == 0) {
                        month(title)?.let { patches.add(Patch(it, component, "", false)) }
                        continue
                    }
                    for (constraint in versions.keySet()) {
                        val source = versions.opt(constraint) as? String
                        val month = month(title) ?: source?.let(::month) ?: continue
                        patches.add(Patch(month, component, constraint, source != null && verify(source, component)))
                    }
                }
            }
            return CloudSecurityPatches(patches.sortedWith(compareBy<Patch> { it.month }.thenBy { it.component }.thenBy { it.constraint }))
        }

        private fun month(identity: String): String? {
            val match = monthlyRelease.find(identity) ?: return null
            val name = match.groupValues[1].lowercase()
            val number = months.indexOfFirst { name == it || name == it.take(3) }.takeIf { it >= 0 }?.plus(1) ?: return null
            return "${match.groupValues[2]}-${number.toString().padStart(2, '0')}"
        }

        private const val VERSION = "v?\\d+\\.\\d+\\.\\d+(?:-p\\d+)?"
        private val range = Regex("($VERSION)\\s+-\\s+($VERSION)")
        private val comparison = Regex("(>=|<=|!=|<>|==|>|<|=)?\\s*($VERSION)")

        /** Exact releases, comparison intersections, inclusive ranges and unions used in patches.json.
         * Other Composer syntax stays unverifiable. Magento -pN sorts after the base release.
         */
        private fun matches(value: String, constraint: String): Boolean? {
            val version = ReleaseVersion.parse(value.removePrefix("v"))?.takeIf { it.stable } ?: return null
            val alternatives = constraint.split("||").map { alternative ->
                val expression = range.replace(alternative, ">=$1 <=$2").replace(',', ' ')
                val terms = comparison.findAll(expression).toList()
                if (terms.isEmpty() || comparison.replace(expression, "").isNotBlank()) return null
                terms.all { term ->
                    val bound = ReleaseVersion.parse(term.groupValues[2].removePrefix("v")) ?: return null
                    when (term.groupValues[1]) {
                        ">=" -> version >= bound
                        "<=" -> version <= bound
                        ">" -> version > bound
                        "<" -> version < bound
                        "!=", "<>" -> version != bound
                        else -> version == bound
                    }
                }
            }
            return alternatives.any { it }
        }
    }
}
