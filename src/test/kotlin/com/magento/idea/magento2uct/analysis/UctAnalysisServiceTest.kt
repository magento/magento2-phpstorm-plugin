/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.analysis

import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.psi.PsiDocumentManager
import com.magento.idea.magento2plugin.PhysicalMagentoTestCase
import com.magento.idea.magento2uct.packages.IssueSeverityLevel
import com.magento.idea.magento2uct.packages.SupportedIssue
import com.magento.idea.magento2uct.settings.UctSettingsService
import com.magento.idea.magento2uct.execution.output.ReportBuilder
import com.magento.idea.magento2uct.execution.output.Summary
import org.json.JSONObject
import com.magento.idea.magento2uct.inspections.UctInspectionManager
import org.junit.Assert.assertThrows
import java.nio.file.Files
import java.nio.file.Path

class UctAnalysisServiceTest : PhysicalMagentoTestCase() {
    override fun setUp() {
        super.setUp()
        myFixture.addFileToProject("vendor/magento/framework/Component/ComponentRegistrar.php", """
            <?php
            namespace Magento\Framework\Component;
            class ComponentRegistrar {
                const MODULE = 'module';
                const THEME = 'theme';
                public static function register(${ '$' }type, ${ '$' }name, ${ '$' }path) {}
            }
        """.trimIndent())
        myFixture.addFileToProject("vendor/magento/test/Types.php", """
            <?php
            namespace Magento\Test;
            class Removed {}
            class Deprecated {}
            class Internal {}
        """.trimIndent())
        myFixture.addFileToProject("app/code/Foo/Bar/registration.php", """
            <?php
            \Magento\Framework\Component\ComponentRegistrar::register(
                \Magento\Framework\Component\ComponentRegistrar::MODULE, 'Foo_Bar', __DIR__
            );
        """.trimIndent())
        myFixture.addFileToProject("app/code/Foo/Bar/Example.php", """
            <?php
            namespace Foo\Bar;
            use Magento\Test\Removed;
            use Magento\Test\Deprecated;
            use Magento\Test\Internal;
            class Example extends Deprecated {
                public function example(Removed ${'$'}value) { return new Internal(); }
            }
        """.trimIndent())
        myFixture.addFileToProject("app/code/Foo/Bar/etc/di.xml", """
            <config>
                <type name="Magento\Test\Removed"/>
                <type name="Magento\Test\Deprecated"/>
            </config>
        """.trimIndent())
        PsiDocumentManager.getInstance(project).commitAllDocuments()
    }

    private fun request(severity: IssueSeverityLevel = IssueSeverityLevel.WARNING, ignore: Boolean = false) = UctAnalysisRequest(
        listOf(myFixture.findFileInTempDir("app/code/Foo/Bar").path),
        "2.4.2", "2.4.3", severity, ignore
    )

    private fun analyze(request: UctAnalysisRequest = request()): UctAnalysisResult =
        UctAnalysisService(project, UctIndexCatalogTest.fixtureCatalog()).analyze(request, Runnable {}, {})

    fun testPhpAndXmlFindingsHaveCodesAndLocations() {
        val result = analyze()
        assertEquals(1, result.modules)
        assertEquals(3, result.processedFiles)
        val issues = result.findings.map { it.issue }.toSet()
        assertTrue(issues.toString(), SupportedIssue.IMPORTED_NON_EXISTENT_CLASS in issues)
        assertTrue(issues.toString(), SupportedIssue.EXTENDING_DEPRECATED_CLASS in issues)
        assertTrue(issues.toString(), SupportedIssue.USED_NON_EXISTENT_TYPE_IN_CONFIG in issues)
        assertTrue(issues.toString(), SupportedIssue.USED_DEPRECATED_TYPE_IN_CONFIG in issues)
        val removedXml = result.findings.single { it.issue == SupportedIssue.USED_NON_EXISTENT_TYPE_IN_CONFIG }
        assertTrue(removedXml.filePath.endsWith("etc/di.xml"))
        assertEquals(2, removedXml.line)
        assertTrue(removedXml.column > 1)
        assertFalse(removedXml.message.startsWith("[1110]"))
        assertEquals(result.findings, analyze().findings)
    }

    fun testSeverityAppliesToPhpAndXmlWithoutChangingSettings() {
        val settings = UctSettingsService.getInstance(project)
        settings.isEnabled = false
        settings.setMinIssueSeverityLevel(IssueSeverityLevel.WARNING.level)
        val before = Files.list(Path.of(project.basePath!!)).use { it.map { path -> path.fileName.toString() }.sorted().toList() }
        val result = analyze(request(IssueSeverityLevel.CRITICAL))
        assertTrue(result.findings.isNotEmpty())
        assertTrue(result.findings.all { it.issue.level == IssueSeverityLevel.CRITICAL })
        assertFalse(settings.isEnabled)
        assertEquals(IssueSeverityLevel.WARNING, settings.minIssueLevel)
        assertNull(settings.configuredCurrentVersion)
        assertNull(settings.configuredTargetVersion)
        assertFalse(UctInspectionManager(project).run(myFixture.findFileInTempDir("app/code/Foo/Bar/Example.php")
            .let { psiManager.findFile(it) })!!.hasResults())
        val after = Files.list(Path.of(project.basePath!!)).use { it.map { path -> path.fileName.toString() }.sorted().toList() }
        assertEquals(before, after)
    }

    fun testIgnoreCurrentVersionSuppressesExistingNonApiUsage() {
        val all = analyze()
        val delta = analyze(request(ignore = true))
        assertTrue(all.findings.any { it.issue.level == IssueSeverityLevel.ERROR })
        assertFalse(delta.findings.any { it.issue.level == IssueSeverityLevel.ERROR })
        assertTrue(delta.findings.any { it.issue.level == IssueSeverityLevel.CRITICAL })
    }

    fun testDirectFileScanAndEmptyDirectoryFailure() {
        val file = myFixture.findFileInTempDir("app/code/Foo/Bar/etc/di.xml")
        val result = analyze(request().copy(paths = listOf(file.path)))
        assertEquals(1, result.processedFiles)
        assertEquals(2, result.findings.size)
        val unrelated = myFixture.addFileToProject("unrelated/readme.txt", "no components").virtualFile.parent
        assertThrows(IllegalArgumentException::class.java) { analyze(request().copy(paths = listOf(unrelated.path))) }
    }

    fun testCancellationStopsBeforeProducingAResult() {
        assertThrows(ProcessCanceledException::class.java) {
            UctAnalysisService(project, UctIndexCatalogTest.fixtureCatalog()).analyze(
                request(), cancelled(), {}
            )
        }
    }

    private fun analyzeWithCatalog(catalog: UctIndexCatalog, request: UctAnalysisRequest) =
        UctAnalysisService(project, catalog).analyze(request, Runnable {}, {})

    private fun cancelled() = Runnable { throw ProcessCanceledException() }

    fun testOverlappingPathsDoNotDuplicateFindings() {
        val single = analyze()
        assertEquals(single, analyze(request().copy(paths = request().paths + request().paths)))
    }

    fun testDirectoryScopeExcludesBundledComponentsAndIncludesThemes() {
        myFixture.addFileToProject("app/code/Magento/Bundled/registration.php", """
            <?php \Magento\Framework\Component\ComponentRegistrar::register('module', 'Magento_Bundled', __DIR__);
        """.trimIndent())
        myFixture.addFileToProject("app/code/Magento/Bundled/etc/di.xml", "<config><type name=\"Magento\\Test\\Removed\"/></config>")
        myFixture.addFileToProject("app/design/frontend/Foo/bar/registration.php", """
            <?php \Magento\Framework\Component\ComponentRegistrar::register('theme', 'frontend/Foo/bar', __DIR__);
        """.trimIndent())
        myFixture.addFileToProject("app/design/frontend/Foo/bar/layout/default.xml", "<page><block class=\"Magento\\Test\\Removed\"/></page>")
        val result = analyze(request().copy(paths = listOf(myFixture.findFileInTempDir("app").path)))
        assertEquals(1, result.modules)
        assertEquals(1, result.themes)
        assertFalse(result.findings.any { it.filePath.contains("Magento/Bundled") })
        assertTrue(result.findings.any { it.filePath.endsWith("layout/default.xml") })
    }

    fun testDetachedFindingsStillProduceTheExistingJsonReport() {
        val result = analyze()
        val report = ReportBuilder(project)
        val summary = Summary.forVersions("2.4.2", "2.4.3")
        summary.processedModules = result.modules
        summary.processedThemes = result.themes
        result.findings.forEach {
            summary.addToSummary(it.issue.level)
            report.addIssue(it.line, it.filePath, it.message, it.issue)
        }
        report.addSummary(summary)
        val file = report.build()
        assertNotNull(file)
        val json = JSONObject(file!!.text)
        assertEquals(result.findings.size, json.getJSONArray("issues").length())
        assertEquals("2.4.2", json.getJSONObject("stats").getString("installedVersion"))
        assertEquals("2.4.3", json.getJSONObject("stats").getString("AdobeCommerceVersion"))
        val first = json.getJSONArray("issues").getJSONObject(0)
        assertEquals(result.findings.first().line, first.getInt("lineNumber"))
        assertEquals(result.findings.first().message, first.getString("message"))
    }
    fun testSuppressionExplainsExistingIssuesWithoutSuppressingNewOnes() {
        val all = analyze()
        val delta = analyze(request(ignore = true).copy(explainSuppressed = true))
        assertTrue(delta.suppressedByRule!!.values.sum() > 0)
        assertTrue(delta.suppressedBySeverity!!.getValue("error") > 0)
        assertTrue(delta.findings.any { it.issue.level == IssueSeverityLevel.CRITICAL })
        assertEquals(all.findings, analyze().findings)
        assertEquals(mapOf("php" to 2, "xml" to 1), delta.fileTypeCounts)
    }

    fun testPolyvariantInheritedTemplateMethodsRemainStableAcrossVersionAndCatalogChanges() {
        myFixture.addFileToProject("vendor/magento/test/Block.php", """
            <?php namespace Magento\Test;
            class Block { public function escapeHtml(${ '$' }value) { return ${ '$' }value; } }
            class Other { public function escapeHtml(${ '$' }value) { return ${ '$' }value; } }
        """.trimIndent())
        myFixture.addFileToProject("app/code/Foo/Bar/Child.php", "<?php namespace Foo\\Bar; class Child extends \\Magento\\Test\\Block {}")
        val template = myFixture.addFileToProject("app/code/Foo/Bar/view/adminhtml/templates/edit.phtml", """
            <?php /** @var \Foo\Bar\Child|\Magento\Test\Other ${ '$' }block */ ?>
            <h1><?= ${ '$' }block->escapeHtml('title') ?></h1>
            <?php /** @var \Magento\Test\Other|\Foo\Bar\Child ${ '$' }other */ ?>
            <h2><?= ${ '$' }other->escapeHtml('subtitle') ?></h2>
        """.trimIndent())
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        val method = "\\Magento\\Test\\Block.escapeHtml"
        val methods = setOf(method, "\\Magento\\Test\\Other.escapeHtml")
        val baseline = UctReleaseFixture.index("2.4.8-p5", methods)
        val target = UctReleaseFixture.index("2.4.9", methods, deprecated = methods)
        val releases = mapOf(baseline.version to baseline, target.version to target)
        val catalog = UctIndexCatalogTest.fixtureCatalog().withReleases(releases)
        val scan = request().copy(paths = listOf(template.virtualFile.path), currentVersion = baseline.version, targetVersion = target.version)
        fun run(c: UctIndexCatalog = catalog, r: UctAnalysisRequest = scan) = analyzeWithCatalog(c, r)
        val before = run()
        assertEquals(before.findings.toString(), 4, before.findings.count { it.issue == SupportedIssue.CALLING_DEPRECATED_METHOD })
        assertTrue(run(r = scan.copy(targetVersion = baseline.version)).findings.none { it.issue == SupportedIssue.CALLING_DEPRECATED_METHOD })
        assertEquals(before.findings, run(r = scan.copy(ignoreCurrentVersion = true)).findings)
        val expanded = catalog.withReleases(releases + ("2.4.8-p4" to UctReleaseFixture.index("2.4.8-p4", setOf("\\Magento\\Test\\Other.escapeHtml"))))
        assertEquals(before.findings, run(expanded).findings)
        assertEquals(before.analysisIdentity, run(expanded).analysisIdentity)
        assertEquals(before.findings, run().findings)
    }

}
