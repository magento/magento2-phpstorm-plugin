/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.analysis

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiBinaryFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.magento.idea.magento2plugin.PhysicalMagentoTestCase
import com.magento.idea.magento2plugin.project.Settings
import com.magento.idea.magento2plugin.stubs.indexes.PluginIndex
import com.magento.idea.magento2uct.packages.IssueSeverityLevel
import com.magento.idea.magento2uct.packages.SupportedIssue
import java.util.Base64

/** Temporary reproduction probe for #1220; uses real PHP PSI and platform indexes. */
class Issue1220ReproductionTest : PhysicalMagentoTestCase() {
    private val imagePath = "app/design/frontend/Omnipro/Dospinos/web/icons/80.png"
    private val leafMethod = "\\Magento\\Issue1220\\Leaf.childOnly"
    private val png = Base64.getDecoder().decode(
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+a9i8AAAAASUVORK5CYII="
    )
    private lateinit var caller: PsiFile

    override fun setUp() {
        super.setUp()
        Settings.getInstance(project).pluginEnabled = true
        myFixture.addFileToProject("vendor/magento/issue1220/Hierarchy.php", """
            <?php namespace Magento\Issue1220;
            class Base {}
            class Level1 extends Base {}
            class Level2 extends Level1 {}
            class Level3 extends Level2 {}
            class Level4 extends Level3 {}
            class Level5 extends Level4 {}
            class Leaf extends Level5 { public function childOnly() {} }
        """.trimIndent())
        caller = myFixture.addFileToProject("app/code/Probe/Issue1220/Caller.php", """
            <?php namespace Probe\Issue1220;
            function callChild(\Magento\Issue1220\Base ${'$'}base, \Magento\Issue1220\Leaf ${'$'}leaf) {
                ${'$'}base->childOnly();
                ${'$'}leaf->childOnly();
            }
        """.trimIndent())
        ready()
    }

    fun testCleanIndexesWithPngAndMissingPluginClass() {
        val image = createImage()
        val di = myFixture.addFileToProject("app/code/Probe/Issue1220/etc/di.xml", """
            <config><type name="Magento\Issue1220\Base">
                <plugin name="missing" type="Probe\Missing\Plugin"/>
                <plugin name="empty" type=""/>
            </type></config>
        """.trimIndent())
        ready()
        assertImage(image)
        val plugins = FileBasedIndex.getInstance().getFileData(PluginIndex.KEY, di.virtualFile, project)
        assertEquals(1, plugins.getValue("Magento\\Issue1220\\Base").size)
        assertTrue(PhpIndex.getInstance(project).getAnyByFQN("\\Probe\\Missing\\Plugin").isEmpty())
        assertResolutionAndInspections()
    }

    fun testIndexedPhpSubclassRenamedAndReplacedWithPng() {
        val transient = myFixture.addFileToProject(
            "app/design/frontend/Omnipro/Dospinos/web/icons/80.php",
            "<?php namespace Magento\\Issue1220; class Transient extends Level5 {}"
        ).virtualFile
        ready()
        assertEquals(7, PhpIndex.getInstance(project).getAllSubclasses("\\Magento\\Issue1220\\Base").size)
        WriteCommandAction.runWriteCommandAction(project) {
            transient.rename(this, "80.png")
            transient.setBinaryContent(png)
        }
        ready()
        assertImage(transient)
        assertTrue(PhpIndex.getInstance(project).getAnyByFQN("\\Magento\\Issue1220\\Transient").isEmpty())
        assertResolutionAndInspections()
    }

    fun testRepeatedDeleteRecreateAndReindexWithThemePng() {
        createImage()
        repeat(5) {
            val transient = myFixture.addFileToProject(
                "app/code/Probe/Issue1220/Transient.php",
                "<?php namespace Magento\\Issue1220; class Transient extends Level5 {}"
            ).virtualFile
            ready()
            assertEquals(7, PhpIndex.getInstance(project).getAllSubclasses("\\Magento\\Issue1220\\Base").size)
            WriteCommandAction.runWriteCommandAction(project) { transient.delete(this) }
            ready()
            assertResolutionAndInspections()
        }
    }

    private fun createImage(): VirtualFile {
        val image = myFixture.tempDirFixture.createFile(imagePath)
        WriteCommandAction.runWriteCommandAction(project) { image.setBinaryContent(png) }
        return image
    }

    private fun assertImage(image: VirtualFile) {
        assertTrue(image.fileType.toString(), image.fileType.isBinary)
        assertTrue(psiManager.findFile(image).toString(), psiManager.findFile(image) is PsiBinaryFile)
    }

    private fun ready() {
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        IndexingTestUtil.waitUntilIndexesAreReady(project)
    }

    private fun assertResolutionAndInspections() {
        assertEquals(6, PhpIndex.getInstance(project).getAllSubclasses("\\Magento\\Issue1220\\Base").size)
        val references = PsiTreeUtil.findChildrenOfType(caller, MethodReference::class.java)
        assertEquals(2, references.size)
        for (reference in references) {
            // Exercise the original 2022 resolve() call as well as today's polyvariant fallback.
            assertEquals(leafMethod, (reference.resolve() as Method).fqn)
            assertTrue(reference.multiResolve(false).any { (it.element as? Method)?.fqn == leafMethod })
        }
        val catalog = UctIndexCatalogTest.fixtureCatalog().withReleases(mapOf(
            "2.4.8-p5" to UctReleaseFixture.index("2.4.8-p5", setOf(leafMethod)),
            "2.4.9" to UctReleaseFixture.index("2.4.9", setOf(leafMethod), deprecated = setOf(leafMethod))
        ))
        val result = UctAnalysisService(project, catalog).analyze(UctAnalysisRequest(
            listOf(caller.virtualFile.path), "2.4.8-p5", "2.4.9", IssueSeverityLevel.WARNING, false
        ), Runnable {}, {})
        assertEquals(1, result.processedFiles)
        val calls = result.findings.filter { it.issue == SupportedIssue.CALLING_DEPRECATED_METHOD }
        // Positive findings prove the inspections ran; an early return cannot pass this probe.
        assertEquals(result.findings.toString(), 2, calls.size)
        assertTrue(calls.all { it.message.contains("Leaf::childOnly") })
    }
}
