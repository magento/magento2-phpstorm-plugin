package com.magento.idea.magento2plugin.mcp

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.PlatformTestUtil
import com.magento.idea.magento2plugin.BaseProjectTestCase
import org.junit.Test

class MagentoEavAttributeCommandsTest : BaseProjectTestCase() {
    @Test
    fun testCreateMagentoProductEavAttributeCreatesDataPatchAndSourceModel() {
        val result = MagentoEavAttributeCommands.createMagentoProductEavAttribute(
            project = project,
            moduleName = "Foo_Bar",
            attributeCode = "mcp_test_attr",
            attributeLabel = "MCP Test Attribute",
            backendType = "varchar",
            frontendInput = "select",
            dataPatchName = "",
            attributeGroup = "General",
            sortOrder = 10,
            scope = "global",
            sourceModelClassFqn = "Foo\\Bar\\Model\\Source\\McpTestAttrSource",
            applyTo = "simple,configurable",
            required = false,
            visible = true,
            usedInGrid = false,
            visibleInGrid = false,
            filterableInGrid = false,
            htmlAllowedOnFront = false,
            visibleOnFront = false,
            options = listOf("Option A", "Option B")
        )

        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        assertContains(result, "Created Magento product EAV attribute \"mcp_test_attr\".")
        assertContains(result, "dataPatch: Foo\\Bar\\Setup\\Patch\\Data\\AddMcpTestAttrProductAttribute")
        assertContains(result, "sourceModel: Foo\\Bar\\Model\\Source\\McpTestAttrSource")
        assertContainsPath(result, "app/code/Foo/Bar/Model/Source/McpTestAttrSource.php")
        assertContainsPath(result, "app/code/Foo/Bar/Setup/Patch/Data/AddMcpTestAttrProductAttribute.php")

        assertFileContains(
            "app/code/Foo/Bar/Model/Source/McpTestAttrSource.php",
            "class McpTestAttrSource extends AbstractSource"
        )
        assertFileContains(
            "app/code/Foo/Bar/Setup/Patch/Data/AddMcpTestAttrProductAttribute.php",
            "'mcp_test_attr'"
        )
        assertFileContains(
            "app/code/Foo/Bar/Setup/Patch/Data/AddMcpTestAttrProductAttribute.php",
            "Foo\\Bar\\Model\\Source\\McpTestAttrSource::class"
        )
    }

    @Test
    fun testCreateMagentoCategoryEavAttributeCreatesDataPatchAndCategoryForm() {
        val result = MagentoEavAttributeCommands.createMagentoCategoryEavAttribute(
            project = project,
            moduleName = "Foo_Bar",
            attributeCode = "mcp_category_attr",
            attributeLabel = "MCP Category Attribute",
            backendType = "varchar",
            frontendInput = "text",
            dataPatchName = "",
            attributeGroup = "Content",
            sortOrder = 12,
            scope = "store",
            sourceModelClassFqn = "",
            required = false,
            visible = true,
            options = emptyList()
        )

        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        assertContains(result, "Created Magento category EAV attribute \"mcp_category_attr\".")
        assertContains(result, "dataPatch: Foo\\Bar\\Setup\\Patch\\Data\\AddMcpCategoryAttrCategoryAttribute")
        assertContainsPath(result, "app/code/Foo/Bar/Setup/Patch/Data/AddMcpCategoryAttrCategoryAttribute.php")
        assertContainsPath(result, "app/code/Foo/Bar/view/adminhtml/ui_component/category_form.xml")

        assertFileContains(
            "app/code/Foo/Bar/Setup/Patch/Data/AddMcpCategoryAttrCategoryAttribute.php",
            "'mcp_category_attr'"
        )
        assertFileContains(
            "app/code/Foo/Bar/view/adminhtml/ui_component/category_form.xml",
            "<field name=\"mcp_category_attr\" sortOrder=\"12\" formElement=\"input\"/>"
        )
    }

    @Test
    fun testCreateMagentoCustomerEavAttributeCreatesDataPatch() {
        val result = MagentoEavAttributeCommands.createMagentoCustomerEavAttribute(
            project = project,
            moduleName = "Foo_Bar",
            attributeCode = "mcp_customer_attr",
            attributeLabel = "MCP Customer Attribute",
            backendType = "varchar",
            frontendInput = "multiselect",
            dataPatchName = "",
            sortOrder = 7,
            sourceModelClassFqn = "Magento\\Eav\\Model\\Entity\\Attribute\\Source\\Table",
            required = false,
            visible = true,
            userDefined = true,
            usedInGrid = false,
            visibleInGrid = false,
            filterableInGrid = false,
            system = false,
            useInAdminhtmlCustomerForm = true,
            useInAdminhtmlCheckoutForm = false,
            useInCustomerAccountCreateForm = false,
            useInCustomerAccountEditForm = false,
            options = listOf("Silver", "Gold")
        )

        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        assertContains(result, "Created Magento customer EAV attribute \"mcp_customer_attr\".")
        assertContains(result, "dataPatch: Foo\\Bar\\Setup\\Patch\\Data\\AddMcpCustomerAttrCustomerAttribute")
        assertContains(result, "sourceModel: Magento\\Eav\\Model\\Entity\\Attribute\\Source\\Table")
        assertContainsPath(result, "app/code/Foo/Bar/Setup/Patch/Data/AddMcpCustomerAttrCustomerAttribute.php")

        assertFileContains(
            "app/code/Foo/Bar/Setup/Patch/Data/AddMcpCustomerAttrCustomerAttribute.php",
            "'mcp_customer_attr'"
        )
        assertFileContains(
            "app/code/Foo/Bar/Setup/Patch/Data/AddMcpCustomerAttrCustomerAttribute.php",
            "'adminhtml_customer'"
        )
        assertFileContains(
            "app/code/Foo/Bar/Setup/Patch/Data/AddMcpCustomerAttrCustomerAttribute.php",
            "Magento\\Eav\\Model\\Entity\\Attribute\\Source\\Table::class"
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
