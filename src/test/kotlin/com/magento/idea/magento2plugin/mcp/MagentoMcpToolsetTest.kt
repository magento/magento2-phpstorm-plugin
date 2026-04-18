package com.magento.idea.magento2plugin.mcp

import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MagentoMcpToolsetTest {

    @Test
    fun testToolsetExposesExpectedToolNames() {
        val toolNames = MagentoMcpToolset::class.java.declaredMethods
            .mapNotNull { it.getAnnotation(McpTool::class.java)?.name }
            .toSet()

        assertEquals(
            setOf(
                "get_magento_root_path",
                "create_magento_module",
                "create_magento_plugin",
                "create_magento_observer",
                "create_magento_entity_crud",
                "create_magento_controller",
                "create_magento_cli_command",
                "create_magento_block",
                "create_magento_view_model",
                "create_magento_product_eav_attribute",
                "create_magento_category_eav_attribute",
                "create_magento_customer_eav_attribute",
                "find_magento_module",
                "find_di_config_for_class",
                "find_plugins_for_method",
                "find_observers_for_event",
                "find_layout_entities",
                "find_ui_component",
                "find_acl_or_menu",
                "describe_magento_cli_environment"
            ),
            toolNames
        )
    }

    @Test
    fun testEntityCrudToolDescriptionExplainsPropertiesFormat() {
        val method = MagentoMcpToolset::class.java.declaredMethods.first { method ->
            method.name == "createMagentoEntityCrud" && method.getAnnotation(McpDescription::class.java) != null
        }

        val description = method.getAnnotation(McpDescription::class.java)?.description
        assertNotNull(description)
        assertTrue(description!!.contains("field_name:type"))
        assertTrue(description.contains("[\"title:string\", \"is_active:bool\", \"price:float\"]"))
        assertTrue(description.contains("do not include it in `properties`"))
    }

    @Test
    fun testCreationToolDescriptionsExplainAcceptedFormats() {
        val expectedSnippets = mapOf(
            "createMagentoModule" to listOf("Foo_Bar", "foo/module-bar", "do not pass `foo/module-bar`"),
            "createMagentoPlugin" to listOf("Vendor_Module", "before`, `around`, or `after`", "`base`, `adminhtml`, `frontend`"),
            "createMagentoObserver" to listOf("must not contain whitespace", "Foo\\Bar\\Observer\\CatalogProductSaveAfter", "`base`, `adminhtml`, `frontend`"),
            "createMagentoCliCommand" to listOf("foo:bar:sync-data", "underscores, hyphens, and colons", "Vendor_Module"),
            "createMagentoBlock" to listOf("Vendor_Module", "Foo\\Bar\\Block\\Product\\BadgeBlock"),
            "createMagentoViewModel" to listOf("Vendor_Module", "Foo\\Bar\\ViewModel\\Product\\BadgeViewModel"),
            "createMagentoProductEavAttribute" to listOf("lower_snake_case", "`static`, `varchar`, `int`, `text`, `datetime`, or `decimal`", "`select` or `multiselect`"),
            "createMagentoCategoryEavAttribute" to listOf("lower_snake_case", "`global`, `store`, or `website`", "`select` or `multiselect`"),
            "createMagentoCustomerEavAttribute" to listOf("lower_snake_case", "`static`, `varchar`, `int`, `text`, `datetime`, or `decimal`", "`select` or `multiselect`")
        )

        for ((methodName, snippets) in expectedSnippets) {
            val method = MagentoMcpToolset::class.java.declaredMethods.first { toolMethod ->
                toolMethod.name == methodName && toolMethod.getAnnotation(McpDescription::class.java) != null
            }
            val description = method.getAnnotation(McpDescription::class.java)?.description
            assertNotNull(description)
            for (snippet in snippets) {
                assertTrue("Expected <$snippet> in description for $methodName: $description", description!!.contains(snippet))
            }
        }
    }

    @Test
    fun testLookupToolDescriptionsExplainAcceptedFormats() {
        val expectedSnippets = mapOf(
            "getMagentoRootPath" to listOf("absolute project path", "resolved Magento root directory"),
            "findMagentoModule" to listOf("`Vendor_Module`", "`Magento_Catalog`", "`Catalog`"),
            "findDiConfigForClass" to listOf("PHP FQN", "virtual type", "Magento\\Catalog\\Model\\Product"),
            "findPluginsForMethod" to listOf("target PHP FQN", "`save` or `getById`", "without `before`/`after`/`around` prefixes"),
            "findObserversForEvent" to listOf("`catalog_product_save_after`", "`product_save`", "`events.xml`"),
            "findLayoutEntities" to listOf("`catalog_product_view`", "`product.info.main`", "layout XML file or insertion point"),
            "findUiComponent" to listOf("`product_form`", "without the `.xml` extension", "admin forms, listings, or data providers"),
            "findAclOrMenu" to listOf("`Magento_Catalog::catalog`", "`Foo_Bar::manage_items`", "ACL and menu XML so you can reuse the correct resource ID")
        )

        for ((methodName, snippets) in expectedSnippets) {
            val method = MagentoMcpToolset::class.java.declaredMethods.first { toolMethod ->
                toolMethod.name == methodName && toolMethod.getAnnotation(McpDescription::class.java) != null
            }
            val description = method.getAnnotation(McpDescription::class.java)?.description
            assertNotNull(description)
            for (snippet in snippets) {
                assertTrue("Expected <$snippet> in description for $methodName: $description", description!!.contains(snippet))
            }
        }
    }

    @Test
    fun testCliEnvironmentToolDescriptionExplainsWhenToUseIt() {
        val method = MagentoMcpToolset::class.java.declaredMethods.first { toolMethod ->
            toolMethod.name == "describeMagentoCliEnvironment"
                    && toolMethod.getAnnotation(McpDescription::class.java) != null
        }

        val description = method.getAnnotation(McpDescription::class.java)?.description
        assertNotNull(description)
        assertTrue(description!!.contains("Mark Shust"))
        assertTrue(description.contains("before running shell commands") || description.contains("Call this before running shell commands"))
        assertTrue(description.contains("use the returned project-local wrapper path exactly"))
        assertTrue(description.contains("`./bin/magento cache:flush`"))
        assertTrue(description.contains("`bin/start`"))
        assertTrue(description.contains("`./bin/start`"))
    }
}
