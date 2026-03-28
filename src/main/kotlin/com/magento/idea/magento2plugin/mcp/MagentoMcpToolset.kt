/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.CoroutineContext

/**
 * Exposes Magento-specific read-only MCP tools backed by the plugin's existing indexes.
 */
class MagentoMcpToolset : McpToolset {
    private companion object {
        private val LOG = Logger.getInstance(MagentoMcpToolset::class.java)
        private const val PREVIEW_LIMIT = 160
    }

    /**
     * Returns the Magento root path configured for the current project.
     */
    @McpTool(name = "get_magento_root_path")
    @McpDescription("Return the Magento root path configured for the current project.")
    suspend fun getMagentoRootPath(): String = withProjectAction(
        toolName = "get_magento_root_path",
        arguments = emptyMap(),
        validateProject = false
    ) {
        MagentoProjectQueries.getMagentoRootPath(it)
    }

    /**
     * Creates a minimal Magento module under the configured Magento root path.
     */
    @McpTool(name = "create_magento_module")
    @McpDescription("Create a Magento module with composer.json, registration.php, and etc/module.xml.")
    suspend fun createMagentoModule(packageName: String, moduleName: String): String = withProjectAction(
        toolName = "create_magento_module",
        arguments = mapOf("packageName" to packageName, "moduleName" to moduleName)
    ) {
        MagentoModuleCommands.createMagentoModule(it, packageName, moduleName)
    }

    /**
     * Resolves Magento modules by exact or fuzzy module name and returns a compact summary.
     */
    @McpTool(name = "find_magento_module")
    @McpDescription("Find Magento modules by exact or partial module name.")
    suspend fun findMagentoModule(moduleName: String): String = withProjectReadAction(
        toolName = "find_magento_module",
        arguments = mapOf("moduleName" to moduleName)
    ) {
        MagentoModuleQueries.findMagentoModule(it, moduleName)
    }

    /**
     * Returns DI declarations that reference the requested class or virtual type.
     */
    @McpTool(name = "find_di_config_for_class")
    @McpDescription("Find dependency injection declarations related to a PHP class or virtual type.")
    suspend fun findDiConfigForClass(className: String): String = withProjectReadAction(
        toolName = "find_di_config_for_class",
        arguments = mapOf("className" to className)
    ) {
        MagentoDiQueries.findDiConfigForClass(it, className)
    }

    /**
     * Finds plugin methods that can intercept the requested target method.
     */
    @McpTool(name = "find_plugins_for_method")
    @McpDescription("Find Magento plugins that intercept a target class method.")
    suspend fun findPluginsForMethod(className: String, methodName: String): String = withProjectReadAction(
        toolName = "find_plugins_for_method",
        arguments = mapOf("className" to className, "methodName" to methodName)
    ) {
        MagentoDiQueries.findPluginsForMethod(it, className, methodName)
    }

    /**
     * Lists observer declarations for matching Magento event names.
     */
    @McpTool(name = "find_observers_for_event")
    @McpDescription("Find Magento observer declarations for an event name.")
    suspend fun findObserversForEvent(eventName: String): String = withProjectReadAction(
        toolName = "find_observers_for_event",
        arguments = mapOf("eventName" to eventName)
    ) {
        MagentoEventQueries.findObserversForEvent(it, eventName)
    }

    /**
     * Finds layout handles, block names, and container names that match the supplied query.
     */
    @McpTool(name = "find_layout_entities")
    @McpDescription("Find Magento layout handles, blocks, and containers by exact or partial name.")
    suspend fun findLayoutEntities(name: String): String = withProjectReadAction(
        toolName = "find_layout_entities",
        arguments = mapOf("name" to name)
    ) {
        MagentoViewQueries.findLayoutEntities(it, name)
    }

    /**
     * Finds UI component XML definitions by file name.
     */
    @McpTool(name = "find_ui_component")
    @McpDescription("Find Magento UI component XML files by exact or partial component name.")
    suspend fun findUiComponent(name: String): String = withProjectReadAction(
        toolName = "find_ui_component",
        arguments = mapOf("name" to name)
    ) {
        MagentoViewQueries.findUiComponent(it, name)
    }

    /**
     * Returns matching ACL resources and admin menu declarations for a shared identifier query.
     */
    @McpTool(name = "find_acl_or_menu")
    @McpDescription("Find Magento ACL resources and admin menu entries by exact or partial identifier.")
    suspend fun findAclOrMenu(identifier: String): String = withProjectReadAction(
        toolName = "find_acl_or_menu",
        arguments = mapOf("identifier" to identifier)
    ) {
        MagentoViewQueries.findAclOrMenu(it, identifier)
    }

    /**
     * Resolves the active IDE project from MCP call context, validates it, and executes the tool body.
     */
    private suspend fun withProjectAction(
        toolName: String,
        arguments: Map<String, String>,
        validateProject: Boolean = true,
        query: (Project) -> String
    ): String {
        LOG.info("Magento MCP start: $toolName(${formatArguments(arguments)})")
        val project = resolveProject(currentCoroutineContext())
            ?: return "MCP project context is unavailable.".also {
                LOG.info("Magento MCP end: $toolName -> project context unavailable")
            }

        if (validateProject) {
            val validationMessage = MagentoMcpSupport.validateProject(project)
            if (validationMessage != null) {
                LOG.info("Magento MCP end: $toolName -> validation failed: ${singleLine(validationMessage)}")
                return validationMessage
            }
        }

        return query(project)
            .also { result ->
                LOG.info(
                    "Magento MCP end: $toolName -> ${result.length} chars, preview=\"${singleLine(result)}\""
                )
            }
    }

    /**
     * Executes a tool body inside a read action after project validation succeeds.
     */
    private suspend fun withProjectReadAction(
        toolName: String,
        arguments: Map<String, String>,
        validateProject: Boolean = true,
        query: (Project) -> String
    ): String = withProjectAction(toolName, arguments, validateProject) { project ->
        ReadAction.compute<String, RuntimeException> {
            query(project)
        }
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

    private fun formatArguments(arguments: Map<String, String>): String {
        return arguments.entries.joinToString(", ") { (name, value) ->
            "$name=\"${singleLine(value)}\""
        }
    }

    private fun singleLine(value: String): String {
        val normalized = value.replace(Regex("\\s+"), " ").trim()
        return if (normalized.length <= PREVIEW_LIMIT) normalized else normalized.take(PREVIEW_LIMIT - 3) + "..."
    }
}
