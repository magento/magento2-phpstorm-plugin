/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project

import com.intellij.util.xmlb.XmlSerializer
import com.magento.idea.magento2plugin.PhysicalMagentoTestCase
import com.magento.idea.magento2plugin.project.MagentoProjectConfigurationService.InstallationType
import org.junit.Test

class MagentoProjectConfigurationServiceTest : PhysicalMagentoTestCase() {

    private fun service(): MagentoProjectConfigurationService =
        MagentoProjectConfigurationService.getInstance(project)

    @Test
    fun testResolvePathRelativeAndNested() {
        val basePath = project.basePath!!
        val s = service()

        assertEquals(basePath, s.resolvePath("."))
        assertNull(s.resolvePath(""))
        assertEquals(basePath, s.resolvePath("./"))
        assertEquals("$basePath/src", s.resolvePath("src"))
        assertEquals("$basePath/src", s.resolvePath("./src/"))
        assertEquals("$basePath/nested/dir", s.resolvePath("nested/dir/"))
    }

    @Test
    fun testRejectEmptyPathWhenEnabling() {
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = false
        settings.magentoPath = null

        val result = service().configure("", true, null, null)

        assertFalse(result.success)
        assertTrue(result.message.contains("cannot be empty when enabling"))
        // Assert settings were NOT partially changed
        assertFalse(settings.pluginEnabled)
        assertNull(settings.magentoPath)
    }

    @Test
    fun testRejectNonexistentPathWithoutChangingSettings() {
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = false
        settings.magentoPath = "/original/path"

        val result = service().configure("nonexistent_subfolder", true, null, null)

        assertFalse(result.success)
        assertTrue(result.validationErrors.any { it.contains("does not exist") })
        assertFalse(settings.pluginEnabled)
        assertEquals("/original/path", settings.magentoPath)
    }

    @Test
    fun testRejectFileInsteadOfDirectory() {
        myFixture.addFileToProject("regular_file.txt", "hello")
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = false
        settings.magentoPath = null

        val result = service().configure("regular_file.txt", true, null, null)

        assertFalse(result.success)
        assertTrue(result.validationErrors.any { it.contains("file, not a directory") })
        assertFalse(settings.pluginEnabled)
    }

    @Test
    fun testRejectProjectMismatch() {
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = false

        val result = service().configure(".", true, null, "/some/different/project")

        assertFalse(result.success)
        assertTrue(result.validationErrors.any { it.contains("projectPath does not match") })
        assertFalse(settings.pluginEnabled)
    }

    @Test
    fun testRecognizedCompleteInstallation() {
        myFixture.addFileToProject("composer.json", """{"name":"magento/project-community-edition","version":"2.4.8"}""")
        myFixture.addFileToProject("composer.lock", """{"packages":[{"name":"magento/product-community-edition","version":"2.4.8"}]}""")
        myFixture.addFileToProject("vendor/magento/framework/composer.json", """{"name":"magento/framework"}""")
        myFixture.addFileToProject("vendor/autoload.php", "<?php\n")

        val result = service().configure(".", true, null, null)

        assertTrue(result.success)
        assertEquals(InstallationType.RECOGNIZED_APPLICATION, result.currentState.installation.type)
        assertEquals(project.basePath, result.currentState.resolvedPath)
        assertTrue(result.currentState.enabled)
        // Default exclusion setting selected
        assertEquals(true, result.currentState.excludeGeneratedFiles)
        assertTrue(result.currentState.installation.missingDependencies.isEmpty())
    }

    @Test
    fun testSupportedPartialCheckoutWithMissingDependencies() {
        myFixture.addFileToProject("composer.json", """{"name":"magento/project-community-edition"}""")
        // Note: vendor/magento/framework is NOT present!

        val result = service().configure(".", true, null, null)

        assertTrue(result.success)
        assertEquals(InstallationType.PARTIAL_CHECKOUT, result.currentState.installation.type)
        assertTrue(result.currentState.installation.missingDependencies.contains("vendor/magento/framework"))
        assertTrue(result.nextUsableAction.contains("composer install"))
    }

    @Test
    fun testExplicitExclusionOptOut() {
        myFixture.addFileToProject("composer.json", """{"name":"magento/project-community-edition","version":"2.4.8"}""")
        myFixture.addFileToProject("vendor/magento/framework/composer.json", """{"name":"magento/framework"}""")

        val result = service().configure(".", true, false, null)

        assertTrue(result.success)
        assertEquals(false, result.currentState.excludeGeneratedFiles)
        assertFalse(Settings.getInstance(project).isExcludeGeneratedFiles)
    }

    @Test
    fun testPreserveExistingUserExclusionChoice() {
        myFixture.addFileToProject("composer.json", """{"name":"magento/project-community-edition","version":"2.4.8"}""")
        myFixture.addFileToProject("vendor/magento/framework/composer.json", """{"name":"magento/framework"}""")
        val settings = Settings.getInstance(project)
        settings.setExcludeGeneratedFiles(false)

        val result = service().configure(".", true, null, null)

        assertTrue(result.success)
        assertEquals(false, result.currentState.excludeGeneratedFiles)
        assertFalse(settings.isExcludeGeneratedFiles)
    }

    @Test
    fun testRepeatedConfigurationIsIdempotent() {
        myFixture.addFileToProject("composer.json", """{"name":"magento/project-community-edition","version":"2.4.8"}""")
        myFixture.addFileToProject("vendor/magento/framework/composer.json", """{"name":"magento/framework"}""")

        val first = service().configure(".", true, true, null)
        assertTrue(first.success)
        assertTrue(first.reindexExecuted)

        val second = service().configure(".", true, true, null)
        assertTrue(second.success)
        assertFalse(second.reindexExecuted)
        assertTrue(second.message.contains("no changes required"))
    }

    @Test
    fun testSettingsSurvivePersistenceSerialization() {
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.setMagentoPath(project.basePath)
        settings.setExcludeGeneratedFiles(true)

        val serialized = XmlSerializer.serialize(settings.state!!)
        val reloaded = Settings()
        reloaded.loadState(XmlSerializer.deserialize(serialized, Settings.State::class.java))

        assertTrue(reloaded.pluginEnabled)
        assertEquals(project.basePath, reloaded.magentoPath)
        assertTrue(reloaded.isExcludeGeneratedFiles)
    }

    @Test
    fun testSourceCheckoutWithoutInstalledDependenciesReportsMissingVendorDependencies() {
        myFixture.addFileToProject("composer.json", """{"name":"magento/magento2ce","version":"2.4.8"}""")
        myFixture.addFileToProject("app/code/Magento/Catalog/registration.php", "<?php\n")
        myFixture.addFileToProject("lib/internal/Magento/Framework/registration.php", "<?php\n")
        // Note: vendor/autoload.php is NOT present!

        val details = service().inspectPath(".")
        assertEquals(InstallationType.SOURCE_CHECKOUT, details.type)
        assertTrue(details.missingDependencies.any { it.contains("vendor dependencies") })
        assertTrue(details.summary.contains("missing vendor dependencies"))

        val result = service().configure(".", true, null, null)
        assertTrue(result.success)
        assertEquals(InstallationType.SOURCE_CHECKOUT, result.currentState.installation.type)
        assertTrue(result.currentState.installation.missingDependencies.any { it.contains("vendor dependencies") })
        assertTrue(result.nextUsableAction.contains("composer install"))
    }

    @Test
    fun testReenablingWithDeletedOrInvalidSavedPathFailsValidation() {
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = false
        settings.magentoPath = "/path/that/does/not/exist/anywhere"

        val result = service().configure(null, true, null, null)
        assertFalse(result.success)
        assertTrue(result.validationErrors.any { it.contains("does not exist") })
        assertFalse(settings.pluginEnabled)
        assertTrue(result.nextUsableAction.contains("Provide a valid path"))
    }

    @Test
    fun testPreserveMftfPreferenceWhenChangingRootOrExclusions() {
        myFixture.addFileToProject("composer.json", """{"name":"magento/project-community-edition","version":"2.4.8"}""")
        myFixture.addFileToProject("vendor/magento/framework/composer.json", """{"name":"magento/framework"}""")

        val settings = Settings.getInstance(project)
        settings.mftfSupportEnabled = false

        val result = service().configure(".", true, true, null)
        assertTrue(result.success)
        assertFalse(settings.mftfSupportEnabled)
    }

    @Test
    fun testDisablingIntegrationPreservesSavedExclusions() {
        myFixture.addFileToProject("composer.json", """{"name":"magento/project-community-edition","version":"2.4.8"}""")
        myFixture.addFileToProject("vendor/magento/framework/composer.json", """{"name":"magento/framework"}""")

        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.setMagentoPath(project.basePath)
        settings.setExcludeGeneratedFiles(false)
        settings.setAdditionalExcludedPaths(listOf(".runtime/"))

        // Disable integration
        val disabledResult = service().configure(null, false, null, null)
        assertTrue(disabledResult.success)
        assertFalse(settings.pluginEnabled)
        // Saved preferences MUST NOT be deleted
        assertEquals(false, settings.excludeGeneratedFiles)
        assertEquals(listOf(".runtime/"), settings.additionalExcludedPaths)

        // Re-enable integration without supplying exclusion parameter
        val enabledResult = service().configure(null, true, null, null)
        assertTrue(enabledResult.success)
        assertTrue(settings.pluginEnabled)
        // Saved preferences remain intact
        assertEquals(false, settings.excludeGeneratedFiles)
        assertEquals(listOf(".runtime/"), settings.additionalExcludedPaths)
    }

    @Test
    fun testSettingsChangeListenerFiresOnOrdinaryMcpUpdate() {
        myFixture.addFileToProject("composer.json", """{"name":"magento/project-community-edition","version":"2.4.8"}""")
        myFixture.addFileToProject("vendor/magento/framework/composer.json", """{"name":"magento/framework"}""")

        val settings = Settings.getInstance(project)
        settings.pluginEnabled = false
        settings.magentoPath = null

        var listenerFired = false
        var observedEnabled = false
        var observedPath: String? = null

        settings.addListener { state, _ ->
            listenerFired = true
            observedEnabled = state.isPluginEnabled
            observedPath = state.magentoPath
        }

        val result = service().configure(".", true, true, null)
        assertTrue(result.success)
        assertTrue(listenerFired)
        assertTrue(observedEnabled)
        assertEquals(project.basePath, observedPath)
    }

    @Test
    fun testSourceCheckoutWithInstalledDependenciesAndUnknownVersionDoesNotReportMissingVendorDependencies() {
        // composer.json without version field
        myFixture.addFileToProject("composer.json", """{"name":"magento/magento2ce"}""")
        myFixture.addFileToProject("app/code/Magento/Catalog/registration.php", "<?php\n")
        myFixture.addFileToProject("lib/internal/Magento/Framework/registration.php", "<?php\n")
        myFixture.addFileToProject("vendor/autoload.php", "<?php\n")

        val details = service().inspectPath(".")
        assertEquals(InstallationType.SOURCE_CHECKOUT, details.type)
        assertTrue(details.missingDependencies.isEmpty())
        assertTrue(details.summary.contains("with installed dependencies"))
        assertTrue(details.summary.contains("version unknown"))

        val result = service().configure(".", true, null, null)
        assertTrue(result.success)
        assertEquals(InstallationType.SOURCE_CHECKOUT, result.currentState.installation.type)
        assertTrue(result.currentState.installation.missingDependencies.isEmpty())
        assertFalse(result.nextUsableAction.contains("composer install"))
        assertTrue(result.nextUsableAction.contains("magento_inspect"))
    }

    @Test
    fun testRejectUnrecognizedDirectoryWhenEnablingIntegration() {
        myFixture.addFileToProject("not_magento/some_file.txt", "hello")

        val result = service().configure("not_magento", true, null, null)
        assertFalse(result.success)
        assertTrue(result.validationErrors.any { it.contains("no recognized Magento structure") })
        assertTrue(result.nextUsableAction.contains("Verify the Magento root directory path"))
    }
}
