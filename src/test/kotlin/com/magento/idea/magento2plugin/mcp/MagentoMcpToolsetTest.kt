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
                "magento_scaffold",
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
    fun testMagentoScaffoldToolDescriptionExplainsThreeStepFlow() {
        val method = MagentoMcpToolset::class.java.declaredMethods.first { toolMethod ->
            toolMethod.name == "magentoScaffold" && toolMethod.getAnnotation(McpDescription::class.java) != null
        }

        val description = method.getAnnotation(McpDescription::class.java)?.description
        assertNotNull(description)
        assertTrue(description!!.contains("mode `help`"))
        assertTrue(description.contains("mode `detailed_schema`"))
        assertTrue(description.contains("mode `render`"))
        assertTrue(description.contains("parametersJson"))
        assertTrue(description.contains("entity_crud"))
        assertTrue(description.contains("product_eav_attribute"))
        assertTrue(description.contains("list, inspect one schema, then render"))
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
