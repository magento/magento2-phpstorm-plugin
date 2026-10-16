/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.updates

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.magento.idea.magento2plugin.magento.packages.code.MagentoVersion
import com.magento.idea.magento2plugin.util.magento.MagentoVersionUtil

enum class MagentoDistribution(val edition: MagentoVersion, val apiId: String) {
    OPEN_SOURCE(MagentoVersion.COMMUNITY_EDITION, "magento-community"),
    COMMERCE(MagentoVersion.ENTERPRISE_EDITION, "magento-commerce"),
    MAGE_OS(MagentoVersion.MAGEOS_COMMUNITY_EDITION, "mage-os");

    val displayName: String get() = edition.displayName
}

data class InstalledMagento(val distribution: MagentoDistribution, val version: String, val development: Boolean = false) {
    companion object {
        /** Uses exactly the same detection as the Magento settings page, including its configured root. */
        fun detect(project: Project, magentoPath: String): InstalledMagento {
            val source = MagentoVersionUtil.getSourceVersionData(project, magentoPath)
            return fromVersionData(source ?: MagentoVersionUtil.getLockedVersionData(project, magentoPath), source != null)
        }

        internal fun fromVersionData(data: Pair<String, String?>, sourceCheckout: Boolean = false): InstalledMagento {
            val distribution = MagentoDistribution.entries.firstOrNull { it.edition.displayName == data.second }
                ?: throw IllegalArgumentException("Cannot detect a Magento or Mage-OS product package in composer.lock.")
            val version = data.first
            val development = sourceCheckout || version.startsWith("dev-", ignoreCase = true) || version.endsWith("-dev", ignoreCase = true)
            require(development || ReleaseVersion.parse(version) != null) { "Update checks require an exact installed release in composer.lock." }
            return InstalledMagento(distribution, version, development)
        }
    }
}

/** Magento's -pN is a patch after a release, not a SemVer prerelease. */
internal data class ReleaseVersion(val major: Int, val minor: Int, val patch: Int, val stage: Int, val number: Int) : Comparable<ReleaseVersion> {
    val stable: Boolean get() = stage >= 0
    val line: List<Int> get() = listOf(major, minor, patch)
    override fun compareTo(other: ReleaseVersion): Int =
        compareValuesBy(this, other, { it.major }, { it.minor }, { it.patch }, { it.stage }, { it.number })

    companion object {
        private val pattern = Regex("(\\d+)\\.(\\d+)\\.(\\d+)(?:-(alpha|beta|rc|p)(\\d+))?", RegexOption.IGNORE_CASE)
        fun parse(value: String): ReleaseVersion? {
            val match = pattern.matchEntire(value) ?: return null
            val major = match.groupValues[1].toIntOrNull() ?: return null
            val minor = match.groupValues[2].toIntOrNull() ?: return null
            val patch = match.groupValues[3].toIntOrNull() ?: return null
            val stage = when (match.groupValues[4].lowercase()) { "alpha" -> -3; "beta" -> -2; "rc" -> -1; "p" -> 1; else -> 0 }
            val number = if (stage == 0) 0 else match.groupValues[5].toIntOrNull() ?: return null
            return ReleaseVersion(major, minor, patch, stage, number)
        }
    }
}
