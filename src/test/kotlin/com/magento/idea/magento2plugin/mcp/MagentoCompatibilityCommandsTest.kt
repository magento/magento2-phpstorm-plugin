/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.replaceService
import com.intellij.openapi.util.Disposer
import com.magento.idea.magento2uct.analysis.UctReleasePreparation
import com.magento.idea.magento2uct.analysis.UctReleaseSource
import com.magento.idea.magento2uct.analysis.UctReleaseFixture
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicInteger
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.testFramework.DumbModeTestUtils
import com.intellij.mcpserver.McpTool
import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.ClientInfo
import com.intellij.mcpserver.McpCallInfo
import com.intellij.mcpserver.McpCallAdditionalDataElement
import com.intellij.mcpserver.McpToolCallResultContent
import com.intellij.mcpserver.McpToolCallResult
import com.intellij.mcpserver.McpToolFilter
import com.intellij.mcpserver.impl.McpServerService
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import com.magento.idea.magento2plugin.PhysicalMagentoTestCase
import com.magento.idea.magento2plugin.project.Settings
import com.magento.idea.magento2plugin.stubs.indexes.xml.ModuleXmlIndex
import com.magento.idea.magento2uct.analysis.UctAnalysisRequest
import com.magento.idea.magento2uct.analysis.UctAnalysisResult
import com.magento.idea.magento2uct.analysis.UctAnalysisRuns
import com.magento.idea.magento2uct.analysis.UctFinding
import com.magento.idea.magento2uct.packages.IssueSeverityLevel
import com.magento.idea.magento2uct.packages.SupportedIssue
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Assert.assertThrows

class MagentoCompatibilityCommandsTest : PhysicalMagentoTestCase() {
    override fun setUp() {
        super.setUp()
        // Every case owns its cache; invalid/cancelled preparation never touches an installed IDE cache.
        val preparation = UctReleasePreparation(project, Files.createTempDirectory("compatibility-test-releases-"), UctReleaseSource())
        val analyses = UctAnalysisRuns(project, Files.createTempDirectory("compatibility-test-reports-"))
        Disposer.register(testRootDisposable, preparation)
        Disposer.register(testRootDisposable, analyses)
        project.getService(UctReleasePreparation::class.java)
        project.getService(UctAnalysisRuns::class.java)
        project.replaceService(UctReleasePreparation::class.java, preparation, testRootDisposable)
        project.replaceService(UctAnalysisRuns::class.java, analyses, testRootDisposable)
    }

    private fun call(mode: String, json: String = "{}"): JSONObject =
        JSONObject(MagentoCompatibilityCommands.execute(project, mode, JSONObject(json)))

    private fun request() = UctAnalysisRequest(listOf(project.basePath!!), null, "2.4.3", IssueSeverityLevel.WARNING, false)
    private fun runs() = project.getService(UctAnalysisRuns::class.java)

    private fun nativeTool(): McpTool = MagentoCompatibilityToolsProvider().getTools().single()

    private fun nativeResult(arguments: String = "{}"): McpToolCallResult {
        val tool = nativeTool()
        val json = Json.parseToJsonElement(arguments).jsonObject
        val info = McpCallInfo(1, ClientInfo("magento-tests", "1"), project, tool.descriptor,
            json, JsonObject(emptyMap()), McpServerService.McpSessionOptions(
                McpServerService.AskCommandExecutionMode.DONT_ASK, McpToolFilter.AllowAll))
        return runBlocking(McpCallAdditionalDataElement(info)) { tool.call(json) }
    }

    private fun transportResult(result: McpToolCallResult): CallToolResult {
        val adapter = Class.forName("com.intellij.mcpserver.impl.McpSessionHandlerKt")
            .getDeclaredMethod("toSdkToolCallResult", McpToolCallResult::class.java)
        adapter.isAccessible = true
        return adapter.invoke(null, result) as CallToolResult
    }

    private fun nativeCall(arguments: String = "{}", expectedError: Boolean = false): JSONObject {
        val result = nativeResult(arguments)
        val text = result.content.filterIsInstance<McpToolCallResultContent.Text>().single().text
        assertEquals(text, expectedError, result.isError)
        assertNull(result.structuredContent)
        // Exercise the real transport: successes, errors and documentation all use JSON text.
        val wire = transportResult(result)
        val wireText = wire.content.filterIsInstance<TextContent>().single().text
        assertEquals(text, wireText)
        assertEquals(expectedError, wire.isError)
        assertNull(wire.structuredContent)
        val envelope = JSONObject(wireText)
        for (key in listOf("schemaVersion", "operation", "state", "terminal", "successful", "complete", "pollRequired", "error", "nextCall", "nextStep", "retryAfterMs")) assertTrue(key, envelope.has(key))
        return JSONObject(wireText)
    }

    private fun followNative(next: JSONObject): JSONObject {
        val arguments = JSONObject(next.getJSONObject("arguments").toString())
        assertEquals(project.basePath, arguments.getString("projectPath"))
        assertEquals("magento_compatibility", next.getString("tool"))
        return nativeCall(arguments.toString())
    }

    fun testNativeMcpPreparesBaselineAndTargetThenAnalyzesPhpAndXmlUsingPreparedData() {
        Settings.getInstance(project).pluginEnabled = true
        myFixture.addFileToProject("composer.lock", """{"packages":[{"name":"magento/magento2-base","version":"2.4.8-p5"}]}""")
        myFixture.addFileToProject("vendor/magento/framework/Removed.php", "<?php namespace Magento\\Framework; class Removed {}")
        myFixture.addFileToProject("app/code/Foo/Bar/registration.php", """
            <?php \Magento\Framework\Component\ComponentRegistrar::register('module', 'Foo_Bar', __DIR__);
        """.trimIndent())
        myFixture.addFileToProject("app/code/Foo/Bar/Example.php", """
            <?php namespace Foo\Bar; use Magento\Framework\Removed; class Example extends Removed {}
        """.trimIndent())
        myFixture.addFileToProject("app/code/Foo/Bar/etc/di.xml", """<config><type name="Magento\Framework\Removed"/></config>""")
        val before = Files.walk(java.nio.file.Path.of(project.basePath!!)).use { it.sorted().toList() }
        val downloads = AtomicInteger()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            downloads.incrementAndGet()
            val version = exchange.requestURI.path.removePrefix("/")
            val extra = if (version == "2.4.8-p5") mapOf(
                "lib/internal/Magento/Framework/Removed.php" to "<?php namespace Magento\\Framework; class Removed {}"
            ) else emptyMap()
            val archive = UctReleaseFixture.archive(version, extra)
            exchange.sendResponseHeaders(200, archive.size.toLong())
            exchange.responseBody.use { it.write(archive) }
        }
        server.start()
        val cache = Files.createTempDirectory("mcp-release-cache-")
        val preparation = UctReleasePreparation(project, cache,
            UctReleaseSource { "http://127.0.0.1:${server.address.port}/$it" })
        Disposer.register(testRootDisposable, preparation)
        project.getService(UctReleasePreparation::class.java) // Verify the real service constructor is usable.
        project.replaceService(UctReleasePreparation::class.java, preparation, testRootDisposable)
        try {
            val original = """{"mode":"status","targetVersion":"2.4.9","currentVersion":"2.4.8-p5","ignoreCurrentVersion":true}"""
            val initial = nativeCall(original)
            assertEquals(listOf("2.4.8-p5", "2.4.9"), initial.getJSONObject("readiness").getJSONArray("missingVersions").toList())
            assertFalse(initial.getJSONObject("readiness").getBoolean("ready"))
            var status = initial
            for (version in listOf("2.4.8-p5", "2.4.9")) {
                val started = followNative(status.getJSONObject("nextCall"))
                assertEquals("prepare", started.getString("operation"))
                assertEquals(version, started.getString("targetVersion"))
                val id = started.getString("runId")
                PlatformTestUtil.waitWithEventsDispatching("native MCP release preparation", { preparation.results(id).state != "running" }, 20)
                val result = followNative(started.getJSONObject("nextCall"))
                assertEquals(result.toString(), "completed", result.getString("state"))
                assertTrue(result.getBoolean("releaseDataReady"))
                assertEquals("call", result.getString("nextStep"))
                assertTrue(result.getBoolean("terminal"))
                assertFalse(result.getBoolean("pollRequired"))
                assertTrue(result.getJSONObject("summary").getInt("existenceSymbols") > 0)
                val continuation = result.getJSONObject("nextCall").getJSONObject("arguments")
                assertEquals("2.4.9", continuation.getString("targetVersion"))
                assertEquals("2.4.8-p5", continuation.getString("currentVersion"))
                assertTrue(continuation.getBoolean("ignoreCurrentVersion"))
                status = followNative(result.getJSONObject("nextCall"))
            }
            val ready = status
            assertTrue(ready.toString(), ready.getJSONObject("readiness").getBoolean("ready"))
            assertEquals("bundled_and_prepared", ready.getString("indexSource"))
            val cached = nativeCall("""{"mode":"prepare","targetVersion":"2.4.9"}""")
            assertTrue(cached.isNull("runId"))
            assertTrue(cached.getBoolean("complete"))
            assertEquals("2.4.9", cached.getJSONObject("nextCall").getJSONObject("arguments").getString("targetVersion"))
            assertTrue(followNative(cached.getJSONObject("nextCall")).getJSONObject("readiness").getBoolean("ready"))
            assertEquals(2, downloads.get())
            val started = nativeCall("""{"mode":"analyze","path":"app/code/Foo/Bar","targetVersion":"2.4.9"}""")
            val id = started.getString("runId")
            PlatformTestUtil.waitWithEventsDispatching("prepared release analysis", { runs().results(id).state != "running" }, 20)
            val result = nativeCall("""{"mode":"results","runId":"$id"}""")
            assertEquals(result.toString(), "completed", result.getString("state"))
            assertEquals("2.4.9", result.getString("targetVersion"))
            val findings = result.getJSONArray("findings").toList().map { it as Map<*, *> }
            assertTrue(findings.toString(), findings.any { it["filePath"] == "app/code/Foo/Bar/Example.php" && it["severity"] == "critical" })
            assertTrue(findings.toString(), findings.any { it["filePath"] == "app/code/Foo/Bar/etc/di.xml" && it["severity"] == "critical" })
            val unrelated = preparation.start("2.4.8-p4") { _, _, _ ->
                UctReleaseFixture.index("2.4.8-p4", setOf("\\Magento\\Framework\\Unrelated"))
            }
            PlatformTestUtil.waitWithEventsDispatching("unrelated release preparation", { preparation.results(unrelated.runId!!).state != "running" }, 20)
            assertEquals("completed", preparation.results(unrelated.runId!!).state)
            val repeated = nativeCall("""{"mode":"analyze","path":"app/code/Foo/Bar","targetVersion":"2.4.9"}""")
            PlatformTestUtil.waitWithEventsDispatching("repeat after preparation", { runs().results(repeated.getString("runId")).state != "running" }, 20)
            val stable = followNative(repeated.getJSONObject("nextCall"))
            assertEquals(result.getJSONArray("findings").toString(), stable.getJSONArray("findings").toString())
            assertEquals(result.getJSONObject("analysisIdentity").toString(), stable.getJSONObject("analysisIdentity").toString())
            val after = Files.walk(java.nio.file.Path.of(project.basePath!!)).use { it.sorted().toList() }
            assertEquals(before, after)
        } finally { server.stop(0) }
    }

    /** Explicit opt-in: fetches real upstream releases through native MCP, never shell scripts. */
    fun testNativeMcpWithActualMagentoReleases() {
        check(System.getenv("MAGENTO_MCP_REAL_RELEASE_TEST") == "true") { "Enable MAGENTO_MCP_REAL_RELEASE_TEST=true for this network integration test." }
        val published = nativeCall("""{"mode":"releases","currentVersion":"2.4.8-p5"}""")
        assertEquals("2.4.9", published.getJSONObject("nextMinorRelease").getString("version"))
        assertFalse(published.getBoolean("cacheHit"))
        assertTrue(nativeCall("""{"mode":"releases","currentVersion":"2.4.8-p5"}""").getBoolean("cacheHit"))
        Settings.getInstance(project).pluginEnabled = true
        myFixture.addFileToProject("composer.lock", """{"packages":[{"name":"magento/magento2-base","version":"2.4.8-p5"}]}""")
        myFixture.addFileToProject("Sample.php", "<?php class Sample {}")
        val preparation = UctReleasePreparation(project, Files.createTempDirectory("mcp-real-releases-"), UctReleaseSource())
        Disposer.register(testRootDisposable, preparation)
        project.getService(UctReleasePreparation::class.java)
        project.replaceService(UctReleasePreparation::class.java, preparation, testRootDisposable)
        for (version in listOf("2.4.8-p5", "2.4.9")) {
            val started = nativeCall("""{"mode":"prepare","targetVersion":"$version"}""")
            val id = started.getString("runId")
            PlatformTestUtil.waitWithEventsDispatching("real Magento $version preparation", { preparation.results(id).state != "running" }, 600)
            val result = nativeCall("""{"mode":"results","runId":"$id"}""")
            assertEquals(result.toString(), "completed", result.getString("state"))
            assertTrue(result.getInt("processedFiles") > 5000)
            val summary = result.getJSONObject("summary")
            assertTrue(summary.getInt("existenceSymbols") > 10000)
            assertTrue(summary.getInt("apiSymbols") > 1000)
            assertTrue(summary.getInt("deprecationSymbols") > 100)
            println("Real Magento $version: ${result.getInt("processedFiles")} PHP files; $summary")
        }
        val ready = nativeCall("""{"mode":"status","targetVersion":"2.4.9","ignoreCurrentVersion":true}""")
        assertTrue(ready.toString(), ready.getJSONObject("readiness").getBoolean("ready"))
        val started = nativeCall("""{"mode":"analyze","path":"Sample.php","targetVersion":"2.4.9","ignoreCurrentVersion":true}""")
        val id = started.getString("runId")
        PlatformTestUtil.waitWithEventsDispatching("real release analysis", { runs().results(id).state != "running" }, 30)
        val result = nativeCall("""{"mode":"results","runId":"$id"}""")
        assertEquals(result.toString(), "completed", result.getString("state"))
        assertEquals("2.4.8-p5", result.getString("currentVersion"))
        assertEquals("2.4.9", result.getString("targetVersion"))
    }

    fun testReadinessSeparatesIdeIndexingFromReleaseDataAndPreservesStatusOptions() {
        Settings.getInstance(project).pluginEnabled = true
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            val status = nativeCall("""{"mode":"status","targetVersion":"2.4.3","currentVersion":"2.4.2","ignoreCurrentVersion":true}""")
            val readiness = status.getJSONObject("readiness")
            assertFalse(readiness.getBoolean("ideReady"))
            assertTrue(readiness.getBoolean("releaseDataReady"))
            assertFalse(readiness.getBoolean("ready"))
            assertEquals(500, status.getInt("retryAfterMs"))
            assertEquals("poll", status.getString("nextStep"))
            val next = status.getJSONObject("nextCall").getJSONObject("arguments")
            assertEquals("status", next.getString("mode"))
            assertEquals("2.4.3", next.getString("targetVersion"))
            assertEquals("2.4.2", next.getString("currentVersion"))
            assertTrue(next.getBoolean("ignoreCurrentVersion"))
        }
        assertTrue(nativeCall("""{"mode":"status","targetVersion":"2.4.3"}""").getJSONObject("readiness").getBoolean("ready"))
    }

    fun testNativeMcpPreparationProgressFailureAndCancellationNeverReportReady() {
        Settings.getInstance(project).pluginEnabled = true
        val preparation = UctReleasePreparation(project, Files.createTempDirectory("mcp-prepare-jobs-"), UctReleaseSource())
        Disposer.register(testRootDisposable, preparation)
        project.getService(UctReleasePreparation::class.java)
        project.replaceService(UctReleasePreparation::class.java, preparation, testRootDisposable)
        val release = CountDownLatch(1)
        val entered = AtomicBoolean()
        try {
            val started = preparation.start("2.4.9") { _, _, progress ->
                progress(com.magento.idea.magento2uct.analysis.UctPreparationProgress("downloading", 123, 456))
                entered.set(true); check(release.await(10, TimeUnit.SECONDS)); UctReleaseFixture.index()
            }
            PlatformTestUtil.waitWithEventsDispatching("preparation progress", entered::get, 10)
            val running = nativeCall("""{"mode":"results","runId":"${started.runId}"}""")
            assertFalse(running.getBoolean("releaseDataReady"))
            assertEquals(123, running.getInt("downloadedBytes"))
            assertEquals("downloading", running.getString("phase"))
            assertEquals(500, running.getInt("retryAfterMs"))
            assertEquals("running", followNative(running.getJSONObject("nextCall")).getString("state"))
            val status = nativeCall("""{"mode":"status","targetVersion":"2.4.10","currentVersion":"2.4.3","ignoreCurrentVersion":true}""")
            assertEquals(started.runId, status.getJSONObject("nextCall").getJSONObject("arguments").getString("runId"))
            val continued = followNative(status.getJSONObject("nextCall"))
            val continuation = continued.getJSONObject("nextCall").getJSONObject("arguments")
            assertEquals("2.4.10", continuation.getString("analysisTargetVersion"))
            assertEquals("2.4.3", continuation.getString("currentVersion"))
            assertTrue(continuation.getBoolean("ignoreCurrentVersion"))
            continuation.put("mode", "cancel")
            assertEquals("cancelled", followNative(continued.getJSONObject("nextCall")).getString("state"))
            val cancelled = nativeCall("""{"mode":"results","runId":"${started.runId}"}""")
            assertFalse(cancelled.getBoolean("complete"))
            assertTrue(cancelled.isNull("summary"))
            assertTrue(cancelled.isNull("nextCall"))
            assertTrue(cancelled.getString("nextAction").contains("cancelled"))
            assertFalse(cancelled.getString("nextAction").contains("After preparation completes"))
            val failed = preparation.start("2.4.8") { _, _, _ -> error("HTTP 404: release unavailable") }
            PlatformTestUtil.waitWithEventsDispatching("failed preparation", { preparation.results(failed.runId!!).state != "running" }, 10)
            val failure = nativeCall("""{"mode":"results","runId":"${failed.runId}"}""", expectedError = true)
            assertEquals("preparation_failed", failure.getJSONObject("error").getString("code"))
            assertFalse(failure.getBoolean("releaseDataReady"))
            assertTrue(failure.isNull("nextCall"))
            assertTrue(failure.getString("nextAction").contains("Preparation failed"))
            assertTrue(failure.getJSONObject("error").getString("action").contains("official release tag"))
        } finally { release.countDown() }
    }

    fun testStatusWorksWithDisabledSupportAndExposesActualCoverage() {
        val status = call("status")
        assertFalse(status.getBoolean("pluginEnabled"))
        assertFalse(status.getBoolean("uctEnabled"))
        assertTrue(status.getJSONArray("supportedVersions").toList().contains("2.4.3"))
        assertEquals("bundled", status.getString("indexSource"))
        assertEquals(0, status.getJSONArray("activeRunIds").length())
        assertEquals(project.basePath, status.getString("projectPath"))
        assertEquals(project.basePath, status.getString("magentoRoot"))
        assertTrue(status.has("pluginVersion"))
        assertTrue(status.getString("ideVersion").isNotBlank())
    }

    fun testStatusDetectsTheInstalledVersionFromTheConfiguredRootWhileIndexing() {
        Settings.getInstance(project).magentoPath = "src"
        Settings.getInstance(project).magentoVersion = "2.4.3"
        myFixture.addFileToProject("composer.lock", """{"packages":[{"name":"magento/product-community-edition","version":"2.4.2"}]}""")
        myFixture.addFileToProject("src/composer.lock", """{"packages":[{"name":"magento/magento2-base","version":"2.4.8-p5"}]}""")
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            val status = call("status")
            assertEquals("${project.basePath}/src", status.getString("magentoRoot"))
            assertEquals("2.4.8-p5", status.getString("detectedMagentoVersion"))
            assertEquals("composer.lock", status.getString("magentoVersionSource"))
            assertEquals("2.4.8-p5", status.getJSONObject("defaults").getString("currentVersion"))
            assertFalse(status.getBoolean("currentVersionCovered"))
            assertEquals("2.4.4-beta4", status.getString("latestSupportedVersion"))
        }
    }

    fun testComposerVersionDetectionPrefersLockedEditionAndDoesNotGuessFromOtherMetadata() {
        myFixture.addFileToProject("composer.lock", """{"packages":[
            {"name":"magento/magento2-base","version":"2.4.2"},
            {"name":"magento/product-community-edition","version":"v2.4.3-p1"},
            {"name":"magento/product-enterprise-edition","version":"2.4.4-beta4"}]}""")
        assertEquals("2.4.4-beta4", call("status").getString("detectedMagentoVersion"))
        myFixture.addFileToProject("composer.lock", "{invalid")
        myFixture.addFileToProject("composer.json", """{"require":{"magento/product-community-edition":"^2.4.8"}}""")
        val unknown = call("status")
        assertTrue(unknown.isNull("detectedMagentoVersion"))
        assertTrue(unknown.getString("versionDetectionWarning").contains("composer.lock"))
        Settings.getInstance(project).magentoVersion = "2.4.8-p5"
        assertTrue(call("status").isNull("detectedMagentoVersion"))
        myFixture.addFileToProject("composer.json", """{"name":"magento/project-community-edition","version":"2.4.9"}""")
        assertTrue(call("status").isNull("detectedMagentoVersion"))
    }

    fun testAnalyzeUsesDetectedBaselineAndExplicitVersionOverridesIt() {
        Settings.getInstance(project).pluginEnabled = true
        myFixture.addFileToProject("composer.lock", """{"packages":[{"name":"magento/product-community-edition","version":"2.4.2"}]}""")
        myFixture.addFileToProject("Sample.php", "<?php class Sample {}")
        val params = JSONObject("""{"path":"Sample.php","targetVersion":"2.4.3"}""")
        assertEquals("2.4.2", MagentoCompatibilityCommands.request(project, params).currentVersion)
        params.put("currentVersion", "2.4.1")
        assertEquals("2.4.1", MagentoCompatibilityCommands.request(project, params).currentVersion)
        assertTrue(com.magento.idea.magento2uct.settings.UctSettingsService.getInstance(project).configuredCurrentVersion == null)
    }

    fun testUnsupportedUpgradeReturnsIdentityCoverageAndARecoveryCallWithoutStartingARun() {
        Settings.getInstance(project).pluginEnabled = true
        myFixture.addFileToProject("composer.lock", """{"packages":[{"name":"magento/magento2-base","version":"2.4.8-p5"}]}""")
        myFixture.addFileToProject("Sample.php", "<?php class Sample {}")
        val result = call("analyze", """{"path":"Sample.php","targetVersion":"2.4.9"}""")
        assertEquals(project.basePath, result.getString("projectPath"))
        assertEquals("2.4.8-p5", result.getString("currentVersion"))
        assertEquals("2.4.9", result.getString("targetVersion"))
        assertEquals("unsupported_target_version", result.getJSONObject("error").getString("kind"))
        assertFalse(result.getJSONObject("coverage").getBoolean("targetVersionCovered"))
        assertTrue(result.getJSONObject("error").getString("action").contains("No scan was started"))
        assertEquals("magento_compatibility", result.getJSONObject("nextCall").getString("tool"))
        assertEquals("prepare", result.getJSONObject("nextCall").getJSONObject("arguments").getString("mode"))
        assertEquals("2.4.8-p5", result.getJSONObject("nextCall").getJSONObject("arguments").getString("targetVersion"))
        assertEquals("2.4.9", result.getJSONObject("nextCall").getJSONObject("arguments").getString("analysisTargetVersion"))
        assertEquals("2.4.8-p5", result.getJSONObject("nextCall").getJSONObject("arguments").getString("currentVersion"))
        assertTrue(runs().activeRunIds().isEmpty())
        assertFalse(result.has("runId"))
    }

    fun testNativeMcpDiscoveryPublishesOnlyTypedArgumentsWithoutRequiredJsonEncoding() {
        val schema = nativeTool().descriptor.inputSchema
        assertTrue(schema.requiredProperties.isEmpty())
        val properties = JSONObject(schema.propertiesSchema.toString())
        assertFalse(properties.has("parametersJson"))
        for ((name, type) in listOf("projectPath" to "string", "moduleName" to "string", "targetVersion" to "string", "analysisTargetVersion" to "string", "runId" to "string", "limit" to "integer", "ignoreCurrentVersion" to "boolean")) {
            assertTrue(name, properties.has(name))
            assertTrue(properties.getJSONObject(name).toString(), properties.getJSONObject(name).toString().contains(type))
        }
    }

    fun testProjectPathWorksForStatusAndDocumentationAndRejectsWrongProject() {
        for (mode in listOf("status", "help", "detailed_schema")) {
            val response = nativeCall(JSONObject().put("mode", mode).put("projectPath", project.basePath).toString())
            assertTrue(response.getBoolean("successful"))
            assertEquals(project.basePath, response.getString("projectPath"))
            val rejected = nativeCall(JSONObject().put("mode", mode).put("projectPath", project.basePath + "/wrong").toString(), expectedError = true)
            assertEquals("projectPath", rejected.getJSONObject("error").getString("parameter"))
            val badType = nativeCall(JSONObject().put("mode", mode).put("projectPath", 123).toString(), expectedError = true)
            assertEquals("projectPath", badType.getJSONObject("error").getString("parameter"))
        }
        val rejected = nativeCall(JSONObject().put("mode", "status").put("projectPath", project.basePath + "/wrong").toString(), expectedError = true)
        assertEquals("projectPath", rejected.getJSONObject("error").getString("parameter"))
    }

    fun testNativeMcpCallsUseDefaultStatusAndTypedAnalyzeAndResults() {
        Settings.getInstance(project).pluginEnabled = true
        myFixture.addFileToProject("composer.lock", """{"packages":[{"name":"magento/magento2-base","version":"2.4.2"}]}""")
        myFixture.addFileToProject("Sample.php", "<?php class Sample {}")
        val status = nativeCall()
        assertEquals(project.basePath, status.getString("projectPath"))
        assertEquals("2.4.2", status.getString("detectedMagentoVersion"))
        val started = nativeCall("""{"mode":"analyze","path":"Sample.php","targetVersion":"2.4.3"}""")
        assertEquals("2.4.2", started.getString("currentVersion"))
        val id = started.getString("runId")
        PlatformTestUtil.waitWithEventsDispatching("native MCP analysis completion", { runs().results(id).state != "running" }, 20)
        val result = nativeCall("""{"mode":"results","runId":"$id","limit":1}""")
        assertEquals("completed", result.getString("state"))
        assertTrue(result.getBoolean("complete"))
        assertEquals(1, result.getInt("processedFiles"))
        assertEquals(0, result.getJSONObject("summary").getInt("totalIssues"))
        val unsupported = nativeCall("""{"mode":"analyze","path":"Sample.php","targetVersion":"2.4.9"}""", expectedError = true)
        assertEquals("unsupported_target_version", unsupported.getJSONObject("error").getString("kind"))
    }

    fun testStatusRemainsAvailableWhileIndexingAndAnalysisExplainsWhyItCannotStart() {
        Settings.getInstance(project).pluginEnabled = true
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            val status = call("status", """{"targetVersion":"2.4.3"}""")
            assertTrue(status.getBoolean("indexing"))
            assertTrue(status.getBoolean("pollRequired"))
            assertFalse(status.getBoolean("terminal"))
            val result = call("analyze")
            assertFalse(result.getBoolean("complete"))
            assertTrue(result.getJSONObject("error").getString("message").contains("Indexes are not ready"))
        }
    }

    fun testNativeMcpValidationFailuresSetErrorFlagAndPreserveStructuredErrors() {
        Settings.getInstance(project).pluginEnabled = true
        myFixture.addFileToProject("Sample.php", "<?php class Sample {}")
        for (arguments in listOf(
            """{"mode":"unknown"}""",
            """{"mode":"prepare"}""",
            """{"mode":"prepare","targetVersion":"latest"}""",
            """{"mode":"results"}""",
            """{"mode":"cancel","runId":"unknown"}""",
            """{"mode":"results","runId":"unknown","limit":0}""",
            """{"mode":"results","runId":"unknown","limit":501}""",
            """{"mode":"results","runId":"unknown","offset":-1}""",
            """{"mode":"analyze","path":"Sample.php","moduleName":"Foo_Bar","targetVersion":"2.4.3"}""",
            """{"mode":"analyze","path":"Sample.php","targetVersion":"2.4.3","minimumSeverity":"info"}""",
            """{"mode":"status","targetVersion":"2.4.3","currentVersion":"2.4.9"}""",
            """{"mode":"prepare","targetVersion":"2.4.8-p5","analysisTargetVersion":"2.4.9","currentVersion":"2.4.10"}"""
        )) {
            val result = nativeCall(arguments, expectedError = true)
            assertEquals(arguments, "failed", result.getString("state"))
            assertFalse(result.getBoolean("complete"))
            assertTrue(result.getJSONObject("error").getString("message").isNotBlank())
        }
        assertTrue(runs().activeRunIds().isEmpty())
        assertTrue(project.getService(UctReleasePreparation::class.java).activeRunIds().isEmpty())
    }

    fun testTransportErrorsKeepJsonTextWhenStructuredContentIsOmitted() {
        val wire = transportResult(nativeResult("""{"mode":"results"}"""))
        assertEquals(true, wire.isError)
        assertNull(wire.structuredContent)
        val error = JSONObject(wire.content.filterIsInstance<TextContent>().single().text)
        assertEquals("failed", error.getString("state"))
        assertEquals("invalid_request", error.getJSONObject("error").getString("code"))
    }

    fun testValidationErrorsIdentifyParametersWithoutUnhelpfulStatusContinuations() {
        Settings.getInstance(project).pluginEnabled = true
        myFixture.addFileToProject("Sample.php", "<?php class Sample {}")
        val rejected = nativeCall("""{"mode":"analyze","path":"Sample.php","targetVersion":"2.4.3","currentVersion":"2.4.2","ignoreCurrentVersion":true,"minimumSeverity":"info"}""", expectedError = true)
        assertTrue(rejected.isNull("nextCall"))
        assertEquals("minimumSeverity", rejected.getJSONObject("error").getString("parameter"))
        assertEquals(listOf("warning", "error", "critical"), rejected.getJSONObject("error").getJSONArray("acceptedValues").toList())
        val preparation = nativeCall("""{"mode":"prepare","targetVersion":"latest","analysisTargetVersion":"2.4.9"}""", expectedError = true)
        assertTrue(preparation.isNull("nextCall"))
        assertEquals("targetVersion", preparation.getJSONObject("error").getString("parameter"))
        val missing = nativeCall("""{"mode":"results"}""", expectedError = true)
        assertEquals("runId", missing.getJSONObject("error").getString("parameter"))
        assertTrue(missing.isNull("nextCall"))
        assertTrue(runs().activeRunIds().isEmpty())
    }

    fun testInvalidVersionOptionsDoNotCreateARecoveryLoop() {
        for (arguments in listOf(
            """{"mode":"status","targetVersion":"latest"}""",
            """{"mode":"status","targetVersion":"2.4.3","currentVersion":"2.4.9"}""",
            """{"mode":"status","targetVersion":"2.4.3","currentVersion":"latest"}""",
            """{"mode":"status","targetVersion":"2.4.3","ignoreCurrentVersion":true}"""
        )) {
            val rejected = nativeCall(arguments, expectedError = true)
            assertTrue(rejected.toString(), rejected.isNull("nextCall"))
            assertTrue(rejected.getString("nextAction").contains("Correct"))
        }
    }

    fun testStatusExposesEffectiveOptionsAndDistinguishesCoverageFromReleaseAvailability() {
        Settings.getInstance(project).pluginEnabled = true
        val ready = nativeCall("""{"mode":"status","targetVersion":"2.4.3","currentVersion":"2.4.2","ignoreCurrentVersion":true}""")
        val defaults = ready.getJSONObject("defaults")
        assertEquals("2.4.3", defaults.getString("targetVersion"))
        assertEquals("2.4.2", defaults.getString("currentVersion"))
        assertTrue(defaults.getBoolean("ignoreCurrentVersion"))
        assertTrue(ready.getJSONObject("readiness").getBoolean("ready"))
        val missing = nativeCall("""{"mode":"status","targetVersion":"2.4.99"}""")
        assertFalse(missing.getJSONObject("readiness").getBoolean("ready"))
        assertTrue(missing.getString("releaseAvailabilityNote").contains("not published releases"))
        assertTrue(missing.getString("nextAction").contains("has not been verified"))
        assertTrue(project.getService(UctReleasePreparation::class.java).activeRunIds().isEmpty())
    }

    fun testNextStepDistinguishesStatusSuccessFromAnalysisReadiness() {
        Settings.getInstance(project).pluginEnabled = true
        val selection = nativeCall()
        assertTrue(selection.getBoolean("successful"))
        assertFalse(selection.getJSONObject("readiness").getBoolean("ready"))
        assertEquals("select_target", selection.getString("nextStep"))

        val missing = nativeCall("""{"mode":"status","targetVersion":"9.9.9"}""")
        assertTrue(missing.getBoolean("successful"))
        assertFalse(missing.getJSONObject("readiness").getBoolean("ready"))
        assertFalse(missing.getBoolean("pollRequired"))
        assertEquals("call", missing.getString("nextStep"))
        assertEquals("prepare", missing.getJSONObject("nextCall").getJSONObject("arguments").getString("mode"))

        val ready = nativeCall("""{"mode":"status","targetVersion":"2.4.3"}""")
        assertTrue(ready.getJSONObject("readiness").getBoolean("ready"))
        assertEquals("analyze", ready.getString("nextStep"))
        assertTrue(ready.isNull("nextCall"))

        Settings.getInstance(project).pluginEnabled = false
        val disabled = nativeCall("""{"mode":"status","targetVersion":"2.4.3"}""")
        assertTrue(disabled.getBoolean("successful"))
        assertFalse(disabled.getJSONObject("readiness").getBoolean("ready"))
        assertEquals("enable_support", disabled.getString("nextStep"))
    }

    fun testNestedMagentoRootPathErrorSuggestsAnExplicitProjectRelativeRetry() {
        Settings.getInstance(project).pluginEnabled = true
        Settings.getInstance(project).magentoPath = "src"
        val file = myFixture.addFileToProject("src/app/code/Application/Blog/Example.php", "<?php class Example {}")
        val rejected = nativeCall("""{"mode":"analyze","path":"app/code/Application/Blog/Example.php","targetVersion":"2.4.3"}""", expectedError = true)
        assertEquals("resolve_error", rejected.getString("nextStep"))
        val error = rejected.getJSONObject("error")
        assertEquals("path", error.getString("parameter"))
        assertEquals(project.basePath, error.getString("pathBase"))
        assertEquals("${project.basePath}/src", error.getString("magentoRoot"))
        assertEquals("src/app/code/Application/Blog/Example.php", error.getString("suggestedPath"))
        assertTrue(runs().activeRunIds().isEmpty())

        val started = nativeCall(JSONObject().put("mode", "analyze").put("projectPath", project.basePath)
            .put("path", error.getString("suggestedPath")).put("targetVersion", "2.4.3").toString())
        val id = started.getString("runId")
        PlatformTestUtil.waitWithEventsDispatching("nested module path scan", { runs().results(id).state != "running" }, 20)
        val result = followNative(started.getJSONObject("nextCall"))
        assertEquals("done", result.getString("nextStep"))
        assertEquals(1, result.getInt("processedFiles"))
        assertEquals(listOf("src/app/code/Application/Blog/Example.php"), result.getJSONArray("paths").toList())

        // An existing IDE-project-relative path stays authoritative even if a nested copy exists.
        myFixture.addFileToProject("app/code/Application/Blog/Example.php", "<?php class OuterExample {}")
        val direct = MagentoCompatibilityCommands.request(project, JSONObject()
            .put("path", "app/code/Application/Blog/Example.php").put("targetVersion", "2.4.3"))
        assertFalse(direct.paths.contains(file.virtualFile.path))
        assertEquals(listOf("${project.basePath}/app/code/Application/Blog/Example.php"), direct.paths)

        val absent = nativeCall("""{"mode":"analyze","path":"missing.php","targetVersion":"2.4.3"}""", expectedError = true)
        assertFalse(absent.getJSONObject("error").has("suggestedPath"))
    }

    fun testUnsupportedSingleFilesAreRejectedBeforeStartingJobs() {
        Settings.getInstance(project).pluginEnabled = true
        for ((path, contents) in listOf("README.md" to "# Module", "composer.json" to "{}", "script.js" to "void 0;")) {
            myFixture.addFileToProject(path, contents)
            val rejected = nativeCall("""{"mode":"analyze","path":"$path","targetVersion":"2.4.3"}""", expectedError = true)
            assertEquals("invalid_request", rejected.getJSONObject("error").getString("code"))
            assertTrue(rejected.getJSONObject("error").getString("message").contains("Unsupported analysis file"))
            assertFalse(rejected.has("runId"))
            assertTrue(runs().activeRunIds().isEmpty())
        }
    }

    fun testNativeMcpFailedAnalysisSetsErrorFlagWithoutSuccessfulSummary() {
        Settings.getInstance(project).pluginEnabled = true
        myFixture.addFileToProject("Empty/composer.json", "{}")
        val started = nativeCall("""{"mode":"analyze","path":"Empty","targetVersion":"2.4.3"}""")
        val id = started.getString("runId")
        PlatformTestUtil.waitWithEventsDispatching("unsupported file failure", { runs().results(id).state != "running" }, 20)
        val result = nativeCall("""{"mode":"results","runId":"$id"}""", expectedError = true)
        assertEquals("analysis_failed", result.getJSONObject("error").getString("code"))
        assertFalse(result.getBoolean("complete"))
        assertTrue(result.isNull("summary"))
        assertTrue(result.isNull("nextCall"))
    }

    fun testNativeMcpHtmlAndPhtmlScopesMatchDocumentedSupport() {
        Settings.getInstance(project).pluginEnabled = true
        for ((path, content) in listOf(
            "Sample.phtml" to "<?php echo 'example'; ?>",
            "Sample.html" to "<div>example</div>",
            "Sample.xml" to "<config/>"
        )) {
            myFixture.addFileToProject(path, content)
            val started = nativeCall("""{"mode":"analyze","path":"$path","targetVersion":"2.4.3"}""")
            val id = started.getString("runId")
            PlatformTestUtil.waitWithEventsDispatching("template analysis", { runs().results(id).state != "running" }, 20)
            val result = nativeCall("""{"mode":"results","runId":"$id"}""")
            assertTrue(result.toString(), result.getBoolean("complete"))
            assertEquals(1, result.getInt("processedFiles"))
            assertEquals(1, result.getInt("totalFiles"))
        }
    }

    fun testCachedPreparationPreservesOriginalStatusOptionsWithoutPolling() {
        Settings.getInstance(project).pluginEnabled = true
        val preparation = UctReleasePreparation(project, Files.createTempDirectory("mcp-cached-options-"), UctReleaseSource())
        Disposer.register(testRootDisposable, preparation)
        project.getService(UctReleasePreparation::class.java)
        project.replaceService(UctReleasePreparation::class.java, preparation, testRootDisposable)
        val started = preparation.start("2.4.9") { _, _, _ -> UctReleaseFixture.index() }
        PlatformTestUtil.waitWithEventsDispatching("cached release", { preparation.results(started.runId!!).state != "running" }, 20)
        val cached = nativeCall("""{"mode":"prepare","targetVersion":"2.4.9","analysisTargetVersion":"2.4.10","currentVersion":"2.4.3","ignoreCurrentVersion":true}""")
        assertTrue(cached.getBoolean("complete"))
        assertTrue(cached.isNull("runId"))
        assertEquals("call", cached.getString("nextStep"))
        assertTrue(cached.getBoolean("terminal"))
        assertFalse(cached.getBoolean("pollRequired"))
        val status = followNative(cached.getJSONObject("nextCall"))
        assertEquals("2.4.10", status.getJSONObject("readiness").getString("targetVersion"))
        assertEquals("2.4.3", status.getJSONObject("defaults").getString("currentVersion"))
        val next = status.getJSONObject("nextCall").getJSONObject("arguments")
        assertEquals("2.4.10", next.getString("targetVersion"))
        assertEquals("2.4.10", next.getString("analysisTargetVersion"))
        assertEquals("2.4.3", next.getString("currentVersion"))
        assertTrue(next.getBoolean("ignoreCurrentVersion"))
    }

    fun testNativeMcpHelpAndSchemaRemainSuccessfulTextResponses() {
        for (mode in listOf("help", "detailed_schema")) {
            val result = nativeResult("""{"mode":"$mode"}""")
            assertFalse(result.isError)
            assertNull(result.structuredContent)
            val text = result.content.filterIsInstance<McpToolCallResultContent.Text>().single().text
            assertTrue(text.contains("PHTML"))
            assertTrue(text.contains("HTML"))
            assertTrue(text.contains("runId=null"))
        }
    }

    fun testMissingNativeProjectContextReturnsAStructuredMcpError() {
        val result = runBlocking { nativeTool().call(JsonObject(emptyMap())) }
        assertTrue(result.isError)
        val text = result.content.filterIsInstance<McpToolCallResultContent.Text>().single().text
        assertNull(result.structuredContent)
        val error = JSONObject(text).getJSONObject("error")
        assertEquals("unavailable", error.getString("code"))
        assertTrue(error.getString("message").contains("project context"))
    }

    fun testUnknownMissingAndInvalidParametersReturnStructuredErrors() {
        for ((mode, parameters) in listOf(
            "unknown" to "{}",
            "status" to "{\"unused\":true}", "results" to "{}", "cancel" to "{}",
            "results" to "{\"runId\":\"unknown\",\"limit\":0}",
            "results" to "{\"runId\":\"unknown\",\"offset\":1.5}",
            "prepare" to "{}", "prepare" to "{\"targetVersion\":\"latest\"}",
            "status" to "{\"targetVersion\":\"2.4.9\",\"ignoreCurrentVersion\":true}",
            "status" to "{\"currentVersion\":\"latest\"}"
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
            "{\"path\":\"Sample.php\",\"targetVersion\":\"2.4.3\",\"ignoreCurrentVersion\":\"true\"}",
            "{\"path\":\"Sample.php\",\"targetVersion\":\"2.4.3\",\"minimumSeverity\":\"info\"}",
            "{\"path\":\"..\",\"targetVersion\":\"2.4.3\"}",
            "{\"path\":\"Sample.php\",\"targetVersion\":\"latest\"}",
            "{\"path\":\"Sample.php\",\"targetVersion\":\"2.4.9\",\"currentVersion\":\"2.4.10\"}",
            "{\"path\":\"Sample.php\",\"targetVersion\":\"2.4.9\",\"ignoreCurrentVersion\":true}"
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

    fun testModuleNameAnalysisPrefersConfiguredRootOverIndexedRuntimeDuplicate() {
        Settings.getInstance(project).pluginEnabled = true
        for (directory in listOf(".runtime", "src")) {
            myFixture.addFileToProject("$directory/app/code/Foo/Bar/etc/module.xml", "<config><module name=\"Foo_Bar\"/></config>")
            myFixture.addFileToProject("$directory/app/code/Foo/Bar/registration.php", """
                <?php \Magento\Framework\Component\ComponentRegistrar::register('module', 'Foo_Bar', __DIR__);
            """.trimIndent())
        }
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        assertEquals(2, FileBasedIndex.getInstance().getContainingFiles(
            ModuleXmlIndex.KEY, "Foo_Bar", GlobalSearchScope.allScope(project)
        ).size)

        for ((configuredRoot, expectedDirectory) in listOf(
            "${project.basePath}/src" to "src",
            "src" to "src",
            "\\src" to "src",
            "${project.basePath}/.runtime" to ".runtime"
        )) {
            Settings.getInstance(project).magentoPath = configuredRoot
            val started = call("analyze", "{\"moduleName\":\"Foo_Bar\",\"targetVersion\":\"2.4.3\"}")
            assertTrue(started.toString(), started.has("runId"))
            assertEquals("$expectedDirectory/app/code/Foo/Bar", started.getJSONArray("paths").getString(0))
            val runId = started.getString("runId")
            PlatformTestUtil.waitWithEventsDispatching("duplicate module analysis completion", { runs().results(runId).state != "running" }, 20)
            val result = call("results", "{\"runId\":\"$runId\"}")
            assertEquals(result.toString(), "completed", result.getString("state"))
            assertEquals(1, result.getJSONObject("summary").getInt("modules"))
            assertEquals(2, result.getInt("processedFiles"))
        }
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
        assertEquals("call", first.getString("nextStep"))
        assertTrue(first.getBoolean("terminal"))
        assertFalse(first.getBoolean("pollRequired"))
        val next = first.getJSONObject("nextCall").getJSONObject("arguments")
        assertEquals(project.basePath, next.getString("projectPath"))
        assertEquals("results", next.getString("mode"))
        next.remove("mode")
        next.remove("projectPath")
        assertEquals(second.getJSONArray("findings").toString(), call("results", next.toString()).getJSONArray("findings").toString())
        assertTrue(second.isNull("nextCall"))
        assertEquals("done", second.getString("nextStep"))
        assertEquals("completed", call("cancel", "{\"runId\":\"${started.runId}\"}").getString("state"))
    }

    fun testCancellationCannotBecomeLateSuccessAndRunningResultsAreNotClean() {
        Settings.getInstance(project).pluginEnabled = true
        myFixture.addFileToProject("Sample.php", "<?php class Sample {}")
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
            val running = nativeCall("{\"mode\":\"results\",\"runId\":\"${started.runId}\"}")
            assertFalse(running.getBoolean("complete"))
            assertTrue(running.isNull("summary"))
            assertEquals(500, running.getInt("retryAfterMs"))
            assertEquals(started.runId, running.getJSONObject("nextCall").getJSONObject("arguments").getString("runId"))
            assertEquals(1, call("status").getJSONArray("activeRunIds").length())
            val concurrent = nativeCall("""{"mode":"analyze","path":"Sample.php","targetVersion":"2.4.3"}""", expectedError = true)
            assertTrue(concurrent.getJSONObject("error").getString("message").contains("already running"))
            assertEquals("cancelled", nativeCall("{\"mode\":\"cancel\",\"runId\":\"${started.runId}\"}").getString("state"))
            release.countDown()
            PlatformTestUtil.waitWithEventsDispatching("cancelled worker return", { returned.get() }, 10)
            assertNull(runs().results(started.runId).result)
            assertEquals("cancelled", runs().cancel(started.runId).state)
        } finally {
            release.countDown()
        }
    }

    fun testOldRunsReloadFromReportsAndDifferentRunStoresCannotAccessThem() {
        val other = UctAnalysisRuns(project, Files.createTempDirectory("other-analysis-history-"))
        val ids = arrayListOf<String>()
        try {
            repeat(6) {
                val started = runs().start(request()) { UctAnalysisResult(emptyList(), 0, 0, 0) }
                ids += started.runId
                PlatformTestUtil.waitWithEventsDispatching("analysis completion", { runs().results(started.runId).state != "running" }, 10)
            }
            val archived = call("results", "{\"runId\":\"${ids.first()}\"}")
            assertEquals("completed", archived.getString("state"))
            assertTrue(archived.getJSONObject("report").getBoolean("saved"))
            assertTrue(Files.isRegularFile(java.nio.file.Path.of(archived.getJSONObject("report").getString("path"))))
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
    fun testNativePaginationDocumentsNumericStringCoercion() {
        val findings = (1..3).map { UctFinding(project.basePath + "/Sample.php", it, 1, "finding $it", SupportedIssue.CALLING_DEPRECATED_METHOD) }
        val run = runs().start(request()) { UctAnalysisResult(findings, 1, 0, 0) }
        PlatformTestUtil.waitWithEventsDispatching("pagination fixture", { runs().results(run.runId).state != "running" }, 10)
        val result = nativeCall("""{"mode":"results","runId":"${run.runId}","offset":"1","limit":"1"}""")
        assertEquals(1, result.getJSONArray("findings").length())
        assertEquals(2, result.getJSONArray("findings").getJSONObject(0).getInt("line"))
        assertEquals(2, result.getInt("nextOffset"))
        assertTrue(MagentoCompatibilityCommands.detailedSchema().contains("coerces numeric strings"))
    }

    fun testNativeResultsExposeAnalysisIdentityScopeAndSuppression() {
        Settings.getInstance(project).pluginEnabled = true
        myFixture.addFileToProject("Test/Sample.php", "<?php class Sample {}")
        val started = nativeCall("""{"mode":"analyze","path":"Test/Sample.php","currentVersion":"2.4.2","targetVersion":"2.4.3","ignoreCurrentVersion":true,"explainSuppressed":true}""")
        PlatformTestUtil.waitWithEventsDispatching("metadata scan", { runs().results(started.getString("runId")).state != "running" }, 10)
        val result = followNative(started.getJSONObject("nextCall"))
        assertEquals(project.basePath, result.getString("pathBase"))
        val identity = result.getJSONObject("analysisIdentity")
        assertEquals(64, identity.getString("indexRevision").length)
        assertEquals("2.4.3", identity.getJSONObject("target").getString("version"))
        assertEquals(1, result.getJSONObject("scope").getJSONObject("fileTypeCounts").getInt("php"))
        assertEquals(1, result.getJSONObject("scope").getInt("testFiles"))
        assertEquals(0, result.getJSONObject("suppression").getInt("total"))
        assertEquals(0, result.getJSONObject("suppression").getInt("suppressedDiagnostics"))
        assertEquals(result.getJSONObject("summary").getInt("totalIssues"), result.getJSONObject("summary").getInt("displayedFindings"))
        assertEquals("upgrade_changes", result.getString("findingsScope"))
        assertEquals(identity.toString(), call("status", """{"currentVersion":"2.4.2","targetVersion":"2.4.3","ignoreCurrentVersion":true}""").getJSONObject("analysisIdentity").toString())
        val invalid = nativeCall("""{"mode":"analyze","path":"Test/Sample.php","targetVersion":"2.4.3","explainSuppressed":true}""", expectedError = true)
        assertEquals("explainSuppressed", invalid.getJSONObject("error").getString("parameter"))
    }

    fun testNativeReleaseDiscoveryReturnsPublishedNextMinorAndStatusContinuation() {
        val source = com.magento.idea.magento2uct.analysis.UctPublishedReleases({ Settings.DEFAULT_PUBLISHED_RELEASES_URL }, { _ -> """[
            {"tag_name":"2.4.8-p5","draft":false,"prerelease":false,"published_at":"2026-05-12T00:00:00Z","html_url":"https://github.com/magento/magento2/releases/tag/2.4.8-p5"},
            {"tag_name":"2.4.9","draft":false,"prerelease":false,"published_at":"2026-05-12T00:00:00Z","html_url":"https://github.com/magento/magento2/releases/tag/2.4.9"}
        ]""" }, { java.time.Instant.parse("2026-10-10T00:00:00Z") })
        project.getService(com.magento.idea.magento2uct.analysis.UctPublishedReleases::class.java)
        project.replaceService(com.magento.idea.magento2uct.analysis.UctPublishedReleases::class.java, source, testRootDisposable)
        myFixture.addFileToProject("composer.lock", """{"packages":[{"name":"magento/magento2-base","version":"2.4.8-p5"}]}""")
        val result = nativeCall("""{"mode":"releases"}""")
        assertEquals("2.4.9", result.getJSONObject("nextMinorRelease").getString("version"))
        assertFalse(result.has("releases"))
        assertEquals(2, result.getInt("releaseCount"))
        val full = nativeCall("""{"mode":"releases","includeReleases":true}""")
        assertEquals(2, full.getJSONArray("releases").length())
        assertTrue(full.getBoolean("cacheHit"))
        assertEquals("2.4.9", result.getJSONObject("nextCall").getJSONObject("arguments").getString("targetVersion"))
        assertEquals("2.4.8-p5", result.getJSONObject("nextCall").getJSONObject("arguments").getString("currentVersion"))
        assertTrue(nativeCall("""{"mode":"releases"}""").getBoolean("cacheHit"))
    }

    fun testNativeReleaseDiscoveryUsesConfiguredUrlAndDiscardsOldSourceCache() {
        val requests = java.util.Collections.synchronizedList(mutableListOf<String>())
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val baseUrl = "http://127.0.0.1:${server.address.port}"
        server.createContext("/") { exchange ->
            requests.add(exchange.requestURI.toString())
            val version = if (exchange.requestURI.path == "/first") "2.4.9" else "2.4.10"
            val body = JSONArray(listOf(JSONObject()
                .put("tag_name", version).put("draft", false).put("prerelease", false)
                .put("published_at", "2026-05-12T00:00:00Z")
                .put("html_url", "$baseUrl/releases/$version"))).toString().toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.start()
        val settings = Settings.getInstance(project)
        val originalUrl = settings.publishedReleasesUrl
        try {
            settings.publishedReleasesUrl = "$baseUrl/first?channel=stable"
            val first = nativeCall("""{"mode":"releases","currentVersion":"2.4.8-p5"}""")
            assertEquals(settings.publishedReleasesUrl, first.getString("sourceUrl"))
            assertEquals("2.4.9", first.getJSONObject("nextMinorRelease").getString("version"))
            assertEquals("$baseUrl/releases/2.4.9", first.getJSONObject("nextMinorRelease").getString("url"))
            assertFalse(first.getBoolean("cacheHit"))
            assertTrue(nativeCall("""{"mode":"releases","currentVersion":"2.4.8-p5"}""").getBoolean("cacheHit"))
            assertEquals(listOf("/first?channel=stable&per_page=100&page=1"), requests.toList())

            settings.publishedReleasesUrl = "$baseUrl/second"
            val second = nativeCall("""{"mode":"releases","currentVersion":"2.4.8-p5"}""")
            assertFalse(second.getBoolean("cacheHit"))
            assertEquals(settings.publishedReleasesUrl, second.getString("sourceUrl"))
            assertEquals("2.4.10", second.getJSONObject("nextMinorRelease").getString("version"))
            assertEquals(2, requests.size)

            settings.publishedReleasesUrl = "file:///tmp/not-a-release-api"
            val invalid = nativeCall("""{"mode":"releases"}""", expectedError = true)
            assertTrue(invalid.getJSONObject("error").getString("message").contains("Magento settings"))
            assertEquals(2, requests.size)
        } finally {
            settings.publishedReleasesUrl = originalUrl
            server.stop(0)
        }
    }

    fun testRawNativeArgumentTypesAlwaysReturnJsonValidationErrors() {
        for ((arguments, parameter) in listOf(
            "{\"mode\":42}" to "mode",
            "{\"mode\":\"results\",\"runId\":\"unknown\",\"limit\":1.5}" to "limit",
            "{\"mode\":\"results\",\"runId\":\"unknown\",\"offset\":2147483648}" to "offset",
            "{\"mode\":\"results\",\"runId\":true}" to "runId",
            "{\"mode\":\"status\",\"ignoreCurrentVersion\":\"false\"}" to "ignoreCurrentVersion",
            "{\"mode\":\"status\",\"targetVersion\":[]}" to "targetVersion",
            "{\"mode\":\"releases\",\"includeReleases\":1}" to "includeReleases"
        )) {
            val result = nativeCall(arguments, expectedError = true)
            assertEquals(parameter, result.getJSONObject("error").getString("parameter"))
            assertTrue(result.getBoolean("terminal"))
            assertFalse(result.getBoolean("successful"))
            assertFalse(result.getBoolean("pollRequired"))
            assertFalse(result.getJSONObject("error").getBoolean("recoverable"))
            assertTrue(result.isNull("nextCall"))
        }
    }

    fun testCoverageRequiredIsRecoverableAndNotAValidationFailure() {
        Settings.getInstance(project).pluginEnabled = true
        myFixture.addFileToProject("Sample.php", "<?php class Sample {}")
        val result = nativeCall("""{"mode":"analyze","path":"Sample.php","targetVersion":"9.9.9"}""", expectedError = true)
        assertEquals("blocked", result.getString("state"))
        assertEquals("coverage_required", result.getJSONObject("error").getString("code"))
        assertEquals("call", result.getString("nextStep"))
        assertTrue(result.getJSONObject("error").getBoolean("recoverable"))
        assertFalse(result.getBoolean("terminal"))
        assertFalse(result.getBoolean("pollRequired"))
        assertEquals("prepare", result.getJSONObject("nextCall").getJSONObject("arguments").getString("mode"))
        assertFalse(result.has("runId"))
    }

    fun testTerminalFlagsDistinguishCancellationFailureAndSuccessfulPagination() {
        val gate = CountDownLatch(1)
        try {
            val run = runs().start(request()) { gate.await(10, TimeUnit.SECONDS); UctAnalysisResult(emptyList(), 0, 0, 0) }
            val running = nativeCall("""{"mode":"results","runId":"${run.runId}"}""")
            assertFalse(running.getBoolean("terminal"))
            assertTrue(running.getBoolean("pollRequired"))
            val cancelled = nativeCall("""{"mode":"cancel","runId":"${run.runId}"}""")
            assertTrue(cancelled.getBoolean("terminal"))
            assertEquals("done", cancelled.getString("nextStep"))
            assertFalse(cancelled.getBoolean("successful"))
            assertFalse(cancelled.getBoolean("complete"))
            assertFalse(cancelled.getBoolean("pollRequired"))
            assertTrue(cancelled.getJSONObject("report").getBoolean("saved"))
        } finally { gate.countDown() }
        val failed = runs().start(request()) { throw IllegalStateException("fixture failure") }
        PlatformTestUtil.waitWithEventsDispatching("failed run", { runs().results(failed.runId).state != "running" }, 10)
        val failure = nativeCall("""{"mode":"results","runId":"${failed.runId}"}""", expectedError = true)
        assertTrue(failure.getBoolean("terminal"))
        assertEquals("resolve_error", failure.getString("nextStep"))
        assertFalse(failure.getBoolean("pollRequired"))
        assertFalse(failure.getBoolean("successful"))
        assertTrue(failure.getJSONObject("report").getBoolean("saved"))
    }

    fun testReportsPreserveEveryFindingAndReloadAfterServiceRestart() {
        val cache = Files.createTempDirectory("analysis-restart-reports-")
        val original = UctAnalysisRuns(project, cache)
        val findings = (1..201).map { UctFinding("${project.basePath}/Sample.php", it, 1, "finding $it", SupportedIssue.CALLING_DEPRECATED_METHOD) }
        val request = request().copy(explainSuppressed = true)
        val result = UctAnalysisResult(findings, 3, 1, 0, "{\"indexRevision\":\"fixture\"}", mapOf("php" to 3), 1, mapOf(1439 to 7), mapOf("warning" to 7))
        val run = original.start(request) { result }
        try {
            PlatformTestUtil.waitWithEventsDispatching("archived analysis", { original.results(run.runId).state != "running" }, 10)
            val report = JSONObject(Files.readString(java.nio.file.Path.of(original.reportInfo(run.runId).getString("path"))))
            assertEquals(201, report.getJSONObject("result").getJSONArray("findings").length())
            original.dispose()
            val restarted = UctAnalysisRuns(project, cache)
            try {
                assertEquals(result, restarted.results(run.runId).result)
                assertEquals(request, restarted.results(run.runId).request)
                assertEquals("completed", restarted.cancel(run.runId).state)
            } finally { restarted.dispose() }
        } finally { original.dispose() }
    }

    fun testCompatibilityProviderIsRegisteredOnceAndHelpNeedsNoProject() {
        val exposed = com.intellij.mcpserver.McpToolsProvider.EP.extensionList.flatMap { it.getTools() }
            .filter { it.descriptor.name == "magento_compatibility" }
        assertEquals(1, exposed.size)
        val result = runBlocking { exposed.single().call(Json.parseToJsonElement("""{"mode":"help"}""").jsonObject) }
        assertFalse(result.isError)
        assertNull(result.structuredContent)
        val body = JSONObject(result.content.filterIsInstance<McpToolCallResultContent.Text>().single().text)
        assertTrue(body.getBoolean("terminal"))
        assertTrue(body.getString("documentation").contains("pollRequired"))
    }

    private fun publishedFixture(vararg versions: String) {
        val source = com.magento.idea.magento2uct.analysis.UctPublishedReleases(
            { Settings.DEFAULT_PUBLISHED_RELEASES_URL }, { _ -> JSONArray(versions.map {
                JSONObject().put("tag_name", it).put("draft", false).put("prerelease", false)
                    .put("published_at", "2026-05-12T00:00:00Z")
                    .put("html_url", "https://github.com/magento/magento2/releases/tag/$it")
            }).toString() }, { java.time.Instant.parse("2026-10-10T00:00:00Z") })
        project.getService(com.magento.idea.magento2uct.analysis.UctPublishedReleases::class.java)
        project.replaceService(com.magento.idea.magento2uct.analysis.UctPublishedReleases::class.java, source, testRootDisposable)
    }

    fun testUpgradePresetFollowsCompleteRequestAcrossPreparationAndServiceRestarts() {
        Settings.getInstance(project).pluginEnabled = true
        myFixture.addFileToProject("composer.lock", """{"packages":[{"name":"magento/magento2-base","version":"2.4.8-p5"}]}""")
        myFixture.addFileToProject("Sample.php", "<?php class Sample {}")
        publishedFixture("2.4.8-p5", "2.4.9")
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val downloads = AtomicInteger()
        server.createContext("/") { exchange ->
            downloads.incrementAndGet()
            val bytes = UctReleaseFixture.archive(exchange.requestURI.path.removePrefix("/"))
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        val cache = Files.createTempDirectory("upgrade-workflow-cache-")
        fun restartPreparation(): UctReleasePreparation {
            val service = UctReleasePreparation(project, cache, UctReleaseSource { "http://127.0.0.1:${server.address.port}/$it" })
            Disposer.register(testRootDisposable, service)
            project.replaceService(UctReleasePreparation::class.java, service, testRootDisposable)
            return service
        }
        var preparation = restartPreparation()
        try {
            var response = nativeCall("""{"mode":"upgrade","path":"Sample.php","minimumSeverity":"error"}""")
            assertEquals("2.4.9", response.getJSONObject("releaseDiscovery").getJSONObject("nextMinorRelease").getString("version"))
            for (version in listOf("2.4.8-p5", "2.4.9")) {
                val next = response.getJSONObject("nextCall").getJSONObject("arguments")
                assertEquals("prepare", next.getString("mode"))
                assertEquals(version, next.getString("targetVersion"))
                assertEquals("Sample.php", next.getString("path"))
                assertEquals("error", next.getString("minimumSeverity"))
                assertTrue(next.getBoolean("explainSuppressed"))
                val started = followNative(response.getJSONObject("nextCall"))
                val runId = started.getString("runId")
                PlatformTestUtil.waitWithEventsDispatching("upgrade preparation", { preparation.results(runId).state != "running" }, 20)
                preparation.dispose()
                preparation = restartPreparation()
                val completed = followNative(started.getJSONObject("nextCall"))
                assertEquals("completed", completed.getString("state"))
                response = followNative(completed.getJSONObject("nextCall"))
            }
            assertEquals(2, downloads.get())
            assertTrue(response.getJSONObject("readiness").getBoolean("ready"))
            val next = response.getJSONObject("nextCall").getJSONObject("arguments")
            assertEquals("analyze", next.getString("mode"))
            assertEquals("2.4.8-p5", next.getString("currentVersion"))
            assertEquals("2.4.9", next.getString("targetVersion"))
            assertTrue(next.getBoolean("ignoreCurrentVersion"))
            assertTrue(next.getBoolean("explainSuppressed"))
            val started = followNative(response.getJSONObject("nextCall"))
            PlatformTestUtil.waitWithEventsDispatching("upgrade analysis", { runs().results(started.getString("runId")).state != "running" }, 20)
            val result = followNative(started.getJSONObject("nextCall"))
            assertEquals("done", result.getString("nextStep"))
            assertEquals("upgrade_changes", result.getString("findingsScope"))
            assertEquals("error", result.getString("minimumSeverity"))
            assertEquals(0, result.getJSONObject("summary").getInt("displayedFindings"))
            assertEquals(0, result.getJSONObject("suppression").getInt("suppressedDiagnostics"))
        } finally { server.stop(0) }
    }

    fun testUpgradeExplicitTargetAndOverridesAvoidReleaseDiscovery() {
        Settings.getInstance(project).pluginEnabled = true
        myFixture.addFileToProject("Sample.php", "<?php class Sample {}")
        val result = nativeCall("""{"mode":"upgrade","path":"Sample.php","currentVersion":"2.4.2","targetVersion":"2.4.3","ignoreCurrentVersion":false}""")
        assertTrue(result.isNull("releaseDiscovery"))
        val next = result.getJSONObject("nextCall").getJSONObject("arguments")
        assertEquals("analyze", next.getString("mode"))
        assertFalse(next.getBoolean("ignoreCurrentVersion"))
        assertFalse(next.getBoolean("explainSuppressed"))
        assertEquals("2.4.3", next.getString("targetVersion"))
    }

    fun testUpgradeWithoutANewerReleaseDoesNotStartAJob() {
        publishedFixture("2.4.9")
        val result = nativeCall("""{"mode":"upgrade","path":"Sample.php","currentVersion":"2.4.9"}""")
        assertEquals("no_newer_release", result.getString("outcome"))
        assertEquals("done", result.getString("nextStep"))
        assertTrue(result.isNull("nextCall"))
        assertTrue(runs().activeRunIds().isEmpty())
    }

    fun testUpgradeWithDisabledSupportRequiresEnablingItWithoutStartingWork() {
        Settings.getInstance(project).pluginEnabled = false
        val result = nativeCall("""{"mode":"upgrade","path":"Sample.php","currentVersion":"2.4.2","targetVersion":"2.4.3"}""")
        assertEquals("enable_support", result.getString("nextStep"))
        assertFalse(result.getJSONObject("readiness").getBoolean("ready"))
        assertTrue(result.isNull("nextCall"))
        assertTrue(runs().activeRunIds().isEmpty())
    }

    fun testUpgradeRejectsInvalidScopeAndOptionsBeforeDiscovery() {
        for ((json, parameter) in listOf(
            """{"mode":"upgrade","currentVersion":"2.4.8-p5"}""" to "path",
            """{"mode":"upgrade","path":"Sample.php","moduleName":"Foo_Bar","currentVersion":"2.4.8-p5"}""" to "path",
            """{"mode":"upgrade","path":"Sample.php","currentVersion":"2.4.8-p5","minimumSeverity":"info"}""" to "minimumSeverity",
            """{"mode":"upgrade","path":"Sample.php","currentVersion":"2.4.8-p5","ignoreCurrentVersion":false,"explainSuppressed":true}""" to "explainSuppressed"
        )) {
            val result = nativeCall(json, expectedError = true)
            assertEquals(parameter, result.getJSONObject("error").getString("parameter"))
            assertEquals("resolve_error", result.getString("nextStep"))
            assertTrue(result.isNull("nextCall"))
        }
    }

    fun testScopedStatusPreservesOptionsWhileIndexing() {
        Settings.getInstance(project).pluginEnabled = true
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            val result = nativeCall("""{"mode":"upgrade","moduleName":"Foo_Bar","currentVersion":"2.4.2","targetVersion":"2.4.3","minimumSeverity":"critical"}""")
            assertEquals("poll", result.getString("nextStep"))
            assertFalse(result.getJSONObject("readiness").getBoolean("ready"))
            val next = result.getJSONObject("nextCall").getJSONObject("arguments")
            assertEquals("status", next.getString("mode"))
            assertEquals("Foo_Bar", next.getString("moduleName"))
            assertEquals("critical", next.getString("minimumSeverity"))
            assertTrue(next.getBoolean("explainSuppressed"))
        }
    }

    fun testCoverageRecoveryPreservesModuleAndAllScanOptions() {
        Settings.getInstance(project).pluginEnabled = true
        myFixture.addFileToProject("app/code/Foo/Bar/etc/module.xml", """<config><module name="Foo_Bar"/></config>""")
        myFixture.addFileToProject("app/code/Foo/Bar/registration.php", "<?php \\Magento\\Framework\\Component\\ComponentRegistrar::register('module', 'Foo_Bar', __DIR__);")
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        val result = nativeCall("""{"mode":"analyze","moduleName":"Foo_Bar","currentVersion":"2.4.8-p5","targetVersion":"2.4.9","minimumSeverity":"critical","ignoreCurrentVersion":true,"explainSuppressed":true}""", expectedError = true)
        assertEquals("coverage_required", result.getJSONObject("error").getString("code"))
        val args = result.getJSONObject("nextCall").getJSONObject("arguments")
        assertEquals("Foo_Bar", args.getString("moduleName"))
        assertFalse(args.has("path"))
        assertEquals("critical", args.getString("minimumSeverity"))
        assertTrue(args.getBoolean("explainSuppressed"))
        assertEquals("2.4.9", args.getString("analysisTargetVersion"))
    }

}
