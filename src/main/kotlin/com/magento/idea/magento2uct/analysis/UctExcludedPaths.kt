/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.analysis

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager

/** Paths excluded from UCT analysis: an excluded directory covers everything below it. */
object UctExcludedPaths {
    /** Excluded paths as given and with symlinks resolved, so an exclusion naming a symlink or its target covers both. */
    class Matcher internal constructor(private val prefixes: List<String>) {
        fun matches(file: VirtualFile?): Boolean = file != null && prefixes.isNotEmpty() &&
            listOfNotNull(file.path, file.canonicalPath).any { path -> prefixes.any { FileUtil.isAncestor(it, path, false) } }
    }

    private class Cached(val excludedPaths: List<String>, val stamp: Long, val matcher: Matcher)

    private val CACHED = Key.create<Cached>("magento.uct.excludedPaths")

    /** Resolves symlinks in the excluded paths now; a scan builds it once. */
    @JvmStatic
    fun matcher(excludedPaths: List<String>): Matcher {
        val given = excludedPaths.map(::normalize).filter { it.isNotEmpty() }
        val resolved = given.mapNotNull { LocalFileSystem.getInstance().findFileByPath(it)?.canonicalPath }
        return Matcher((given + resolved).map(::normalize).filter { it.isNotEmpty() }.distinct())
    }

    /**
     * Editor inspections ask for every element, so each project reuses its resolved paths until the excluded paths
     * or the file system structure change; replacing a symlink, as when its target moves, is a structure change.
     */
    @JvmStatic
    fun matcher(project: Project, excludedPaths: List<String>): Matcher {
        val stamp = VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS.modificationCount
        project.getUserData(CACHED)?.takeIf { it.stamp == stamp && it.excludedPaths == excludedPaths }?.let { return it.matcher }
        return matcher(excludedPaths).also { project.putUserData(CACHED, Cached(excludedPaths.toList(), stamp, it)) }
    }

    private fun normalize(path: String): String = path.trim().replace('\\', '/').trimEnd('/')
}
