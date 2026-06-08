/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool

/**
 * Exposes Magento MCP tools that do not require the JetBrains PHP plugin.
 */
class MagentoMcpProjectToolset : McpToolset {
    /**
     * Returns the Magento root path configured for the current project.
     */
    @McpTool(name = "get_magento_root_path")
    @McpDescription("Return the Magento root path configured for the current IDE project. Use this when a tool or shell command needs an absolute project path. The result is the plugin's resolved Magento root directory, not a filesystem search result.")
    suspend fun getMagentoRootPath(): String = MagentoMcpToolsetSupport.withProjectAction(
        validateProject = false
    ) {
        MagentoProjectQueries.getMagentoRootPath(it)
    }

    /**
     * Detects project-local CLI wrappers such as Mark Shust Docker scripts and explains how to invoke them.
     */
    @McpTool(name = "describe_magento_cli_environment")
    @McpDescription("Inspect the current Magento project for local CLI wrappers under the project root or configured Magento root `bin/`, including Mark Shust Docker scripts such as `bin/magento`, `bin/n98-magerun2`, `bin/php`, `bin/composer`, or stack lifecycle wrappers like `bin/start`, `bin/stop`, and `bin/restart`. Call this before running shell commands that would normally use Magento CLI, PHP, Composer, n98-magerun, or project environment wrappers. The result lists detected wrapper commands, configured wrapper candidates, and example invocations; agents should use the returned project-local wrapper path exactly, for example `./bin/magento cache:flush` or `./bin/start`, instead of global binaries. When the Magento root is nested deeper in the project, edits still belong under the configured Magento root, but wrappers detected outside that root are still valid and should be run from the returned path.")
    suspend fun describeMagentoCliEnvironment(): String = MagentoMcpToolsetSupport.withProjectAction(
        requireSmartMode = false
    ) {
        MagentoCliToolQueries.describeCliEnvironment(it)
    }
}
