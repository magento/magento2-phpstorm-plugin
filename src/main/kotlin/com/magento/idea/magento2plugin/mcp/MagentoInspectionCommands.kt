/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import org.json.JSONException
import org.json.JSONObject

internal object MagentoInspectionCommands {
    const val MISSING_TYPE_MESSAGE =
        "Provide queryType to inspect. Call magento_inspect with mode `help` for the compact query catalog."

    private val supportedTypes = listOf(
        "module",
        "module_dependencies",
        "di_config",
        "plugins_for_method",
        "observers_for_event",
        "layout_entities",
        "ui_component",
        "ui_template",
        "acl_or_menu"
    )

    /** Query types with structured results, and the filters each supports. */
    private val jsonFilters = mapOf(
        "module" to setOf("path"),
        "di_config" to setOf("module", "area", "path"),
        "plugins_for_method" to setOf("module", "area", "path"),
        "observers_for_event" to setOf("module", "area", "path"),
        "layout_entities" to setOf("module", "area", "path"),
        "ui_component" to setOf("module", "area", "path"),
        "acl_or_menu" to setOf("module", "area", "path")
    )

    private val effectiveQueries = setOf("plugins_for_method", "observers_for_event")
    private val viewQueries = setOf("layout_entities", "ui_component")
    private const val VIEW_AREA_HELP = "area: frontend, adminhtml or base, inferred from the Magento view path."

    private val filterHelp = mapOf(
        "module" to "module: exact name of the module that owns the file.",
        "area" to "area: global, frontend, adminhtml or another etc/<area> folder.",
        "path" to "path: project-relative directory or file prefix. A module at the project root has path `.`, " +
            "which no path filter selects; its files, such as `etc/di.xml`, are selected as usual."
    )

    private val queryParameters = mapOf(
        "module" to "moduleName",
        "di_config" to "className",
        "plugins_for_method" to "className",
        "observers_for_event" to "eventName",
        "layout_entities" to "name",
        "ui_component" to "name",
        "acl_or_menu" to "identifier"
    )

    private val querySummaries = mapOf(
        "module" to "Find modules by exact or partial Magento module name.",
        "module_dependencies" to "Inspect declared sequence ordering and Composer package requirements in either direction.",
        "di_config" to "Find di.xml declarations related to a PHP FQN or virtual type.",
        "plugins_for_method" to "Find declared plugin methods or resolve them for an effective area.",
        "observers_for_event" to "Find observer declarations by full or partial Magento event name.",
        "layout_entities" to "Find layout handles, block names, and container names.",
        "ui_component" to "Find UI component XML definitions by component file name.",
        "ui_template" to "Resolve a literal UI template alias to candidate HTML files.",
        "acl_or_menu" to "Find ACL resource IDs and admin menu entries."
    )

    @JvmOverloads
    fun help(queryTypes: Set<String>? = null): String = """
        Magento inspection library

        Use this as a three-step lookup flow to keep context small:
        1. `help`: choose a queryType from this compact catalog.
        2. `detailed_schema`: load detailed parameters only for the chosen queryType.
        3. `query`: run the selected inspection with queryType and parametersJson.

        Queries:
        ${availableTypes().filter { queryTypes == null || it in queryTypes }.joinToString("\n") { "- `$it`: ${querySummaries.getValue(it)}" }}
        ${if (MagentoMethodPluginQueryProvider.isAvailable()) "" else MagentoMethodPluginQueryProvider.UNAVAILABLE_MESSAGE}

        Next: call mode `detailed_schema` with one queryType before querying.
    """.trimIndent()

    @JvmOverloads
    fun detailedSchema(queryType: String, queryTypes: Set<String>? = null): String {
        val normalizedType = normalizeType(queryType)
        if (normalizedType.isEmpty()) {
            return MISSING_TYPE_MESSAGE
        }

        if (normalizedType !in supportedTypes) {
            return unknownTypeMessage(normalizedType, queryTypes)
        }

        hostUnavailable(normalizedType, queryTypes)?.let { return it }

        if (!isAvailable(normalizedType)) {
            return MagentoMethodPluginQueryProvider.UNAVAILABLE_MESSAGE
        }

        val filters = jsonFilters[normalizedType] ?: return typeHelp(normalizedType)
        val parameter = queryParameters.getValue(normalizedType)
        val example = when (normalizedType) {
            "plugins_for_method" -> """{"className":"Magento\\Catalog\\Api\\ProductRepositoryInterface","methodName":"save","effectiveArea":"adminhtml","area":"global","format":"json","limit":50}"""
            "observers_for_event" -> """{"eventName":"catalog_product_save_after","effectiveArea":"frontend","area":"global","format":"json","limit":50}"""
            else -> """{"$parameter":"…","format":"json","limit":50}"""
        }
        return typeHelp(normalizedType) + "\n\n" + """
            Structured results (optional): add "format":"json" to parametersJson.
            Optional with format json: limit (1-${MagentoInspectionResults.MAX_LIMIT}, default ${MagentoInspectionResults.DEFAULT_LIMIT}) and cursor (nextCursor from the previous page; keep the query and filters the same).
            Supported JSON filters ${filters.sorted().joinToString(", ")}.
            ${filters.sorted().joinToString(" ") { if (it == "area" && normalizedType in viewQueries) VIEW_AREA_HELP else filterHelp.getValue(it) }}
            Reply: schemaVersion, queryType, query, scope, status (ok, no_matches, indexing, unavailable, invalid_request, stale_cursor), total, offset, returned, complete, nextCursor, and records with kind, key, path, line, module, area and query-specific fields.
            Line is null for whole-file records such as modules, layout handles and UI components.
            For successful replies, total counts records after filtering, and complete means the last matching record has been returned.
            Example parametersJson:
            $example
        """.trimIndent()
    }

    @JvmOverloads
    fun query(
        project: Project, queryType: String, parametersJson: String,
        queryTypes: Set<String>? = null, indexKeys: MagentoIndexKeys? = null
    ): String {
        val normalizedType = normalizeType(queryType)
        if (normalizedType !in supportedTypes) {
            val message = unknownTypeMessage(normalizedType, queryTypes)
            return if (wantsJson(parametersJson)) {
                MagentoInspectionResults.status(normalizedType, "", null, "invalid_request", message)
            } else message
        }

        val unavailableMessage = hostUnavailable(normalizedType, queryTypes)
            ?: MagentoMethodPluginQueryProvider.UNAVAILABLE_MESSAGE.takeUnless { isAvailable(normalizedType) }
        if (unavailableMessage != null) return unavailable(normalizedType, parametersJson, unavailableMessage)

        val parameters = try {
            parseParameters(parametersJson)
        } catch (exception: JSONException) {
            return "parametersJson must be a valid JSON object. ${exception.message}"
        }

        if (MagentoInspectionResults.isJson(parameters)) {
            return jsonQuery(project, normalizedType, parameters, indexKeys)
        }
        return try {
            // Queries with a native payload, such as module_dependencies, keep their own parameters.
            val options = jsonFilters[normalizedType]?.let {
                MagentoInspectionResults.options(parameters, it, normalizedType in effectiveQueries)
            }
            if (DumbService.getInstance(project).isDumb) {
                return MagentoMcpSupport.INDEXES_NOT_READY_MESSAGE
            }
            dispatch(project, normalizedType, parameters, indexKeys, options)
        } catch (exception: MissingParameterException) {
            "Missing required parameter `${exception.parameterName}` for queryType `$normalizedType`. Call magento_inspect with mode `detailed_schema` and queryType `$normalizedType` for the expected parameters."
        } catch (exception: InvalidInspectionRequest) {
            exception.message.orEmpty()
        } catch (_: IndexNotReadyException) {
            MagentoMcpSupport.INDEXES_NOT_READY_MESSAGE
        }
    }

    fun requiresSmartMode(queryType: String): Boolean {
        val normalizedType = normalizeType(queryType)
        return normalizedType in supportedTypes && isAvailable(normalizedType)
    }

    private fun hostUnavailable(type: String, queryTypes: Set<String>?): String? =
        if (queryTypes != null && type !in queryTypes)
            "Query `$type` is unavailable in this host. Available queries: ${queryTypes.sorted().joinToString()}."
        else null

    private fun availableTypes(): List<String> = supportedTypes.filter(::isAvailable)

    private fun isAvailable(queryType: String): Boolean =
        queryType != "plugins_for_method" || MagentoMethodPluginQueryProvider.isAvailable()

    private fun dispatch(
        project: Project,
        queryType: String,
        parameters: JSONObject,
        indexKeys: MagentoIndexKeys?,
        options: InspectionOptions?
    ): String {
        return when (queryType) {
            "module" -> MagentoModuleQueries.findMagentoModule(
                project = project,
                moduleName = parameters.requireString("moduleName"),
                indexKeys = indexKeys
            )

            "module_dependencies" -> MagentoModuleDependencyQueries.query(
                project, parameters.requireString("moduleName"), parameters.optString("direction", "both"),
                if (parameters.has("limit")) parameters.opt("limit") else 20
            )

            "di_config" -> MagentoDiQueries.findDiConfigForClass(
                project = project,
                className = parameters.requireString("className")
            )

            "plugins_for_method" -> MagentoDiQueries.findPluginsForMethod(
                project = project,
                className = parameters.requireString("className"),
                methodName = parameters.requireString("methodName"),
                effectiveArea = options?.effectiveArea, fileArea = options?.area
            )

            "observers_for_event" -> MagentoEventQueries.findObserversForEvent(
                project = project,
                eventName = parameters.requireString("eventName"), effectiveArea = options?.effectiveArea, fileArea = options?.area
            )

            "layout_entities" -> MagentoViewQueries.findLayoutEntities(
                project = project,
                name = parameters.requireString("name"),
                indexKeys = indexKeys
            )

            "ui_component" -> MagentoViewQueries.findUiComponent(
                project = project,
                name = parameters.requireString("name")
            )

            "ui_template" -> MagentoViewQueries.findUiTemplate(
                project = project,
                templatePath = parameters.requireString("templatePath")
            )

            "acl_or_menu" -> MagentoViewQueries.findAclOrMenu(
                project = project,
                identifier = parameters.requireString("identifier")
            )

            else -> unknownTypeMessage(queryType)
        }
    }

    fun unavailable(queryType: String, parametersJson: String, message: String): String =
        if (wantsJson(parametersJson)) {
            MagentoInspectionResults.status(normalizeType(queryType), "", null, "unavailable", message)
        } else message

    /** Whether the caller asked for structured results; malformed JSON is reported by [query]. */
    fun wantsJson(parametersJson: String): Boolean = try {
        MagentoInspectionResults.isJson(parseParameters(parametersJson))
    } catch (_: JSONException) {
        false
    }

    private fun jsonQuery(project: Project, queryType: String, parameters: JSONObject, indexKeys: MagentoIndexKeys?): String {
        val filters = jsonFilters[queryType] ?: return MagentoInspectionResults.status(queryType, "", null,
            "invalid_request", "format `json` is not available for queryType `$queryType`. Omit format.")
        val queryParameter = queryParameters.getValue(queryType)
        val query = parameters.optString(queryParameter).trim()
        fun status(status: String, message: String, options: InspectionOptions? = null) =
            MagentoInspectionResults.status(queryType, query, options, status, message)
        val options = try {
            MagentoInspectionResults.options(parameters, filters, queryType in effectiveQueries)
        } catch (exception: InvalidInspectionRequest) {
            return status("invalid_request", exception.message.orEmpty())
        }
        MagentoMcpSupport.validateProject(project, requireSmartMode = false)?.let { return status("unavailable", it, options) }
        if (DumbService.getInstance(project).isDumb) {
            return status("indexing", MagentoMcpSupport.INDEXES_NOT_READY_MESSAGE, options)
        }
        return try {
            val value = parameters.requireString(queryParameter)
            val method = if (queryType == "plugins_for_method") parameters.requireString("methodName") else null
            val result = when (queryType) {
                "module" -> InspectionQueryResult(MagentoModuleQueries.moduleRecords(project, value, indexKeys))
                "di_config" -> InspectionQueryResult(MagentoDiQueries.diConfigRecords(project, value))
                "layout_entities" -> InspectionQueryResult(MagentoViewQueries.layoutRecords(project, value, indexKeys))
                "ui_component" -> InspectionQueryResult(MagentoViewQueries.uiComponentRecords(project, value))
                "acl_or_menu" -> InspectionQueryResult(MagentoViewQueries.aclOrMenuRecords(project, value))
                "plugins_for_method" -> {
                    val provider = MagentoMethodPluginQueryProvider.EP_NAME.extensionList.firstOrNull()
                        ?: return status("unavailable", MagentoMethodPluginQueryProvider.UNAVAILABLE_MESSAGE, options)
                    provider.pluginRecords(project, value, checkNotNull(method), options.effectiveArea)
                }
                else -> MagentoEventQueries.observerRecords(project, value, options.effectiveArea)
            }
            MagentoInspectionResults.respond(project, queryType, query, result, options, method, indexKeys)
        } catch (exception: MissingParameterException) {
            status("invalid_request", "Missing required parameter `${exception.parameterName}` for queryType `$queryType`.", options)
        } catch (exception: InvalidInspectionRequest) {
            status("invalid_request", exception.message.orEmpty(), options)
        } catch (exception: MagentoInspectionResults.StaleCursor) {
            status("stale_cursor", exception.message.orEmpty(), options)
        } catch (_: IndexNotReadyException) {
            status("indexing", MagentoMcpSupport.INDEXES_NOT_READY_MESSAGE, options)
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

    private val effectiveAreaHelp =
        "Optional in text or JSON: area filters by source configuration area, and effectiveArea selects the Magento " +
            "runtime area. Both accept area names such as global, frontend or adminhtml. You can combine them. " +
            "Resolution uses all applicable declarations before any record filters or paging. " +
            "In JSON, mode and message describe resolution coverage, including candidates when module load order is " +
            "unknown. Record status is the decision for that declaration. Records include decidedBy, instance, " +
            "instanceFile, declaredDisabled and sortOrder when applicable. Text is the default."

    private fun typeHelp(queryType: String): String = when (queryType) {
        "module" -> """
            queryType: module
            Finds Magento modules by exact or partial module name.
            Required: moduleName. Use `Vendor_Module` when known, or partial searches such as `Catalog`, `Magento_`, or `Foo`.
            Example parametersJson:
            {"moduleName":"Magento_Catalog"}
        """.trimIndent()

        "module_dependencies" -> """
            queryType: module_dependencies
            Requires XML and JSON PSI. Reads native module indexes and cached PSI snapshots.
            Required: moduleName (exact declaration name). Optional: direction=dependencies|dependents|both (default both), limit=1..100 (JSON integer, default 20).
            Returns JSON: typed sequence_after, composer_require, composer_require_dev edges with source ranges and raw version constraints, reverse references, explicit module-root package mappings, counts, truncation and metadata problems.
            sequence_after declares ordering, not a hard dependency. Composer facts come only from composer.json directly at module roots; parent and multi-module package ownership is not inferred. Unmapped packages remain package edges.
            No Composer solving, installed/enabled-state checks, transitive closure, effective load order, PHP or runtime validation.
            Status is ok, partial, module_not_found, or invalid_request; complete=false marks truncated scans/results or malformed metadata. Host/indexing/parameter errors may be plain text, like other inspection queries.
            Limits: 2000 module files, 256 KiB per metadata file, 20000 edges, 20 returned declarations/problems. Ranges are 1-based line/column and UTF-16 offsets, exclusive end.
            Example parametersJson:
            {"moduleName":"Magento_Catalog","direction":"dependents","limit":20}
        """.trimIndent()

        "di_config" -> """
            queryType: di_config
            Finds dependency injection declarations related to a PHP class, interface, preference target, or virtual type.
            Required: className. Use a PHP FQN such as `Magento\\Catalog\\Model\\Product`, an interface FQN, or a virtual type name.
            Result summarizes matching preferences, types, virtual types, arguments, and plugins.
            Reports XML declarations, without validating PHP classes, inheritance, or effective runtime configuration.
            Example parametersJson:
            {"className":"Magento\\Catalog\\Api\\ProductRepositoryInterface"}
        """.trimIndent()

        "plugins_for_method" -> """
            queryType: plugins_for_method
            Lists declaration matches across the PHP hierarchy, including inherited interfaces, by default. With effectiveArea, resolves configuration in module and PHP inheritance order.
            $effectiveAreaHelp
            A target PHP class that cannot be resolved returns mode=unresolved_target with a diagnostic.
            Required: className, methodName. methodName must be the bare PHP method name, such as `save` or `getById`, without `before`, `around`, or `after` prefixes.
            Example parametersJson:
            {"className":"Magento\\Catalog\\Api\\ProductRepositoryInterface","methodName":"save"}
        """.trimIndent()

        "observers_for_event" -> """
            queryType: observers_for_event
            Lists observer declarations by default. With effectiveArea, merges applicable observer configuration in enabled module order.
            $effectiveAreaHelp
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

        "ui_template" -> """
            queryType: ui_template
            Resolves a literal UI template path to candidate HTML files using the shared editor resolver.
            Required: templatePath. Use a forward-slash alias such as Vendor_Module/widget, optionally prefixed with text! or ending in .html. Maximum length: 4096 characters. Absolute paths, URLs and parent traversal are unsupported.
            Returns normalizedPath, deterministic project-relative file paths, candidate counts and truncation (maximum ${MagentoMcpSupport.MAX_MATCHES} files). Requires ready indexes. Reads indexed scope only; no writes or JavaScript/PHP plugin required.
            Candidates do not establish effective theme fallback, the active area, RequireJS maps, PHP semantics or runtime behavior.
            Example parametersJson:
            {"templatePath":"Magento_Ui/form/element/input"}
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

    private fun unknownTypeMessage(queryType: String, queryTypes: Set<String>? = null): String {
        return "Unknown queryType `$queryType`. Supported values: ${availableTypes().filter { queryTypes == null || it in queryTypes }.joinToString(", ")}. Call magento_inspect with mode `help` for the compact query catalog."
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
