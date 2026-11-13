package com.magento.idea.magento2plugin.project

import com.intellij.openapi.application.WriteAction
import com.magento.idea.magento2plugin.PhysicalMagentoProjectTestCase
import com.magento.idea.magento2plugin.project.indexing.MagentoIndexingExclusionService
import org.junit.Test

class SettingsFormExclusionTest : PhysicalMagentoProjectTestCase() {

    override fun setUp() {
        super.setUp()
        MagentoIndexingExclusionService.getInstance(project).resetExclusions()
    }

    override fun tearDown() {
        try {
            MagentoIndexingExclusionService.getInstance(project).resetExclusions()
        } finally {
            super.tearDown()
        }
    }

    private fun getProjectRootPath(): String {
        return myFixture.findFileInTempDir("")!!.path
    }

    @Test
    fun testDefaultCheckboxSelectedOnFreshProject() {
        val settings = Settings.getInstance(project)
        settings.setExcludeGeneratedFiles(null)

        val form = SettingsForm(project)
        form.createComponent()

        assertTrue(form.excludeGeneratedFilesCheckBox.isSelected)
    }

    @Test
    fun testInvalidPathShowsInstructionInEffectiveExclusions() {
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.setExcludeGeneratedFiles(true)

        val form = SettingsForm(project)
        form.createComponent()

        form.magentoPathField.text = "/non/existent/magento/dir"
        form.updateEffectiveExclusionsDisplay()

        val display = form.effectiveExclusionsArea.text
        assertTrue(display.contains("Configure a valid Magento installation path before indexing exclusions can be applied."))
    }

    @Test
    fun testValidPathDisplaysExclusionsAndReasons() {
        val rootPath = getProjectRootPath()
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = rootPath
        settings.setExcludeGeneratedFiles(true)

        val form = SettingsForm(project)
        form.createComponent()
        form.magentoPathField.text = rootPath
        form.updateEffectiveExclusionsDisplay()

        val display = form.effectiveExclusionsArea.text
        assertTrue(display.contains("var/cache — Magento application cache"))
        assertTrue(display.contains("var/page_cache — Full page cache"))
        assertTrue(display.contains("var/view_preprocessed — Preprocessed view templates and assets"))
        assertTrue(display.contains("var/log — Application logs"))
        assertTrue(display.contains("var/report — Crash reports"))
        assertTrue(display.contains("var/session — File-based session storage"))
        assertTrue(display.contains("generated/metadata — Compiled dependency injection metadata"))
        assertTrue(display.contains("pub/static — Deployed static view files and frontend assets"))
    }

    @Test
    fun testTogglingCheckboxUpdatesDisplayAndPersistsOptOut() {
        val rootPath = getProjectRootPath()
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = rootPath
        settings.setExcludeGeneratedFiles(true)

        val form = SettingsForm(project)
        form.createComponent()
        form.magentoPathField.text = rootPath

        // Uncheck the exclusion checkbox
        form.excludeGeneratedFilesCheckBox.isSelected = false
        form.updateEffectiveExclusionsDisplay()

        val display = form.effectiveExclusionsArea.text
        assertTrue(display.contains("Indexing exclusions are disabled."))

        // Apply settings
        form.apply()
        assertFalse(settings.isExcludeGeneratedFiles())

        // Recreate form to simulate reopen
        val reopenedForm = SettingsForm(project)
        reopenedForm.createComponent()
        assertFalse(reopenedForm.excludeGeneratedFilesCheckBox.isSelected)
        assertTrue(reopenedForm.effectiveExclusionsArea.text.contains("Indexing exclusions are disabled."))
    }

    @Test
    fun testAdditionalExcludedPathsInForm() {
        val rootPath = getProjectRootPath()
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = rootPath
        settings.setExcludeGeneratedFiles(true)

        val form = SettingsForm(project)
        form.createComponent()
        form.magentoPathField.text = rootPath
        form.additionalExcludedPathsField.text = ".runtime, var/custom_cache"
        form.updateEffectiveExclusionsDisplay()

        val display = form.effectiveExclusionsArea.text
        assertTrue(display.contains(".runtime — User-configured project output directory"))
        assertTrue(display.contains("var/custom_cache — User-configured project output directory"))

        form.apply()
        assertEquals(listOf(".runtime", "var/custom_cache"), settings.additionalExcludedPaths)
        val managed = settings.getManagedExcludedFolders()
        assertTrue(managed.any { it.endsWith("/.runtime") })
        assertTrue(managed.any { it.endsWith("/var/custom_cache") })
    }
}
