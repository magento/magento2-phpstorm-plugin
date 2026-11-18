/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.maintenance

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileCopyEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import com.magento.idea.magento2plugin.mcp.MaintenanceSources
import java.io.IOException

internal const val MAINTENANCE_PATH_LIMIT = 512

internal data class MaintenanceFileChanges(
    val root: String,
    val files: Set<String>,
    val limited: Boolean,
    val enabledModules: Set<String>?,
    val newlyEnabledModules: Set<String>
)

internal class MaintenanceChangeListener(
    private val magentoRoot: () -> String?,
    private val onChange: (MaintenanceFileChanges) -> Unit
) : BulkFileListener {
    private var beforePaths: ChangedPaths? = null
    private var beforeModules: Set<String>? = null

    override fun before(events: List<VFileEvent>) {
        beforePaths = null
        beforeModules = null
        val root = magentoRoot() ?: return
        val paths = ChangedPaths(root)
        for (event in events) {
            when (event) {
                is VFileDeleteEvent, is VFileMoveEvent -> paths.record(event.file, descend = true)
                is VFilePropertyChangeEvent -> if (event.isRename) paths.record(event.file, descend = true)
            }
        }
        if (paths.moduleListChanged || events.any { it.path == "${paths.rootPrefix}$MODULE_LIST" }) {
            beforeModules = enabledModules(paths)
        }
        beforePaths = paths
    }

    override fun after(events: List<VFileEvent>) {
        val previousPaths = beforePaths
        val previousModules = beforeModules
        beforePaths = null
        beforeModules = null
        val root = magentoRoot() ?: return
        val sameRoot = previousPaths?.root == root
        val paths = previousPaths?.takeIf { sameRoot } ?: ChangedPaths(root)
        for (event in events) {
            when (event) {
                is VFileDeleteEvent -> Unit
                is VFileCopyEvent -> paths.record(event.findCreatedFile(), descend = true)
                is VFileCreateEvent, is VFileMoveEvent -> paths.record(event.file, descend = true)
                is VFilePropertyChangeEvent -> paths.record(event.file, descend = event.isRename)
                else -> paths.record(event.file, descend = false)
            }
        }
        if (paths.files.isEmpty()) return
        val current = if (paths.moduleListChanged) enabledModules(paths).orEmpty() else null
        val added = if (sameRoot && current != null && previousModules != null) current - previousModules else emptySet()
        onChange(MaintenanceFileChanges(root, paths.files, paths.limited, current, added))
    }

    private fun enabledModules(paths: ChangedPaths): Set<String>? {
        val file = LocalFileSystem.getInstance().findFileByPath("${paths.rootPrefix}$MODULE_LIST") ?: return null
        if (file.isDirectory || file.length > 1024 * 1024) return null
        return try {
            MaintenanceSources.enabledModules(file.contentsToByteArray().toString(Charsets.UTF_8))
        } catch (_: IOException) {
            null
        }
    }

    private class ChangedPaths(val root: String) {
        val rootPrefix = root.trimEnd('/') + "/"
        val files = LinkedHashSet<String>()
        var limited = false
            private set
        var moduleListChanged = false
            private set

        fun record(file: VirtualFile?, descend: Boolean) {
            if (file == null || !file.isValid) return
            if (!file.isDirectory) {
                recordFile(file.path)
            } else if (descend && underOrAboveRoot(file)) {
                var matched = 0
                val completed = VfsUtilCore.iterateChildrenRecursively(file, ::underOrAboveRoot, { child ->
                    if (!child.isDirectory && recordFile(child.path)) matched++
                    matched <= MAINTENANCE_PATH_LIMIT
                }, VirtualFileVisitor.NO_FOLLOW_SYMLINKS)
                if (!completed) limited = true
            }
        }

        private fun underOrAboveRoot(file: VirtualFile): Boolean =
            file.path.startsWith(rootPrefix) || rootPrefix.startsWith(file.path.trimEnd('/') + "/")

        private fun recordFile(path: String): Boolean {
            if (!path.startsWith(rootPrefix)) return false
            val relative = path.removePrefix(rootPrefix)
            if (MaintenanceSources.checkId(relative) == null || !MaintenanceSources.validPath(relative)) return false
            if (relative == MODULE_LIST) moduleListChanged = true
            files.remove(relative)
            files.add(relative)
            if (files.size > MAINTENANCE_PATH_LIMIT) {
                files.remove(files.first())
                limited = true
            }
            return true
        }
    }

    private companion object {
        const val MODULE_LIST = "app/etc/config.php"
    }
}
