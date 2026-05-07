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
    fun testGetEditableThemeNamesSupportsWindowsStyleMagentoRootFromStoredSettings() {
        myFixture.addFileToProject(
            "app/design/frontend/Foo/bar/registration.php",
            """
            <?php

            use Magento\Framework\Component\ComponentRegistrar;

            ComponentRegistrar::register(
                ComponentRegistrar::THEME,
                'frontend/Foo/bar',
                __DIR__
            );
            """.trimIndent()
        )
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
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        val settings = Settings.getInstance(project)
        val state = settings.state ?: error("Expected settings state to be available")
        state.magentoPath = "\\src"
        settings.loadState(state)

        val themeNames = ModuleIndex(project).editableThemeNames

        assertTrue(
            "Expected editable theme names to contain frontend/Foo/bar, got: $themeNames",
            themeNames.contains("frontend/Foo/bar")
        )
    }
}
