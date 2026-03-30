package com.magento.idea.magento2plugin.mcp

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.psi.PsiDirectory
import com.magento.idea.magento2plugin.actions.generation.data.ModuleComposerJsonData
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleComposerJsonGenerator
import com.magento.idea.magento2plugin.BaseProjectTestCase
import com.magento.idea.magento2plugin.indexes.ModuleIndex
import com.magento.idea.magento2plugin.project.Settings
import org.junit.Test

class MagentoModuleCommandsTest : BaseProjectTestCase() {
    @Test
    fun testCreateMagentoModuleCreatesModuleFiles() {
        Settings.getInstance(project).magentoPath = getMagentoRootPathFromFixture()

        val result = MagentoModuleCommands.createMagentoModule(project, "Mcp", "Generated")

        assertContains(result, "Created Magento module \"Mcp_Generated\".")
        assertContains(result, "moduleName: Mcp_Generated")
        assertContainsPath(result, "app/code/Mcp/Generated")
        assertContains(result, "composerPackage: mcp/module-generated")
        assertContainsPath(result, "app/code/Mcp/Generated/composer.json")
        assertContainsPath(result, "app/code/Mcp/Generated/registration.php")
        assertContainsPath(result, "app/code/Mcp/Generated/etc/module.xml")

        assertFileContains(
            "app/code/Mcp/Generated/composer.json",
            "\"name\": \"mcp/module-generated\""
        )
        assertFileContains(
            "app/code/Mcp/Generated/registration.php",
            "'Mcp_Generated'"
        )
        assertFileContains(
            "app/code/Mcp/Generated/etc/module.xml",
            "<module name=\"Mcp_Generated\""
        )
    }

    @Test
    fun testCreateMagentoModuleRollsBackPartialWrites() {
        Settings.getInstance(project).magentoPath = getMagentoRootPathFromFixture()

        val result = MagentoModuleCommands.createMagentoModule(project, "Mcp", "Broken") { context ->
            listOfNotNull(
                ModuleComposerJsonGenerator(
                    ModuleComposerJsonData(
                        context.packageName,
                        context.moduleName,
                        context.moduleDirectory,
                        context.moduleDescription,
                        context.composerPackageName,
                        "1.0.0",
                        context.moduleLicense,
                        emptyList<String>(),
                        false
                    ),
                    project
                ).generate("test")
            )
        }

        assertContains(result, "rolled back")
        assertDirectoryMissing("app/code/Mcp/Broken")
    }

    @Test
    fun testFindMagentoModuleSeesNewModuleBeforeIndexesCatchUp() {
        Settings.getInstance(project).magentoPath = getMagentoRootPathFromFixture()

        MagentoModuleCommands.createMagentoModule(project, "Mcp", "Lookup")

        val result = MagentoModuleQueries.findMagentoModule(project, "Mcp_Lookup")

        assertContains(result, "Found 1 Magento module match(es) for \"Mcp_Lookup\".")
        assertContains(result, "\nMcp_Lookup\n")
        assertContainsPath(result, "app/code/Mcp/Lookup")
        assertContains(result, "editable: yes")
    }

    @Test
    fun testCreateMagentoBlockWorksImmediatelyAfterModuleCreation() {
        Settings.getInstance(project).magentoPath = getMagentoRootPathFromFixture()

        MagentoModuleCommands.createMagentoModule(project, "Mcp", "BlockTarget")

        val result = MagentoBlockCommands.createMagentoBlock(
            project = project,
            moduleName = "Mcp_BlockTarget",
            blockClassFqn = "Mcp\\BlockTarget\\Block\\ImmediateBlock"
        )

        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        assertContains(result, "Created Magento block class \"Mcp\\BlockTarget\\Block\\ImmediateBlock\".")
        assertContainsPath(result, "app/code/Mcp/BlockTarget/Block/ImmediateBlock.php")
    }

    @Test
    fun testCreateMagentoEntityCrudWorksImmediatelyAfterModuleCreation() {
        Settings.getInstance(project).magentoPath = getMagentoRootPathFromFixture()

        MagentoModuleCommands.createMagentoModule(project, "Mcp", "CrudTarget")

        val result = MagentoEntityCrudCommands.createMagentoEntityCrud(
            project = project,
            moduleName = "Mcp_CrudTarget",
            entityName = "ImmediateCrud",
            tableName = "",
            idFieldName = "",
            properties = listOf("title:string"),
            createAdminUiComponents = false,
            createDataInterface = true,
            createWebApi = false
        )

        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        assertContains(result, "Created Magento entity CRUD scaffold \"ImmediateCrud\".")
        assertContainsPath(result, "app/code/Mcp/CrudTarget/Model/ImmediateCrudModel.php")
        assertContainsPath(result, "app/code/Mcp/CrudTarget/etc/db_schema.xml")
    }

    private fun assertContains(text: String, expected: String) {
        assertTrue("Expected to find <$expected> in:\n$text", text.contains(expected))
    }

    private fun assertContainsPath(text: String, expectedPathSuffix: String) {
        val normalized = text.replace('\\', '/')
        assertTrue("Expected to find path suffix <$expectedPathSuffix> in:\n$text", normalized.contains(expectedPathSuffix))
    }

    private fun assertFileContains(relativePath: String, expected: String) {
        val pathParts = relativePath.split('/')
        var directory = getMagentoRootDirectoryFromFixture()
        for (part in pathParts.dropLast(1)) {
            directory = directory.findSubdirectory(part)
                ?: error("Expected directory to exist in path: $relativePath")
        }
        val psiFile = directory.findFile(pathParts.last())
            ?: error("Expected file to exist: $relativePath")

        assertContains(psiFile.text, expected)
    }

    private fun assertDirectoryMissing(relativePath: String) {
        val pathParts = relativePath.split('/')
        var directory = getMagentoRootDirectoryFromFixture()
        for (part in pathParts.dropLast(1)) {
            directory = directory.findSubdirectory(part)
                ?: return
        }

        assertNull("Expected directory to be absent: $relativePath", directory.findSubdirectory(pathParts.last()))
    }

    private fun getMagentoRootPathFromFixture(): String {
        return getMagentoRootDirectoryFromFixture().virtualFile.path
    }

    private fun getMagentoRootDirectoryFromFixture(): PsiDirectory {
        val moduleDirectory = ModuleIndex(project).getModuleDirectoryByModuleName("Foo_Bar")
            ?: error("Fixture module Foo_Bar was not resolved")
        return moduleDirectory.parentDirectory
            ?.parentDirectory
            ?.parentDirectory
            ?.parentDirectory
            ?: error("Fixture Magento root directory was not resolved")
    }
}
