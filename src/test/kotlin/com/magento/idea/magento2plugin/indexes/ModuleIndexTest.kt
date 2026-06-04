/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.indexes

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.PlatformTestUtil
import com.magento.idea.magento2plugin.BaseProjectTestCase
import com.magento.idea.magento2plugin.project.Settings
import org.junit.Test

class ModuleIndexTest : BaseProjectTestCase() {
    @Test
    fun testGetEditableThemeNamesUsesThemeXmlWithoutRegistrationPhp() {
        myFixture.addFileToProject(
            "app/design/frontend/Foo/bar/theme.xml",
            """
            <?xml version="1.0"?>
            <theme xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:noNamespaceSchemaLocation="urn:magento:framework:Config/etc/theme.xsd">
                <title>Foo Bar</title>
            </theme>
            """.trimIndent()
        )

        prepareIndexWithWindowsStyleMagentoRoot()

        val themeNames = ModuleIndex(project).editableThemeNames

        assertTrue(
            "Expected editable theme names to contain frontend/Foo/bar, got: $themeNames",
            themeNames.contains("frontend/Foo/bar")
        )
    }

    @Test
    fun testGetEditableModuleNamesUsesModuleXmlWithoutRegistrationPhp() {
        myFixture.addFileToProject(
            "app/code/Foo/XmlOnly/etc/module.xml",
            """
            <?xml version="1.0"?>
            <config xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:noNamespaceSchemaLocation="urn:magento:framework:Module/etc/module.xsd">
                <module name="Foo_XmlOnly"/>
            </config>
            """.trimIndent()
        )

        prepareIndexWithWindowsStyleMagentoRoot()

        val moduleIndex = ModuleIndex(project)
        val moduleNames = moduleIndex.editableModuleNames

        assertTrue(
            "Expected editable module names to contain Foo_XmlOnly, got: $moduleNames",
            moduleNames.contains("Foo_XmlOnly")
        )
        assertNotNull(moduleIndex.getModuleDirectoryByModuleName("Foo_XmlOnly"))
    }

    private fun prepareIndexWithWindowsStyleMagentoRoot() {
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        val settings = Settings.getInstance(project)
        val state = settings.state ?: error("Expected settings state to be available")
        state.magentoPath = "\\src"
        settings.loadState(state)
    }
}
