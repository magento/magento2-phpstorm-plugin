/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.magento.idea.magento2plugin.BaseProjectTestCase
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

    private fun assertContains(text: String, expected: String) {
        assertTrue("Expected to find <$expected> in:\n$text", text.contains(expected))
    }
}
