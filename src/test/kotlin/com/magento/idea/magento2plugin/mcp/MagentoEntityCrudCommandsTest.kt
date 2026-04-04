package com.magento.idea.magento2plugin.mcp

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.PlatformTestUtil
import com.magento.idea.magento2plugin.BaseProjectTestCase
import org.junit.Test

class MagentoEntityCrudCommandsTest : BaseProjectTestCase() {
    @Test
    fun testCreateMagentoEntityCrudCreatesExpectedScaffold() {
        val result = MagentoEntityCrudCommands.createMagentoEntityCrud(
            project = project,
            moduleName = "Foo_Bar",
            entityName = "McpCrud",
            tableName = "",
            idFieldName = "",
            properties = listOf(
                "title:string",
                "is_active:bool"
            ),
            createAdminUiComponents = true,
            createDataInterface = true,
            createWebApi = true
        )

        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        assertContains(result, "Created Magento entity CRUD scaffold \"McpCrud\".")
        assertContains(result, "table: mcp_crud")
        assertContains(result, "idField: mcp_crud_id")
        assertContainsPath(result, "app/code/Foo/Bar/Model/McpCrudModel.php")
        assertContainsPath(result, "app/code/Foo/Bar/Model/ResourceModel/McpCrudModel/McpCrudCollection.php")
        assertContainsPath(result, "app/code/Foo/Bar/etc/db_schema.xml")
        assertContainsPath(result, "app/code/Foo/Bar/view/adminhtml/ui_component/mcp_crud_listing.xml")
        assertContainsPath(result, "app/code/Foo/Bar/Api/GetMcpCrudListInterface.php")

        assertFileContains(
            "app/code/Foo/Bar/Model/McpCrudModel.php",
            "class McpCrudModel"
        )
        assertFileContains(
            "app/code/Foo/Bar/Model/ResourceModel/McpCrudModel/McpCrudCollection.php",
            "class McpCrudCollection"
        )
        assertFileContains(
            "app/code/Foo/Bar/etc/db_schema.xml",
            "<table name=\"mcp_crud\""
        )
        assertFileContains(
            "app/code/Foo/Bar/etc/db_schema.xml",
            "name=\"title\""
        )
        assertFileContains(
            "app/code/Foo/Bar/view/adminhtml/ui_component/mcp_crud_listing.xml",
            "mcp_crud_listing_data_source"
        )
        assertFileContains(
            "app/code/Foo/Bar/Api/GetMcpCrudListInterface.php",
            "interface GetMcpCrudListInterface"
        )
        assertFileContains(
            "app/code/Foo/Bar/etc/webapi.xml",
            "url=\"/V1/mcp-crud/get-list\""
        )
    }

    private fun assertContains(text: String, expected: String) {
        assertTrue("Expected to find <$expected> in:\n$text", text.contains(expected))
    }

    private fun assertContainsPath(text: String, expectedPathSuffix: String) {
        val normalized = text.replace('\\', '/')
        assertTrue(
            "Expected to find path suffix <$expectedPathSuffix> in:\n$text",
            normalized.contains(expectedPathSuffix)
        )
    }

    private fun assertFileContains(relativePath: String, expected: String) {
        val virtualFile = myFixture.findFileInTempDir(relativePath)
            ?: error("Expected file to exist: $relativePath")
        val psiFile = myFixture.psiManager.findFile(virtualFile)
            ?: error("Expected PSI file to exist: $relativePath")

        assertContains(psiFile.text, expected)
    }
}
