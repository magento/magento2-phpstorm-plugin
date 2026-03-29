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
    fun testCliEnvironmentToolDescriptionExplainsWhenToUseIt() {
        val method = MagentoMcpToolset::class.java.declaredMethods.first { toolMethod ->
            toolMethod.name == "describeMagentoCliEnvironment"
                    && toolMethod.getAnnotation(McpDescription::class.java) != null
        }

        val description = method.getAnnotation(McpDescription::class.java)?.description
        assertNotNull(description)
        assertTrue(description!!.contains("Mark Shust"))
        assertTrue(description.contains("before running shell commands"))
        assertTrue(description.contains("prefer the returned wrapper paths"))
    }
}
