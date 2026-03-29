package com.magento.idea.magento2plugin.mcp

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.PlatformTestUtil
import com.magento.idea.magento2plugin.BaseProjectTestCase
import org.junit.Test

class MagentoControllerCommandsTest : BaseProjectTestCase() {
    @Test
    fun testCreateMagentoControllerCreatesAdminhtmlControllerClass() {
        val result = MagentoControllerCommands.createMagentoController(
            project = project,
            moduleName = "Foo_Bar",
            controllerClassFqn = "Foo\\Bar\\Controller\\Adminhtml\\McpTest\\Index",
            httpMethod = "GET",
            inheritClass = true,
            aclResource = "Foo_Bar::management"
        )

        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        assertContains(result, "Created Magento controller class \"Foo\\Bar\\Controller\\Adminhtml\\McpTest\\Index\".")
        assertContains(result, "area: adminhtml")
        assertContains(result, "httpMethod: GET")
        assertContains(result, "aclResource: Foo_Bar::management")
        assertContainsPath(result, "app/code/Foo/Bar/Controller/Adminhtml/McpTest/Index.php")

        assertFileContains(
            "app/code/Foo/Bar/Controller/Adminhtml/McpTest/Index.php",
            "class Index extends Action implements HttpGetActionInterface"
        )
        assertFileContains(
            "app/code/Foo/Bar/Controller/Adminhtml/McpTest/Index.php",
            "public const ADMIN_RESOURCE = 'Foo_Bar::management';"
        )
        assertFileContains(
            "app/code/Foo/Bar/Controller/Adminhtml/McpTest/Index.php",
            "use Magento\\Backend\\App\\Action;"
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
