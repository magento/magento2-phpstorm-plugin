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
        assertFalse(contributors.any { it.endsWith(".XmlJsCompletionContributor") })
        val ids = IndexManager.getRegisteredIndexIds().map { it.name }
        assertTrue(ids.any { it.endsWith(".module_xml") })
        assertFalse(ids.any { it.endsWith(".requirejs") || it.contains(".js_mix") || it.contains(".ui_component_component_declarations") })
        IndexManager.manualReindex()
        com.intellij.testFramework.IndexingTestUtil.waitUntilIndexesAreReady(project)
    }

    @Test fun testAllSharedQueriesAndDiagnosticsRemainAvailable() {
        Settings.getInstance(project).apply {
            pluginEnabled = true
            magentoPath = myFixture.tempDirFixture.getFile("")!!.path
        }
        IndexManager.manualReindex()
        com.intellij.testFramework.IndexingTestUtil.waitUntilIndexesAreReady(project)
        for ((kind, parameters, expected) in listOf(
            Triple("module", """{"moduleName":"Foo_Bar2"}""", "app/code/Foo/Bar2"),
            Triple("layout_entities", """{"name":"test_index_index_block2"}""", "blocks"),
            Triple("di_config", """{"className":"Magento\\Theme\\Block\\Html\\Topmenu"}""", "catalogTopmenu"),
            Triple("observers_for_event", """{"eventName":"test_event_in_test_class"}""", "test_observer"),
            Triple("ui_component", """{"name":"recently_viewed_2"}""", "recently_viewed_2"),
            Triple("acl_or_menu", """{"identifier":"Magento_Catalog::test"}""", "Test Resource")
        )) {
            val result = MagentoInspectionCommands.query(project, kind, parameters)
            assertTrue("$kind: $result", result.contains(expected))
        }
        assertTrue(MagentoInspectionCommands.query(project, "plugins_for_method", "{}").contains("unavailable"))
        val file = myFixture.addFileToProject("app/code/Foo/Bar2/view/frontend/layout/default.xml",
            "<page><body><block cacheable=\"false\"/></body></page>") as XmlFile
        val diagnostics = MagentoXmlDiagnostics.checkFile(file)
        assertEquals("ok", diagnostics.getString("status"))
        assertEquals("CacheableFalseInDefaultLayoutInspection", diagnostics.getJSONArray("diagnostics").getJSONObject(0).getString("ruleId"))
    }
}
