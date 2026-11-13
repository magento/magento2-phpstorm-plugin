package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.magento.idea.magento2plugin.BaseProjectTestCase
import com.magento.idea.magento2plugin.project.Settings
import com.magento.idea.magento2plugin.project.SettingsForm
import com.magento.idea.magento2plugin.project.indexing.MagentoIndexingExclusionService
import org.junit.Test

class MagentoIndexingExclusionMcpTest : BaseProjectTestCase() {

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

    private fun assertContains(text: String, expected: String) {
        assertTrue("Expected to find <$expected> in:\n$text", text.contains(expected))
    }

    @Test
    fun testGetStatusReportsConfiguredExclusionsAndDiskPresence() {
        val rootPath = getProjectRootPath()
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = rootPath
        settings.setExcludeGeneratedFiles(true)

        MagentoIndexingExclusionService.getInstance(project).applyExclusions()

        val status = MagentoIndexingExclusionCommands.getStatus(project)

        assertContains(status, "Magento Indexing Exclusions Status")
        assertContains(status, "Magento integration enabled: yes")
        assertContains(status, "Exclusion setting enabled: yes")
        assertContains(status, "Configured Magento path: $rootPath")
        assertContains(status, "Effective Excluded Paths")
        assertContains(status, "var/cache — Magento application cache")
        assertContains(status, "var/page_cache — Full page cache")
        assertContains(status, "var/view_preprocessed — Preprocessed view templates and assets")
        assertContains(status, "var/log — Application logs")
        assertContains(status, "var/report — Crash reports")
        assertContains(status, "var/session — File-based session storage")
        assertContains(status, "generated/metadata — Compiled dependency injection metadata")
        assertContains(status, "pub/static — Deployed static view files and frontend assets")
        assertContains(status, "Managed Exclusions: 8 folder URL(s)")
        assertContains(status, "Pending index refresh:")
    }

    @Test
    fun testGetStatusActionableErrorWhenPathInvalidOrUnset() {
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = "/invalid/magento/path"

        val statusInvalid = MagentoIndexingExclusionCommands.getStatus(project)
        assertContains(statusInvalid, "Exclusions cannot be applied: Magento installation path is invalid (/invalid/magento/path).")
        assertContains(statusInvalid, "Configure a valid Magento path before managing indexing exclusions.")

        settings.magentoPath = ""
        val statusEmpty = MagentoIndexingExclusionCommands.getStatus(project)
        assertContains(statusEmpty, "Exclusions cannot be applied: Magento installation path is not configured.")
        assertContains(statusEmpty, "Configure a valid Magento path before managing indexing exclusions.")
    }

    @Test
    fun testConfigurePreviewDoesNotModifySettings() {
        val rootPath = getProjectRootPath()
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = rootPath
        settings.setExcludeGeneratedFiles(true)

        val preview = MagentoIndexingExclusionCommands.configure(
            project = project,
            action = "preview",
            enabled = true,
            additionalPaths = listOf(".runtime")
        )

        assertContains(preview, "Magento Indexing Exclusions Preview (no changes applied)")
        assertContains(preview, "var/cache — Magento application cache")
        assertContains(preview, ".runtime — User-configured project output directory")
        assertContains(preview, "URLs to add: 9")

        // Assert nothing was applied yet
        assertTrue(settings.getManagedExcludedFolders().isEmpty())
    }

    @Test
    fun testConfigureApplyAndRoundTrip() {
        val rootPath = getProjectRootPath()
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = rootPath

        val applyResult = MagentoIndexingExclusionCommands.configure(
            project = project,
            action = "apply",
            enabled = true,
            additionalPaths = listOf(".runtime", "build/tmp")
        )

        assertContains(applyResult, "Magento Indexing Exclusions Apply")
        assertContains(applyResult, "Result: success")
        assertContains(applyResult, "Added exclusions (10):")
        assertContains(applyResult, "+ $rootPath/var/cache")
        assertContains(applyResult, "+ $rootPath/.runtime")
        assertContains(applyResult, "+ $rootPath/build/tmp")

        assertTrue(settings.isExcludeGeneratedFiles())
        assertEquals(listOf(".runtime", "build/tmp"), settings.additionalExcludedPaths)
        assertEquals(10, settings.getManagedExcludedFolders().size)

        // Verify IDE indexing scope
        val runtimeFile = myFixture.addFileToProject(".runtime/cache.dat", "data")
        val cacheFile = myFixture.addFileToProject("var/cache/app.cache", "data")
        val sourceFile = myFixture.findFileInTempDir("app/code/Foo/Bar/etc/module.xml")

        val fileIndex = ProjectFileIndex.getInstance(project)
        assertTrue(fileIndex.isExcluded(runtimeFile.virtualFile))
        assertTrue(fileIndex.isExcluded(cacheFile.virtualFile))
        if (sourceFile != null) {
            assertFalse(fileIndex.isExcluded(sourceFile))
        }

        // Verify UI form round-trip
        val form = SettingsForm(project)
        form.createComponent()
        assertTrue(form.excludeGeneratedFilesCheckBox.isSelected)
        assertEquals(".runtime, build/tmp", form.additionalExcludedPathsField.text)
    }

    @Test
    fun testConfigureResetRemovesManagedExclusions() {
        val rootPath = getProjectRootPath()
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = rootPath

        MagentoIndexingExclusionCommands.configure(
            project = project,
            action = "apply",
            enabled = true,
            additionalPaths = listOf(".runtime")
        )
        assertFalse(settings.getManagedExcludedFolders().isEmpty())

        val resetResult = MagentoIndexingExclusionCommands.configure(
            project = project,
            action = "reset"
        )

        assertContains(resetResult, "Magento Indexing Exclusions Reset")
        assertContains(resetResult, "Result: success")
        assertContains(resetResult, "Removed managed exclusions: 9")

        assertTrue(settings.getManagedExcludedFolders().isEmpty())
        val contentEntryUrls = ModuleRootManager.getInstance(myFixture.module)
            .contentEntries[0].excludeFolderUrls.toList()
        assertFalse(contentEntryUrls.any { it.contains("var/cache") })
        assertFalse(contentEntryUrls.any { it.contains(".runtime") })
    }

    @Test
    fun testConfigureActionValidationAndErrorHandling() {
        val rootPath = getProjectRootPath()
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = rootPath

        val unknownResult = MagentoIndexingExclusionCommands.configure(
            project = project,
            action = "unsupported_action"
        )
        assertContains(unknownResult, "Unknown action \"unsupported_action\"")
        assertContains(unknownResult, "Supported actions are \"apply\", \"update\", \"reset\", and \"preview\"")

        // Invalid Magento path handling
        settings.magentoPath = "/non/existent/path"
        val errorResult = MagentoIndexingExclusionCommands.configure(
            project = project,
            action = "apply"
        )
        assertContains(errorResult, "Error: Magento installation path is not configured or is invalid.")
    }

    @Test
    fun testProjectToolsetExposesIndexingExclusionTools() {
        val toolNames = MagentoMcpProjectToolset::class.java.declaredMethods
            .mapNotNull { it.getAnnotation(com.intellij.mcpserver.annotations.McpTool::class.java)?.name }
            .toSet()

        assertTrue(toolNames.contains("get_magento_indexing_exclusions"))
        assertTrue(toolNames.contains("configure_magento_indexing_exclusions"))

        val getMethod = MagentoMcpProjectToolset::class.java.declaredMethods.first { toolMethod ->
            toolMethod.name == "getMagentoIndexingExclusions" && toolMethod.getAnnotation(com.intellij.mcpserver.annotations.McpDescription::class.java) != null
        }
        val getDescription = getMethod.getAnnotation(com.intellij.mcpserver.annotations.McpDescription::class.java)?.description
        assertNotNull(getDescription)
        assertTrue(getDescription!!.contains("indexing exclusion"))
        assertTrue(getDescription.contains("preview"))

        val configureMethod = MagentoMcpProjectToolset::class.java.declaredMethods.first { toolMethod ->
            toolMethod.name == "configureMagentoIndexingExclusions" && toolMethod.getAnnotation(com.intellij.mcpserver.annotations.McpDescription::class.java) != null
        }
        val configureDescription = configureMethod.getAnnotation(com.intellij.mcpserver.annotations.McpDescription::class.java)?.description
        assertNotNull(configureDescription)
        assertTrue(configureDescription!!.contains("apply"))
        assertTrue(configureDescription.contains("update"))
        assertTrue(configureDescription.contains("reset"))
        assertTrue(configureDescription.contains("preview"))
        assertTrue(configureDescription.contains(".runtime/"))
    }
}
