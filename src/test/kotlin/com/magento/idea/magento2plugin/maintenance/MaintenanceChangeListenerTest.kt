/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.maintenance

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.vfs.VirtualFileManager
import com.magento.idea.magento2plugin.PhysicalMagentoTestCase
import com.magento.idea.magento2plugin.mcp.MaintenanceSources

class MaintenanceChangeListenerTest : PhysicalMagentoTestCase() {
    fun testDeletingLayoutDirectoryRecordsItsXmlFiles() {
        val path = "app/code/Acme/News/view/frontend/layout"
        val layout = myFixture.addFileToProject("$path/default.xml", "<page/>").virtualFile.parent
        myFixture.addFileToProject("$path/news_index_index.xml", "<page/>")
        myFixture.addFileToProject("$path/README.md", "Layout notes")
        val batches = listen()

        WriteAction.runAndWait<Throwable> { layout.delete(this) }

        val change = batches.single()
        assertEquals(setOf("$path/default.xml", "$path/news_index_index.xml"), change.files)
        assertEquals(setOf("cache_clean"), change.files.map(MaintenanceSources::checkId).toSet())
        assertFalse(change.limited)
    }

    fun testMovingSetupPatchDirectoryRecordsNewAndOldPaths() {
        val module = "app/code/Acme/News"
        val staged = myFixture.addFileToProject("$module/staging/Release/AddFeed.php", "<?php").virtualFile.parent
        val data = myFixture.addFileToProject("$module/Setup/Patch/Data/Existing.php", "<?php").virtualFile.parent
        val schema = myFixture.addFileToProject("$module/Setup/Patch/Schema/Existing.php", "<?php").virtualFile.parent
        val batches = listen()

        WriteAction.runAndWait<Throwable> { staged.move(this, data) }
        WriteAction.runAndWait<Throwable> { staged.move(this, schema) }

        val oldPath = "$module/Setup/Patch/Data/Release/AddFeed.php"
        val newPath = "$module/Setup/Patch/Schema/Release/AddFeed.php"
        assertEquals(listOf(setOf(oldPath), setOf(oldPath, newPath)), batches.map { it.files })
        assertEquals(setOf("setup_upgrade"), batches.flatMap { it.files }.map(MaintenanceSources::checkId).toSet())
    }

    fun testDirectoryExpansionReportsThePathLimit() {
        val path = "app/code/Acme/News/view/frontend/layout"
        val layout = myFixture.addFileToProject("$path/default.xml", "<page/>").virtualFile.parent
        WriteAction.runAndWait<Throwable> {
            repeat(MAINTENANCE_PATH_LIMIT + 1) { index -> layout.createChildData(this, "handle_$index.xml") }
        }
        val batches = listen()

        WriteAction.runAndWait<Throwable> { layout.delete(this) }

        val change = batches.single()
        assertTrue(change.limited)
        assertEquals(MAINTENANCE_PATH_LIMIT, change.files.size)
    }

    private fun listen(): List<MaintenanceFileChanges> {
        val batches = mutableListOf<MaintenanceFileChanges>()
        project.messageBus.connect(testRootDisposable).subscribe(
            VirtualFileManager.VFS_CHANGES,
            MaintenanceChangeListener({ project.basePath }) { batches += it }
        )
        return batches
    }
}
