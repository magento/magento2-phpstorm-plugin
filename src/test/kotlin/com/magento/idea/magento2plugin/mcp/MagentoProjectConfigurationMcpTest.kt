/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.magento.idea.magento2plugin.PhysicalMagentoTestCase
import com.magento.idea.magento2plugin.project.Settings
import org.junit.Test

class MagentoProjectConfigurationMcpTest : PhysicalMagentoTestCase() {

    @Test
    fun testGetStatusWhenNotConfigured() {
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = false
        settings.magentoPath = null

        val status = MagentoProjectConfigurationCommands.getStatus(project)
        assertTrue(status.contains("Magento Project Integration Status"))
        assertTrue(status.contains("Integration enabled: no"))
        assertTrue(status.contains("Configured Magento path: (not configured)"))
        assertTrue(status.contains("Installation type: Not configured"))
        assertTrue(status.contains("Next usable action: Magento integration is disabled"))
    }

    @Test
    fun testGetStatusWhenRecognized() {
        myFixture.addFileToProject("composer.json", """{"name":"magento/project-community-edition","version":"2.4.8"}""")
        myFixture.addFileToProject("composer.lock", """{"packages":[{"name":"magento/product-community-edition","version":"2.4.8"}]}""")
        myFixture.addFileToProject("vendor/magento/framework/composer.json", """{"name":"magento/framework"}""")

        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.setMagentoPath(project.basePath)
        settings.setExcludeGeneratedFiles(true)

        val status = MagentoProjectConfigurationCommands.getStatus(project)
        assertTrue(status.contains("Magento Project Integration Status"))
        assertTrue(status.contains("Integration enabled: yes"))
        assertTrue(status.contains(project.basePath!!))
        assertTrue(status.contains("Recognized Magento 2 application"))
        assertTrue(status.contains("Platform version: 2.4.8"))
        assertTrue(status.contains("Root composer.json: present"))
        assertTrue(status.contains("Framework directory: present"))
        assertTrue(status.contains("Indexing exclusions: enabled"))
        assertTrue(status.contains("Pending index refresh:"))
        assertTrue(status.contains("Next usable action:"))
    }

    @Test
    fun testHelpCommand() {
        val help = MagentoProjectConfigurationCommands.help()
        assertTrue(help.contains("Magento Project Configuration Tool"))
        assertTrue(help.contains("action=status"))
        assertTrue(help.contains("action=configure"))
        assertTrue(help.contains("action=help"))
        assertTrue(help.contains("magentoPath"))
        assertTrue(help.contains("excludeGeneratedFiles"))
    }

    @Test
    fun testConfigureSuccessAndError() {
        myFixture.addFileToProject("composer.json", """{"name":"magento/project-community-edition","version":"2.4.8"}""")
        myFixture.addFileToProject("composer.lock", """{"packages":[{"name":"magento/product-community-edition","version":"2.4.8"}]}""")
        myFixture.addFileToProject("vendor/magento/framework/composer.json", """{"name":"magento/framework"}""")

        val result = MagentoProjectConfigurationCommands.configure(
            project = project,
            action = "configure",
            magentoPath = ".",
            enabled = true,
            excludeGeneratedFiles = true
        )
        assertTrue(result.contains("Magento Project Configuration"))
        assertTrue(result.contains("Result: success"))
        assertTrue(result.contains("Previous State:"))
        assertTrue(result.contains("Current State:"))
        assertTrue(result.contains("Installation recognized: yes"))

        val err = MagentoProjectConfigurationCommands.configure(
            project = project,
            action = "configure",
            magentoPath = "does_not_exist",
            enabled = true
        )
        assertTrue(err.contains("Result: error"))
        assertTrue(err.contains("Validation Failures:"))
        assertTrue(err.contains("does_not_exist"))
        assertTrue(err.contains("Current State:\n  (unchanged - prior settings preserved)"))
    }

    @Test
    fun testUnsupportedActionRejectedWithoutChangingSettings() {
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = false
        settings.magentoPath = null

        val result = MagentoProjectConfigurationCommands.configure(
            project = project,
            action = "preview",
            enabled = false
        )
        assertTrue(result.contains("Result: error"))
        assertTrue(result.contains("Unsupported action 'preview'"))
        assertFalse(settings.pluginEnabled)
    }

    @Test
    fun testControlledConfigurationDuringIndexing() {
        myFixture.addFileToProject("composer.json", """{"name":"magento/project-community-edition","version":"2.4.8"}""")
        myFixture.addFileToProject("composer.lock", """{"packages":[{"name":"magento/product-community-edition","version":"2.4.8"}]}""")
        myFixture.addFileToProject("vendor/magento/framework/composer.json", """{"name":"magento/framework"}""")

        com.intellij.testFramework.DumbModeTestUtils.runInDumbModeSynchronously(project) {
            val status = MagentoProjectConfigurationCommands.getStatus(project)
            assertTrue(status.contains("Pending index refresh: yes"))

            val result = MagentoProjectConfigurationCommands.configure(
                project = project,
                action = "configure",
                magentoPath = ".",
                enabled = true
            )
            assertTrue(result.contains("Result: success"))
            assertTrue(result.contains("Pending index refresh: yes"))
        }
    }

    @Test
    fun testSourceCheckoutWithUnknownVersionRendersUnknownNotAny() {
        myFixture.addFileToProject("lib/internal/Magento/Framework/composer.json", """{"name":"magento/framework"}""")
        myFixture.addFileToProject("app/code/Magento/Catalog/composer.json", """{"name":"magento/module-catalog"}""")
        myFixture.addFileToProject("composer.json", """{"name":"custom/magento-source"}""")
        myFixture.addFileToProject("vendor/autoload.php", "<?php\n")

        val result = MagentoProjectConfigurationCommands.configure(
            project = project,
            action = "configure",
            magentoPath = ".",
            enabled = true,
            excludeGeneratedFiles = true
        )
        assertTrue(result.contains("Result: success"))
        assertTrue(result.contains("Platform version: unknown"))
        assertFalse(result.contains("Platform version: any"))

        val status = MagentoProjectConfigurationCommands.getStatus(project)
        assertTrue(status.contains("Platform version: unknown"))
        assertFalse(status.contains("Platform version: any"))
        assertFalse(status.contains("missing vendor dependencies"))
    }
}
