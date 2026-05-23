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
                "magento_inspect",
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
        assertTrue(description.contains("list, load one schema, then render"))
    }

    @Test
    fun testMagentoInspectToolDescriptionExplainsThreeStepFlow() {
        val method = MagentoMcpToolset::class.java.declaredMethods.first { toolMethod ->
            toolMethod.name == "magentoInspect" && toolMethod.getAnnotation(McpDescription::class.java) != null
        }

        val description = method.getAnnotation(McpDescription::class.java)?.description
        assertNotNull(description)
        assertTrue(description!!.contains("mode `help`"))
        assertTrue(description.contains("mode `detailed_schema`"))
        assertTrue(description.contains("mode `query`"))
        assertTrue(description.contains("queryType"))
        assertTrue(description.contains("di_config"))
        assertTrue(description.contains("acl_or_menu"))
        assertTrue(description.contains("list, inspect one schema, then query"))
    }

    @Test
    fun testRootPathToolDescriptionExplainsAcceptedFormats() {
        val method = MagentoMcpToolset::class.java.declaredMethods.first { toolMethod ->
            toolMethod.name == "getMagentoRootPath" && toolMethod.getAnnotation(McpDescription::class.java) != null
        }
        val description = method.getAnnotation(McpDescription::class.java)?.description
        assertNotNull(description)
        assertTrue(description!!.contains("absolute project path"))
        assertTrue(description.contains("resolved Magento root directory"))
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
