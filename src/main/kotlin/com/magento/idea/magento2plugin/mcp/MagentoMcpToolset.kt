/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool

/**
 * Exposes Magento-specific MCP tools that require the JetBrains PHP plugin.
 */
class MagentoMcpToolset : McpToolset {
    /**
     * Provides one low-noise entry point for Magento scaffolding.
     */
    @McpTool(name = "magento_scaffold")
    @McpDescription("Magento scaffold library and renderer with three context-conscious modes. Use mode `help` first to get only a compact catalog of scaffoldType values and short descriptions. Use mode `detailed_schema` with one scaffoldType to load the detailed required parameters, optional parameters, defaults, constraints, and example parametersJson only for that scaffold. Use mode `render` with scaffoldType and parametersJson to create files in the currently opened Magento project. Supported scaffoldType values are `module`, `plugin`, `observer`, `entity_crud`, `controller`, `cli_command`, `block`, `view_model`, `product_eav_attribute`, `category_eav_attribute`, and `customer_eav_attribute`. Prefer this single tool over generator-specific tools because it keeps the agent context small: list, load one schema, then render. Most scaffold types require `moduleName` in Magento `Vendor_Module` format; PHP class names must be FQNs such as `Foo\\Bar\\Block\\Product\\BadgeBlock`; JSON backslashes must be escaped.")
    suspend fun magentoScaffold(
        mode: String,
        scaffoldType: String,
        parametersJson: String
    ): String {
        return when (mode.trim().lowercase().replace('-', '_').replace(' ', '_')) {
            "help" -> MagentoScaffoldCommands.help()
            "schema", "detailed_schema" -> MagentoScaffoldCommands.detailedSchema(scaffoldType)
            "render" -> MagentoMcpToolsetSupport.withProjectEdtAction {
                MagentoScaffoldCommands.render(it, scaffoldType, parametersJson)
            }
            else -> "mode must be `help`, `detailed_schema`, or `render`. Use `help` to view the compact Magento scaffold catalog."
        }
    }

    /**
     * Provides one low-noise entry point for Magento project inspections.
     */
    @McpTool(name = "magento_inspect")
    @McpDescription("Magento inspection library with three context-conscious modes. Use mode `help` first to get only a compact catalog of queryType values and short descriptions. Use mode `detailed_schema` with one queryType to load detailed required parameters, accepted values, and example parametersJson only for that inspection. Use mode `query` with queryType and parametersJson to run the inspection in the currently opened Magento project. Supported queryType values are `module`, `di_config`, `plugins_for_method`, `observers_for_event`, `layout_entities`, `ui_component`, and `acl_or_menu`. Prefer this single tool over individual finder tools because it keeps the agent context small: list, inspect one schema, then query.")
    suspend fun magentoInspect(
        mode: String,
        queryType: String,
        parametersJson: String
    ): String {
        return when (mode.trim().lowercase().replace('-', '_').replace(' ', '_')) {
            "help" -> MagentoInspectionCommands.help()
            "schema", "detailed_schema" -> MagentoInspectionCommands.detailedSchema(queryType)
            "query" -> MagentoMcpToolsetSupport.withProjectReadAction(
                requireSmartMode = MagentoInspectionCommands.requiresSmartMode(queryType)
            ) {
                MagentoInspectionCommands.query(it, queryType, parametersJson)
            }
            else -> "mode must be `help`, `detailed_schema`, or `query`. Use `help` to inspect the compact Magento inspection catalog."
        }
    }
}
