/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.updates

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.magento.idea.magento2plugin.PhysicalMagentoTestCase
import com.magento.idea.magento2plugin.util.magento.MagentoVersionUtil

class MagentoSourceVersionTest : PhysicalMagentoTestCase() {
    fun testLockFileDetectionWithoutPhpFileTypeAssociation() {
        val fileTypes = FileTypeManager.getInstance()
        val original = fileTypes.getFileTypeByFileName("composer.lock")
        try {
            ApplicationManager.getApplication().runWriteAction(Runnable {
                fileTypes.associatePattern(PlainTextFileType.INSTANCE, "composer.lock")
            })
            val root = myFixture.addFileToProject("composer.lock", """{"packages":[{"name":"mage-os/product-community-edition","version":"3.5.0"}]}""").virtualFile.parent.path
            assertEquals("3.5.0", MagentoVersionUtil.getVersionData(project, root).first)
        } finally {
            ApplicationManager.getApplication().runWriteAction(Runnable {
                fileTypes.associatePattern(original, "composer.lock")
            })
        }
    }

    private fun source(composer: String): String {
        myFixture.addFileToProject("source/app/code/Magento/Store/registration.php", "<?php")
        myFixture.addFileToProject("source/lib/internal/Magento/Framework/registration.php", "<?php")
        return myFixture.addFileToProject("source/composer.json", composer).virtualFile.parent.path
    }

    fun testSourceReleaseVersionUsesTheConfiguredRoot() {
        myFixture.addFileToProject("composer.lock", """{"packages":[{"name":"mage-os/product-community-edition","version":"3.5.0"}]}""")
        val root = source("""{"name":"magento/magento2ce","version":"2.4.8"}""")
        val detected = MagentoVersionUtil.getVersionData(project, root)
        assertEquals("2.4.8", detected.first)
        assertEquals("Magento Open Source", detected.second)
        assertTrue(InstalledMagento.detect(project, root).development)
        assertEquals("any", MagentoVersionUtil.getLockedVersionData(project, root).first)
    }

    fun testSourceBranchWithoutVersionStillDetectsTheEdition() {
        val root = source("""{"name":"magento/magento2ce"}""")
        val detected = MagentoVersionUtil.getVersionData(project, root)
        assertEquals("any", detected.first)
        assertEquals("Magento Open Source", detected.second)
        assertTrue(InstalledMagento.detect(project, root).development)
    }

    fun testCommerceSourceVersion() {
        val root = source("""{"name":"magento/magento2ee","version":"2.4.9-dev"}""")
        val detected = MagentoVersionUtil.getVersionData(project, root)
        assertEquals("2.4.9-dev", detected.first)
        assertEquals("Adobe Commerce", detected.second)
    }

    fun testProjectTemplateVersionNeverOverridesInstalledProduct() {
        val root = source("""{"name":"magento/project-community-edition","version":"2.4.0"}""")
        myFixture.addFileToProject("source/composer.lock", """{"packages":[{"name":"magento/product-community-edition","version":"2.4.8-p5"}]}""")
        assertEquals("2.4.8-p5", MagentoVersionUtil.getVersionData(project, root).first)
        assertFalse(InstalledMagento.detect(project, root).development)
    }

    fun testSourceNameWithoutCoreSourceDirectoriesIsNotEnough() {
        val root = myFixture.addFileToProject("composer.json", """{"name":"magento/magento2ce","version":"2.4.8"}""").virtualFile.parent.path
        assertNull(MagentoVersionUtil.getSourceVersionData(project, root))
        assertEquals("any", MagentoVersionUtil.getVersionData(project, root).first)
    }
}
