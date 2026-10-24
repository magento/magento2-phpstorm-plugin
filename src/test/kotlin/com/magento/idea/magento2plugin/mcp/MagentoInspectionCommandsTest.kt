/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.testFramework.ExtensionTestUtil
import com.intellij.testFramework.DumbModeTestUtils
import com.magento.idea.magento2plugin.BaseProjectTestCase
import org.json.JSONObject
import org.junit.Test

class MagentoInspectionCommandsTest : BaseProjectTestCase() {
    @Test
    fun testGeneralHelpExplainsInspectionLibraryFlow() {
        val result = MagentoInspectionCommands.help()

        assertContains(result, "Magento inspection library")
        assertContains(result, "`help`")
        assertContains(result, "`detailed_schema`")
        assertContains(result, "`query`")
        assertContains(result, "`module`: Find modules")
        assertContains(result, "`di_config`: Find di.xml")
    }

    @Test
    fun testDetailedSchemaExplainsPluginsForMethodParameters() {
        val result = MagentoInspectionCommands.detailedSchema("plugins-for-method")

        assertContains(result, "queryType: plugins_for_method")
        assertContains(result, "className, methodName")
        assertContains(result, "without `before`, `around`, or `after` prefixes")
    }

    @Test
    fun testQueryReportsMissingRequiredParameter() {
        val result = MagentoInspectionCommands.query(project, "acl_or_menu", "{}")

        assertContains(result, "Missing required parameter `identifier`")
        assertContains(result, "queryType `acl_or_menu`")
        assertContains(result, "mode `detailed_schema`")
    }

    @Test
    fun testQueryFindsAclThroughInspectionTool() {
        val result = MagentoInspectionCommands.query(
            project = project,
            queryType = "acl_or_menu",
            parametersJson = """{"identifier":"Magento_Catalog::catalog"}"""
        )

        assertContains(result, "Magento_Catalog::catalog")
    }

    @Test
    fun testSharedQueriesWorkWithoutPhpInspectionProvider() {
        ExtensionTestUtil.maskExtensions(MagentoMethodPluginQueryProvider.EP_NAME, emptyList(), testRootDisposable)
        val queries = listOf(
            Triple("module", "moduleName" to "Foo_Bar2", "app/code/Foo/Bar2"),
            Triple("di_config", "className" to "Magento\\Theme\\Block\\Html\\Topmenu", "plugin name=catalogTopmenu"),
            Triple("observers_for_event", "eventName" to "test_event_in_test_class", "observer=test_observer"),
            Triple("layout_entities", "name" to "test_index_index_block2", "blocks"),
            Triple("ui_component", "name" to "recently_viewed_2", "rootTag: listing"),
            Triple("acl_or_menu", "identifier" to "Magento_Catalog::catalog", "menu entries")
        )
        val help = MagentoInspectionCommands.help()
        for ((queryType, parameter, expected) in queries) {
            assertContains(help, "- `$queryType`:")
            assertContains(MagentoInspectionCommands.detailedSchema(queryType), "queryType: $queryType")
            val result = MagentoInspectionCommands.query(
                project, queryType, JSONObject().put(parameter.first, parameter.second).toString()
            )
            assertContains(result.replace('\\', '/'), expected.replace('\\', '/'))
        }
        assertFalse(help.contains("- `plugins_for_method`:"))
        assertContains(help, "requires the JetBrains PHP plugin")
    }

    @Test
    fun testUnavailablePhpQueryIsNotReportedAsAnEmptyResult() {
        ExtensionTestUtil.maskExtensions(MagentoMethodPluginQueryProvider.EP_NAME, emptyList(), testRootDisposable)
        assertFalse(MagentoInspectionCommands.requiresSmartMode("plugins_for_method"))
        assertFalse(MagentoInspectionCommands.requiresSmartMode("plugin_method"))
        assertTrue(MagentoInspectionCommands.requiresSmartMode("di_config"))
        assertEquals(MagentoMethodPluginQueryProvider.UNAVAILABLE_MESSAGE,
            MagentoInspectionCommands.detailedSchema("plugin_method"))
        assertEquals(MagentoMethodPluginQueryProvider.UNAVAILABLE_MESSAGE,
            MagentoInspectionCommands.query(project, "plugins", "{}"))
        assertEquals(MagentoMethodPluginQueryProvider.UNAVAILABLE_MESSAGE,
            MagentoDiQueries.findPluginsForMethod(project, "Missing\\Class", "execute"))
        assertContains(MagentoInspectionCommands.query(project, "module", "{invalid"),
            "parametersJson must be a valid JSON object")
    }

    @Test
    fun testPhpQueryIsAdvertisedWhenOptionalProviderIsRegistered() {
        assertTrue(MagentoMethodPluginQueryProvider.isAvailable())
        assertTrue(MagentoInspectionCommands.requiresSmartMode("plugins_for_method"))
        assertContains(MagentoInspectionCommands.help(), "- `plugins_for_method`:")
    }

    @Test
    fun testIndexingDoesNotReturnEmptyInspectionResults() {
        val queries = listOf(
            "module" to "moduleName",
            "di_config" to "className",
            "plugins_for_method" to "className",
            "observers_for_event" to "eventName",
            "layout_entities" to "name",
            "ui_component" to "name",
            "acl_or_menu" to "identifier"
        )
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            for ((type, parameter) in queries) {
                assertTrue(MagentoInspectionCommands.requiresSmartMode(type))
                assertEquals(MagentoMcpSupport.INDEXES_NOT_READY_MESSAGE,
                    MagentoInspectionCommands.query(project, type,
                        JSONObject().put(parameter, "missing").put("methodName", "execute").toString()))
            }
            assertEquals(MagentoMcpSupport.INDEXES_NOT_READY_MESSAGE,
                MagentoModuleQueries.findMagentoModule(project, "missing"))
            assertContains(MagentoInspectionCommands.detailedSchema("layout_entities"),
                "queryType: layout_entities")
        }
    }

    @Test
    fun testUnavailablePhpCapabilityRemainsExplicitDuringIndexing() {
        ExtensionTestUtil.maskExtensions(MagentoMethodPluginQueryProvider.EP_NAME, emptyList(), testRootDisposable)
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            assertEquals(MagentoMethodPluginQueryProvider.UNAVAILABLE_MESSAGE,
                MagentoInspectionCommands.query(project, "plugins_for_method", "{}"))
        }
    }

    private fun assertContains(text: String, expected: String) {
        assertTrue("Expected to find <$expected> in:\n$text", text.contains(expected))
    }
}
