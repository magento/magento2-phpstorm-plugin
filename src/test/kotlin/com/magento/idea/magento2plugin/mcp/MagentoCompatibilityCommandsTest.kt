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
    private fun call(mode: String, json: String = "{}"): JSONObject =
        JSONObject(MagentoCompatibilityCommands.execute(project, mode, JSONObject(json)))

    private fun request() = UctAnalysisRequest(listOf(project.basePath!!), null, "2.4.3", IssueSeverityLevel.WARNING, false)
    private fun runs() = project.getService(UctAnalysisRuns::class.java)

    private fun nativeTool(): McpTool {
        val asTools = Class.forName("com.intellij.mcpserver.impl.util.ToolsetReflection_utilKt")
            .getMethod("asTools", McpToolset::class.java, Json::class.java)
        @Suppress("UNCHECKED_CAST")
        val tools = asTools.invoke(null, MagentoMcpToolset(), Json.Default) as List<McpTool>
        return tools.single { it.descriptor.name == "magento_compatibility" }
    }

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
        assertEquals(Json.parseToJsonElement(text), result.structuredContent)
        // Exercise the actual transport adapter: error structuredContent is intentionally omitted by JetBrains.
        val wire = transportResult(result)
        val wireText = wire.content.filterIsInstance<TextContent>().single().text
        assertEquals(text, wireText)
        assertEquals(expectedError, wire.isError)
        if (wire.structuredContent != null) assertEquals(Json.parseToJsonElement(wireText), wire.structuredContent)
        return JSONObject(wireText)
    }

    private fun followNative(next: JSONObject): JSONObject {
        val arguments = JSONObject(next.getJSONObject("arguments").toString())
        assertEquals(project.basePath, arguments.remove("projectPath"))
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
            val after = Files.walk(java.nio.file.Path.of(project.basePath!!)).use { it.sorted().toList() }
            assertEquals(before, after)
        } finally { server.stop(0) }
    }

    /** Explicit opt-in: fetches real upstream releases through native MCP, never shell scripts. */
    fun testNativeMcpWithActualMagentoReleases() {
        check(System.getenv("MAGENTO_MCP_REAL_RELEASE_TEST") == "true") { "Enable MAGENTO_MCP_REAL_RELEASE_TEST=true for this network integration test." }
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
        for ((name, type) in listOf("moduleName" to "string", "targetVersion" to "string", "analysisTargetVersion" to "string", "runId" to "string", "limit" to "integer", "ignoreCurrentVersion" to "boolean")) {
            assertTrue(name, properties.has(name))
            assertTrue(properties.getJSONObject(name).toString(), properties.getJSONObject(name).toString().contains(type))
        }
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
            assertTrue(call("status").getBoolean("indexing"))
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

    fun testValidationRecoveryPreservesOriginalVersionOptions() {
        Settings.getInstance(project).pluginEnabled = true
        myFixture.addFileToProject("Sample.php", "<?php class Sample {}")
        val rejected = nativeCall("""{"mode":"analyze","path":"Sample.php","targetVersion":"2.4.3","currentVersion":"2.4.2","ignoreCurrentVersion":true,"minimumSeverity":"info"}""", expectedError = true)
        val next = rejected.getJSONObject("nextCall").getJSONObject("arguments")
        assertEquals("status", next.getString("mode"))
        assertEquals("2.4.3", next.getString("targetVersion"))
        assertEquals("2.4.2", next.getString("currentVersion"))
        assertTrue(next.getBoolean("ignoreCurrentVersion"))
        assertFalse(next.has("minimumSeverity"))
        assertTrue(followNative(rejected.getJSONObject("nextCall")).getJSONObject("readiness").getBoolean("ready"))
        assertTrue(runs().activeRunIds().isEmpty())

        val preparation = nativeCall("""{"mode":"prepare","targetVersion":"latest","analysisTargetVersion":"2.4.9","currentVersion":"2.4.8-p5","ignoreCurrentVersion":true}""", expectedError = true)
        val recovery = preparation.getJSONObject("nextCall").getJSONObject("arguments")
        assertEquals("2.4.9", recovery.getString("targetVersion"))
        assertEquals("2.4.8-p5", recovery.getString("currentVersion"))
        assertTrue(recovery.getBoolean("ignoreCurrentVersion"))
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
        val result = runBlocking { MagentoMcpToolset().magentoCompatibility() }
        assertTrue(result.isError)
        val text = result.content.filterIsInstance<McpToolCallResultContent.Text>().single().text
        assertEquals(Json.parseToJsonElement(text), result.structuredContent)
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
            "{\"path\":\"Sample.php\",\"targetVersion\":\"99.0.0\"}",
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
        val next = first.getJSONObject("nextCall").getJSONObject("arguments")
        assertEquals(project.basePath, next.getString("projectPath"))
        assertEquals("results", next.getString("mode"))
        next.remove("mode")
        next.remove("projectPath")
        assertEquals(second.getJSONArray("findings").toString(), call("results", next.toString()).getJSONArray("findings").toString())
        assertTrue(second.isNull("nextCall"))
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
