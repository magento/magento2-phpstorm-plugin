package com.magento.idea.magento2plugin.mcp

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.PlatformTestUtil
import com.magento.idea.magento2plugin.BaseProjectTestCase
import org.junit.Test

class MagentoPluginCommandsTest : BaseProjectTestCase() {
    @Test
    fun testCreateMagentoPluginCreatesPluginClassAndDiXml() {
        val result = MagentoPluginCommands.createMagentoPlugin(
            project = project,
            moduleName = "Foo_Bar",
            targetClassName = "Foo\\Bar\\Service\\SimpleService",
            targetMethodName = "execute",
            pluginType = "before",
            pluginName = "mcp_test_plugin",
            pluginClassFqn = "Foo\\Bar\\Plugin\\McpTestPlugin",
            area = "frontend",
            sortOrder = 10
        )

        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        assertContains(result, "Created Magento plugin \"mcp_test_plugin\".")
        assertContains(result, "target: Foo\\Bar\\Service\\SimpleService::execute()")
        assertContains(result, "pluginType: before")
        assertContains(result, "pluginClass: Foo\\Bar\\Plugin\\McpTestPlugin")
        assertContainsPath(result, "app/code/Foo/Bar/Plugin/McpTestPlugin.php")
        assertContainsPath(result, "app/code/Foo/Bar/etc/frontend/di.xml")

        assertFileContains(
            "app/code/Foo/Bar/Plugin/McpTestPlugin.php",
            "class McpTestPlugin"
        )
        assertFileContains(
            "app/code/Foo/Bar/Plugin/McpTestPlugin.php",
            "function beforeExecute("
        )
        assertFileContains(
            "app/code/Foo/Bar/etc/frontend/di.xml",
            "plugin name=\"mcp_test_plugin\""
        )
        assertFileContains(
            "app/code/Foo/Bar/etc/frontend/di.xml",
            "type=\"Foo\\Bar\\Plugin\\McpTestPlugin\""
        )
        assertFileContains(
            "app/code/Foo/Bar/etc/frontend/di.xml",
            "sortOrder=\"10\""
        )
        assertFileContains(
            "app/code/Foo/Bar/etc/frontend/di.xml",
            "<type name=\"Foo\\Bar\\Service\\SimpleService\">"
        )
    }

    private fun assertContains(text: String, expected: String) {
        assertTrue("Expected to find <$expected> in:\n$text", text.contains(expected))
    }

    private fun assertContainsPath(text: String, expectedPathSuffix: String) {
        val normalized = text.replace('\\', '/')
        assertTrue(
            "Expected to find path suffix <$expectedPathSuffix> in:\n$text",
            normalized.contains(expectedPathSuffix)
        )
    }

    private fun assertFileContains(relativePath: String, expected: String) {
        val virtualFile = myFixture.findFileInTempDir(relativePath)
            ?: error("Expected file to exist: $relativePath")
        val psiFile = myFixture.psiManager.findFile(virtualFile)
            ?: error("Expected PSI file to exist: $relativePath")

        assertContains(psiFile.text, expected)
    }
}
