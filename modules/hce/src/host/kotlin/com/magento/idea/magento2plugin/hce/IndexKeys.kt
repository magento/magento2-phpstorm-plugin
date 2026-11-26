/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.hce

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.search.FilenameIndex
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.ID
import com.magento.idea.magento2plugin.mcp.MagentoIndexKeys

/** HCE restricts global key enumeration; read the registered index's existing per-file data. */
internal object IndexKeys : MagentoIndexKeys {
    private val EXTENSIONS = listOf("xml", "js", "html", "php")

    override fun keys(id: ID<String, *>, project: Project): Collection<String> {
        val extension = FileBasedIndexExtension.EXTENSION_POINT_NAME.extensionList.first { it.name == id }
        val index = FileBasedIndex.getInstance()
        return sortedSetOf<String>().apply {
            for (ext in EXTENSIONS) {
                for (file in FilenameIndex.getAllFilesByExt(project, ext)) {
                    ProgressManager.checkCanceled()
                    if (extension.inputFilter.acceptInput(file)) addAll(index.getFileData(id, file, project).keys)
                }
            }
        }
    }
}
