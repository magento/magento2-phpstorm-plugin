/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.testFramework.ExtensionTestUtil
import com.intellij.testFramework.DumbModeTestUtils
import com.magento.idea.magento2plugin.BaseProjectTestCase
import org.json.JSONObject
import org.junit.Test

class MagentoInspectionCommandsTest : BaseProjectTestCase() {
    @Test
    fun testGeneralHelpExplainsInspectionLibraryFlow() {
        val result = MagentoInspectionCommands.help()

        assertContains(result, "Magento inspection library")
        assertContains(result, "`help`")
        assertContains(result, "`detailed_schema`")
        assertContains(result, "`query`")
        assertContains(result, "`module`: Find modules")
        assertContains(result, "`di_config`: Find di.xml")
    }

    @Test
    fun testDetailedSchemaExplainsPluginsForMethodParameters() {
        val result = MagentoInspectionCommands.detailedSchema("plugins-for-method")

        assertContains(result, "queryType: plugins_for_method")
        assertContains(result, "className, methodName")
        assertContains(result, "without `before`, `around`, or `after` prefixes")
    }

    @Test
    fun testQueryReportsMissingRequiredParameter() {
        val result = MagentoInspectionCommands.query(project, "acl_or_menu", "{}")

        assertContains(result, "Missing required parameter `identifier`")
        assertContains(result, "queryType `acl_or_menu`")
        assertContains(result, "mode `detailed_schema`")
    }

    @Test
    fun testQueryFindsAclThroughInspectionTool() {
        val result = MagentoInspectionCommands.query(
            project = project,
            queryType = "acl_or_menu",
            parametersJson = """{"identifier":"Magento_Catalog::catalog"}"""
        )

        assertContains(result, "Magento_Catalog::catalog")
    }

    @Test
    fun testSharedQueriesWorkWithoutPhpInspectionProvider() {
        ExtensionTestUtil.maskExtensions(MagentoMethodPluginQueryProvider.EP_NAME, emptyList(), testRootDisposable)
        val queries = listOf(
            Triple("module", "moduleName" to "Foo_Bar2", "app/code/Foo/Bar2"),
            Triple("di_config", "className" to "Magento\\Theme\\Block\\Html\\Topmenu", "plugin name=catalogTopmenu"),
            Triple("observers_for_event", "eventName" to "test_event_in_test_class", "observer=test_observer"),
            Triple("layout_entities", "name" to "test_index_index_block2", "blocks"),
            Triple("ui_component", "name" to "recently_viewed_2", "rootTag: listing"),
            Triple("acl_or_menu", "identifier" to "Magento_Catalog::catalog", "menu entries")
        )
        val help = MagentoInspectionCommands.help()
        for ((queryType, parameter, expected) in queries) {
            assertContains(help, "- `$queryType`:")
            assertContains(MagentoInspectionCommands.detailedSchema(queryType), "queryType: $queryType")
            val result = MagentoInspectionCommands.query(
                project, queryType, JSONObject().put(parameter.first, parameter.second).toString()
            )
            assertContains(result.replace('\\', '/'), expected.replace('\\', '/'))
        }
        assertFalse(help.contains("- `plugins_for_method`:"))
        assertContains(help, "requires the JetBrains PHP plugin")
    }

    @Test
    fun testUnavailablePhpQueryIsNotReportedAsAnEmptyResult() {
        ExtensionTestUtil.maskExtensions(MagentoMethodPluginQueryProvider.EP_NAME, emptyList(), testRootDisposable)
        assertFalse(MagentoInspectionCommands.requiresSmartMode("plugins_for_method"))
        assertFalse(MagentoInspectionCommands.requiresSmartMode("plugin_method"))
        assertTrue(MagentoInspectionCommands.requiresSmartMode("di_config"))
        assertEquals(MagentoMethodPluginQueryProvider.UNAVAILABLE_MESSAGE,
            MagentoInspectionCommands.detailedSchema("plugin_method"))
        assertEquals(MagentoMethodPluginQueryProvider.UNAVAILABLE_MESSAGE,
            MagentoInspectionCommands.query(project, "plugins", "{}"))
        assertEquals(MagentoMethodPluginQueryProvider.UNAVAILABLE_MESSAGE,
            MagentoDiQueries.findPluginsForMethod(project, "Missing\\Class", "execute"))
        assertContains(MagentoInspectionCommands.query(project, "module", "{invalid"),
            "parametersJson must be a valid JSON object")
    }

    @Test
    fun testPhpQueryIsAdvertisedWhenOptionalProviderIsRegistered() {
        assertTrue(MagentoMethodPluginQueryProvider.isAvailable())
        assertTrue(MagentoInspectionCommands.requiresSmartMode("plugins_for_method"))
        assertContains(MagentoInspectionCommands.help(), "- `plugins_for_method`:")
    }

    @Test
    fun testIndexingDoesNotReturnEmptyInspectionResults() {
        val queries = listOf(
            "module" to "moduleName",
            "di_config" to "className",
            "plugins_for_method" to "className",
            "observers_for_event" to "eventName",
            "layout_entities" to "name",
            "ui_component" to "name",
            "ui_template" to "templatePath",
            "js_config" to "name",
            "acl_or_menu" to "identifier"
        )
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            for ((type, parameter) in queries) {
                assertTrue(MagentoInspectionCommands.requiresSmartMode(type))
                assertEquals(MagentoMcpSupport.INDEXES_NOT_READY_MESSAGE,
                    MagentoInspectionCommands.query(project, type,
                        JSONObject().put(parameter, "missing").put("methodName", "execute").toString()))
            }
            assertEquals(MagentoMcpSupport.INDEXES_NOT_READY_MESSAGE,
                MagentoModuleQueries.findMagentoModule(project, "missing"))
            assertContains(MagentoInspectionCommands.detailedSchema("layout_entities"),
                "queryType: layout_entities")
        }
    }

    @Test
    fun testUnavailablePhpCapabilityRemainsExplicitDuringIndexing() {
        ExtensionTestUtil.maskExtensions(MagentoMethodPluginQueryProvider.EP_NAME, emptyList(), testRootDisposable)
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            assertEquals(MagentoMethodPluginQueryProvider.UNAVAILABLE_MESSAGE,
                MagentoInspectionCommands.query(project, "plugins_for_method", "{}"))
        }
    }

    @Test
    fun testUiTemplateResultBoundsAndInvalidPaths() {
        for (index in 0..20) myFixture.addFileToProject(
            "app/code/Foo/Bar2/view/frontend/web/template/$index/ui-template-bounded-case.html", "<div/>")
        com.intellij.testFramework.IndexingTestUtil.waitUntilIndexesAreReady(project)
        val result = MagentoInspectionCommands.query(project, "ui_template", """{"templatePath":"ui-template-bounded-case"}""")
        assertContains(result, "totalCandidates: 21")
        assertContains(result, "returnedCandidates: 20")
        assertContains(result, "truncated: true")
        val paths = result.lineSequence().filter { it.startsWith("file: ") }.toList()
        assertEquals(20, paths.size)
        assertEquals(paths.sorted(), paths)
        for (path in listOf("../outside", "/absolute", "file:///outside", "''")) {
            assertContains(MagentoInspectionCommands.query(project, "ui_template", JSONObject().put("templatePath", path).toString()),
                "Invalid templatePath")
        }
        assertContains(MagentoInspectionCommands.query(project, "ui_template", JSONObject().put("templatePath", "x".repeat(4097)).toString()),
            "maximum length is 4096")
        assertContains(MagentoInspectionCommands.query(project, "ui_template", "{}"), "Missing required parameter `templatePath`")
        assertContains(MagentoInspectionCommands.query(project, "ui_template", "{invalid"), "parametersJson must be a valid JSON object")
    }

    @Test
    fun testModuleDependencyCapabilityAndCancellation() {
        val available = setOf("module")
        assertFalse(MagentoInspectionCommands.help(available).contains("- `module_dependencies`:"))
        assertContains(MagentoInspectionCommands.query(project, "module_dependencies", "{}", available), "unavailable")
        val indicator = com.intellij.openapi.progress.EmptyProgressIndicator()
        try {
            com.intellij.openapi.progress.ProgressManager.getInstance().runProcess(
                com.intellij.openapi.util.Computable {
                    indicator.cancel()
                    MagentoInspectionCommands.query(project, "module_dependencies", """{"moduleName":"Foo_Bar2"}""")
                }, indicator)
            fail("Cancellation must propagate instead of becoming an empty dependency graph")
        } catch (_: com.intellij.openapi.progress.ProcessCanceledException) { }
    }

    @Test
    fun testUiTemplateCancellationDoesNotBecomeAnEmptyResult() {
        val indicator = com.intellij.openapi.progress.EmptyProgressIndicator()
        try {
            com.intellij.openapi.progress.ProgressManager.getInstance().runProcess(
                com.intellij.openapi.util.Computable {
                    indicator.cancel()
                    MagentoInspectionCommands.query(project, "ui_template", """{"templatePath":"Foo_Bar2/missing"}""")
                }, indicator)
            fail("Cancellation must propagate to the host adapter rather than returning no matches")
        } catch (_: com.intellij.openapi.progress.ProcessCanceledException) { }
    }

    @Test
    fun testHostIndexKeysPreserveQueryResultsAndReadiness() {
        val hostKeys = MagentoIndexKeys { id, _ ->
            when (id) {
                com.magento.idea.magento2plugin.stubs.indexes.xml.ModuleXmlIndex.KEY ->
                    listOf("Foo_Bar2", "Magento_Catalog", "Magento_CatalogGraphQl",
                        "Magento_CatalogRuleConfigurable", "Missing_Module")
                com.magento.idea.magento2plugin.stubs.indexes.BlockNameIndex.KEY ->
                    listOf("test_index_index_block2")
                com.magento.idea.magento2plugin.stubs.indexes.ContainerNameIndex.KEY ->
                    listOf("test_index_index_container2")
                else -> error("Unexpected index: $id")
            }
        }
        for ((type, parameters) in listOf(
            "module" to """{"moduleName":"Foo_Bar2"}""",
            "module" to """{"moduleName":"Magento_Catalog"}""",
            "layout_entities" to """{"name":"test_index_index_block2"}""",
            "layout_entities" to """{"name":"test_index_index_container2"}"""
        )) {
            assertEquals(MagentoInspectionCommands.query(project, type, parameters),
                MagentoInspectionCommands.query(project, type, parameters, indexKeys = hostKeys))
        }
        // A key with no in-scope files must not be reported as a module.
        assertContains(MagentoInspectionCommands.query(project, "module",
            """{"moduleName":"Missing_Module"}""", indexKeys = hostKeys), "No Magento modules matched")
        // No filesystem fallback: the host's indexed scope is authoritative.
        assertContains(MagentoInspectionCommands.query(project, "module",
            """{"moduleName":"Foo_Bar2"}""", indexKeys = MagentoIndexKeys { _, _ -> emptyList() }),
            "No Magento modules matched")
        // Structured replies use the same host keys.
        for ((type, parameters) in listOf(
            "module" to """{"moduleName":"Foo_Bar2","format":"json"}""",
            "layout_entities" to """{"name":"test_index_index_block2","format":"json"}"""
        )) {
            assertEquals(MagentoInspectionCommands.query(project, type, parameters),
                MagentoInspectionCommands.query(project, type, parameters, indexKeys = hostKeys))
            assertEquals(0, JSONObject(MagentoInspectionCommands.query(project, type, parameters,
                indexKeys = MagentoIndexKeys { _, _ -> emptyList() })).getInt("total"))
        }
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            for ((type, parameters) in listOf("module" to """{"moduleName":"Foo_Bar2"}""",
                "layout_entities" to """{"name":"test_index_index_block2"}""")) {
                assertEquals(MagentoMcpSupport.INDEXES_NOT_READY_MESSAGE,
                    MagentoInspectionCommands.query(project, type, parameters,
                        indexKeys = MagentoIndexKeys { _, _ -> error("Index access during indexing") }))
            }
        }
    }

    @Test
    fun testQueriesWithoutStructuredResultsKeepTheirParameters() {
        // module_dependencies has its own limit and replies in JSON without format.
        val dependencies = MagentoInspectionCommands.query(project, "module_dependencies",
            """{"moduleName":"Foo_Bar","direction":"dependents","limit":20}""")
        assertEquals(dependencies, "Foo_Bar", JSONObject(dependencies).getString("moduleName"))
        for ((type, parameters) in listOf(
            "module_dependencies" to """{"moduleName":"Foo_Bar","format":"json"}""",
            "ui_template" to """{"templatePath":"Magento_Ui/form/field","format":"json"}"""
        )) {
            val json = JSONObject(MagentoInspectionCommands.query(project, type, parameters))
            assertEquals(json.toString(), "invalid_request", json.getString("status"))
            assertContains(json.getString("message"), "format `json` is not available for queryType `$type`")
        }
    }

    @Test
    fun testHostCapabilitiesFilterCatalogSchemaAndDispatchBeforeIndexAccess() {
        val supported = setOf("di_config", "observers_for_event", "ui_component", "acl_or_menu")
        val help = MagentoInspectionCommands.help(supported)
        for (type in supported) assertContains(help, "- `$type`:")
        for (type in listOf("module", "layout_entities", "plugins_for_method")) {
            assertFalse(help.contains("- `$type`:"))
            assertContains(MagentoInspectionCommands.detailedSchema(type, supported), "unavailable in this host")
        }
        // Default desktop catalog is unchanged, including its optional PHP provider.
        assertContains(MagentoInspectionCommands.help(), "- `module`:")
        assertContains(MagentoInspectionCommands.detailedSchema("di", supported), "Required: className")
        val unknown = MagentoInspectionCommands.detailedSchema("unknown", supported)
        assertFalse(unknown.contains("plugins_for_method"))
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            for (alias in listOf("modules", "layout", "plugins")) {
                assertContains(MagentoInspectionCommands.query(project, alias, "{invalid", supported), "unavailable in this host")
            }
            val json = JSONObject(MagentoInspectionCommands.query(project, "module",
                "{\"moduleName\":\"Magento_Catalog\",\"format\":\"json\"}", supported))
            assertEquals("unavailable", json.getString("status"))
            assertContains(json.getString("message"), "unavailable in this host")
            assertEquals(MagentoMcpSupport.INDEXES_NOT_READY_MESSAGE,
                MagentoInspectionCommands.query(project, "di", "{\"className\":\"Missing\"}", supported))
        }
    }

    @Test
    fun testJsConfigFindsRequireJsAndUiComponentUsages() {
        myFixture.addFileToProject(
            "app/code/Foo/Bar2/view/frontend/requirejs-config.js",
            """
            var config = {
                map: {
                    '*': {
                        'checkoutStepAlias': 'Foo_Bar2/js/view/checkout-step'
                    }
                },
                paths: {
                    'customLib': 'Foo_Bar2/js/lib/custom-lib'
                },
                config: {
                    mixins: {
                        'Foo_Bar2/js/view/checkout-step': {
                            'Foo_Bar2/js/view/checkout-step-mixin': true
                        }
                    }
                }
            };
            """.trimIndent()
        )
        myFixture.addFileToProject(
            "app/code/Foo/Bar2/view/frontend/web/js/view/checkout-step.js",
            """
            define(['uiComponent'], function (Component) {
                return Component.extend({
                    defaults: {
                        template: 'Foo_Bar2/checkout-step',
                        displayArea: 'checkout-steps'
                    }
                });
            });
            """.trimIndent()
        )
        myFixture.addFileToProject(
            "app/code/Foo/Bar2/view/frontend/web/template/checkout-step.html",
            "<!-- ko foreach: getRegion('checkout-steps') --><!-- /ko -->"
        )
        myFixture.addFileToProject(
            "app/code/Foo/Bar2/view/frontend/layout/checkout_index_index.xml",
            """
            <?xml version="1.0"?>
            <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                <body>
                    <referenceBlock name="checkout.root">
                        <arguments>
                            <argument name="jsLayout" xsi:type="array">
                                <item name="components" xsi:type="array">
                                    <item name="checkout-step" xsi:type="array">
                                        <item name="component" xsi:type="string">Foo_Bar2/js/view/checkout-step</item>
                                        <item name="template" xsi:type="string">Foo_Bar2/checkout-step</item>
                                    </item>
                                </item>
                            </argument>
                        </arguments>
                    </referenceBlock>
                </body>
            </page>
            """.trimIndent()
        )
        com.intellij.testFramework.IndexingTestUtil.waitUntilIndexesAreReady(project)

        val textResult = MagentoInspectionCommands.query(
            project,
            "requirejs",
            """{"name":"checkout-step"}"""
        )
        assertContains(textResult, "mixins")
        assertContains(textResult, "Foo_Bar2/js/view/checkout-step -> mixin=Foo_Bar2/js/view/checkout-step-mixin (enabled)")
        assertContains(textResult, "map")
        assertContains(textResult, "[*] checkoutStepAlias -> Foo_Bar2/js/view/checkout-step")
        assertContains(textResult, "ui components and templates")
        assertContains(textResult, "ui_component_declaration: Foo_Bar2/js/view/checkout-step")
        assertContains(textResult, "ui_template_declaration: Foo_Bar2/checkout-step")
        assertContains(textResult, "knockout_template: Foo_Bar2/checkout-step")
        assertContains(textResult, "display_area: checkout-steps")
        assertContains(textResult, "get_region: checkout-steps")
        assertFalse(textResult.contains("componentJsPath:"))
        assertFalse(textResult.contains("componentName:"))
        assertFalse(textResult.contains("templatePath:"))

        val jsonResult = JSONObject(
            MagentoInspectionCommands.query(
                project,
                "js_config",
                """{"name":"checkout-step","format":"json"}"""
            )
        )
        assertEquals("ok", jsonResult.getString("status"))
        val kinds = mutableSetOf<String>()
        val byKind = mutableMapOf<String, MutableList<JSONObject>>()
        val records = jsonResult.getJSONArray("records")
        for (i in 0 until records.length()) {
            val record = records.getJSONObject(i)
            val kind = record.getString("kind")
            kinds += kind
            byKind.getOrPut(kind) { mutableListOf() }.add(record)
        }
        assertTrue(kinds.toString(), "js_mixin" in kinds)
        assertTrue(kinds.toString(), "requirejs_map" in kinds)
        assertTrue(kinds.toString(), "ui_component_declaration" in kinds)
        assertTrue(kinds.toString(), "ui_template_declaration" in kinds)
        assertTrue(kinds.toString(), "knockout_template" in kinds)
        assertTrue(kinds.toString(), "display_area" in kinds)
        assertTrue(kinds.toString(), "get_region" in kinds)

        val componentRecord = byKind.getValue("ui_component_declaration").first()
        assertEquals("Foo_Bar2/js/view/checkout-step", componentRecord.getString("key"))
        assertFalse(componentRecord.isNull("line"))
        val componentCandidates = componentRecord.getJSONArray("candidates").let { arr ->
            (0 until arr.length()).map { arr.getString(it) }
        }
        assertTrue(componentCandidates.toString(),
            "app/code/Foo/Bar2/view/frontend/web/js/view/checkout-step.js" in componentCandidates)

        val templateRecord = byKind.getValue("ui_template_declaration").first {
            it.getString("path").endsWith("checkout_index_index.xml")
        }
        assertEquals("Foo_Bar2/checkout-step", templateRecord.getString("key"))
        assertFalse(templateRecord.isNull("line"))
        val templateCandidates = templateRecord.getJSONArray("candidates").let { arr ->
            (0 until arr.length()).map { arr.getString(it) }
        }
        assertTrue(templateCandidates.toString(),
            "app/code/Foo/Bar2/view/frontend/web/template/checkout-step.html" in templateCandidates)
    }

    @Test
    fun testJsConfigPropagatesIndexErrorsAndCancellation() {
        val indexNotReadyKeys = MagentoIndexKeys { _, _ ->
            throw com.intellij.openapi.project.IndexNotReadyException.create()
        }
        assertEquals(
            MagentoMcpSupport.INDEXES_NOT_READY_MESSAGE,
            MagentoInspectionCommands.query(
                project,
                "js_config",
                """{"name":"checkout-step"}""",
                indexKeys = indexNotReadyKeys
            )
        )
        val notReadyJson = JSONObject(
            MagentoInspectionCommands.query(
                project,
                "js_config",
                """{"name":"checkout-step","format":"json"}""",
                indexKeys = indexNotReadyKeys
            )
        )
        assertEquals("indexing", notReadyJson.getString("status"))

        val canceledKeys = MagentoIndexKeys { _, _ ->
            throw com.intellij.openapi.progress.ProcessCanceledException()
        }
        try {
            MagentoInspectionCommands.query(
                project,
                "js_config",
                """{"name":"checkout-step"}""",
                indexKeys = canceledKeys
            )
            fail("ProcessCanceledException must propagate from js_config")
        } catch (_: com.intellij.openapi.progress.ProcessCanceledException) { }

        val failingKeys = MagentoIndexKeys { _, _ ->
            throw IllegalStateException("Simulated index corruption")
        }
        try {
            MagentoInspectionCommands.query(
                project,
                "js_config",
                """{"name":"checkout-step","format":"json"}""",
                indexKeys = failingKeys
            )
            fail("Unexpected index failure must propagate instead of returning no_matches")
        } catch (e: IllegalStateException) {
            assertEquals("Simulated index corruption", e.message)
        }
    }

    @Test
    fun testJsConfigRequireJsMatchesBetweenJsPsiAndFallbackModes() {
        myFixture.addFileToProject(
            "app/code/Foo/Bar2/view/frontend/requirejs-config.js",
            """
            var config = {
                map: { '*': { 'checkoutStepAlias': 'Foo_Bar2/js/view/checkout-step' } },
                paths: { 'checkoutStepPath': 'Foo_Bar2/js/view/checkout-step' },
                config: { mixins: { 'Foo_Bar2/js/view/checkout-step': { 'Foo_Bar2/js/view/checkout-step-mixin': true } } }
            };
            """.trimIndent()
        )
        myFixture.addFileToProject(
            "app/code/Foo/Bar2/view/frontend/web/js/view/checkout-step.js",
            "define([], function () { return { defaults: { template: 'Foo_Bar2/checkout-step', displayArea: 'checkout-steps' } }; });"
        )
        myFixture.addFileToProject(
            "app/code/Foo/Bar2/view/frontend/web/template/checkout-step.html",
            "<!-- ko foreach: getRegion('checkout-steps') --><!-- /ko -->"
        )
        com.intellij.testFramework.IndexingTestUtil.waitUntilIndexesAreReady(project)

        val withJsJson = JSONObject(
            MagentoInspectionCommands.query(
                project,
                "js_config",
                """{"name":"checkout-step","format":"json"}"""
            )
        )
        assertEquals("ok", withJsJson.getString("status"))
        val withJsRecords = withJsJson.getJSONArray("records").toString()

        com.intellij.testFramework.ExtensionTestUtil.maskExtensions(
            com.magento.idea.magento2plugin.util.magento.js.JsPsiSupportProvider.EP_NAME,
            emptyList(),
            testRootDisposable
        )
        val fallbackJson = JSONObject(
            MagentoInspectionCommands.query(
                project,
                "js_config",
                """{"name":"checkout-step","format":"json"}"""
            )
        )
        assertEquals("ok", fallbackJson.getString("status"))
        assertEquals(withJsRecords, fallbackJson.getJSONArray("records").toString())
    }

    private fun assertContains(text: String, expected: String) {
        assertTrue("Expected to find <$expected> in:\n$text", text.contains(expected))
    }
}
