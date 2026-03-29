package com.magento.idea.magento2plugin.mcp

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.PlatformTestUtil
import com.magento.idea.magento2plugin.BaseProjectTestCase
import org.junit.Test

class MagentoCliCommandCommandsTest : BaseProjectTestCase() {
    @Test
    fun testCreateMagentoCliCommandCreatesClassAndDiXmlRegistration() {
        val result = MagentoCliCommandCommands.createMagentoCliCommand(
            project = project,
            moduleName = "Foo_Bar",
            commandClassFqn = "Foo\\Bar\\Console\\Command\\McpTestCommand",
            commandName = "foo:bar:mcp-test",
            commandDescription = "Run the MCP test command"
        )

        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        assertContains(result, "Created Magento CLI command \"foo:bar:mcp-test\".")
        assertContains(result, "commandClass: Foo\\Bar\\Console\\Command\\McpTestCommand")
        assertContains(result, "diXmlItemName: foo_bar_mcp_test_command")
        assertContainsPath(result, "app/code/Foo/Bar/Console/Command/McpTestCommand.php")
        assertContainsPath(result, "app/code/Foo/Bar/etc/di.xml")

        assertFileContains(
            "app/code/Foo/Bar/Console/Command/McpTestCommand.php",
            "class McpTestCommand extends Command"
        )
        assertFileContains(
            "app/code/Foo/Bar/Console/Command/McpTestCommand.php",
            "\$this->setName('foo:bar:mcp-test');"
        )
        assertFileContains(
            "app/code/Foo/Bar/Console/Command/McpTestCommand.php",
            "\$this->setDescription('Run the MCP test command');"
        )
        assertFileContains(
            "app/code/Foo/Bar/etc/di.xml",
            "<item name=\"foo_bar_mcp_test_command\" xsi:type=\"object\">Foo\\Bar\\Console\\Command\\McpTestCommand</item>"
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
