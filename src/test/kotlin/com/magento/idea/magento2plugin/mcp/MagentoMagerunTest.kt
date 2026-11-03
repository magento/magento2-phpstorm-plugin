package com.magento.idea.magento2plugin.mcp

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.util.Comparator
import java.util.concurrent.TimeUnit

class MagentoMagerunTest {
    @Test
    fun testDdevDiscoveryAndProbesWithoutHostMagerun() = fixture { directory ->
        val root = Files.createDirectories(directory.resolve("project"))
        file(root, ".ddev/config.yaml", "type: magento2\n")
        Files.createDirectories(root.resolve("src"))
        val launcher = file(directory, "host tools/ddev", "fixture", true)
        val env = MagentoMagerunSupport.discover(root, "src", DEFAULTS) { launcher }
        val runner = FakeRunner()
        val service = MagentoMagerunService(runner)
        val status = service.execute(env, MagerunRequest())
        assertEquals("detected", status.getString("status"))
        assertTrue(runner.calls.isEmpty())
        assertEquals(root.resolve("src").toString(), status.getJSONObject("invocation").getString("cwd"))
        assertEquals("ddev exec --raw --service=web -- magerun2 --version", status.getJSONObject("terminalGuidance").getString("command"))
        val result = service.execute(env, MagerunRequest("mcp_config"))
        assertEquals("ok", result.getString("status"))
        val prefix = listOf(launcher.toString(), "exec", "--raw", "--service=web", "--", "magerun2")
        assertTrue(runner.calls.all { it.take(prefix.size) == prefix })
        assertTrue(runner.calls.none { args -> args.any { it.startsWith("--root-dir=") } })
        val server = result.getJSONObject("configuration").getJSONObject("mcpServers").getJSONObject("magento-magerun2")
        assertEquals(prefix.drop(1), server.getJSONArray("args").toList().take(prefix.size - 1))
        val callsBefore = runner.calls.size
        file(root, ".ddev/config.yaml", "type: magento2\nname: changed\n")
        service.execute(env, MagerunRequest("commands"))
        assertEquals(callsBefore + 2, runner.calls.size)
        file(directory, "host tools/ddev", "changed launcher", true)
        service.execute(env, MagerunRequest("commands"))
        assertEquals(callsBefore + 4, runner.calls.size)
    }

    @Test
    fun testMissingDdevLauncherRetainsConfigurationAndTerminalGuidance() = fixture { root ->
        file(root, ".ddev/config.yaml", "type: magento2\n")
        val env = MagentoMagerunSupport.discover(root, null, DEFAULTS) { null }
        val runner = FakeRunner()
        val result = MagentoMagerunService(runner).execute(env, MagerunRequest("commands"))
        assertEquals("missing_launcher", result.getString("status"))
        assertEquals(root.resolve(".ddev/config.yaml").toString(), result.getJSONObject("selected").getString("path"))
        assertTrue(result.getJSONObject("terminalGuidance").getString("message").contains("PhpStorm's Terminal"))
        assertTrue(runner.calls.isEmpty())
    }

    @Test
    fun testDdevFailuresRetainInvocationAndNeverRetryAnotherRuntime() = fixture { root ->
        file(root, ".ddev/config.yaml", "type: magento2\n")
        val launcher = file(root, "host/ddev", "fixture", true)
        val env = MagentoMagerunSupport.discover(root, null, DEFAULTS) { launcher }
        for ((failure, status) in listOf(
            MagerunProcessResult(127, stderr = "magerun2: command not found") to "missing_installation",
            MagerunProcessResult(1, stderr = "project is stopped") to "runtime_unavailable",
            MagerunProcessResult(1, stderr = "permission denied") to "runtime_unavailable",
            MagerunProcessResult(-1, timedOut = true) to "timeout",
            MagerunProcessResult(-1, outputLimited = true) to "output_limit"
        )) {
            var calls = 0
            val service = MagentoMagerunService(MagerunRunner { _, cwd, _ ->
                calls++
                assertEquals(root, cwd)
                failure
            })
            val result = service.execute(env, MagerunRequest("commands"))
            assertEquals(status, result.getString("status"))
            assertEquals("ddev", result.getJSONObject("invocation").getString("runtime"))
            assertEquals(root.toString(), result.getJSONObject("terminalGuidance").getString("cwd"))
            assertEquals(1, calls)
            assertEquals("detected", service.execute(env, MagerunRequest()).getString("status"))
        }
        val unavailable = MagentoMagerunService(MagerunRunner { _, _, _ -> throw java.io.IOException("denied") })
            .execute(env, MagerunRequest("commands"))
        assertEquals("runtime_unavailable", unavailable.getString("status"))
        assertTrue(unavailable.has("terminalGuidance"))
    }

    @Test
    fun testDdevKeepsLocalCandidatePriorityAndRejectsOutsideConfiguration() = fixture { root ->
        file(root, ".ddev/config.yaml", "type: magento2\n")
        php(root, "bin/magerun2")
        val env = MagentoMagerunSupport.discover(root, null, DEFAULTS) { null }
        assertEquals("local", MagentoMagerunSupport.plan(env).runtime)
        val nested = Files.createDirectories(root.resolve("nested"))
        assertTrue(MagentoMagerunSupport.discover(nested, null, DEFAULTS) {
            throw AssertionError("Must not resolve DDEV without in-project configuration")
        }.candidates.isEmpty())
        Files.createSymbolicLink(nested.resolve(".ddev"), root.resolve(".ddev"))
        assertTrue(MagentoMagerunSupport.discover(nested, null, DEFAULTS) { null }.candidates.isEmpty())
    }

    @Test
    fun testStatusNeverRunsAProcess() = fixture { root ->
        php(root, "bin/n98-magerun2")
        val runner = FakeRunner()
        val result = MagentoMagerunService(runner).execute(environment(root), MagerunRequest())
        assertEquals("detected", result.getString("status"))
        assertFalse(result.getBoolean("runtimeProbed"))
        assertFalse(result.getJSONObject("selected").getBoolean("identityVerified"))
        assertTrue(runner.calls.isEmpty())
    }

    @Test
    fun testRecognizesPharComposerAndModernAliasLayouts() = fixture { root ->
        for (name in listOf("n98-magerun2.phar", "bin/n98-magerun2.phar", "vendor/bin/n98-magerun2", "bin/magerun2")) {
            val project = Files.createDirectories(root.resolve(name.replace('/', '_')))
            php(project, name, executable = false)
            val env = environment(project)
            assertEquals(project.resolve(name), env.selected?.path)
            val plan = MagentoMagerunSupport.plan(env)
            assertEquals(listOf("php", project.resolve(name).toString(), "--root-dir=$project"), plan.argv)
        }
    }

    @Test
    fun testDockerDirectoryBeatsNestedInstallationEvenWithDifferentAlias() = fixture { root ->
        file(root, "compose.yaml", "services: {}")
        php(root, "bin/magerun2")
        php(root, "src/bin/n98-magerun2")
        php(root, "src/vendor/bin/n98-magerun2")
        val env = environment(root, "src")
        assertEquals(root.resolve("bin/magerun2"), env.selected?.path)
        assertEquals(root, env.selected?.cwd)
        assertEquals(env.selected?.displayPath, MagentoCliToolQueries.discoverTools(root, listOf(".", "src"), DEFAULTS)
            .first { MagentoMagerunSupport.isMagerunName(it.name) }.command)
    }

    @Test
    fun testPrefersWrapperOverPharAndComposerInSameDirectory() = fixture { root ->
        php(root, "vendor/bin/n98-magerun2")
        php(root, "n98-magerun2.phar")
        php(root, "bin/n98-magerun2")
        assertEquals(root.resolve("bin/n98-magerun2"), environment(root).selected?.path)
    }

    @Test
    fun testReportsAndRejectsMagentoOneAliasBeforeListingCommands() = fixture { root ->
        php(root, "bin/n98-magerun")
        val runner = FakeRunner(version = "n98-magerun version 3.0.0")
        val result = MagentoMagerunService(runner).execute(environment(root), MagerunRequest("commands"))
        assertEquals("incompatible_tool", result.getString("status"))
        assertEquals(1, runner.calls.size)
    }

    @Test
    fun testVerifiesModernIdentityBehindAmbiguousAlias() = fixture { root ->
        php(root, "bin/magerun")
        val result = MagentoMagerunService(FakeRunner()).execute(environment(root), MagerunRequest("commands"))
        assertTrue(result.getJSONObject("selected").getBoolean("identityVerified"))
        assertEquals("n98-magerun2", result.getJSONObject("selected").getString("family"))
    }

    @Test
    fun testMarkShustBootstrapIsNeverExecuted() = fixture { root ->
        docker(root)
        val runner = FakeRunner()
        val env = environment(root, "src")
        val result = MagentoMagerunService(runner).execute(env, MagerunRequest("commands"))
        assertEquals("ok", result.getString("status"))
        assertEquals(listOf(root.resolve("bin/clinotty").toString(), "test", "-f", "bin/n98-magerun2.phar"), runner.calls.first())
        assertTrue(runner.calls.all { it.first() == root.resolve("bin/clinotty").toString() })
        assertTrue(runner.calls.none { root.resolve("bin/n98-magerun2").toString() in it || "curl" in it || "up" in it })
        assertTrue(runner.calls.drop(1).all { it.take(3) == listOf(root.resolve("bin/clinotty").toString(), "php", "bin/n98-magerun2.phar") })
        assertTrue(runner.calls.none { command -> command.any { it.startsWith("--root-dir=") } })
    }

    @Test
    fun testMissingContainerPharDoesNotDownloadOrUseFallback() = fixture { root ->
        docker(root)
        php(root, "src/bin/n98-magerun2")
        val runner = FakeRunner(preflight = MagerunProcessResult(1))
        val result = MagentoMagerunService(runner).execute(environment(root, "src"), MagerunRequest("commands"))
        assertEquals("missing_installation", result.getString("status"))
        assertEquals(1, runner.calls.size)
    }

    @Test
    fun testStoppedContainerDoesNotStartServicesOrUseHost() = fixture { root ->
        docker(root)
        php(root, "src/bin/n98-magerun2")
        val runner = FakeRunner(preflight = MagerunProcessResult(1, stderr = "service phpfpm is not running"))
        val result = MagentoMagerunService(runner).execute(environment(root, "src"), MagerunRequest("commands"))
        assertEquals("runtime_unavailable", result.getString("status"))
        assertEquals(1, runner.calls.size)
    }

    @Test
    fun testUnknownShellWrapperIsNotRun() = fixture { root ->
        file(root, "bin/n98-magerun2", "#!/bin/sh\ncurl example.invalid/tool > tool\nexec ./tool \"\$@\"\n", true)
        val runner = FakeRunner()
        val result = MagentoMagerunService(runner).execute(environment(root), MagerunRequest("commands"))
        assertEquals("unsupported_wrapper", result.getString("status"))
        assertTrue(runner.calls.isEmpty())
    }

    @Test
    fun testPureLocalForwarderKeepsOriginalWrapperAndWorkingDirectory() = fixture { root ->
        file(root, "bin/magerun", "#!/bin/sh\nexec ./vendor/bin/n98-magerun2 \"\$@\"\n", true)
        php(root, "vendor/bin/n98-magerun2")
        val plan = MagentoMagerunSupport.plan(environment(root))
        assertEquals(root.resolve("bin/magerun"), plan.source)
        assertEquals(listOf(root.resolve("vendor/bin/n98-magerun2").toString(), "--root-dir=$root"), plan.argv.takeLast(2))
        assertEquals(root, plan.cwd)
    }

    @Test
    fun testSymlinkOutsideProjectIsNotDiscovered() = fixture { directory ->
        val root = Files.createDirectories(directory.resolve("project"))
        val outside = php(directory, "n98-magerun2.phar")
        Files.createSymbolicLink(root.resolve("n98-magerun2.phar"), outside)
        assertTrue(environment(root).candidates.isEmpty())
    }

    @Test
    fun testCommandCatalogueIsSortedFilteredAndPagedIncludingCustomCommands() = fixture { root ->
        php(root, "bin/n98-magerun2")
        val runner = FakeRunner()
        val service = MagentoMagerunService(runner)
        val first = service.execute(environment(root), MagerunRequest("commands", limit = 1))
        assertEquals("mcp:server:start", first.getJSONArray("commands").getJSONObject(0).getString("name"))
        assertEquals(1, first.getInt("nextOffset"))
        val custom = service.execute(environment(root), MagerunRequest("commands", search = "custom"))
        assertEquals(1, custom.getInt("total"))
        assertEquals("shop:custom", custom.getJSONArray("commands").getJSONObject(0).getString("name"))
        assertTrue(custom.getBoolean("cached"))
        assertEquals(2, runner.calls.size)
    }

    @Test
    fun testHelpResolvesAliasAndReturnsStructuredSchemaWithoutExecutingCommand() = fixture { root ->
        php(root, "bin/n98-magerun2")
        val runner = FakeRunner()
        val service = MagentoMagerunService(runner)
        val result = service.execute(environment(root), MagerunRequest("command_help", command = "si"))
        assertEquals("sys:info", result.getJSONObject("command").getString("name"))
        assertTrue(result.getJSONObject("command").getJSONObject("definition").has("options"))
        assertEquals(listOf("help", "sys:info", "--format=json", "--no-ansi", "--no-interaction"), runner.calls.last().takeLast(5))
        service.execute(environment(root), MagerunRequest("command_help", command = "sys:info"))
        assertEquals(3, runner.calls.size)
    }

    @Test
    fun testRejectsArgumentsAndShellSyntaxBeforeAnyProcessStarts() = fixture { root ->
        php(root, "bin/n98-magerun2")
        for (command in listOf("--version", "sys:info;touch /tmp/bad", "db:query SELECT 1", "sys:info\nlist")) {
            val runner = FakeRunner()
            val result = MagentoMagerunService(runner).execute(environment(root), MagerunRequest("command_help", command = command))
            assertEquals("invalid_request", result.getString("status"))
            assertTrue(runner.calls.isEmpty())
        }
    }

    @Test
    fun testUnknownCommandIsNotExecutedOrAskedForHelp() = fixture { root ->
        php(root, "bin/n98-magerun2")
        val runner = FakeRunner()
        val result = MagentoMagerunService(runner).execute(environment(root), MagerunRequest("command_help", command = "shop:missing"))
        assertEquals("unknown_command", result.getString("status"))
        assertEquals(2, runner.calls.size)
    }

    @Test
    fun testCacheExpiresAndRefreshClearsStatusWithoutProbing() = fixture { root ->
        php(root, "bin/n98-magerun2")
        var now = 0L
        val runner = FakeRunner()
        val service = MagentoMagerunService(runner) { now }
        service.execute(environment(root), MagerunRequest("commands"))
        val status = service.execute(environment(root), MagerunRequest("status"))
        assertEquals("cached", status.getString("status"))
        assertTrue(status.getJSONObject("selected").getBoolean("identityVerified"))
        assertEquals(2, runner.calls.size)
        now += TimeUnit.MINUTES.toNanos(5)
        service.execute(environment(root), MagerunRequest("commands"))
        assertEquals(4, runner.calls.size)
        assertEquals("detected", service.execute(environment(root), MagerunRequest("status", refresh = true)).getString("status"))
        assertEquals(4, runner.calls.size)
        service.execute(environment(root), MagerunRequest("commands", refresh = true))
        assertEquals(6, runner.calls.size)
    }

    @Test
    fun testComposerChangesAndDifferentProjectsInvalidateCache() = fixture { root ->
        php(root, "bin/n98-magerun2")
        val runner = FakeRunner()
        val service = MagentoMagerunService(runner)
        service.execute(environment(root), MagerunRequest("commands"))
        file(root, "composer.lock", "{}")
        service.execute(environment(root), MagerunRequest("commands"))
        assertEquals(4, runner.calls.size)
        val other = Files.createDirectories(root.resolve("other"))
        php(other, "bin/n98-magerun2")
        service.execute(environment(other), MagerunRequest("commands"))
        assertEquals(6, runner.calls.size)
    }

    @Test
    fun testMalformedOrPollutedJsonIsNotCached() = fixture { root ->
        php(root, "bin/n98-magerun2")
        for (output in listOf("not json", "NOTICE\n{}", "{\"commands\":[]} trailing", "{\"commands\":[]}")) {
            val runner = FakeRunner(catalogue = output)
            val service = MagentoMagerunService(runner)
            assertEquals("invalid_output", service.execute(environment(root), MagerunRequest("commands")).getString("status"))
            assertEquals("detected", service.execute(environment(root), MagerunRequest()).getString("status"))
        }
    }

    @Test
    fun testTimeoutOutputLimitAndFailureDoNotUseFallback() = fixture { root ->
        php(root, "bin/n98-magerun2", executable = false)
        php(root, "vendor/bin/n98-magerun2")
        for ((failure, expected) in listOf(MagerunProcessResult(-1, timedOut = true) to "timeout",
            MagerunProcessResult(-1, outputLimited = true) to "output_limit", MagerunProcessResult(2, stderr = "failed") to "probe_failed")) {
            val calls = mutableListOf<List<String>>()
            val runner = MagerunRunner { args, _, _ -> calls.add(args); failure }
            assertEquals(expected, MagentoMagerunService(runner).execute(environment(root), MagerunRequest("commands")).getString("status"))
            assertEquals(1, calls.size)
            assertEquals(listOf("php", root.resolve("bin/n98-magerun2").toString()), calls.single().take(2))
        }
    }

    @Test
    fun testGeneratesNativeMcpConfigurationWithoutStartingServer() = fixture { root ->
        docker(root)
        val runner = FakeRunner()
        val result = MagentoMagerunService(runner).execute(environment(root, "src"), MagerunRequest("mcp_config"))
        assertEquals("ok", result.getString("status"))
        assertFalse(result.getBoolean("serverStarted"))
        assertFalse(result.getBoolean("configurationWritten"))
        val server = result.getJSONObject("configuration").getJSONObject("mcpServers").getJSONObject("magento-magerun2")
        assertEquals(root.resolve("bin/clinotty").toString(), server.getString("command"))
        assertEquals(root.toString(), server.getString("cwd"))
        val args = server.getJSONArray("args").toList()
        assertEquals(listOf("php", "bin/n98-magerun2.phar"), args.take(2))
        assertTrue("mcp:server:start" in args)
        assertTrue("--include=sys:info" in args)
        assertEquals("help", runner.calls.last()[3])
    }

    @Test
    fun testNativeMcpIsCapabilityGatedAndFiltersAreSeparateArguments() = fixture { root ->
        php(root, "bin/n98-magerun2")
        val missing = FakeRunner(catalogue = CATALOGUE.replace("mcp:server:start", "other:command"))
        assertEquals("native_mcp_unavailable", MagentoMagerunService(missing).execute(environment(root), MagerunRequest("mcp_config")).getString("status"))
        val runner = FakeRunner()
        val result = MagentoMagerunService(runner).execute(environment(root),
            MagerunRequest("mcp_config", include = "@read-only sys:*", exclude = "@unsafe"))
        val args = result.getJSONObject("configuration").getJSONObject("mcpServers").getJSONObject("magento-magerun2").getJSONArray("args").toList()
        assertTrue("--include=@read-only sys:*" in args)
        assertTrue("--exclude=@unsafe" in args)
    }

    private class FakeRunner(val version: String = "n98-magerun2 9.3.0 by netz98", val catalogue: String = CATALOGUE,
                             val preflight: MagerunProcessResult = MagerunProcessResult(0)) : MagerunRunner {
        val calls = mutableListOf<List<String>>()
        override fun run(argv: List<String>, cwd: Path, checkCancelled: () -> Unit): MagerunProcessResult {
            calls.add(argv)
            return when {
                "test" in argv -> preflight
                "--version" in argv -> MagerunProcessResult(0, version)
                "list" in argv -> MagerunProcessResult(0, catalogue)
                "help" in argv -> MagerunProcessResult(0, JSONObject().put("name", argv[argv.indexOf("help") + 1])
                    .put("definition", JSONObject().put("arguments", JSONObject()).put("options", JSONObject()
                        .put("include", JSONObject().put("name", "--include")).put("exclude", JSONObject().put("name", "--exclude")))).toString())
                else -> throw AssertionError("Unexpected executable command: $argv")
            }
        }
    }

    private fun environment(root: Path, magento: String? = null) = MagentoMagerunSupport.discover(root, magento, DEFAULTS)
    private fun php(root: Path, name: String, executable: Boolean = true) = file(root, name, "#!/usr/bin/env php\n<?php\n", executable)
    private fun file(root: Path, name: String, content: String, executable: Boolean = false): Path {
        val path = root.resolve(name)
        Files.createDirectories(path.parent)
        Files.writeString(path, content)
        if (executable) assertTrue(path.toFile().setExecutable(true))
        return path
    }
    private fun docker(root: Path) {
        file(root, "compose.yaml", "services: {}")
        Files.createDirectories(root.resolve("src"))
        val script = javaClass.getResourceAsStream("/magerun/mark-shust-wrapper")!!.bufferedReader().use { it.readText() }
        file(root, "bin/n98-magerun2", script, true)
        file(root, "bin/clinotty", "#!/bin/sh\nbin/docker-compose exec -T phpfpm \"\$@\"\n", true)
    }
    private fun fixture(test: (Path) -> Unit) {
        val directory = Files.createTempDirectory("magento-magerun-test")
        try { test(directory) } finally {
            Files.walk(directory).use { paths -> paths.sorted(Comparator.reverseOrder()).forEach { Files.deleteIfExists(it) } }
        }
    }
    companion object {
        private val DEFAULTS = listOf("bin/magento", "bin/n98-magerun2", "bin/n98-magerun", "bin/magerun")
        private val CATALOGUE = """{"application":{"name":"n98-magerun2","version":"9.3.0"},"commands":[
            {"name":"sys:info","description":"System info","usage":["sys:info","si"]},
            {"name":"shop:custom","description":"Project custom command","usage":["shop:custom"]},
            {"name":"mcp:server:start","description":"MCP server","usage":["mcp:server:start"]}
        ]}"""
    }
}
