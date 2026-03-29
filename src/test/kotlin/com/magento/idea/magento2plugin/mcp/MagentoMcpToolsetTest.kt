package com.magento.idea.magento2plugin.mcp

import com.intellij.mcpserver.annotations.McpTool
import org.junit.Assert.assertEquals
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
                "find_acl_or_menu"
            ),
            toolNames
        )
    }
}
