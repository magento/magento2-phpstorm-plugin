package com.magento.idea.magento2plugin.mcp

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.PlatformTestUtil
import com.magento.idea.magento2plugin.BaseProjectTestCase
import org.junit.Test

class MagentoMcpQueriesTest : BaseProjectTestCase() {
    @Test
    fun testGetMagentoRootPathReturnsConfiguredSetting() {
        val result = MagentoProjectQueries.getMagentoRootPath(project)

        assertContains(result, "Configured Magento root path: /src")
    }

    @Test
    fun testFindMagentoModuleReturnsModuleDetails() {
        val result = MagentoModuleQueries.findMagentoModule(project, "Foo_Bar2")

        assertContains(result, "Found 1 Magento module match(es) for \"Foo_Bar2\".")
        assertContains(result, "\nFoo_Bar2\n")
        assertContains(result, "path:")
        assertContainsPath(result, "app/code/Foo/Bar2")
        assertContains(result, "editable: yes")
        assertContains(result, "etc:")
        assertContainsPath(result, "app/code/Foo/Bar2/etc")
    }

    @Test
    fun testFindDiConfigForClassReturnsTypeAndPluginDeclarations() {
        val result = MagentoDiQueries.findDiConfigForClass(project, "Magento\\Theme\\Block\\Html\\Topmenu")

        assertContains(result, "DI configuration for \"Magento\\Theme\\Block\\Html\\Topmenu\"")
        assertContains(result, "type declarations")
        assertContains(
            result,
            "vendor/magento/module-catalog/etc/di.xml -> type name=Magento\\Theme\\Block\\Html\\Topmenu"
        )
        assertContains(result, "plugin declarations")
        assertContains(
            result,
            "plugin name=catalogTopmenu type=Magento\\Catalog\\Plugin\\Block\\Topmenu sortOrder=0 disabled=false"
        )
    }

    @Test
    fun testFindPluginsForMethodReturnsMatchingPluginMethods() {
        myFixture.addFileToProject(
            "vendor/magento/module-theme/Block/PluginClass.php",
            """
            <?php
            
            namespace Magento\Theme\Block;
            
            class PluginClass
            {
                public function someMethod()
                {
                }
            }
            """.trimIndent()
        )
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        val result = MagentoDiQueries.findPluginsForMethod(project, "Magento\\Theme\\Block\\PluginClass", "someMethod")

        assertContains(result, "Found 1 plugin method match(es) for Magento\\Theme\\Block\\PluginClass::someMethod().")
        assertContains(result, "before plugin")
        assertContains(result, "target: Magento\\Theme\\Block\\PluginClass::someMethod()")
        assertContains(result, "pluginClass: Magento\\Catalog\\Plugin\\PluginClass")
        assertContains(result, "pluginMethod: beforeSomeMethod()")
        assertContains(result, "file:")
        assertContainsPath(result, "vendor/magento/module-catalog/Plugin/PluginClass.php")
    }

    @Test
    fun testFindObserversForEventReturnsObserverDeclarations() {
        val result = MagentoEventQueries.findObserversForEvent(project, "test_event_in_test_class")

        assertContains(result, "Found 1 event match(es) for \"test_event_in_test_class\".")
        assertContains(result, "file:")
        assertContains(
            result,
            "observer=test_observer instance=Magento\\Catalog\\Observer\\TestObserver disabled=false"
        )
        assertContainsPath(result, "vendor/magento/module-catalog/etc/events.xml")
    }

    @Test
    fun testFindLayoutEntitiesReturnsHandleBlockAndContainerMatches() {
        val handleResult = MagentoViewQueries.findLayoutEntities(project, "test_index_index2")
        assertContains(handleResult, "layout handles")
        assertContains(handleResult, "test_index_index2 ->")
        assertContainsPath(handleResult, "vendor/magento/module-catalog/view/frontend/layout/test_index_index2.xml")

        val blockResult = MagentoViewQueries.findLayoutEntities(project, "test_index_index_block2")
        assertContains(blockResult, "blocks")
        assertContains(
            blockResult,
            "test_index_index_block2 ->"
        )
        assertContainsPath(blockResult, "vendor/magento/module-catalog/view/frontend/layout/test_index_index.xml")
        assertContains(blockResult, "class=- template=-")

        val containerResult = MagentoViewQueries.findLayoutEntities(project, "test_index_index_container2")
        assertContains(containerResult, "containers")
        assertContains(
            containerResult,
            "test_index_index_container2 ->"
        )
        assertContainsPath(containerResult, "vendor/magento/module-catalog/view/frontend/layout/test_index_index.xml")
        assertContains(containerResult, "htmlTag=- htmlClass=-")
    }

    @Test
    fun testFindUiComponentReturnsMatchingFile() {
        val result = MagentoViewQueries.findUiComponent(project, "recently_viewed_2")

        assertContains(result, "Found 1 UI component match(es) for \"recently_viewed_2\".")
        assertContains(result, "\nrecently_viewed_2\n")
        assertContains(result, "file:")
        assertContainsPath(result, "vendor/magento/module-catalog/view/frontend/ui_component/recently_viewed_2.xml")
        assertContains(result, "rootTag: listing")
    }

    @Test
    fun testFindAclOrMenuReturnsAclMatches() {
        val result = MagentoViewQueries.findAclOrMenu(project, "Magento_Catalog::test")

        assertContains(result, "ACL and menu matches for \"Magento_Catalog::test\"")
        assertContains(result, "acl resources")
        assertContains(result, "Magento_Catalog::test -> title=Test Resource")
        assertContains(result, "file:")
        assertContainsPath(result, "vendor/magento/module-catalog/etc/acl.xml")
    }

    @Test
    fun testFindAclOrMenuReturnsMenuMatches() {
        val result = MagentoViewQueries.findAclOrMenu(project, "Magento_Catalog::catalog")

        assertContains(result, "ACL and menu matches for \"Magento_Catalog::catalog\"")
        assertContains(result, "menu entries")
        assertContains(
            result,
            "Magento_Catalog::catalog -> title=Catalog resource=Magento_Catalog::catalog parent=- action=-"
        )
        assertContains(result, "file:")
        assertContainsPath(result, "vendor/magento/module-catalog/etc/adminhtml/menu.xml")
    }

    private fun assertContains(text: String, expected: String) {
        assertTrue("Expected to find <$expected> in:\n$text", text.contains(expected))
    }

    private fun assertContainsPath(text: String, expectedPathSuffix: String) {
        val normalized = text.replace('\\', '/')
        assertTrue("Expected to find path suffix <$expectedPathSuffix> in:\n$text", normalized.contains(expectedPathSuffix))
    }
}
