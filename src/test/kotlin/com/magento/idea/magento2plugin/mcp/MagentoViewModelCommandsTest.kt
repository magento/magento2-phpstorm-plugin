package com.magento.idea.magento2plugin.mcp

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.PlatformTestUtil
import com.magento.idea.magento2plugin.BaseProjectTestCase
import org.junit.Test

class MagentoViewModelCommandsTest : BaseProjectTestCase() {
    @Test
    fun testCreateMagentoViewModelCreatesViewModelClass() {
        val result = MagentoViewModelCommands.createMagentoViewModel(
            project = project,
            moduleName = "Foo_Bar",
            viewModelClassFqn = "Foo\\Bar\\ViewModel\\McpTestViewModel"
        )

        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        assertContains(result, "Created Magento view model class \"Foo\\Bar\\ViewModel\\McpTestViewModel\".")
        assertContainsPath(result, "app/code/Foo/Bar/ViewModel/McpTestViewModel.php")

        assertFileContains(
            "app/code/Foo/Bar/ViewModel/McpTestViewModel.php",
            "class McpTestViewModel implements ArgumentInterface"
        )
        assertFileContains(
            "app/code/Foo/Bar/ViewModel/McpTestViewModel.php",
            "use Magento\\Framework\\View\\Element\\Block\\ArgumentInterface;"
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
