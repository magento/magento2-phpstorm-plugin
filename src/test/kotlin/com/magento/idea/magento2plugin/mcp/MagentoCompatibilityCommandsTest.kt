/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.DumbModeTestUtils
import com.magento.idea.magento2plugin.PhysicalMagentoTestCase
import com.magento.idea.magento2plugin.project.Settings
import com.magento.idea.magento2uct.analysis.UctAnalysisRequest
import com.magento.idea.magento2uct.analysis.UctAnalysisResult
import com.magento.idea.magento2uct.analysis.UctAnalysisRuns
import com.magento.idea.magento2uct.analysis.UctFinding
import com.magento.idea.magento2uct.packages.IssueSeverityLevel
import com.magento.idea.magento2uct.packages.SupportedIssue
import org.json.JSONObject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Assert.assertThrows

class MagentoCompatibilityCommandsTest : PhysicalMagentoTestCase() {
    private fun call(mode: String, json: String = "{}"): JSONObject =
        JSONObject(MagentoCompatibilityCommands.execute(project, mode, json))

    private fun request() = UctAnalysisRequest(listOf(project.basePath!!), null, "2.4.3", IssueSeverityLevel.WARNING, false)
    private fun runs() = project.getService(UctAnalysisRuns::class.java)

    fun testStatusWorksWithDisabledSupportAndExposesActualCoverage() {
        val status = call("status")
        assertFalse(status.getBoolean("pluginEnabled"))
        assertFalse(status.getBoolean("uctEnabled"))
        assertTrue(status.getJSONArray("supportedVersions").toList().contains("2.4.3"))
        assertEquals("bundled", status.getString("indexSource"))
        assertEquals(0, status.getJSONArray("activeRunIds").length())
    }

    fun testStatusRemainsAvailableWhileIndexingAndAnalysisExplainsWhyItCannotStart() {
        Settings.getInstance(project).pluginEnabled = true
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            assertTrue(call("status").getBoolean("indexing"))
            val result = call("analyze")
            assertFalse(result.getBoolean("complete"))
            assertTrue(result.getJSONObject("error").getString("message").contains("Indexes are not ready"))
        }
    }

    fun testMalformedUnknownAndMissingParametersReturnStructuredErrors() {
        for ((mode, parameters) in listOf(
            "analyze" to "[]", "status" to "{broken", "unknown" to "{}",
            "status" to "{\"unused\":true}", "results" to "{}", "cancel" to "{}",
            "results" to "{\"runId\":\"unknown\",\"limit\":0}",
            "results" to "{\"runId\":\"unknown\",\"offset\":1.5}"
        )) {
            val result = call(mode, parameters)
            assertEquals(result.toString(), "failed", result.getString("state"))
            assertFalse(result.getBoolean("complete"))
            assertTrue(result.getJSONObject("error").getString("message").isNotEmpty())
        }
    }

    fun testAnalyzeRejectsDisabledProjectInvalidScopeAndUnsupportedVersions() {
        assertEquals("failed", call("analyze").getString("state"))
        Settings.getInstance(project).pluginEnabled = true
        myFixture.addFileToProject("Sample.php", "<?php class Sample {}")
        for (parameters in listOf(
            "{}", "{\"path\":\"Sample.php\",\"moduleName\":\"Foo_Bar\"}",
            "{\"path\":\"Sample.php\",\"targetVersion\":\"99.0.0\"}",
            "{\"path\":\"Sample.php\",\"targetVersion\":\"2.4.3\",\"ignoreCurrentVersion\":\"true\"}",
            "{\"path\":\"Sample.php\",\"targetVersion\":\"2.4.3\",\"minimumSeverity\":\"info\"}",
            "{\"path\":\"..\",\"targetVersion\":\"2.4.3\"}"
        )) {
            assertEquals(parameters, "failed", call("analyze", parameters).getString("state"))
        }
        assertTrue(runs().activeRunIds().isEmpty())
    }

    fun testAnalyzeRunsARealFileAndReturnsACompleteResult() {
        Settings.getInstance(project).pluginEnabled = true
        myFixture.addFileToProject("Sample.php", "<?php class Sample {}")
        val started = call("analyze", "{\"path\":\"Sample.php\",\"targetVersion\":\"2.4.3\"}")
        assertTrue(started.toString(), started.has("runId"))
        val runId = started.getString("runId")
        PlatformTestUtil.waitWithEventsDispatching("real analysis completion", { runs().results(runId).state != "running" }, 20)
        val result = call("results", "{\"runId\":\"$runId\"}")
        assertEquals(result.toString(), "completed", result.getString("state"))
        assertTrue(result.getBoolean("complete"))
        assertEquals(1, result.getInt("processedFiles"))
        assertEquals(0, result.getJSONObject("summary").getInt("totalIssues"))
    }

    fun testModuleNameResolvesToTheExactModuleScope() {
        Settings.getInstance(project).pluginEnabled = true
        Settings.getInstance(project).magentoPath = project.basePath
        myFixture.addFileToProject("app/code/Foo/Bar/etc/module.xml", "<config><module name=\"Foo_Bar\"/></config>")
        myFixture.addFileToProject("app/code/Foo/Bar/registration.php", """
            <?php \Magento\Framework\Component\ComponentRegistrar::register('module', 'Foo_Bar', __DIR__);
        """.trimIndent())
        val started = call("analyze", "{\"moduleName\":\"Foo_Bar\",\"targetVersion\":\"2.4.3\"}")
        assertTrue(started.toString(), started.has("runId"))
        assertEquals("app/code/Foo/Bar", started.getJSONArray("paths").getString(0))
        val runId = started.getString("runId")
        PlatformTestUtil.waitWithEventsDispatching("module analysis completion", { runs().results(runId).state != "running" }, 20)
        val result = call("results", "{\"runId\":\"$runId\"}")
        assertEquals(result.toString(), "completed", result.getString("state"))
        assertEquals(1, result.getJSONObject("summary").getInt("modules"))
    }

    fun testResultPagesAndTotalsRemainStableAfterCompletion() {
        val findings = (1..3).map {
            UctFinding("${project.basePath}/Sample.php", it, 1, "Issue $it", SupportedIssue.USED_NON_EXISTENT_TYPE)
        }
        val started = runs().start(request()) { UctAnalysisResult(findings, 1, 1, 0) }
        PlatformTestUtil.waitWithEventsDispatching("analysis completion", { runs().results(started.runId).state != "running" }, 10)
        val first = call("results", "{\"runId\":\"${started.runId}\",\"limit\":2}")
        assertTrue(first.getBoolean("complete"))
        assertEquals(3, first.getJSONObject("summary").getInt("critical"))
        assertEquals(2, first.getJSONArray("findings").length())
        assertEquals("Sample.php", first.getJSONArray("findings").getJSONObject(0).getString("filePath"))
        assertEquals(2, first.getInt("nextOffset"))
        val second = call("results", "{\"runId\":\"${started.runId}\",\"offset\":2,\"limit\":2}")
        assertEquals(3, second.getJSONArray("findings").getJSONObject(0).getInt("line"))
        assertFalse(second.getBoolean("hasMore"))
        assertTrue(second.isNull("nextOffset"))
        assertEquals("completed", call("cancel", "{\"runId\":\"${started.runId}\"}").getString("state"))
    }

    fun testCancellationCannotBecomeLateSuccessAndRunningResultsAreNotClean() {
        val release = CountDownLatch(1)
        val entered = AtomicBoolean(false)
        val returned = AtomicBoolean(false)
        try {
            val started = runs().start(request()) {
                entered.set(true)
                try {
                    release.await(10, TimeUnit.SECONDS)
                    UctAnalysisResult(emptyList(), 1, 1, 0)
                } finally { returned.set(true) }
            }
            PlatformTestUtil.waitWithEventsDispatching("analysis start", { entered.get() }, 10)
            val running = call("results", "{\"runId\":\"${started.runId}\"}")
            assertFalse(running.getBoolean("complete"))
            assertTrue(running.isNull("summary"))
            assertEquals(1, call("status").getJSONArray("activeRunIds").length())
            assertThrows(IllegalArgumentException::class.java) { runs().start(request()) { UctAnalysisResult(emptyList(), 0, 0, 0) } }
            assertEquals("cancelled", call("cancel", "{\"runId\":\"${started.runId}\"}").getString("state"))
            release.countDown()
            PlatformTestUtil.waitWithEventsDispatching("cancelled worker return", { returned.get() }, 10)
            assertNull(runs().results(started.runId).result)
            assertEquals("cancelled", runs().cancel(started.runId).state)
        } finally {
            release.countDown()
        }
    }

    fun testOldRunsExpireAndDifferentRunStoresCannotAccessThem() {
        val other = UctAnalysisRuns(project)
        val ids = arrayListOf<String>()
        try {
            repeat(6) {
                val started = runs().start(request()) { UctAnalysisResult(emptyList(), 0, 0, 0) }
                ids += started.runId
                PlatformTestUtil.waitWithEventsDispatching("analysis completion", { runs().results(started.runId).state != "running" }, 10)
            }
            assertEquals("failed", call("results", "{\"runId\":\"${ids.first()}\"}").getString("state"))
            assertEquals("completed", runs().results(ids.last()).state)
            assertThrows(IllegalArgumentException::class.java) { other.results(ids.last()) }
        } finally {
            other.dispose()
        }
    }

    fun testFailedRunHasAnErrorAndNoSuccessfulSummary() {
        val started = runs().start(request()) { throw IllegalStateException("Missing test index") }
        PlatformTestUtil.waitWithEventsDispatching("analysis failure", { runs().results(started.runId).state != "running" }, 10)
        val result = call("results", "{\"runId\":\"${started.runId}\"}")
        assertEquals("failed", result.getString("state"))
        assertFalse(result.getBoolean("complete"))
        assertTrue(result.isNull("summary"))
        assertEquals("Missing test index", result.getJSONObject("error").getString("message"))
    }
}
