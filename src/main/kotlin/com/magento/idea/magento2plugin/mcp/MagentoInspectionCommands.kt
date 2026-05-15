/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project
import org.json.JSONException
import org.json.JSONObject

internal object MagentoInspectionCommands {
    private val supportedTypes = listOf(
        "module",
        "di_config",
        "plugins_for_method",
        "observers_for_event",
        "layout_entities",
        "ui_component",
        "acl_or_menu"
    )

    private val querySummaries = mapOf(
        "module" to "Find modules by exact or partial Magento module name.",
        "di_config" to "Find di.xml declarations related to a PHP FQN or virtual type.",
        "plugins_for_method" to "Find plugins that intercept a target class or interface method.",
        "observers_for_event" to "Find observer declarations by full or partial Magento event name.",
        "layout_entities" to "Find layout handles, block names, and container names.",
        "ui_component" to "Find UI component XML definitions by component file name.",
        "acl_or_menu" to "Find ACL resource IDs and admin menu entries."
    )

    fun help(): String = """
        Magento inspection library

        Use this as a three-step lookup flow to keep context small:
        1. `help`: choose a queryType from this compact catalog.
        2. `detailed_schema`: load detailed parameters only for the chosen queryType.
        3. `query`: run the selected inspection with queryType and parametersJson.

        Queries:
        ${supportedTypes.joinToString("\n") { "- `$it`: ${querySummaries.getValue(it)}" }}

        Next: call mode `detailed_schema` with one queryType before querying.
    """.trimIndent()

    fun detailedSchema(queryType: String): String {
        val normalizedType = normalizeType(queryType)
        if (normalizedType.isEmpty()) {
            return "Provide queryType to inspect. Call magento_inspect with mode `help` for the compact query catalog."
        }

        if (normalizedType !in supportedTypes) {
            return unknownTypeMessage(normalizedType)
        }

        return typeHelp(normalizedType)
    }

    fun query(project: Project, queryType: String, parametersJson: String): String {
        val normalizedType = normalizeType(queryType)
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
            "Missing required parameter `${exception.parameterName}` for queryType `$normalizedType`. Call magento_inspect with mode `detailed_schema` and queryType `$normalizedType` for the expected parameters."
        }
    }

    fun requiresSmartMode(queryType: String): Boolean {
        return normalizeType(queryType) !in setOf("layout_entities", "ui_component")
    }

    private fun dispatch(project: Project, queryType: String, parameters: JSONObject): String {
        return when (queryType) {
            "module" -> MagentoModuleQueries.findMagentoModule(
                project = project,
                moduleName = parameters.requireString("moduleName")
            )

            "di_config" -> MagentoDiQueries.findDiConfigForClass(
                project = project,
                className = parameters.requireString("className")
            )

            "plugins_for_method" -> MagentoDiQueries.findPluginsForMethod(
                project = project,
                className = parameters.requireString("className"),
                methodName = parameters.requireString("methodName")
            )

            "observers_for_event" -> MagentoEventQueries.findObserversForEvent(
                project = project,
                eventName = parameters.requireString("eventName")
            )

            "layout_entities" -> MagentoViewQueries.findLayoutEntities(
                project = project,
                name = parameters.requireString("name")
            )

            "ui_component" -> MagentoViewQueries.findUiComponent(
                project = project,
                name = parameters.requireString("name")
            )

            "acl_or_menu" -> MagentoViewQueries.findAclOrMenu(
                project = project,
                identifier = parameters.requireString("identifier")
            )

            else -> unknownTypeMessage(queryType)
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

    private fun normalizeType(queryType: String): String {
        return when (queryType.trim().lowercase().replace('-', '_')) {
            "magento_module", "modules" -> "module"
            "di", "di_for_class" -> "di_config"
            "plugins", "plugin_method" -> "plugins_for_method"
            "observers", "event_observers" -> "observers_for_event"
            "layout" -> "layout_entities"
            "ui", "ui_components" -> "ui_component"
            "acl", "menu" -> "acl_or_menu"
            else -> queryType.trim().lowercase().replace('-', '_')
        }
    }

    private fun typeHelp(queryType: String): String = when (queryType) {
        "module" -> """
            queryType: module
            Finds Magento modules by exact or partial module name.
            Required: moduleName. Use `Vendor_Module` when known, or partial searches such as `Catalog`, `Magento_`, or `Foo`.
            Example parametersJson:
            {"moduleName":"Magento_Catalog"}
        """.trimIndent()

        "di_config" -> """
            queryType: di_config
            Finds dependency injection declarations related to a PHP class, interface, preference target, or virtual type.
            Required: className. Use a PHP FQN such as `Magento\\Catalog\\Model\\Product`, an interface FQN, or a virtual type name.
            Result summarizes matching preferences, types, virtual types, arguments, and plugins.
            Example parametersJson:
            {"className":"Magento\\Catalog\\Api\\ProductRepositoryInterface"}
        """.trimIndent()

        "plugins_for_method" -> """
            queryType: plugins_for_method
            Finds Magento plugins that intercept a target class or interface method.
            Required: className, methodName. methodName must be the bare PHP method name, such as `save` or `getById`, without `before`, `around`, or `after` prefixes.
            Example parametersJson:
            {"className":"Magento\\Catalog\\Api\\ProductRepositoryInterface","methodName":"save"}
        """.trimIndent()

        "observers_for_event" -> """
            queryType: observers_for_event
            Finds observer declarations for a Magento event.
            Required: eventName. Use a full event name such as `catalog_product_save_after` or a partial search such as `product_save`.
            Example parametersJson:
            {"eventName":"catalog_product_save_after"}
        """.trimIndent()

        "layout_entities" -> """
            queryType: layout_entities
            Finds layout handles, block names, and container names by exact or partial value.
            Required: name. Use a layout handle such as `catalog_product_view`, a block name such as `product.info.main`, a container name, or a fragment such as `checkout`.
            Example parametersJson:
            {"name":"catalog_product_view"}
        """.trimIndent()

        "ui_component" -> """
            queryType: ui_component
            Finds Magento UI component XML files by exact or partial component name.
            Required: name. Use the UI component XML base name without `.xml`, such as `product_form`, `sales_order_grid`, or `category_form`.
            Example parametersJson:
            {"name":"product_form"}
        """.trimIndent()

        "acl_or_menu" -> """
            queryType: acl_or_menu
            Finds ACL resources and admin menu entries by exact or partial identifier.
            Required: identifier. Use an ACL or menu resource ID such as `Magento_Catalog::catalog` or `Foo_Bar::manage_items`, or a fragment such as `catalog`.
            Example parametersJson:
            {"identifier":"Magento_Catalog::catalog"}
        """.trimIndent()

        else -> unknownTypeMessage(queryType)
    }

    private fun unknownTypeMessage(queryType: String): String {
        return "Unknown queryType `$queryType`. Supported values: ${supportedTypes.joinToString(", ")}. Call magento_inspect with mode `help` for the compact query catalog."
    }

    private fun JSONObject.requireString(name: String): String {
        if (!has(name) || isNull(name)) {
            throw MissingParameterException(name)
        }
        val value = optString(name, "").trim()
        if (value.isEmpty()) {
            throw MissingParameterException(name)
        }
        return value
    }

    private class MissingParameterException(val parameterName: String) : RuntimeException(parameterName)
}
