/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

internal object MagentoScaffoldCommands {
    private val supportedTypes = listOf(
        "module",
        "plugin",
        "observer",
        "entity_crud",
        "controller",
        "cli_command",
        "block",
        "view_model",
        "product_eav_attribute",
        "category_eav_attribute",
        "customer_eav_attribute"
    )

    private val scaffoldSummaries = mapOf(
        "module" to "Create composer.json, registration.php, and etc/module.xml.",
        "plugin" to "Create a plugin class and matching di.xml declaration.",
        "observer" to "Create an observer class and matching events.xml declaration.",
        "entity_crud" to "Create DB schema, models, CQRS-style query/command classes, ACL/menu, and optional admin UI/API files.",
        "controller" to "Create a frontend or adminhtml controller class.",
        "cli_command" to "Create a Symfony console command class and register it in etc/di.xml.",
        "block" to "Create a block class under the module Block namespace.",
        "view_model" to "Create a view model class under the module ViewModel namespace.",
        "product_eav_attribute" to "Create a product EAV attribute data patch and optional source model.",
        "category_eav_attribute" to "Create a category EAV attribute data patch, admin form field, and optional source model.",
        "customer_eav_attribute" to "Create a customer EAV attribute data patch and optional source model."
    )

    fun help(): String = generalHelp()

    fun detailedSchema(scaffoldType: String): String {
        val normalizedType = normalizeType(scaffoldType)
        if (normalizedType.isEmpty()) {
            return "Provide scaffoldType to inspect. Call magento_scaffold with mode `help` for the compact scaffold catalog."
        }

        if (normalizedType !in supportedTypes) {
            return unknownTypeMessage(normalizedType)
        }

        return typeHelp(normalizedType)
    }

    fun render(project: Project, scaffoldType: String, parametersJson: String): String {
        val normalizedType = normalizeType(scaffoldType)
        if (normalizedType !in supportedTypes) {
            return unknownTypeMessage(normalizedType)
        }

        val parameters = try {
            parseParameters(parametersJson)
        } catch (exception: JSONException) {
            return "parametersJson must be a valid JSON object. ${exception.message}"
        }

        return try {
            dispatch(project, normalizedType, parameters)
        } catch (exception: MissingParameterException) {
            "Missing required parameter `${exception.parameterName}` for scaffoldType `$normalizedType`. Call magento_scaffold with mode `detailed_schema` and scaffoldType `$normalizedType` for the expected parameters."
        }
    }

    private fun dispatch(project: Project, scaffoldType: String, parameters: JSONObject): String {
        return when (scaffoldType) {
            "module" -> MagentoModuleCommands.createMagentoModule(
                project = project,
                packageName = parameters.requireString("packageName"),
                moduleName = parameters.requireString("moduleName")
            )

            "plugin" -> MagentoPluginCommands.createMagentoPlugin(
                project = project,
                moduleName = parameters.requireString("moduleName"),
                targetClassName = parameters.requireString("targetClassName"),
                targetMethodName = parameters.requireString("targetMethodName"),
                pluginType = parameters.requireString("pluginType"),
                pluginName = parameters.requireString("pluginName"),
                pluginClassFqn = parameters.requireString("pluginClassFqn"),
                area = parameters.optionalString("area", "base"),
                sortOrder = parameters.optionalInt("sortOrder", 10)
            )

            "observer" -> MagentoObserverCommands.createMagentoObserver(
                project = project,
                moduleName = parameters.requireString("moduleName"),
                eventName = parameters.requireString("eventName"),
                observerName = parameters.requireString("observerName"),
                observerClassFqn = parameters.requireString("observerClassFqn"),
                area = parameters.optionalString("area", "base")
            )

            "entity_crud" -> MagentoEntityCrudCommands.createMagentoEntityCrud(
                project = project,
                moduleName = parameters.requireString("moduleName"),
                entityName = parameters.requireString("entityName"),
                tableName = parameters.optionalString("tableName", ""),
                idFieldName = parameters.optionalString("idFieldName", ""),
                properties = parameters.requireStringList("properties"),
                createAdminUiComponents = parameters.optionalBoolean("createAdminUiComponents", true),
                createDataInterface = parameters.optionalBoolean("createDataInterface", true),
                createWebApi = parameters.optionalBoolean("createWebApi", false)
            )

            "controller" -> MagentoControllerCommands.createMagentoController(
                project = project,
                moduleName = parameters.requireString("moduleName"),
                controllerClassFqn = parameters.requireString("controllerClassFqn"),
                httpMethod = parameters.optionalString("httpMethod", "GET"),
                inheritClass = parameters.optionalBoolean("inheritClass", true),
                aclResource = parameters.optionalString("aclResource", "")
            )

            "cli_command" -> MagentoCliCommandCommands.createMagentoCliCommand(
                project = project,
                moduleName = parameters.requireString("moduleName"),
                commandClassFqn = parameters.requireString("commandClassFqn"),
                commandName = parameters.requireString("commandName"),
                commandDescription = parameters.requireString("commandDescription")
            )

            "block" -> MagentoBlockCommands.createMagentoBlock(
                project = project,
                moduleName = parameters.requireString("moduleName"),
                blockClassFqn = parameters.requireString("blockClassFqn")
            )

            "view_model" -> MagentoViewModelCommands.createMagentoViewModel(
                project = project,
                moduleName = parameters.requireString("moduleName"),
                viewModelClassFqn = parameters.requireString("viewModelClassFqn")
            )

            "product_eav_attribute" -> MagentoEavAttributeCommands.createMagentoProductEavAttribute(
                project = project,
                moduleName = parameters.requireString("moduleName"),
                attributeCode = parameters.requireString("attributeCode"),
                attributeLabel = parameters.requireString("attributeLabel"),
                backendType = parameters.optionalString("backendType", "varchar"),
                frontendInput = parameters.optionalString("frontendInput", "text"),
                dataPatchName = parameters.requireString("dataPatchName"),
                attributeGroup = parameters.optionalString("attributeGroup", "General"),
                sortOrder = parameters.optionalInt("sortOrder", 100),
                scope = parameters.optionalString("scope", "global"),
                sourceModelClassFqn = parameters.optionalString("sourceModelClassFqn", ""),
                applyTo = parameters.optionalString("applyTo", ""),
                required = parameters.optionalBoolean("required", false),
                visible = parameters.optionalBoolean("visible", true),
                usedInGrid = parameters.optionalBoolean("usedInGrid", false),
                visibleInGrid = parameters.optionalBoolean("visibleInGrid", false),
                filterableInGrid = parameters.optionalBoolean("filterableInGrid", false),
                htmlAllowedOnFront = parameters.optionalBoolean("htmlAllowedOnFront", false),
                visibleOnFront = parameters.optionalBoolean("visibleOnFront", false),
                options = parameters.optionalStringList("options")
            )

            "category_eav_attribute" -> MagentoEavAttributeCommands.createMagentoCategoryEavAttribute(
                project = project,
                moduleName = parameters.requireString("moduleName"),
                attributeCode = parameters.requireString("attributeCode"),
                attributeLabel = parameters.requireString("attributeLabel"),
                backendType = parameters.optionalString("backendType", "varchar"),
                frontendInput = parameters.optionalString("frontendInput", "text"),
                dataPatchName = parameters.requireString("dataPatchName"),
                attributeGroup = parameters.optionalString("attributeGroup", "General Information"),
                sortOrder = parameters.optionalInt("sortOrder", 100),
                scope = parameters.optionalString("scope", "global"),
                sourceModelClassFqn = parameters.optionalString("sourceModelClassFqn", ""),
                required = parameters.optionalBoolean("required", false),
                visible = parameters.optionalBoolean("visible", true),
                options = parameters.optionalStringList("options")
            )

            "customer_eav_attribute" -> MagentoEavAttributeCommands.createMagentoCustomerEavAttribute(
                project = project,
                moduleName = parameters.requireString("moduleName"),
                attributeCode = parameters.requireString("attributeCode"),
                attributeLabel = parameters.requireString("attributeLabel"),
                backendType = parameters.optionalString("backendType", "varchar"),
                frontendInput = parameters.optionalString("frontendInput", "text"),
                dataPatchName = parameters.requireString("dataPatchName"),
                sortOrder = parameters.optionalInt("sortOrder", 100),
                sourceModelClassFqn = parameters.optionalString("sourceModelClassFqn", ""),
                required = parameters.optionalBoolean("required", false),
                visible = parameters.optionalBoolean("visible", true),
                userDefined = parameters.optionalBoolean("userDefined", true),
                usedInGrid = parameters.optionalBoolean("usedInGrid", false),
                visibleInGrid = parameters.optionalBoolean("visibleInGrid", false),
                filterableInGrid = parameters.optionalBoolean("filterableInGrid", false),
                system = parameters.optionalBoolean("system", false),
                useInAdminhtmlCustomerForm = parameters.optionalBoolean("useInAdminhtmlCustomerForm", true),
                useInAdminhtmlCheckoutForm = parameters.optionalBoolean("useInAdminhtmlCheckoutForm", false),
                useInCustomerAccountCreateForm = parameters.optionalBoolean("useInCustomerAccountCreateForm", true),
                useInCustomerAccountEditForm = parameters.optionalBoolean("useInCustomerAccountEditForm", true),
                options = parameters.optionalStringList("options")
            )

            else -> unknownTypeMessage(scaffoldType)
        }
    }

    private fun parseParameters(parametersJson: String): JSONObject {
        val trimmed = parametersJson.trim()
        return if (trimmed.isEmpty()) {
            JSONObject()
        } else {
            JSONObject(trimmed)
        }
    }

    private fun normalizeType(scaffoldType: String): String {
        return when (scaffoldType.trim().lowercase().replace('-', '_')) {
            "crud", "entity" -> "entity_crud"
            "command", "console_command", "cli" -> "cli_command"
            "viewmodel" -> "view_model"
            "product_attribute", "product_eav" -> "product_eav_attribute"
            "category_attribute", "category_eav" -> "category_eav_attribute"
            "customer_attribute", "customer_eav" -> "customer_eav_attribute"
            else -> scaffoldType.trim().lowercase().replace('-', '_')
        }
    }

    private fun generalHelp(): String = """
        Magento scaffold library

        Use this as a three-step generator to keep context small:
        1. `help`: choose a scaffoldType from this compact catalog.
        2. `detailed_schema`: load detailed parameters only for the chosen scaffoldType.
        3. `render`: create files with scaffoldType and parametersJson.

        Scaffolds:
        ${supportedTypes.joinToString("\n") { "- `$it`: ${scaffoldSummaries.getValue(it)}" }}

        Next: call mode `detailed_schema` with one scaffoldType before rendering.
    """.trimIndent()

    private fun typeHelp(scaffoldType: String): String = when (scaffoldType) {
        "module" -> """
            scaffoldType: module
            Creates composer.json, registration.php, and etc/module.xml.
            Required: packageName, moduleName. Pass module parts, not composer package names.
            Example parametersJson:
            {"packageName":"Foo","moduleName":"Bar"}
            Result moduleName will be `Foo_Bar`; use that value for follow-up scaffolds.
        """.trimIndent()

        "plugin" -> """
            scaffoldType: plugin
            Creates a plugin class and di.xml declaration.
            Required: moduleName, targetClassName, targetMethodName, pluginType, pluginName, pluginClassFqn.
            Optional: area defaults to `base`; sortOrder defaults to 10.
            Enums: pluginType `before`, `around`, `after`; area `base`, `adminhtml`, `frontend`, `crontab`, `webapi_rest`, `webapi_soap`, `graphql`.
            Example parametersJson:
            {"moduleName":"Foo_Bar","targetClassName":"Magento\\Catalog\\Api\\ProductRepositoryInterface","targetMethodName":"save","pluginType":"before","pluginName":"foo_bar_product_save","pluginClassFqn":"Foo\\Bar\\Plugin\\ProductRepositoryPlugin","area":"frontend","sortOrder":10}
        """.trimIndent()

        "observer" -> """
            scaffoldType: observer
            Creates an observer class and events.xml declaration.
            Required: moduleName, eventName, observerName, observerClassFqn.
            Optional: area defaults to `base`.
            Example parametersJson:
            {"moduleName":"Foo_Bar","eventName":"catalog_product_save_after","observerName":"foo_bar_product_save_after","observerClassFqn":"Foo\\Bar\\Observer\\CatalogProductSaveAfter","area":"adminhtml"}
        """.trimIndent()

        "entity_crud" -> """
            scaffoldType: entity_crud
            Creates DB schema, model/resource model/collection, CQRS-style query and command classes, ACL/menu entries, and optional admin UI/API files.
            Required: moduleName, entityName, properties.
            Optional: tableName and idFieldName default from entityName; createAdminUiComponents true; createDataInterface true; createWebApi false.
            Structure note: this scaffold keeps read and write concerns separate. It generates classes such as `GetListQuery`, `Save<Entity>Command`, and `Delete<Entity>ByIdCommand` instead of a standard Magento repository-only layout.
            properties must be strings in `field_name:type` format. Types: int, float, string, bool. Do not include the primary ID field.
            Example parametersJson:
            {"moduleName":"Foo_Bar","entityName":"ReviewQueue","properties":["title:string","is_active:bool"],"createAdminUiComponents":true,"createDataInterface":true,"createWebApi":false}
        """.trimIndent()

        "controller" -> """
            scaffoldType: controller
            Creates a frontend or adminhtml controller class.
            Required: moduleName, controllerClassFqn.
            Optional: httpMethod defaults to GET; inheritClass defaults to true; aclResource is only used for adminhtml controllers with inheritClass true.
            Example parametersJson:
            {"moduleName":"Foo_Bar","controllerClassFqn":"Foo\\Bar\\Controller\\Adminhtml\\Order\\Index","httpMethod":"GET","aclResource":"Foo_Bar::orders"}
        """.trimIndent()

        "cli_command" -> """
            scaffoldType: cli_command
            Creates a Symfony console command class and registers it in etc/di.xml.
            Required: moduleName, commandClassFqn, commandName, commandDescription.
            Example parametersJson:
            {"moduleName":"Foo_Bar","commandClassFqn":"Foo\\Bar\\Console\\Command\\SyncData","commandName":"foo:bar:sync-data","commandDescription":"Synchronize Foo Bar data"}
        """.trimIndent()

        "block" -> """
            scaffoldType: block
            Creates a block class under the module Block namespace.
            Required: moduleName, blockClassFqn.
            Example parametersJson:
            {"moduleName":"Foo_Bar","blockClassFqn":"Foo\\Bar\\Block\\Product\\BadgeBlock"}
        """.trimIndent()

        "view_model" -> """
            scaffoldType: view_model
            Creates a view model class under the module ViewModel namespace.
            Required: moduleName, viewModelClassFqn.
            Example parametersJson:
            {"moduleName":"Foo_Bar","viewModelClassFqn":"Foo\\Bar\\ViewModel\\Product\\BadgeViewModel"}
        """.trimIndent()

        "product_eav_attribute" -> eavAttributeHelp(
            scaffoldType = "product_eav_attribute",
            extra = "Optional product-only fields: attributeGroup default `General`; scope default `global`; applyTo comma-separated product types; grid/front visibility booleans default false."
        )

        "category_eav_attribute" -> eavAttributeHelp(
            scaffoldType = "category_eav_attribute",
            extra = "Also creates view/adminhtml/ui_component/category_form.xml. Optional attributeGroup defaults to `General Information`; scope defaults to `global`."
        )

        "customer_eav_attribute" -> eavAttributeHelp(
            scaffoldType = "customer_eav_attribute",
            extra = "Optional form flags: useInAdminhtmlCustomerForm true, useInAdminhtmlCheckoutForm false, useInCustomerAccountCreateForm true, useInCustomerAccountEditForm true."
        )

        else -> unknownTypeMessage(scaffoldType)
    }

    private fun eavAttributeHelp(scaffoldType: String, extra: String): String = """
        scaffoldType: $scaffoldType
        Creates an EAV attribute data patch and optional source model.
        Required: moduleName, attributeCode, attributeLabel, dataPatchName.
        Optional: backendType default `varchar`; frontendInput default `text`; sourceModelClassFqn default empty; required false; visible true; sortOrder 100.
        Enums: backendType `static`, `varchar`, `int`, `text`, `datetime`, `decimal`; frontendInput `text`, `textarea`, `boolean`, `select`, `multiselect`, `date`, `price`, `hidden`; scope `global`, `store`, `website`.
        Options: provide `options` only for select or multiselect inputs.
        $extra
        Example parametersJson:
        {"moduleName":"Foo_Bar","attributeCode":"marketing_label","attributeLabel":"Marketing Label","backendType":"varchar","frontendInput":"text","dataPatchName":"AddMarketingLabelAttribute"}
    """.trimIndent()

    private fun unknownTypeMessage(scaffoldType: String): String {
        return "Unknown scaffoldType `$scaffoldType`. Supported values: ${supportedTypes.joinToString(", ")}. Call magento_scaffold with mode `help` for the compact scaffold catalog."
    }

    private fun JSONObject.requireString(name: String): String {
        val value = optionalString(name, "")
        if (value.isEmpty()) {
            throw MissingParameterException(name)
        }
        return value
    }

    private fun JSONObject.optionalString(name: String, defaultValue: String): String {
        if (!has(name) || isNull(name)) {
            return defaultValue
        }
        return optString(name, defaultValue).trim()
    }

    private fun JSONObject.optionalInt(name: String, defaultValue: Int): Int {
        return if (!has(name) || isNull(name)) {
            defaultValue
        } else {
            getInt(name)
        }
    }

    private fun JSONObject.optionalBoolean(name: String, defaultValue: Boolean): Boolean {
        return if (!has(name) || isNull(name)) {
            defaultValue
        } else {
            getBoolean(name)
        }
    }

    private fun JSONObject.requireStringList(name: String): List<String> {
        val values = optionalStringList(name)
        if (values.isEmpty()) {
            throw MissingParameterException(name)
        }
        return values
    }

    private fun JSONObject.optionalStringList(name: String): List<String> {
        if (!has(name) || isNull(name)) {
            return emptyList()
        }
        val value = get(name)
        if (value is JSONArray) {
            return (0 until value.length())
                .map { value.getString(it).trim() }
                .filter { it.isNotEmpty() }
        }
        if (value is String && value.trim().isNotEmpty()) {
            return listOf(value.trim())
        }
        return emptyList()
    }

    private class MissingParameterException(val parameterName: String) : RuntimeException(parameterName)
}
