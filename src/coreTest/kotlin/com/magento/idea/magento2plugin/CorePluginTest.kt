/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.extensions.PluginId
import com.intellij.psi.xml.XmlFile
import com.magento.idea.magento2plugin.indexes.IndexManager
import com.magento.idea.magento2plugin.mcp.MagentoInspectionCommands
import com.magento.idea.magento2plugin.mcp.MagentoXmlDiagnostics
import com.magento.idea.magento2plugin.project.Settings
import org.junit.Test

/** Actual plugin classloader and extension registration coverage with JavaScript/PHP disabled. */
class CorePluginTest : BaseProjectTestCase() {
    @Test fun testBaseLoadsAndReindexesWithoutJavaScriptOrPhp() {
        val loaded = PluginManagerCore.loadedPlugins.map { it.pluginId.idString }
        assertTrue(loaded.contains("com.magento.idea.magento2plugin"))
        assertFalse(loaded.contains("JavaScript"))
        assertFalse(loaded.contains("com.jetbrains.php"))
        val loader = PluginManagerCore.getPlugin(PluginId.getId("com.magento.idea.magento2plugin"))!!.pluginClassLoader!!
        try {
            loader.loadClass("com.intellij.lang.javascript.psi.JSFile")
            fail("Main plugin must not be able to load disabled JavaScript classes")
        } catch (_: ClassNotFoundException) { }
        val contributors = CompletionContributor.forLanguage(XMLLanguage.INSTANCE).map { it.javaClass.name }
        assertTrue(contributors.any { it.endsWith(".XmlCoreCompletionContributor") })
        assertTrue(contributors.any { it.endsWith(".XmlJsCompletionContributor") })
        val ids = IndexManager.getRegisteredIndexIds().map { it.name }
        assertTrue(ids.any { it.endsWith(".module_xml") })
        assertTrue(ids.any { it.endsWith(".require_js") })
        assertTrue(ids.any { it.endsWith(".js_mixins") })
        assertTrue(ids.any { it.endsWith(".knockout_templates") })
        assertTrue(ids.any { it.endsWith(".ui_component_component_declarations") })
        assertTrue(ids.any { it.endsWith(".ui_component_template_declarations") })
        assertTrue(ids.any { it.endsWith(".ui_component_display_areas") })
        assertTrue(ids.any { it.endsWith(".ui_component_get_region_usages") })
        assertTrue(ids.any { it.endsWith(".magento_lib") })
        IndexManager.manualReindex()
        com.intellij.testFramework.IndexingTestUtil.waitUntilIndexesAreReady(project)
    }

    @Test fun testModuleDependenciesUseNativeJsonWithoutJavaScriptOrPhp() {
        Settings.getInstance(project).apply {
            pluginEnabled = true
            magentoPath = myFixture.tempDirFixture.getFile("")!!.path
        }
        myFixture.addFileToProject("app/code/Graph/Core/etc/module.xml", "<config><module name=\"Graph_Core\"/></config>")
        myFixture.addFileToProject("app/code/Graph/Core/composer.json", """{"name":"graph/core","require":{"php":">=8.2"}}""")
        myFixture.addFileToProject("app/code/Graph/Feature/etc/module.xml", "<config><module name=\"Graph_Feature\"><sequence><module name=\"Graph_Core\"/></sequence></module></config>")
        com.intellij.testFramework.IndexingTestUtil.waitUntilIndexesAreReady(project)
        val result = org.json.JSONObject(MagentoInspectionCommands.query(project, "module_dependencies", """{"moduleName":"Graph_Core"}"""))
        assertEquals(result.toString(), "ok", result.getString("status"))
        assertEquals(2, result.getInt("totalEdges"))
        assertTrue(result.getJSONArray("edges").toString().contains("composer_require"))
    }

    @Test fun testAllSharedQueriesAndDiagnosticsRemainAvailable() {
        Settings.getInstance(project).apply {
            pluginEnabled = true
            magentoPath = myFixture.tempDirFixture.getFile("")!!.path
        }
        myFixture.addFileToProject("app/code/Foo/Bar2/view/frontend/web/template/core-widget.html", "<div>Core template</div>")
        myFixture.addFileToProject(
            "app/code/Foo/Bar2/view/frontend/requirejs-config.js",
            """
            var config = {
                map: {
                    '*': {
                        'coreWidgetAlias': 'Foo_Bar2/js/core-widget'
                    }
                },
                paths: {
                    'coreLibAlias': 'Foo_Bar2/js/lib/core-lib'
                },
                config: {
                    mixins: {
                        'Foo_Bar2/js/core-widget': {
                            'Foo_Bar2/js/core-widget-mixin': true
                        }
                    }
                }
            };
            """.trimIndent()
        )
        myFixture.addFileToProject(
            "app/code/Foo/Bar2/view/frontend/web/js/core-widget.js",
            """
            define(['uiComponent'], function (Component) {
                return Component.extend({
                    defaults: {
                        template: 'Foo_Bar2/core-widget',
                        displayArea: 'core-region'
                    }
                });
            });
            """.trimIndent()
        )
        myFixture.addFileToProject(
            "app/code/Foo/Bar2/view/frontend/web/js/core-widget-mixin.js",
            "define([], function () { return function (target) { return target; }; });"
        )
        IndexManager.manualReindex()
        com.intellij.testFramework.IndexingTestUtil.waitUntilIndexesAreReady(project)
        for ((kind, parameters, expected) in listOf(
            Triple("module", """{"moduleName":"Foo_Bar2"}""", "app/code/Foo/Bar2"),
            Triple("layout_entities", """{"name":"test_index_index_block2"}""", "blocks"),
            Triple("di_config", """{"className":"Magento\\Theme\\Block\\Html\\Topmenu"}""", "catalogTopmenu"),
            Triple("observers_for_event", """{"eventName":"test_event_in_test_class"}""", "test_observer"),
            Triple("ui_template", """{"templatePath":"Foo_Bar2/core-widget"}""", "app/code/Foo/Bar2/view/frontend/web/template/core-widget.html"),
            Triple("ui_component", """{"name":"recently_viewed_2"}""", "recently_viewed_2"),
            Triple("js_config", """{"name":"core-widget"}""", "Foo_Bar2/js/core-widget-mixin"),
            Triple("js_config", """{"name":"coreWidgetAlias"}""", "Foo_Bar2/js/core-widget"),
            Triple("js_config", """{"name":"core-region"}""", "display_area: core-region"),
            Triple("acl_or_menu", """{"identifier":"Magento_Catalog::test"}""", "Test Resource")
        )) {
            val result = MagentoInspectionCommands.query(project, kind, parameters)
            assertTrue("$kind: $result", result.contains(expected))
        }
        val jsJson = org.json.JSONObject(
            MagentoInspectionCommands.query(project, "js_config", """{"name":"core-widget","format":"json"}""")
        )
        assertEquals(jsJson.toString(), "ok", jsJson.getString("status"))
        assertTrue(jsJson.toString(), jsJson.getInt("total") >= 2)
        assertTrue(MagentoInspectionCommands.query(project, "plugins_for_method", "{}").contains("unavailable"))
        val file = myFixture.addFileToProject("app/code/Foo/Bar2/view/frontend/layout/default.xml",
            "<page><body><block cacheable=\"false\"/></body></page>") as XmlFile
        val diagnostics = MagentoXmlDiagnostics.checkFile(file)
        assertEquals("ok", diagnostics.getString("status"))
        assertEquals("CacheableFalseInDefaultLayoutInspection", diagnostics.getJSONArray("diagnostics").getJSONObject(0).getString("ruleId"))
    }
}
