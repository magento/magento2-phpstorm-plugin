package com.magento.idea.magento2plugin.mcp

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.PlatformTestUtil
import com.magento.idea.magento2plugin.BaseProjectTestCase
import org.junit.Test

class MagentoObserverCommandsTest : BaseProjectTestCase() {
    @Test
    fun testCreateMagentoObserverCreatesObserverClassAndEventsXml() {
        val result = MagentoObserverCommands.createMagentoObserver(
            project = project,
            moduleName = "Foo_Bar",
            eventName = "mcp_test_event",
            observerName = "mcp_test_observer",
            observerClassFqn = "Foo\\Bar\\Observer\\McpTestObserver",
            area = "frontend"
        )

        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        assertContains(result, "Created Magento observer \"mcp_test_observer\".")
        assertContains(result, "event: mcp_test_event")
        assertContains(result, "observerClass: Foo\\Bar\\Observer\\McpTestObserver")
        assertContainsPath(result, "app/code/Foo/Bar/Observer/McpTestObserver.php")
        assertContainsPath(result, "app/code/Foo/Bar/etc/frontend/events.xml")

        assertFileContains(
            "app/code/Foo/Bar/Observer/McpTestObserver.php",
            "class McpTestObserver implements ObserverInterface"
        )
        assertFileContains(
            "app/code/Foo/Bar/Observer/McpTestObserver.php",
            "public function execute(Observer \$observer)"
        )
        assertFileContains(
            "app/code/Foo/Bar/etc/frontend/events.xml",
            "<event name=\"mcp_test_event\">"
        )
        assertFileContains(
            "app/code/Foo/Bar/etc/frontend/events.xml",
            "observer name=\"mcp_test_observer\""
        )
        assertFileContains(
            "app/code/Foo/Bar/etc/frontend/events.xml",
            "instance=\"Foo\\Bar\\Observer\\McpTestObserver\""
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
