/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.util.SystemInfoRt
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class MagentoDdevEnvironmentTest {
    @Test
    fun findsFirstExecutableOnPathAndSkipsInvalidEntries() = fixture { root ->
        val name = if (SystemInfoRt.isWindows) "ddev.exe" else "ddev"
        Files.createDirectories(root.resolve("directory/$name"))
        val first = file(root, "first tools/$name", "fixture")
        val second = file(root, "second tools/$name", "fixture")
        assertTrue(first.toFile().setExecutable(true))
        assertTrue(second.toFile().setExecutable(true))
        val entries = listOf("", "relative", "invalid\u0000path", root.resolve("missing").toString(),
            root.resolve("directory").toString(), first.parent.toString(), second.parent.toString())
        assertEquals(first, MagentoDdevEnvironment.findExecutable(entries.joinToString(File.pathSeparator)))
        Files.delete(first)
        assertEquals(second, MagentoDdevEnvironment.findExecutable(entries.joinToString(File.pathSeparator)))
        if (SystemInfoRt.isWindows) {
            assertEquals(second, MagentoDdevEnvironment.findExecutable("\"${second.parent}\""))
        }
    }

    @Test
    fun missingPathDoesNotSelectAnExecutable() {
        assertNull(MagentoDdevEnvironment.findExecutable(null))
        assertNull(MagentoDdevEnvironment.findExecutable(""))
        assertNull(MagentoDdevEnvironment.findExecutable("relative"))
    }

    @Test
    fun skipsNonExecutableFilesOnPath() = fixture { root ->
        org.junit.Assume.assumeFalse("POSIX executable permissions", SystemInfoRt.isWindows)
        val blocked = file(root, "blocked/ddev", "fixture")
        assertTrue(blocked.toFile().setExecutable(false, false))
        val executable = file(root, "available/ddev", "fixture")
        assertTrue(executable.toFile().setExecutable(true))
        assertEquals(executable, MagentoDdevEnvironment.findExecutable(
            listOf(blocked.parent, executable.parent).joinToString(File.pathSeparator)))
    }

    @Test
    fun parsesRuntimeSettingsAndBuildsGeneralCommandPrefix() = fixture { root ->
        file(root, ".ddev/config.yaml", """
            name: 'commerce-local' # project name
            type: magento2
            docroot: src/pub
            composer_root: src
            php_version: "8.3"
            nodejs_version: "22"
            webserver_type: nginx-fpm
            database: {type: mariadb, version: "10.11"}
            working_dir: {web: '/var/www/html/src with spaces', db: /tmp}
            web_environment: [PRIVATE_TOKEN=do-not-export]
            hooks:
              post-start:
                - exec: 'echo do-not-run-or-export'
        """.trimIndent())
        Files.createDirectories(root.resolve("src"))
        val runtime = MagentoDdevEnvironment.discover(root, root.resolve("src")) { null }!!
        assertEquals("commerce-local", runtime.configuration.string("name"))
        assertEquals("src/pub", runtime.configuration.string("docroot"))
        assertEquals("10.11", runtime.configuration.string("database.version"))
        assertEquals(root.resolve(".ddev/config.yaml"), runtime.configuration.sources["working_dir.web"])
        assertEquals(listOf("exec", "--raw", "--service=web", "--project=commerce-local",
            "--dir=/var/www/html/src with spaces", "--", "php", "bin/magento", "list"),
            runtime.execArguments(listOf("php", "bin/magento", "list")))
        assertEquals(listOf("exec", "--raw", "--service=db", "--project=commerce-local", "--dir=/tmp", "--", "pwd"),
            runtime.execArguments(listOf("pwd"), "db"))
        val json = runtime.json()
        assertEquals("missing_launcher", json.getString("status"))
        assertEquals(root.resolve("src").toString(), json.getString("cwd"))
        assertFalse(json.toString().contains("do-not-"))
        assertTrue(json.getJSONObject("terminalGuidance").getString("command").endsWith("-- php -v"))
        assertTrue(json.getJSONObject("configuration").getString("coverage").contains("running-container state"))
    }

    @Test
    fun listsProjectCommandsWithDdevFileRulesAndDirectives() = fixture { root ->
        file(root, ".ddev/config.yaml", "name: shop\ntype: magento2\n")
        file(root, ".ddev/commands/web/test-integration", """
            #!/usr/bin/env bash
            ## Description: Run Magento integration tests
            ## Usage: test-integration <path> [phpunit options]
            ## Example: ddev test-integration dev/tests/integration\nddev test-integration app/code
            ## Aliases: ti, integration
            ## OSTypes: darwin,linux
            phpunit "${'$'}@"
        """.trimIndent())
        file(root, ".ddev/commands/host/perf-check", "#!/usr/bin/env bash\n## Description: 'Check changed files'\n")
        file(root, ".ddev/commands/web/plain", "#!/usr/bin/env bash\necho no directives\n")
        file(root, ".ddev/commands/web/README.txt", "## Description: not a command\n")
        file(root, ".ddev/commands/web/sample.example", "## Description: not a command\n")
        file(root, ".ddev/commands/web/.hidden", "## Description: not a command\n")
        file(root, ".ddev/commands/.hidden-service/tool", "## Description: not a command\n")
        file(root, ".ddev/commands/web/drupal-only", "## ProjectTypes: drupal10,drupal11\n")
        file(root, ".ddev/commands/web/windows", "## Description: x\r\n")
        file(root, ".ddev/commands/worker/test-integration", "## Description: a later service folder does not replace it\n")
        val json = MagentoDdevEnvironment.discover(root, root) { null }!!.json().getJSONObject("projectCommands")
        val commands = (0 until json.getJSONArray("commands").length()).map { json.getJSONArray("commands").getJSONObject(it) }
        assertEquals(listOf("test-integration", "perf-check", "plain").sorted(), commands.map { it.getString("name") }.sorted())
        val integration = commands.single { it.getString("name") == "test-integration" }
        assertEquals("web", integration.getString("service"))
        assertEquals("container", integration.getString("runsOn"))
        assertEquals("ddev test-integration", integration.getString("invocation"))
        assertEquals(".ddev/commands/web/test-integration", integration.getString("path"))
        assertEquals("Run Magento integration tests", integration.getString("description"))
        assertEquals("test-integration <path> [phpunit options]", integration.getString("usage"))
        assertEquals("ddev test-integration dev/tests/integration\nddev test-integration app/code", integration.getString("example"))
        assertEquals(listOf("ti", "integration"), integration.getJSONArray("aliases").toList())
        assertEquals("darwin,linux", integration.getJSONObject("unevaluatedConditions").getString("OSTypes"))
        val host = commands.single { it.getString("name") == "perf-check" }
        assertEquals("host", host.getString("runsOn"))
        assertEquals("Check changed files", host.getString("description"))
        val plain = commands.single { it.getString("name") == "plain" }
        assertEquals("plain", plain.getString("description"))
        assertEquals("plain [flags] [args]", plain.getString("usage"))
        assertEquals(listOf("web/windows: CRLF line endings, which DDEV skips"), json.getJSONArray("skipped").toList())
        assertFalse(json.getBoolean("truncated"))
    }

    @Test
    fun projectCommandsStayInsideTheProjectAndAreBounded() = fixture { directory ->
        val root = Files.createDirectories(directory.resolve("project"))
        file(root, ".ddev/config.yaml", "type: magento2\n")
        assertEquals(0, MagentoDdevEnvironment.discover(root, root) { null }!!.json()
            .getJSONObject("projectCommands").getJSONArray("commands").length())
        val outside = file(directory, "outside/secret", "## Description: outside\n")
        Files.createDirectories(root.resolve(".ddev/commands/web"))
        if (!SystemInfoRt.isWindows) Files.createSymbolicLink(root.resolve(".ddev/commands/web/a-linked"), outside)
        for (index in 0 until 101) file(root, ".ddev/commands/web/command-%03d".format(index), "#!/bin/sh\n")
        val json = MagentoDdevEnvironment.discover(root, root) { null }!!.json().getJSONObject("projectCommands")
        assertEquals(100, json.getJSONArray("commands").length())
        assertTrue(json.getBoolean("truncated"))
        assertFalse(json.toString().contains("secret"))
        if (!SystemInfoRt.isWindows) assertTrue(json.getJSONArray("skipped").toList().contains("web/a-linked: unreadable or outside the project"))
    }

    @Test
    fun mergesOrderedOverridesAndRetainsFieldEvidence() = fixture { root ->
        file(root, ".ddev/config.yaml", "name: base\ndocroot: pub\nphp_version: '8.3'\nworking_dir: {web: /original, db: /database}\n")
        file(root, ".ddev/config.z.yaml", "name: final\nworking_dir: {web: '/final path'}\n")
        file(root, ".ddev/config.a.yml", "name: first\nphp_version: '8.4'\nworking_dir: {web: /first}\n")
        val result = MagentoDdevConfiguration.read(root, root.resolve(".ddev/config.yaml"))
        assertNull(result.error)
        assertEquals("final", result.string("name"))
        assertEquals("8.4", result.string("php_version"))
        assertEquals("pub", result.string("docroot"))
        assertEquals("/final path", result.string("working_dir.web"))
        assertEquals("/database", result.string("working_dir.db"))
        assertEquals(root.resolve(".ddev/config.z.yaml"), result.sources["name"])
        assertEquals(root.resolve(".ddev/config.a.yml"), result.sources["php_version"])
        file(root, ".ddev/config.zz.yaml", "override_config: true\nworking_dir: {web: ''}\n")
        val runtime = MagentoDdevEnvironment.discover(root, root) { null }!!
        assertFalse(runtime.execArguments(listOf("php", "-v")).any { it.startsWith("--dir=") })
    }

    @Test
    fun malformedOrUnsupportedConfigurationDoesNotProduceInvocation() = fixture { root ->
        val invalid = listOf("name: [unterminated", "- list", "name: first\nname: second",
            "working_dir: [wrong]", "working_dir: {web: [wrong]}",
            "name: !!java.net.URL ['https://example.invalid']", "name: 'a\nb'\nworking_dir: {web: true}")
        for (body in invalid) {
            file(root, ".ddev/config.yaml", body)
            val runtime = MagentoDdevEnvironment.discover(root, root) { null }!!
            assertNotNull(body, runtime.configuration.error)
            assertEquals("invalid_configuration", runtime.json().getString("status"))
            assertFalse(runtime.json().has("execPrefix"))
            val runner = MagerunRunner { _, _, _ -> throw AssertionError("Invalid configuration must not execute") }
            val result = MagentoMagerunService(runner).execute(
                MagentoMagerunSupport.discover(root, null, emptyList()) { null }, MagerunRequest("commands"))
            assertEquals("invalid_configuration", result.getString("status"))
            assertFalse(result.has("invocation"))
        }
    }

    @Test
    fun boundsConfigurationReadsAndRejectsOutsideOverrides() = fixture { directory ->
        val root = Files.createDirectories(directory.resolve("project"))
        file(root, ".ddev/config.yaml", "name: local\n")
        val outside = file(directory, "outside.yaml", "name: outside\n")
        Files.createSymbolicLink(root.resolve(".ddev/config.local.yaml"), outside)
        assertNotNull(MagentoDdevConfiguration.read(root, root.resolve(".ddev/config.yaml")).error)
        Files.delete(root.resolve(".ddev/config.local.yaml"))
        file(root, ".ddev/config.local.yaml", "#" + "x".repeat(262_144))
        assertTrue(MagentoDdevConfiguration.read(root, root.resolve(".ddev/config.yaml")).error!!.contains("size"))
    }

    private fun file(root: Path, path: String, text: String): Path {
        val file = root.resolve(path)
        Files.createDirectories(file.parent)
        return Files.writeString(file, text)
    }

    private fun fixture(test: (Path) -> Unit) {
        val directory = Files.createTempDirectory("magento-ddev")
        try { test(directory) } finally {
            Files.walk(directory).use { paths -> paths.sorted(Comparator.reverseOrder()).forEach(Files::deleteIfExists) }
        }
    }
}
