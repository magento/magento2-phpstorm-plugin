/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.testFramework.IndexingTestUtil
import com.magento.idea.magento2plugin.indexes.IndexManager
import com.magento.idea.magento2plugin.project.Settings
import com.magento.idea.magento2plugin.util.magento.ui.TemplatePathResolver
import org.junit.Test

/** Runs in the dedicated IDEA sandbox with JavaScript/PHP absent, including from its parent classloader. */
class XmlTemplateReferencesTest : BaseProjectTestCase() {
    private val module = "app/code/Literal/Example"
    private val targetPath = "$module/view/frontend/web/template/widget.html"

    private fun prepare() {
        Settings.getInstance(project).apply {
            pluginEnabled = true
            magentoPath = myFixture.tempDirFixture.getFile("")!!.path
        }
        myFixture.addFileToProject("$module/etc/module.xml", "<config><module name=\"Literal_Example\"/></config>")
        myFixture.addFileToProject(targetPath, "<div>Literal template</div>")
        IndexManager.manualReindex()
        IndexingTestUtil.waitUntilIndexesAreReady(project)
    }

    private fun targets(body: String): Set<String> {
        myFixture.configureByText("literal_form.xml", body)
        val reference = myFixture.file.findReferenceAt(myFixture.caretOffset) ?: return emptySet()
        val resolved = if (reference is PsiPolyVariantReference) reference.multiResolve(false).mapNotNull { it.element }
            else listOfNotNull(reference.resolve())
        return resolved.mapNotNull { it.containingFile?.virtualFile?.path }.toSet()
    }

    private fun assertTarget(body: String, expected: String = targetPath) {
        val paths = targets(body)
        assertTrue("Expected $expected from $body; got $paths", paths.any { it.endsWith(expected) })
    }

    @Test fun testAttributesAndItemsNavigateLiteralTemplatesWithoutOptionalLanguages() {
        prepare()
        for (name in listOf("template", "childTemplate", "elementTmpl")) {
            assertTarget("<form $name=\"Literal_Example/<caret>widget\"/>")
            assertTarget("<form><item name=\"$name\">Literal_Example/<caret>widget</item></form>")
        }
        assertTarget("<form><item name=\"templates\"><item name=\"arbitrarySlot\">Literal_Example/<caret>widget</item></item></form>")
        assertTarget("<form template=\"text!Literal_Example/template/<caret>widget.html\"/>")
    }

    @Test fun testModuleDeclarationsVendorAreasAndThemeCandidatesAreReused() {
        prepare()
        val vendor = "vendor/custom/nonstandard/view/adminhtml/web/templates/admin.html"
        myFixture.addFileToProject("vendor/custom/nonstandard/etc/module.xml", "<config><module name=\"Custom_Literal\"/></config>")
        myFixture.addFileToProject(vendor, "<div>Vendor template</div>")
        val theme = "app/design/frontend/Custom/theme/Literal_Example/web/template/theme-only.html"
        myFixture.addFileToProject(theme, "<div>Theme candidate</div>")
        IndexManager.manualReindex()
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        assertTarget("<form template=\"Custom_Literal/<caret>admin\"/>", vendor)
        assertTarget("<form template=\"Literal_Example/<caret>theme-only\"/>", theme)
        val aliases = TemplatePathResolver.getInstance().getTemplateRequireJsPaths(myFixture.findFileInTempDir(targetPath)
            .let { com.intellij.psi.PsiManager.getInstance(project).findFile(it)!! })
        assertTrue(aliases.contains("Literal_Example/widget"))
        assertTrue(aliases.contains("Literal_Example/template/widget"))
    }

    @Test fun testMissingAndNonTemplateItemsDoNotBecomeTemplateReferences() {
        prepare()
        assertFalse(targets("<form template=\"Literal_Example/<caret>missing\"/>").any { it.endsWith(targetPath) })
        for (name in listOf("component", "displayArea", "unrelated")) {
            myFixture.configureByText("unrelated.xml", "<form><item name=\"$name\">Literal_Example/<caret>widget</item></form>")
            val element = myFixture.file.findElementAt(myFixture.caretOffset)!!
            // Generic file navigation is independent; the template provider must not classify these as template slots.
            val references = com.magento.idea.magento2plugin.reference.provider.UiComponentTemplateItemReferenceProvider()
                .getReferencesByElement(element, com.intellij.util.ProcessingContext())
            assertEquals("Unexpected template classification for $name", 0, references.size)
        }
        Settings.getInstance(project).pluginEnabled = false
        val file = myFixture.configureByText("disabled.xml", "<form template=\"Literal_Example/widget\"/>") as com.intellij.psi.xml.XmlFile
        val value = file.rootTag!!.getAttribute("template")!!.valueElement!!
        assertEquals(0, com.magento.idea.magento2plugin.reference.provider.KnockoutTemplatePathReferenceProvider()
            .getReferencesByElement(value, com.intellij.util.ProcessingContext()).size)
    }

    @Test fun testTemplateRenameRefreshesNavigation() {
        prepare()
        assertTarget("<form childTemplate=\"Literal_Example/<caret>widget\"/>")
        WriteCommandAction.runWriteCommandAction(project) {
            myFixture.findFileInTempDir(targetPath).rename(this, "renamed.html")
        }
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        assertTrue(TemplatePathResolver.getInstance().findTemplateFiles(project, "Literal_Example/widget").isEmpty())
        assertTarget("<form childTemplate=\"Literal_Example/<caret>renamed\"/>", targetPath.replace("widget.html", "renamed.html"))
    }
}
