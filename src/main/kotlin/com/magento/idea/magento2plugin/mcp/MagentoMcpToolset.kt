/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.CoroutineContext

/**
 * Exposes Magento-specific MCP tools backed by the plugin's existing indexes and generators.
 */
class MagentoMcpToolset : McpToolset {
    /**
     * Returns the Magento root path configured for the current project.
     */
    @McpTool(name = "get_magento_root_path")
    @McpDescription("Return the Magento root path configured for the current project.")
    suspend fun getMagentoRootPath(): String = withProjectAction(
        validateProject = false
    ) {
        MagentoProjectQueries.getMagentoRootPath(it)
    }

    /**
     * Creates a minimal Magento module under the configured Magento root path.
     */
    @McpTool(name = "create_magento_module")
    @McpDescription("Create a Magento module with composer.json, registration.php, and etc/module.xml.")
    suspend fun createMagentoModule(packageName: String, moduleName: String): String = withProjectAction {
        MagentoModuleCommands.createMagentoModule(it, packageName, moduleName)
    }

    /**
     * Creates a Magento plugin class and matching di.xml declaration inside an editable module.
     */
    @McpTool(name = "create_magento_plugin")
    @McpDescription("Create a Magento plugin class and corresponding di.xml declaration.")
    suspend fun createMagentoPlugin(
        moduleName: String,
        targetClassName: String,
        targetMethodName: String,
        pluginType: String,
        pluginName: String,
        pluginClassFqn: String,
        area: String,
        sortOrder: Int
    ): String = withProjectAction {
        MagentoPluginCommands.createMagentoPlugin(
            project = it,
            moduleName = moduleName,
            targetClassName = targetClassName,
            targetMethodName = targetMethodName,
            pluginType = pluginType,
            pluginName = pluginName,
            pluginClassFqn = pluginClassFqn,
            area = area,
            sortOrder = sortOrder
        )
    }

    /**
     * Creates a Magento observer class and matching events.xml declaration inside an editable module.
     */
    @McpTool(name = "create_magento_observer")
    @McpDescription("Create a Magento observer class and corresponding events.xml declaration.")
    suspend fun createMagentoObserver(
        moduleName: String,
        eventName: String,
        observerName: String,
        observerClassFqn: String,
        area: String
    ): String = withProjectAction {
        MagentoObserverCommands.createMagentoObserver(
            project = it,
            moduleName = moduleName,
            eventName = eventName,
            observerName = observerName,
            observerClassFqn = observerClassFqn,
            area = area
        )
    }

    /**
     * Creates a Magento block class inside an editable module.
     */
    @McpTool(name = "create_magento_block")
    @McpDescription("Create a Magento block class inside an editable module.")
    suspend fun createMagentoBlock(moduleName: String, blockClassFqn: String): String = withProjectAction {
        MagentoBlockCommands.createMagentoBlock(
            project = it,
            moduleName = moduleName,
            blockClassFqn = blockClassFqn
        )
    }

    /**
     * Creates a Magento view model class inside an editable module.
     */
    @McpTool(name = "create_magento_view_model")
    @McpDescription("Create a Magento view model class inside an editable module.")
    suspend fun createMagentoViewModel(moduleName: String, viewModelClassFqn: String): String = withProjectAction {
        MagentoViewModelCommands.createMagentoViewModel(
            project = it,
            moduleName = moduleName,
            viewModelClassFqn = viewModelClassFqn
        )
    }

    /**
     * Creates a Magento product EAV attribute data patch and optional source model inside an editable module.
     */
    @McpTool(name = "create_magento_product_eav_attribute")
    @McpDescription("Create a Magento product EAV attribute data patch and optional source model.")
    suspend fun createMagentoProductEavAttribute(
        moduleName: String,
        attributeCode: String,
        attributeLabel: String,
        backendType: String,
        frontendInput: String,
        dataPatchName: String,
        attributeGroup: String,
        sortOrder: Int,
        scope: String,
        sourceModelClassFqn: String,
        applyTo: String,
        required: Boolean,
        visible: Boolean,
        usedInGrid: Boolean,
        visibleInGrid: Boolean,
        filterableInGrid: Boolean,
        htmlAllowedOnFront: Boolean,
        visibleOnFront: Boolean,
        options: List<String>
    ): String = withProjectAction {
        MagentoEavAttributeCommands.createMagentoProductEavAttribute(
            project = it,
            moduleName = moduleName,
            attributeCode = attributeCode,
            attributeLabel = attributeLabel,
            backendType = backendType,
            frontendInput = frontendInput,
            dataPatchName = dataPatchName,
            attributeGroup = attributeGroup,
            sortOrder = sortOrder,
            scope = scope,
            sourceModelClassFqn = sourceModelClassFqn,
            applyTo = applyTo,
            required = required,
            visible = visible,
            usedInGrid = usedInGrid,
            visibleInGrid = visibleInGrid,
            filterableInGrid = filterableInGrid,
            htmlAllowedOnFront = htmlAllowedOnFront,
            visibleOnFront = visibleOnFront,
            options = options
        )
    }

    /**
     * Creates a Magento category EAV attribute data patch, admin form field, and optional source model.
     */
    @McpTool(name = "create_magento_category_eav_attribute")
    @McpDescription("Create a Magento category EAV attribute data patch, category_form.xml field, and optional source model.")
    suspend fun createMagentoCategoryEavAttribute(
        moduleName: String,
        attributeCode: String,
        attributeLabel: String,
        backendType: String,
        frontendInput: String,
        dataPatchName: String,
        attributeGroup: String,
        sortOrder: Int,
        scope: String,
        sourceModelClassFqn: String,
        required: Boolean,
        visible: Boolean,
        options: List<String>
    ): String = withProjectAction {
        MagentoEavAttributeCommands.createMagentoCategoryEavAttribute(
            project = it,
            moduleName = moduleName,
            attributeCode = attributeCode,
            attributeLabel = attributeLabel,
            backendType = backendType,
            frontendInput = frontendInput,
            dataPatchName = dataPatchName,
            attributeGroup = attributeGroup,
            sortOrder = sortOrder,
            scope = scope,
            sourceModelClassFqn = sourceModelClassFqn,
            required = required,
            visible = visible,
            options = options
        )
    }

    /**
     * Creates a Magento customer EAV attribute data patch and optional source model inside an editable module.
     */
    @McpTool(name = "create_magento_customer_eav_attribute")
    @McpDescription("Create a Magento customer EAV attribute data patch and optional source model.")
    suspend fun createMagentoCustomerEavAttribute(
        moduleName: String,
        attributeCode: String,
        attributeLabel: String,
        backendType: String,
        frontendInput: String,
        dataPatchName: String,
        sortOrder: Int,
        sourceModelClassFqn: String,
        required: Boolean,
        visible: Boolean,
        userDefined: Boolean,
        usedInGrid: Boolean,
        visibleInGrid: Boolean,
        filterableInGrid: Boolean,
        system: Boolean,
        useInAdminhtmlCustomerForm: Boolean,
        useInAdminhtmlCheckoutForm: Boolean,
        useInCustomerAccountCreateForm: Boolean,
        useInCustomerAccountEditForm: Boolean,
        options: List<String>
    ): String = withProjectAction {
        MagentoEavAttributeCommands.createMagentoCustomerEavAttribute(
            project = it,
            moduleName = moduleName,
            attributeCode = attributeCode,
            attributeLabel = attributeLabel,
            backendType = backendType,
            frontendInput = frontendInput,
            dataPatchName = dataPatchName,
            sortOrder = sortOrder,
            sourceModelClassFqn = sourceModelClassFqn,
            required = required,
            visible = visible,
            userDefined = userDefined,
            usedInGrid = usedInGrid,
            visibleInGrid = visibleInGrid,
            filterableInGrid = filterableInGrid,
            system = system,
            useInAdminhtmlCustomerForm = useInAdminhtmlCustomerForm,
            useInAdminhtmlCheckoutForm = useInAdminhtmlCheckoutForm,
            useInCustomerAccountCreateForm = useInCustomerAccountCreateForm,
            useInCustomerAccountEditForm = useInCustomerAccountEditForm,
            options = options
        )
    }

    /**
     * Resolves Magento modules by exact or fuzzy module name and returns a compact summary.
     */
    @McpTool(name = "find_magento_module")
    @McpDescription("Find Magento modules by exact or partial module name.")
    suspend fun findMagentoModule(moduleName: String): String = withProjectReadAction {
        MagentoModuleQueries.findMagentoModule(it, moduleName)
    }

    /**
     * Returns DI declarations that reference the requested class or virtual type.
     */
    @McpTool(name = "find_di_config_for_class")
    @McpDescription("Find dependency injection declarations related to a PHP class or virtual type.")
    suspend fun findDiConfigForClass(className: String): String = withProjectReadAction {
        MagentoDiQueries.findDiConfigForClass(it, className)
    }

    /**
     * Finds plugin methods that can intercept the requested target method.
     */
    @McpTool(name = "find_plugins_for_method")
    @McpDescription("Find Magento plugins that intercept a target class method.")
    suspend fun findPluginsForMethod(className: String, methodName: String): String = withProjectReadAction {
        MagentoDiQueries.findPluginsForMethod(it, className, methodName)
    }

    /**
     * Lists observer declarations for matching Magento event names.
     */
    @McpTool(name = "find_observers_for_event")
    @McpDescription("Find Magento observer declarations for an event name.")
    suspend fun findObserversForEvent(eventName: String): String = withProjectReadAction {
        MagentoEventQueries.findObserversForEvent(it, eventName)
    }

    /**
     * Finds layout handles, block names, and container names that match the supplied query.
     */
    @McpTool(name = "find_layout_entities")
    @McpDescription("Find Magento layout handles, blocks, and containers by exact or partial name.")
    suspend fun findLayoutEntities(name: String): String = withProjectReadAction {
        MagentoViewQueries.findLayoutEntities(it, name)
    }

    /**
     * Finds UI component XML definitions by file name.
     */
    @McpTool(name = "find_ui_component")
    @McpDescription("Find Magento UI component XML files by exact or partial component name.")
    suspend fun findUiComponent(name: String): String = withProjectReadAction {
        MagentoViewQueries.findUiComponent(it, name)
    }

    /**
     * Returns matching ACL resources and admin menu declarations for a shared identifier query.
     */
    @McpTool(name = "find_acl_or_menu")
    @McpDescription("Find Magento ACL resources and admin menu entries by exact or partial identifier.")
    suspend fun findAclOrMenu(identifier: String): String = withProjectReadAction {
        MagentoViewQueries.findAclOrMenu(it, identifier)
    }

    /**
     * Resolves the active IDE project from MCP call context, validates it, and executes the tool body.
     */
    private suspend fun withProjectAction(
        validateProject: Boolean = true,
        query: (Project) -> String
    ): String {
        val project = resolveProject(currentCoroutineContext()) ?: return "MCP project context is unavailable."

        if (validateProject) {
            val validationMessage = MagentoMcpSupport.validateProject(project)
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
        query: (Project) -> String
    ): String = withProjectAction(validateProject) { project ->
        try {
            ReadAction.computeCancellable<String, RuntimeException> {
                query(project)
            }
        } catch (_: ReadAction.CannotReadException) {
            "The request was cancelled by a pending write action. Retry."
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
}
