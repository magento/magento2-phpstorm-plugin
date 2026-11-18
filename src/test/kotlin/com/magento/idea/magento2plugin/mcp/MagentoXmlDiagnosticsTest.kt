/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.psi.xml.XmlFile
import com.intellij.testFramework.DumbModeTestUtils
import com.magento.idea.magento2plugin.BaseProjectTestCase
import com.magento.idea.magento2plugin.project.Settings
import org.junit.Test

class MagentoXmlDiagnosticsTest : BaseProjectTestCase() {
    private val cacheRule = "CacheableFalseInDefaultLayoutInspection"
    private val moduleRule = "ModuleDeclarationInModuleXmlInspection"

    private fun xml(path: String, text: String) = myFixture.addFileToProject(path, text) as XmlFile

    @Test fun testExistingInspectionsProduceStableRulesMessagesAndExactRangesWithoutFixes() {
        val text = "<page>\n<body><referenceBlock name=\"test\" cacheable=\"false\"/></body></page>"
        val file = xml("app/code/Diagnostics/Test/view/frontend/layout/default.xml", text)
        val result = MagentoXmlDiagnostics.checkFile(file)
        assertEquals("ok", result.getString("status"))
        assertTrue(result.getBoolean("complete"))
        val finding = result.getJSONArray("diagnostics").getJSONObject(0)
        assertEquals(cacheRule, finding.getString("ruleId"))
        assertEquals("warning", finding.getString("severity"))
        assertTrue(finding.getString("message").contains("disable cache site-wide"))
        val range = finding.getJSONObject("range")
        val start = range.getJSONObject("start")
        assertEquals(2, start.getInt("line"))
        assertEquals("cacheable=\"false\"", text.substring(start.getInt("offset"), range.getJSONObject("end").getInt("offset")))
        assertEquals(text, file.text)
    }

    @Test fun testModuleDeclarationRetainsEditableAndSequenceBoundaries() {
        Settings.getInstance(project).magentoPath = myFixture.tempDirFixture.getFile("")!!.path
        val text = "<config><module name=\"Wrong_Name\"><sequence><module name=\"Other_Dependency\"/></sequence></module></config>"
        val file = xml("app/code/Diagnostics/Test/etc/module.xml", text)
        val findings = MagentoXmlDiagnostics.checkFile(file).getJSONArray("diagnostics")
        assertEquals(1, findings.length())
        assertEquals(moduleRule, findings.getJSONObject(0).getString("ruleId"))
        assertEquals("error", findings.getJSONObject(0).getString("severity"))
        assertTrue(findings.getJSONObject(0).getString("message").contains("Diagnostics_Test"))
        val vendor = xml("vendor/diagnostics/test/etc/module.xml", text)
        assertEquals(0, MagentoXmlDiagnostics.checkFile(vendor).getJSONArray("diagnostics").length())
        val valid = xml("app/code/Diagnostics/Valid/etc/module.xml", "<config><module name=\"Diagnostics_Valid\"/></config>")
        assertEquals(0, MagentoXmlDiagnostics.checkFile(valid).getJSONArray("diagnostics").length())
    }

    @Test fun testValidAndInapplicableFilesAndExplicitRuleSelection() {
        val valid = xml("app/code/Diagnostics/Test/view/frontend/layout/default.xml", "<page><body><block cacheable=\"true\"/></body></page>")
        assertEquals(0, MagentoXmlDiagnostics.checkFile(valid).getJSONArray("diagnostics").length())
        val other = xml("app/code/Diagnostics/Test/view/frontend/layout/other.xml", "<page><body><block cacheable=\"false\"/></body></page>")
        val skipped = MagentoXmlDiagnostics.checkFile(other)
        assertEquals(0, skipped.getJSONArray("ruleIds").length())
        assertEquals(2, skipped.getJSONArray("skippedRules").length())
        val selected = MagentoXmlDiagnostics.checkFile(valid, listOf(moduleRule))
        assertEquals(0, selected.getJSONArray("ruleIds").length())
        assertEquals(1, selected.getJSONArray("skippedRules").length())
    }

    @Test fun testSuppressionUsesThePublishedDesktopRuleId() {
        val file = xml("app/code/Diagnostics/Test/view/frontend/layout/default.xml",
            "<page><body><!--suppress $cacheRule --><referenceBlock name=\"test\" cacheable=\"false\"/></body></page>")
        assertEquals(0, MagentoXmlDiagnostics.checkFile(file).getJSONArray("diagnostics").length())
    }

    @Test fun testMalformedXmlAndBoundsAreIncompleteRatherThanClean() {
        val invalid = xml("app/code/Diagnostics/Test/view/frontend/layout/default.xml", "<page><body></page>")
        val malformed = MagentoXmlDiagnostics.checkFile(invalid)
        assertEquals("invalid_xml", malformed.getString("status"))
        assertFalse(malformed.getBoolean("complete"))
        val many = xml("app/code/Diagnostics/Many/view/frontend/layout/default.xml",
            "<page><body>" + "<block cacheable=\"false\"/>".repeat(3) + "</body></page>")
        val limited = MagentoXmlDiagnostics.checkFile(many, limit = 1)
        assertEquals("partial", limited.getString("status"))
        assertFalse(limited.getBoolean("complete"))
        assertEquals(3, limited.getInt("collectedFindings"))
        assertEquals(1, limited.getJSONArray("diagnostics").length())
        assertEquals("invalid_request", MagentoXmlDiagnostics.checkFile(many, limit = 0).getString("status"))
        assertEquals("invalid_request", MagentoXmlDiagnostics.checkFile(many, listOf("PhpClassExistence")).getString("status"))
    }

    @Test fun testResourceLimitsNeverProduceACompleteEmptyResult() {
        val oversized = xml("app/code/Diagnostics/Huge/view/frontend/layout/default.xml",
            "<page>" + " ".repeat(1_048_576) + "</page>")
        assertEquals("file_too_large", MagentoXmlDiagnostics.checkFile(oversized).getString("status"))
        val elements = xml("app/code/Diagnostics/Elements/view/frontend/layout/default.xml",
            "<page>" + "<item/>".repeat(50_001) + "</page>")
        val bounded = MagentoXmlDiagnostics.checkFile(elements)
        assertEquals("partial", bounded.getString("status"))
        assertFalse(bounded.getBoolean("complete"))
        val findings = xml("app/code/Diagnostics/Findings/view/frontend/layout/default.xml",
            "<page>" + "<block cacheable=\"false\"/>".repeat(1_002) + "</page>")
        val limited = MagentoXmlDiagnostics.checkFile(findings, limit = 100)
        assertEquals("partial", limited.getString("status"))
        assertEquals(1_000, limited.getInt("collectedFindings"))
        assertEquals(100, limited.getJSONArray("diagnostics").length())
    }

    @Test fun testIndexingAndCancellationAreExplicitIncompleteStates() {
        val file = xml("app/code/Diagnostics/Test/view/frontend/layout/default.xml", "<page/>")
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            assertEquals("indexing", MagentoXmlDiagnostics.checkFile(file).getString("status"))
        }
        val cancelled = MagentoXmlDiagnostics.checkFile(file) { throw ProcessCanceledException() }
        assertEquals("cancelled", cancelled.getString("status"))
        assertFalse(cancelled.getBoolean("complete"))
    }
}
