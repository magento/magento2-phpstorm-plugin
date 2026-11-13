package com.magento.idea.magento2plugin.project.indexing

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.PlatformTestUtil
import com.magento.idea.magento2plugin.BaseProjectTestCase
import com.magento.idea.magento2plugin.project.Settings
import org.junit.Test
import java.io.File
import java.nio.file.Files

class MagentoIndexingExclusionServiceTest : BaseProjectTestCase() {

    override fun setUp() {
        super.setUp()
        getService().resetExclusions()
    }

    override fun tearDown() {
        try {
            getService().resetExclusions()
        } finally {
            super.tearDown()
        }
    }

    private fun getProjectRootPath(): String {
        return myFixture.findFileInTempDir("")!!.path
    }

    private fun getService(): MagentoIndexingExclusionService {
        return MagentoIndexingExclusionService.getInstance(project)
    }

    @Test
    fun testDefaultCandidatesExcludedWhenEnabled() {
        val rootPath = getProjectRootPath()
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = rootPath
        settings.setExcludeGeneratedFiles(true)

        val service = getService()
        val result = service.applyExclusions()

        assertTrue(result.success())
        assertFalse(result.addedPaths().isEmpty())

        val managed = settings.getManagedExcludedFolders()
        assertNotNull(managed)
        assertTrue(managed.any { it.contains("var/cache") })
        assertTrue(managed.any { it.contains("var/page_cache") })
        assertTrue(managed.any { it.contains("var/view_preprocessed") })
        assertTrue(managed.any { it.contains("var/log") })
        assertTrue(managed.any { it.contains("var/report") })
        assertTrue(managed.any { it.contains("var/session") })
        assertTrue(managed.any { it.contains("generated/metadata") })
        assertTrue(managed.any { it.contains("pub/static") })

        // Create a file in var/cache and assert ProjectFileIndex marks it as excluded
        val cacheFile = myFixture.addFileToProject("var/cache/test_cache.php", "<?php // cache")
        val fileIndex = ProjectFileIndex.getInstance(project)
        assertTrue(fileIndex.isExcluded(cacheFile.virtualFile))

        // Assert source files are NOT excluded
        val sourceFile = myFixture.findFileInTempDir("app/code/Foo/Bar/etc/module.xml")
        if (sourceFile != null) {
            assertFalse(fileIndex.isExcluded(sourceFile))
        }
    }

    @Test
    fun testProtectedDirectoriesNeverExcluded() {
        val rootPath = getProjectRootPath()
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = rootPath
        settings.setExcludeGeneratedFiles(true)
        settings.additionalExcludedPaths = listOf("app/code", "vendor", "lib/web", "generated/code", "dev/tests")

        val service = getService()
        val preview = service.previewExclusions(null, null)

        val skippedPaths = preview.skippedEntries().map { it.path() }
        assertTrue(skippedPaths.any { it.endsWith("/app/code") })
        assertTrue(skippedPaths.any { it.endsWith("/vendor") })
        assertTrue(skippedPaths.any { it.endsWith("/lib/web") })
        assertTrue(skippedPaths.any { it.endsWith("/generated/code") })
        assertTrue(skippedPaths.any { it.endsWith("/dev/tests") })

        val result = service.applyExclusions()
        assertTrue(result.success())

        val managed = settings.getManagedExcludedFolders()
        assertFalse(managed.any { it.endsWith("/app/code") })
        assertFalse(managed.any { it.endsWith("/vendor") })
    }

    @Test
    fun testAdditionalProjectRelativePathOutsideNestedRoot() {
        val projectRoot = getProjectRootPath()
        // Create nested root directory
        val nestedMagentoDir = myFixture.tempDirFixture.findOrCreateDir("magento_root")
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = nestedMagentoDir.path
        settings.setExcludeGeneratedFiles(true)
        settings.additionalExcludedPaths = listOf(".runtime", "custom_output")

        val service = getService()
        val result = service.applyExclusions()

        assertTrue(result.success())
        val managed = settings.getManagedExcludedFolders()
        assertTrue(managed.any { it.endsWith("/.runtime") })
        assertTrue(managed.any { it.endsWith("/custom_output") })
        // Magento default candidates are relative to nested magento root
        assertTrue(managed.any { it.contains("/magento_root/var/cache") })

        // Test indexing scope
        val runtimeFile = myFixture.addFileToProject(".runtime/data.tmp", "temp data")
        assertTrue(ProjectFileIndex.getInstance(project).isExcluded(runtimeFile.virtualFile))
    }

    @Test
    fun testMissingOrInvalidMagentoPathPreventsExclusion() {
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = "/invalid/nonexistent/magento/path"
        settings.setExcludeGeneratedFiles(true)

        val service = getService()
        val result = service.applyExclusions()

        assertFalse(result.success())
        assertTrue(result.message().contains("invalid") || result.message().contains("not configured"))
        assertTrue(result.addedPaths().isEmpty())

        val preview = service.previewExclusions(null, null)
        assertFalse(preview.magentoPathValid())
        assertTrue(preview.statusMessage().contains("Configure a valid Magento installation path"))
    }

    @Test
    fun testPreExistingUserExclusionsPreserved() {
        val rootPath = getProjectRootPath()
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = rootPath
        settings.setExcludeGeneratedFiles(true)

        // Manually add user exclusion to content entry
        val userExcludedDir = myFixture.tempDirFixture.findOrCreateDir("user_custom_excluded")
        val userExcludedUrl = userExcludedDir.url

        WriteAction.runAndWait<Throwable> {
            val moduleModel = ModuleRootManager.getInstance(myFixture.module).modifiableModel
            val contentEntry = moduleModel.contentEntries[0]
            contentEntry.addExcludeFolder(userExcludedUrl)
            moduleModel.commit()
        }

        val service = getService()
        service.applyExclusions()

        // Verify user exclusion is still present alongside managed exclusions
        val currentExcludeUrls = ModuleRootManager.getInstance(myFixture.module)
            .contentEntries[0].excludeFolderUrls.toList()
        assertTrue(currentExcludeUrls.contains(userExcludedUrl))
        assertFalse(settings.getManagedExcludedFolders().contains(userExcludedUrl))

        // Reset or disable exclusions
        service.resetExclusions()

        // User exclusion MUST still be present!
        val afterResetUrls = ModuleRootManager.getInstance(myFixture.module)
            .contentEntries[0].excludeFolderUrls.toList()
        assertTrue(afterResetUrls.contains(userExcludedUrl))
        // But managed exclusions must be gone
        assertTrue(settings.getManagedExcludedFolders().isEmpty())
    }

    @Test
    fun testRepeatedApplicationIsIdempotent() {
        val rootPath = getProjectRootPath()
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = rootPath
        settings.setExcludeGeneratedFiles(true)

        val service = getService()
        val firstResult = service.applyExclusions()
        assertTrue(firstResult.success())
        assertFalse(firstResult.addedPaths().isEmpty())

        val secondResult = service.applyExclusions()
        assertTrue(secondResult.success())
        assertTrue(secondResult.addedPaths().isEmpty())
        assertTrue(secondResult.removedPaths().isEmpty())
        assertEquals(firstResult.addedPaths().size, secondResult.unchangedPaths().size)
    }

    @Test
    fun testChangingMagentoRootRemovesOldManagedExclusions() {
        val rootPathA = myFixture.tempDirFixture.findOrCreateDir("magentoA").path
        val rootPathB = myFixture.tempDirFixture.findOrCreateDir("magentoB").path

        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = rootPathA
        settings.setExcludeGeneratedFiles(true)

        val service = getService()
        service.applyExclusions()

        val managedA = settings.getManagedExcludedFolders().toList()
        assertTrue(managedA.all { it.contains("magentoA") })

        // Switch to rootPathB
        settings.magentoPath = rootPathB
        val resultB = service.applyExclusions()

        assertTrue(resultB.success())
        val managedB = settings.getManagedExcludedFolders().toList()
        assertTrue(managedB.all { it.contains("magentoB") })

        // Assert old URLs from rootA are removed
        val contentEntryUrls = ModuleRootManager.getInstance(myFixture.module)
            .contentEntries[0].excludeFolderUrls.toList()
        assertFalse(contentEntryUrls.any { it.contains("magentoA") })
        assertTrue(contentEntryUrls.any { it.contains("magentoB") })
    }

    @Test
    fun testDisablingIntegrationRemovesManagedExclusions() {
        val rootPath = getProjectRootPath()
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = rootPath
        settings.setExcludeGeneratedFiles(true)

        val service = getService()
        service.applyExclusions()
        assertFalse(settings.getManagedExcludedFolders().isEmpty())

        // Disable integration
        settings.pluginEnabled = false
        val result = service.applyExclusions()

        assertTrue(result.success())
        assertTrue(settings.getManagedExcludedFolders().isEmpty())

        val contentEntryUrls = ModuleRootManager.getInstance(myFixture.module)
            .contentEntries[0].excludeFolderUrls.toList()
        assertFalse(contentEntryUrls.any { it.contains("var/cache") })
    }

    @Test
    fun testFutureDirectoriesExcluded() {
        val rootPath = getProjectRootPath()
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = rootPath
        settings.setExcludeGeneratedFiles(true)

        val service = getService()
        service.applyExclusions()

        // Create var/report which might not have existed initially
        val reportFile = myFixture.addFileToProject("var/report/crash_123.log", "report data")
        val fileIndex = ProjectFileIndex.getInstance(project)
        assertTrue(fileIndex.isExcluded(reportFile.virtualFile))
    }

    @Test
    fun testSymlinkTargetValidation() {
        val rootPath = getProjectRootPath()
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = rootPath
        settings.setExcludeGeneratedFiles(true)

        // Try symlink pointing to app/code
        val symlinkPath = File(rootPath, "var/symlinked_code").toPath()
        val targetPath = File(rootPath, "app/code").toPath()
        try {
            Files.createDirectories(symlinkPath.parent)
            Files.createSymbolicLink(symlinkPath, targetPath)
            settings.additionalExcludedPaths = listOf("var/symlinked_code")

            val service = getService()
            val preview = service.previewExclusions(null, null)
            val skipped = preview.skippedEntries().map { it.path() }
            assertTrue(skipped.any { it.contains("var/symlinked_code") })
        } catch (e: Exception) {
            // Some test environments or OS permissions might not allow creating symlinks; ignore if unsupported
        }
    }

    @Test
    fun testAncestorOfNestedMagentoRootRejected() {
        // Project root: /project
        // Magento root: /project/src/magento
        val projectRoot = getProjectRootPath()
        val nestedMagentoDir = myFixture.tempDirFixture.findOrCreateDir("src/magento")
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = nestedMagentoDir.path
        settings.setExcludeGeneratedFiles(true)

        // Try to exclude "src" which encloses the Magento root
        settings.additionalExcludedPaths = listOf("src", "src/magento/app")

        val service = getService()
        val preview = service.previewExclusions(null, null)
        val skippedPaths = preview.skippedEntries().map { it.path() }

        assertTrue(skippedPaths.any { it.endsWith("/src") })
        assertTrue(skippedPaths.any { it.endsWith("/src/magento/app") })

        val result = service.applyExclusions()
        assertTrue(result.success())

        val managed = settings.getManagedExcludedFolders()
        // Ensure "src" was not excluded
        assertFalse(managed.any { it.endsWith("/src") })
        assertFalse(managed.any { it.endsWith("/src/magento/app") })
        // But Magento's var/cache inside nested root IS excluded
        assertTrue(managed.any { it.contains("/src/magento/var/cache") })
    }

    @Test
    fun testDisablingIntegrationRemovesExclusionsEvenWhenMagentoPathIsClearedOrInvalid() {
        val rootPath = getProjectRootPath()
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = rootPath
        settings.setExcludeGeneratedFiles(true)

        val service = getService()
        val applyResult = service.applyExclusions()
        assertTrue(applyResult.success())
        assertFalse(settings.getManagedExcludedFolders().isEmpty())

        // Clear Magento path or make it invalid
        settings.magentoPath = ""
        settings.pluginEnabled = false

        // Disabling integration must successfully remove exclusions even without valid Magento path
        val disableResult = service.applyExclusions()
        assertTrue(disableResult.success())
        assertTrue(settings.getManagedExcludedFolders().isEmpty())

        val contentEntryUrls = ModuleRootManager.getInstance(myFixture.module)
            .contentEntries[0].excludeFolderUrls.toList()
        assertFalse(contentEntryUrls.any { it.contains("var/cache") })
    }

    @Test
    fun testStartupReconciliationAppliesDefaultExclusions() {
        val rootPath = getProjectRootPath()
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = rootPath
        settings.setExcludeGeneratedFiles(true)
        // Simulate freshly opened project where managed exclusions haven't been applied yet
        settings.setManagedExcludedFolders(null)

        val service = getService()
        service.reconcileExclusionsOnStartup()
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()

        val managed = settings.getManagedExcludedFolders()
        assertFalse(managed.isEmpty())
        assertTrue(managed.any { it.contains("var/cache") })
    }

    @Test
    fun testStartupReconciliationPreservesExplicitOptOut() {
        val rootPath = getProjectRootPath()
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = rootPath
        settings.setExcludeGeneratedFiles(false) // Explicit opt-out
        settings.setManagedExcludedFolders(null)

        val service = getService()
        service.reconcileExclusionsOnStartup()
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()

        assertTrue(settings.getManagedExcludedFolders().isEmpty())
    }

    @Test
    fun testExternalMagentoPathDoesNotThrowWhenContentEntryDoesNotContainTarget() {
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        // Set Magento path to an external directory outside the module content roots
        val externalDir = com.intellij.openapi.util.io.FileUtil.createTempDirectory("external_magento", null)
        File(externalDir, "var/cache").mkdirs()
        settings.magentoPath = externalDir.absolutePath
        settings.setExcludeGeneratedFiles(true)

        val service = getService()
        // Must NOT throw IllegalArgumentException when content entry does not contain external target
        val result = service.applyExclusions()
        assertTrue(result.success())

        // Module content entry has no exclude folders added because external path is not under its content root
        val contentEntryUrls = ModuleRootManager.getInstance(myFixture.module)
            .contentEntries[0].excludeFolderUrls.toList()
        assertFalse(contentEntryUrls.any { it.contains("external_magento") })
    }

    @Test
    fun testMultipleContentEntriesScoping() {
        val rootPath = getProjectRootPath()
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = rootPath
        settings.setExcludeGeneratedFiles(true)

        // Add a second content entry to the module
        val secondContentDir = myFixture.tempDirFixture.findOrCreateDir("second_content_root")
        ModuleRootModificationUtil.addContentRoot(myFixture.module, secondContentDir)

        val service = getService()
        val result = service.applyExclusions()
        assertTrue(result.success())

        val contentEntries = ModuleRootManager.getInstance(myFixture.module).contentEntries
        assertEquals(2, contentEntries.size)

        val primaryEntry = contentEntries.first { it.url.contains(rootPath) }
        val secondEntry = contentEntries.first { it.url.contains("second_content_root") }

        // Primary entry contains Magento exclusions
        assertTrue(primaryEntry.excludeFolderUrls.any { it.contains("var/cache") })
        // Second entry must NOT have Magento exclusions erroneously added to it
        assertTrue(secondEntry.excludeFolderUrls.isEmpty())
    }
}
