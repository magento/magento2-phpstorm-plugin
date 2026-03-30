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
     * Creates a minimal Magento module under the configured Magento root path.
     */
    @McpTool(name = "create_magento_module")
    @McpDescription("Create a Magento module with composer.json, registration.php, and etc/module.xml. `packageName` and `moduleName` are Magento module parts, not a Composer package string: pass `Foo` and `Bar` to create the Magento module `Foo_Bar`. Each value must be non-empty, contain only letters and numbers, and start with an uppercase letter or digit. The Composer package name is derived automatically as `foo/module-bar`, so do not pass `foo/module-bar` as `packageName`.")
    suspend fun createMagentoModule(packageName: String, moduleName: String): String = withProjectEdtAction {
        MagentoModuleCommands.createMagentoModule(it, packageName, moduleName)
    }

    /**
     * Creates a Magento plugin class and matching di.xml declaration inside an editable module.
     */
    @McpTool(name = "create_magento_plugin")
    @McpDescription("Create a Magento plugin class and corresponding di.xml declaration. `moduleName` must use Magento `Vendor_Module` format such as `Foo_Bar`. `targetClassName` must be an existing PHP class or interface FQN such as `Magento\\Catalog\\Api\\ProductRepositoryInterface`, and `targetMethodName` must be an existing interceptable method on that type. `pluginType` must be one of `before`, `around`, or `after`. `pluginName` may contain only letters, numbers, underscores, hyphens, and colons. `pluginClassFqn` must be inside the module namespace and should usually live under `Plugin\\`, for example `Foo\\Bar\\Plugin\\ProductRepositoryPlugin`. `area` must be one of `base`, `adminhtml`, `frontend`, `crontab`, `webapi_rest`, `webapi_soap`, or `graphql`. `sortOrder` must be zero or greater.")
    suspend fun createMagentoPlugin(
        moduleName: String,
        targetClassName: String,
        targetMethodName: String,
        pluginType: String,
        pluginName: String,
        pluginClassFqn: String,
        area: String,
        sortOrder: Int
    ): String = withProjectEdtAction {
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
    @McpDescription("Create a Magento observer class and corresponding events.xml declaration. `moduleName` must use Magento `Vendor_Module` format such as `Foo_Bar`. `eventName` must be non-empty and must not contain whitespace, for example `catalog_product_save_after`. `observerName` may contain only letters, numbers, underscores, hyphens, and colons. `observerClassFqn` must be inside the module namespace and should usually live under `Observer\\`, for example `Foo\\Bar\\Observer\\CatalogProductSaveAfter`. `area` must be one of `base`, `adminhtml`, `frontend`, `crontab`, `webapi_rest`, `webapi_soap`, or `graphql`.")
    suspend fun createMagentoObserver(
        moduleName: String,
        eventName: String,
        observerName: String,
        observerClassFqn: String,
        area: String
    ): String = withProjectEdtAction {
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
     * Creates a Magento Entity CRUD scaffold with sensible defaults for database, ACL, menu, and optional UI/API layers.
     */
    @McpTool(name = "create_magento_entity_crud")
    @McpDescription("Create a Magento Entity CRUD scaffold for a module. `moduleName` must use Magento `Vendor_Module` format such as `Foo_Bar`. `entityName` must contain only letters and numbers, for example `ReviewQueue`. `tableName` should use lower_snake_case such as `review_queue`; leave it blank to derive it from `entityName`. `idFieldName` may contain letters, numbers, underscores, and hyphens; leave it blank to derive defaults such as `review_queue_id`. `properties` must be a list of `field_name:type` strings where `field_name` is lower_snake_case starting with a letter and `type` is one of `int`, `float`, `string`, or `bool`; for example `[\"title:string\", \"is_active:bool\", \"price:float\"]`. The primary ID column is generated automatically, so do not include it in `properties`. The tool generates the db schema, model/resource model/collection, data and command classes, ACL and menu entries, and optional admin UI components and Web API contracts.")
    suspend fun createMagentoEntityCrud(
        moduleName: String,
        entityName: String,
        tableName: String,
        idFieldName: String,
        properties: List<String>,
        createAdminUiComponents: Boolean,
        createDataInterface: Boolean,
        createWebApi: Boolean
    ): String = withProjectEdtAction {
        MagentoEntityCrudCommands.createMagentoEntityCrud(
            project = it,
            moduleName = moduleName,
            entityName = entityName,
            tableName = tableName,
            idFieldName = idFieldName,
            properties = properties,
            createAdminUiComponents = createAdminUiComponents,
            createDataInterface = createDataInterface,
            createWebApi = createWebApi
        )
    }

    /**
     * Creates a Magento controller class inside an editable module.
     */
    @McpTool(name = "create_magento_controller")
    @McpDescription("Create a Magento controller class inside an editable module. `moduleName` must use Magento `Vendor_Module` format such as `Foo_Bar`. `controllerClassFqn` must be inside the module `Controller` namespace, for example `Foo\\Bar\\Controller\\Index\\Index` or `Foo\\Bar\\Controller\\Adminhtml\\Order\\Index`; it must include at least one controller path segment and the final action class name. The target area is inferred from the namespace, with `Controller\\Adminhtml\\...` meaning `adminhtml` and all other controller namespaces meaning `frontend`. `httpMethod` must be one of `GET`, `POST`, `PUT`, or `DELETE`. `aclResource` may contain only letters, numbers, underscores, hyphens, and colons, and is only used when creating an adminhtml controller with `inheritClass=true`.")
    suspend fun createMagentoController(
        moduleName: String,
        controllerClassFqn: String,
        httpMethod: String,
        inheritClass: Boolean,
        aclResource: String
    ): String = withProjectEdtAction {
        MagentoControllerCommands.createMagentoController(
            project = it,
            moduleName = moduleName,
            controllerClassFqn = controllerClassFqn,
            httpMethod = httpMethod,
            inheritClass = inheritClass,
            aclResource = aclResource
        )
    }

    /**
     * Creates a Magento CLI command class and di.xml registration inside an editable module.
     */
    @McpTool(name = "create_magento_cli_command")
    @McpDescription("Create a Magento CLI command class and register it in `etc/di.xml`. `moduleName` must use Magento `Vendor_Module` format such as `Foo_Bar`. `commandClassFqn` must be inside the module namespace and should usually live under `Console\\Command`, for example `Foo\\Bar\\Console\\Command\\SyncData`. `commandName` is the executable CLI name such as `foo:bar:sync-data` and may contain only letters, numbers, underscores, hyphens, and colons. `commandDescription` must be non-empty and becomes the Symfony command description shown in `bin/magento list`.")
    suspend fun createMagentoCliCommand(
        moduleName: String,
        commandClassFqn: String,
        commandName: String,
        commandDescription: String
    ): String = withProjectEdtAction {
        MagentoCliCommandCommands.createMagentoCliCommand(
            project = it,
            moduleName = moduleName,
            commandClassFqn = commandClassFqn,
            commandName = commandName,
            commandDescription = commandDescription
        )
    }

    /**
     * Creates a Magento block class inside an editable module.
     */
    @McpTool(name = "create_magento_block")
    @McpDescription("Create a Magento block class inside an editable module. `moduleName` must use Magento `Vendor_Module` format such as `Foo_Bar`. `blockClassFqn` must be inside the module namespace and specifically under `Block\\`, for example `Foo\\Bar\\Block\\Product\\BadgeBlock`.")
    suspend fun createMagentoBlock(moduleName: String, blockClassFqn: String): String = withProjectEdtAction {
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
    @McpDescription("Create a Magento view model class inside an editable module. `moduleName` must use Magento `Vendor_Module` format such as `Foo_Bar`. `viewModelClassFqn` must be inside the module namespace and specifically under `ViewModel\\`, for example `Foo\\Bar\\ViewModel\\Product\\BadgeViewModel`.")
    suspend fun createMagentoViewModel(moduleName: String, viewModelClassFqn: String): String = withProjectEdtAction {
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
    @McpDescription("Create a Magento product EAV attribute data patch and optional source model. `moduleName` must use Magento `Vendor_Module` format such as `Foo_Bar`. `attributeCode` must use lower_snake_case starting with a letter, for example `marketing_label`. `backendType` must be one of `static`, `varchar`, `int`, `text`, `datetime`, or `decimal`. `frontendInput` must be one of `text`, `textarea`, `boolean`, `select`, `multiselect`, `date`, `price`, or `hidden`. `dataPatchName` must be a valid PHP class name such as `AddMarketingLabelAttribute`. `attributeGroup` must be non-empty. `sortOrder` must be zero or greater. `scope` must be one of `global`, `store`, or `website`. `applyTo` must be a comma-separated list without spaces inside values, for example `simple,virtual`. `sourceModelClassFqn`, when provided, must be a valid PHP class FQN inside the module namespace. `options` may only be provided for `select` or `multiselect` inputs.")
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
    ): String = withProjectEdtAction {
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
    @McpDescription("Create a Magento category EAV attribute data patch, category_form.xml field, and optional source model. `moduleName` must use Magento `Vendor_Module` format such as `Foo_Bar`. `attributeCode` must use lower_snake_case starting with a letter. `backendType` must be one of `static`, `varchar`, `int`, `text`, `datetime`, or `decimal`. `frontendInput` must be one of `text`, `textarea`, `boolean`, `select`, `multiselect`, `date`, `price`, or `hidden`. `dataPatchName` must be a valid PHP class name. `attributeGroup` must be non-empty. `sortOrder` must be zero or greater. `scope` must be one of `global`, `store`, or `website`. `sourceModelClassFqn`, when provided, must be a valid PHP class FQN inside the module namespace. `options` may only be provided for `select` or `multiselect` inputs.")
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
    ): String = withProjectEdtAction {
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
    @McpDescription("Create a Magento customer EAV attribute data patch and optional source model. `moduleName` must use Magento `Vendor_Module` format such as `Foo_Bar`. `attributeCode` must use lower_snake_case starting with a letter. `backendType` must be one of `static`, `varchar`, `int`, `text`, `datetime`, or `decimal`. `frontendInput` must be one of `text`, `textarea`, `boolean`, `select`, `multiselect`, `date`, `price`, or `hidden`. `dataPatchName` must be a valid PHP class name. `sortOrder` must be zero or greater. `sourceModelClassFqn`, when provided, must be a valid PHP class FQN inside the module namespace. `options` may only be provided for `select` or `multiselect` inputs.")
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
    ): String = withProjectEdtAction {
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
    suspend fun findLayoutEntities(name: String): String = withProjectReadAction {
        MagentoViewQueries.findLayoutEntities(it, name)
    }

    /**
     * Finds UI component XML definitions by file name.
     */
    @McpTool(name = "find_ui_component")
    @McpDescription("Find Magento UI component XML files by exact or partial component name. `name` should usually be the UI component XML base name such as `product_form`, `sales_order_grid`, or `category_form`, without the `.xml` extension, but partial searches are also accepted. Use this to locate the defining file and owning module before changing admin forms, listings, or data providers.")
    suspend fun findUiComponent(name: String): String = withProjectReadAction {
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
    @McpDescription("Inspect the current Magento project for local CLI wrappers under `bin/`, including Mark Shust Docker scripts such as `bin/magento`, `bin/n98-magerun2`, `bin/php`, or `bin/composer`. Call this before running shell commands that would normally use Magento CLI, PHP, Composer, or n98-magerun. The result lists detected wrapper commands, configured wrapper candidates, and example invocations; agents should use the returned project-local wrapper path exactly, for example `./bin/magento cache:flush`, instead of global binaries.")
    suspend fun describeMagentoCliEnvironment(): String = withProjectReadAction {
        MagentoCliToolQueries.describeCliEnvironment(it)
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
