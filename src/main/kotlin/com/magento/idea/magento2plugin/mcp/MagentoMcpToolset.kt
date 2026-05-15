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
    @McpDescription("Magento scaffold library and renderer with three context-conscious modes. Use mode `help` first to get only a compact catalog of scaffoldType values and short descriptions. Use mode `detailed_schema` with one scaffoldType to load the detailed required parameters, optional parameters, defaults, constraints, and example parametersJson only for that scaffold. Use mode `render` with scaffoldType and parametersJson to create files in the currently opened Magento project. Supported scaffoldType values are `module`, `plugin`, `observer`, `entity_crud`, `controller`, `cli_command`, `block`, `view_model`, `product_eav_attribute`, `category_eav_attribute`, and `customer_eav_attribute`. Prefer this single tool over generator-specific tools because it keeps the agent context small: list, inspect one schema, then render. Most scaffold types require `moduleName` in Magento `Vendor_Module` format; PHP class names must be FQNs such as `Foo\\Bar\\Block\\Product\\BadgeBlock`; JSON backslashes must be escaped.")
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
            else -> "mode must be `help`, `detailed_schema`, or `render`. Use `help` to inspect the compact Magento scaffold catalog."
        }
    }

    /**
     * Resolves Magento modules by exact or fuzzy module name and returns a compact summary.
     */
    @McpTool(name = "find_magento_module")
    @McpDescription("Find Magento modules by exact or partial Magento module name. `moduleName` should usually use `Vendor_Module` format such as `Magento_Catalog` or `Foo_Bar`, but partial searches such as `Catalog`, `Magento_`, or `Foo` are also accepted. Use this to confirm the canonical module name and installation path before generating code or querying related configuration.")
    suspend fun findMagentoModule(moduleName: String): String = withProjectReadAction {
        MagentoModuleQueries.findMagentoModule(it, moduleName)
    }

    /**
     * Returns DI declarations that reference the requested class or virtual type.
     */
    @McpTool(name = "find_di_config_for_class")
    @McpDescription("Find dependency injection declarations related to a PHP class, interface, preference target, or virtual type. `className` should usually be a PHP FQN such as `Magento\\Catalog\\Api\\ProductRepositoryInterface` or `Magento\\Catalog\\Model\\Product`, but Magento virtual type names are also accepted. The result summarizes matching `di.xml` declarations such as preferences, types, virtual types, arguments, and plugins that reference the requested name.")
    suspend fun findDiConfigForClass(className: String): String = withProjectReadAction {
        MagentoDiQueries.findDiConfigForClass(it, className)
    }

    /**
     * Finds plugin methods that can intercept the requested target method.
     */
    @McpTool(name = "find_plugins_for_method")
    @McpDescription("Find Magento plugins that intercept a target class or interface method. `className` should be the target PHP FQN, such as `Magento\\Catalog\\Api\\ProductRepositoryInterface`, and `methodName` should be the PHP method name only, such as `save` or `getById`, without `before`/`after`/`around` prefixes. Use this before changing service behavior so you can see existing interception points, plugin classes, sort order, and declared areas.")
    suspend fun findPluginsForMethod(className: String, methodName: String): String = withProjectReadAction {
        MagentoDiQueries.findPluginsForMethod(it, className, methodName)
    }

    /**
     * Lists observer declarations for matching Magento event names.
     */
    @McpTool(name = "find_observers_for_event")
    @McpDescription("Find Magento observer declarations for an event name. `eventName` may be the full Magento event name such as `catalog_product_save_after` or a partial search such as `product_save`. The result summarizes matching `events.xml` declarations, including observer names, classes, and areas, so you can see what code listens to the event before adding or changing observers.")
    suspend fun findObserversForEvent(eventName: String): String = withProjectReadAction {
        MagentoEventQueries.findObserversForEvent(it, eventName)
    }

    /**
     * Finds layout handles, block names, and container names that match the supplied query.
     */
    @McpTool(name = "find_layout_entities")
    @McpDescription("Find Magento layout handles, block names, and container names by exact or partial name. `name` may be a layout handle such as `catalog_product_view`, a block name such as `product.info.main`, a container name, or a partial fragment such as `checkout` or `product.info`. Use this when locating the correct layout XML file or insertion point before editing blocks, containers, or template references.")
    suspend fun findLayoutEntities(name: String): String = withProjectReadAction(
        requireSmartMode = false
    ) {
        MagentoViewQueries.findLayoutEntities(it, name)
    }

    /**
     * Finds UI component XML definitions by file name.
     */
    @McpTool(name = "find_ui_component")
    @McpDescription("Find Magento UI component XML files by exact or partial component name. `name` should usually be the UI component XML base name such as `product_form`, `sales_order_grid`, or `category_form`, without the `.xml` extension, but partial searches are also accepted. Use this to locate the defining file and owning module before changing admin forms, listings, or data providers.")
    suspend fun findUiComponent(name: String): String = withProjectReadAction(
        requireSmartMode = false
    ) {
        MagentoViewQueries.findUiComponent(it, name)
    }

    /**
     * Returns matching ACL resources and admin menu declarations for a shared identifier query.
     */
    @McpTool(name = "find_acl_or_menu")
    @McpDescription("Find Magento ACL resources and admin menu entries by exact or partial identifier. `identifier` should usually be an ACL or menu resource ID such as `Magento_Catalog::catalog` or `Foo_Bar::manage_items`, but partial searches such as `catalog` or `manage_items` are also accepted. The result combines matches from ACL and menu XML so you can reuse the correct resource ID when generating admin controllers, menus, or authorization checks.")
    suspend fun findAclOrMenu(identifier: String): String = withProjectReadAction {
        MagentoViewQueries.findAclOrMenu(it, identifier)
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
