/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.analysis

/** Paths excluded from UCT analysis: an excluded directory covers everything below it. */
object UctExcludedPaths {
    @JvmStatic
    fun contains(excludedPaths: List<String>, path: String?): Boolean {
        if (path == null) return false
        val candidate = normalize(path)
        return excludedPaths.map(::normalize).any { it.isNotEmpty() && (candidate == it || candidate.startsWith("$it/")) }
    }

    private fun normalize(path: String): String = path.trim().replace('\\', '/').trimEnd('/')
}
