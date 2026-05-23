/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import kotlinx.coroutines.currentCoroutineContext
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext

/**
 * Exposes Magento-specific MCP tools backed by the plugin's existing indexes and generators.
 */
class MagentoMcpToolset : McpToolset {
    /**
     * Returns the Magento root path configured for the current project.
     */
    @McpTool(name = "get_magento_root_path")
    @McpDescription("Return the Magento root path configured for the current IDE project. Use this when a tool or shell command needs an absolute project path. The result is the plugin's resolved Magento root directory, not a filesystem search result.")
    suspend fun getMagentoRootPath(): String = withProjectAction(
        validateProject = false
    ) {
        MagentoProjectQueries.getMagentoRootPath(it)
    }

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
            "render" -> withProjectEdtAction {
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
            "query" -> withProjectReadAction(
                requireSmartMode = MagentoInspectionCommands.requiresSmartMode(queryType)
            ) {
                MagentoInspectionCommands.query(it, queryType, parametersJson)
            }
            else -> "mode must be `help`, `detailed_schema`, or `query`. Use `help` to inspect the compact Magento inspection catalog."
        }
    }

    /**
     * Detects project-local CLI wrappers such as Mark Shust Docker scripts and explains how to invoke them.
     */
    @McpTool(name = "describe_magento_cli_environment")
    @McpDescription("Inspect the current Magento project for local CLI wrappers under the project root or configured Magento root `bin/`, including Mark Shust Docker scripts such as `bin/magento`, `bin/n98-magerun2`, `bin/php`, `bin/composer`, or stack lifecycle wrappers like `bin/start`, `bin/stop`, and `bin/restart`. Call this before running shell commands that would normally use Magento CLI, PHP, Composer, n98-magerun, or project environment wrappers. The result lists detected wrapper commands, configured wrapper candidates, and example invocations; agents should use the returned project-local wrapper path exactly, for example `./bin/magento cache:flush` or `./bin/start`, instead of global binaries. When the Magento root is nested deeper in the project, edits still belong under the configured Magento root, but wrappers detected outside that root are still valid and should be run from the returned path.")
    suspend fun describeMagentoCliEnvironment(): String = withProjectAction(
        requireSmartMode = false
    ) {
        MagentoCliToolQueries.describeCliEnvironment(it)
    }

    /**
     * Resolves the active IDE project from MCP call context, validates it, and executes the tool body.
     */
    private suspend fun withProjectAction(
        validateProject: Boolean = true,
        requireSmartMode: Boolean = true,
        query: (Project) -> String
    ): String {
        val project = resolveProject(currentCoroutineContext()) ?: return "MCP project context is unavailable."

        if (validateProject) {
            val validationMessage = MagentoMcpSupport.validateProject(
                project,
                requireSmartMode = requireSmartMode
            )
            if (validationMessage != null) {
                return validationMessage
            }
        }

        return query(project)
    }

    /**
     * Executes a tool body inside a read action after project validation succeeds.
     */
    private suspend fun withProjectReadAction(
        validateProject: Boolean = true,
        requireSmartMode: Boolean = true,
        query: (Project) -> String
    ): String = withProjectAction(validateProject, requireSmartMode) { project ->
        try {
            MagentoMcpReadActionSupport.retryOnCancellation {
                ReadAction.computeCancellable<String, RuntimeException> {
                    query(project)
                }
            }
        } catch (throwable: Throwable) {
            if (MagentoMcpReadActionSupport.isCancellation(throwable)) {
                MagentoMcpReadActionSupport.cancellationMessage()
            } else {
                throw throwable
            }
        }
    }

    /**
     * Executes a tool body on the EDT after project validation succeeds.
     */
    private suspend fun withProjectEdtAction(
        validateProject: Boolean = true,
        query: (Project) -> String
    ): String = withProjectAction(validateProject) { project ->
        val application = ApplicationManager.getApplication()
        if (application.isDispatchThread) {
            return@withProjectAction query(project)
        }

        val result = AtomicReference<String>()
        val failure = AtomicReference<Throwable>()
        application.invokeAndWait {
            try {
                result.set(query(project))
            } catch (throwable: Throwable) {
                failure.set(throwable)
            }
        }

        failure.get()?.let { throw it }
        result.get() ?: ""
    }

    /**
     * Uses the MCP runtime helper when available without hard-linking to a specific helper binary signature.
     */
    private fun resolveProject(context: CoroutineContext): Project? {
        return try {
            val helperClass = Class.forName("com.intellij.mcpserver.McpCallInfoKt")
            val helperMethod = helperClass.getMethod(
                "getProjectOrNull",
                CoroutineContext::class.java
            )
            helperMethod.invoke(null, context) as? Project
        } catch (_: ReflectiveOperationException) {
            null
        }
    }
}
