/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

/** Shared candidate naming and ordering, independent of IDE and process APIs. */
internal object MagentoMagerunNames {
    private val MODERN_NAMES = listOf("n98-magerun2", "magerun2", "n98-magerun2.phar", "magerun2.phar")
    private val LEGACY_NAMES = listOf("n98-magerun", "n98-magerun.phar", "magerun")
    val DEFAULT_CANDIDATES = (MODERN_NAMES + LEGACY_NAMES).flatMap { name ->
        listOf("bin/$name", "vendor/bin/$name", name)
    }

    fun isModernName(name: String): Boolean = name.lowercase() in MODERN_NAMES
    fun isMagerunName(name: String): Boolean = isModernName(name) || name.lowercase() in LEGACY_NAMES

    internal fun candidatePriority(command: String, configured: List<String>): Int {
        val relative = command.removePrefix("./")
        // Dedicated bin wrappers precede PHAR/Composer installations, including when aliases differ.
        val pathRank = when {
            relative.contains("/vendor/bin/") || relative.startsWith("vendor/bin/") -> 200
            relative.endsWith(".phar") -> 100
            else -> 0
        }
        val configuredIndex = configured.indexOfFirst { it.substringAfterLast('/') == relative.substringAfterLast('/') }
        val nameRank = (MODERN_NAMES + LEGACY_NAMES).indexOf(relative.substringAfterLast('/')).coerceAtLeast(0)
        return pathRank + if (configuredIndex >= 0) configuredIndex.coerceAtMost(50) else 50 + nameRank
    }

}
