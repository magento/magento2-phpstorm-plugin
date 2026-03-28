package com.magento.idea.magento2plugin.mcp

import com.intellij.psi.PsiDirectory
import com.magento.idea.magento2plugin.BaseProjectTestCase
import com.magento.idea.magento2plugin.indexes.ModuleIndex
import com.magento.idea.magento2plugin.project.Settings
import org.junit.Test

class MagentoModuleCommandsTest : BaseProjectTestCase() {
    @Test
    fun testCreateMagentoModuleCreatesModuleFiles() {
        Settings.getInstance(project).magentoPath = getMagentoRootPathFromFixture()

        val result = MagentoModuleCommands.createMagentoModule(project, "Mcp", "Generated")

        assertContains(result, "Created Magento module \"Mcp_Generated\".")
        assertContains(result, "composerPackage: mcp/module-generated")
        assertContainsPath(result, "app/code/Mcp/Generated/composer.json")
        assertContainsPath(result, "app/code/Mcp/Generated/registration.php")
        assertContainsPath(result, "app/code/Mcp/Generated/etc/module.xml")

        assertFileContains(
            "app/code/Mcp/Generated/composer.json",
            "\"name\": \"mcp/module-generated\""
        )
        assertFileContains(
            "app/code/Mcp/Generated/registration.php",
            "'Mcp_Generated'"
        )
        assertFileContains(
            "app/code/Mcp/Generated/etc/module.xml",
            "<module name=\"Mcp_Generated\""
        )
    }

    private fun assertContains(text: String, expected: String) {
        assertTrue("Expected to find <$expected> in:\n$text", text.contains(expected))
    }

    private fun assertContainsPath(text: String, expectedPathSuffix: String) {
        val normalized = text.replace('\\', '/')
        assertTrue("Expected to find path suffix <$expectedPathSuffix> in:\n$text", normalized.contains(expectedPathSuffix))
    }

    private fun assertFileContains(relativePath: String, expected: String) {
        val pathParts = relativePath.split('/')
        var directory = getMagentoRootDirectoryFromFixture()
        for (part in pathParts.dropLast(1)) {
            directory = directory.findSubdirectory(part)
                ?: error("Expected directory to exist in path: $relativePath")
        }
        val psiFile = directory.findFile(pathParts.last())
            ?: error("Expected file to exist: $relativePath")

        assertContains(psiFile.text, expected)
    }

    private fun getMagentoRootPathFromFixture(): String {
        return getMagentoRootDirectoryFromFixture().virtualFile.path
    }

    private fun getMagentoRootDirectoryFromFixture(): PsiDirectory {
        val moduleDirectory = ModuleIndex(project).getModuleDirectoryByModuleName("Foo_Bar")
            ?: error("Fixture module Foo_Bar was not resolved")
        return moduleDirectory.parentDirectory
            ?.parentDirectory
            ?.parentDirectory
            ?.parentDirectory
            ?: error("Fixture Magento root directory was not resolved")
    }
}
