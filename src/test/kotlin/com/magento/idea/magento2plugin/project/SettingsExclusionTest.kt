/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test

class SettingsExclusionTest : BasePlatformTestCase() {

    @Test
    fun testDefaultStateHasExclusionsEnabledByDefault() {
        val settings = Settings()
        assertTrue("Exclusions should be enabled by default", settings.isExcludeGeneratedFiles())
        assertNull("Raw setting is null initially to distinguish unset from explicit choice", settings.excludeGeneratedFiles)
    }

    @Test
    fun testExplicitOptOutPersistsAcrossReopenAndTogglingIntegration() {
        val settings = Settings()

        // Explicit opt-out
        settings.excludeGeneratedFiles = false
        assertFalse(settings.isExcludeGeneratedFiles())
        assertEquals(java.lang.Boolean.FALSE, settings.excludeGeneratedFiles)

        // Simulate save and reload state
        val state = settings.state
        assertNotNull(state)
        assertEquals(java.lang.Boolean.FALSE, state!!.excludeGeneratedFiles)

        val newSettings = Settings()
        newSettings.loadState(state)
        assertFalse(newSettings.isExcludeGeneratedFiles())
        assertEquals(java.lang.Boolean.FALSE, newSettings.excludeGeneratedFiles)

        // Disable integration and re-enable
        newSettings.pluginEnabled = false
        assertFalse(newSettings.isExcludeGeneratedFiles())

        newSettings.pluginEnabled = true
        assertFalse("Explicit opt-out must not reset when re-enabling integration", newSettings.isExcludeGeneratedFiles())
    }

    @Test
    fun testAdditionalExcludedPathsNormalizationAndParsing() {
        val parsed = Settings.parseAdditionalExcludedPaths(".runtime/, ./var/custom_cache, \\build\\reports\\, ")
        assertEquals(listOf(".runtime", "var/custom_cache", "build/reports"), parsed)
        assertEquals(".runtime, var/custom_cache, build/reports", Settings.getNormalizedAdditionalExcludedPaths(parsed))
    }

    @Test
    fun testManagedExcludedFoldersTracking() {
        val settings = Settings()
        assertNotNull(settings.getManagedExcludedFolders())
        assertTrue(settings.getManagedExcludedFolders().isEmpty())

        settings.managedExcludedFolders = listOf("file:///path/to/var/cache", "file:///path/to/var/log")
        assertEquals(2, settings.getManagedExcludedFolders().size)

        val state = settings.state
        assertNotNull(state)
        assertEquals(listOf("file:///path/to/var/cache", "file:///path/to/var/log"), state!!.managedExcludedFolders)
    }
}
