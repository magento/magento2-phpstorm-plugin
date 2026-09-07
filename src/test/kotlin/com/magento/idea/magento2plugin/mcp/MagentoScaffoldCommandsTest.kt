package com.magento.idea.magento2plugin.mcp

import com.magento.idea.magento2plugin.BaseProjectTestCase
import com.magento.idea.magento2plugin.indexes.ModuleIndex
import com.magento.idea.magento2plugin.project.Settings
import com.intellij.psi.PsiDirectory
import org.junit.Test

class MagentoScaffoldCommandsTest : BaseProjectTestCase() {
    @Test
    fun testGeneralHelpExplainsScaffoldLibraryFlow() {
        val result = MagentoScaffoldCommands.help()

        assertContains(result, "Magento scaffold library")
        assertContains(result, "`help`")
        assertContains(result, "`detailed_schema`")
        assertContains(result, "`render`")
        assertContains(result, "`module`: Create composer.json")
        assertContains(result, "`entity_crud`: Create DB schema")
    }

    @Test
    fun testDetailedSchemaExplainsCrudProperties() {
        val result = MagentoScaffoldCommands.detailedSchema("entity-crud")

        assertContains(result, "scaffoldType: entity_crud")
        assertContains(result, "field_name:type")
        assertContains(result, "Do not include the primary ID field")
    }

    @Test
    fun testRenderReportsMissingRequiredParameter() {
        val result = MagentoScaffoldCommands.render(project, "block", "{}")

        assertContains(result, "Missing required parameter `moduleName`")
        assertContains(result, "scaffoldType `block`")
        assertContains(result, "mode `detailed_schema`")
    }

    @Test
    fun testRenderCreatesModuleThroughScaffoldTool() {
        Settings.getInstance(project).magentoPath = getMagentoRootPathFromFixture()

        val result = MagentoScaffoldCommands.render(
            project = project,
            scaffoldType = "module",
            parametersJson = """{"packageName":"Mcp","moduleName":"Scaffolded"}"""
        )

        assertContains(result, "Created Magento module \"Mcp_Scaffolded\".")
        assertContains(result, "moduleName: Mcp_Scaffolded")
        assertContainsPath(result, "app/code/Mcp/Scaffolded")
    }

    private fun assertContains(text: String, expected: String) {
        assertTrue("Expected to find <$expected> in:\n$text", text.contains(expected))
    }

    private fun assertContainsPath(text: String, expectedPathSuffix: String) {
        val normalized = text.replace('\\', '/')
        assertTrue("Expected to find path suffix <$expectedPathSuffix> in:\n$text", normalized.contains(expectedPathSuffix))
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
