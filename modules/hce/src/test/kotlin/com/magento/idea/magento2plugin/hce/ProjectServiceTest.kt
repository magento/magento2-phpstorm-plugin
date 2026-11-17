/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.hce

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Executors

class ProjectServiceTest {
    @TempDir lateinit var directory: Path
    private fun project(name: String = "project") = Files.createDirectories(directory.resolve(name)).toRealPath()
    private fun store() = SettingsStore(directory.resolve("settings"))
    private fun service() = ProjectService(store())

    @Test fun `status and unavailable discovery do not create settings`() {
        val project = project()
        assertFalse(service().configure(project).getJSONObject("settings").getBoolean("enabled"))
        assertEquals("unconfigured", service().root(project).getString("status"))
        assertEquals("disabled", service().describeEnvironment(project).getString("status"))
        assertFalse(Files.exists(directory.resolve("settings")))
    }

    @Test fun `configuration survives new service and store instances`() {
        val project = project()
        val magento = Files.createDirectories(project.resolve("src"))
        val result = service().configure(project, "configure", "src", true, listOf("./bin/magento", "tools/cli"))
        assertEquals("ok", result.getString("status"))
        assertTrue(result.getBoolean("configurationWritten"))
        val loaded = store().load(project)
        assertTrue(loaded.enabled)
        assertEquals(magento.toString(), loaded.magentoPath)
        assertEquals(listOf("bin/magento", "tools/cli"), loaded.cliToolCandidates)
        assertEquals(magento.toString(), service().root(project).getString("magentoPath"))
    }

    @Test fun `partial updates retain root and wrappers and disabled state survives reload`() {
        val project = project()
        service().configure(project, "configure", ".", true, listOf("tools/cli"))
        service().configure(project, "configure", enabled = false)
        val state = store().load(project)
        assertFalse(state.enabled)
        assertEquals(project.toString(), state.magentoPath)
        assertEquals(listOf("tools/cli"), state.cliToolCandidates)
        assertEquals(2L, state.revision)
        assertEquals("disabled", service().describeEnvironment(project).getString("status"))
    }

    @Test fun `invalid updates leave existing settings bytes unchanged`() {
        val project = project()
        service().configure(project, "configure", ".", true)
        val file = store().settingsFile(project)
        val before = Files.readString(file)
        assertEquals("invalid_request", service().configure(project, "configure", "../outside", true).getString("status"))
        assertEquals("invalid_request", service().configure(project, "configure", cliToolCandidates = listOf("../cli")).getString("status"))
        assertEquals("unsupported_capability", service().configure(project, "configure", excludeGeneratedFiles = false).getString("status"))
        assertEquals(before, Files.readString(file))
    }

    @Test fun `oversized UTF-8 updates preserve readable settings and allow correction`() {
        val project = project()
        service().configure(project, "configure", ".", true)
        val file = store().settingsFile(project)
        val before = Files.readAllBytes(file)
        val previous = store().load(project)
        // Each component fits filesystem limits; UTF-8 bytes exceed the limit even
        // though the serialized character count remains below it.
        val candidates = (1..100).map { "tools/$it/" + List(5) { "é".repeat(100) }.joinToString("/") }
        val serialized = previous.copy(cliToolCandidates = candidates, revision = previous.revision + 1).json().toString(2) + "\n"
        assertTrue(serialized.length < 65_536)
        assertTrue(serialized.toByteArray(Charsets.UTF_8).size > 65_536)
        val result = service().configure(project, "configure", cliToolCandidates = candidates)
        assertEquals("invalid_request", result.getString("status"))
        assertFalse(result.getBoolean("successful"))
        assertFalse(result.getBoolean("configurationWritten"))
        assertArrayEquals(before, Files.readAllBytes(file))
        assertEquals(previous, store().load(project))
        assertEquals("ok", service().configure(project).getString("status"))
        assertEquals("ok", service().configure(project, "configure", cliToolCandidates = listOf("bin/magento")).getString("status"))
        assertEquals(previous.revision + 1, store().load(project).revision)
    }

    @Test fun `missing retained root can be disabled but enabling and explicit replacements are validated`() {
        val project = project()
        val magento = Files.createDirectories(project.resolve("src"))
        service().configure(project, "configure", "src", true, listOf("tools/cli"))
        Files.delete(magento)
        val before = store().load(project)
        assertEquals("invalid_request", service().configure(project, "configure", cliToolCandidates = listOf("bin/magento")).getString("status"))
        assertEquals(before, store().load(project))
        val result = service().configure(project, "configure", enabled = false)
        assertEquals("ok", result.getString("status"))
        assertTrue(result.getBoolean("configurationWritten"))
        val disabled = store().load(project)
        assertEquals(before.copy(enabled = false, revision = before.revision + 1), disabled)
        assertEquals("disabled", service().describeEnvironment(project).getString("status"))
        assertEquals("invalid_request", service().configure(project, "configure", enabled = true).getString("status"))
        assertEquals("invalid_request", service().configure(project, "configure", "missing", false).getString("status"))
        assertEquals(disabled, store().load(project))
        assertEquals("ok", service().configure(project, "configure", ".", true).getString("status"))
    }

    @Test fun `project identities isolate settings and real paths share identity`() {
        val first = project("first")
        val second = project("second")
        service().configure(first, "configure", ".", true)
        assertFalse(store().load(second).enabled)
        assertNotEquals(store().settingsFile(first), store().settingsFile(second))
        assertEquals(store().settingsFile(first), store().settingsFile(first.resolve(".")))
    }

    @Test fun `root cannot escape through a symlink`() {
        val project = project()
        val outside = project("outside")
        Files.createSymbolicLink(project.resolve("external"), outside)
        assertEquals("invalid_request", service().configure(project, "configure", "external", true).getString("status"))
        assertFalse(Files.exists(store().settingsFile(project)))
    }

    @Test fun `corrupt settings are explicit and never replaced by defaults`() {
        val project = project()
        service().configure(project, "configure", ".", true)
        val file = store().settingsFile(project)
        Files.writeString(file, "{broken-private-input")
        val status = service().configure(project)
        assertEquals("invalid_configuration", status.getString("status"))
        assertFalse(status.toString().contains("broken-private-input"))
        assertEquals("invalid_configuration", service().configure(project, "configure", ".", true).getString("status"))
        assertEquals("{broken-private-input", Files.readString(file))
    }

    @Test fun `concurrent independent services serialize updates and report the actual previous revision`() {
        val project = project()
        val executor = Executors.newFixedThreadPool(4)
        try {
            val tasks = (1..24).map { executor.submit {
                val result = service().configure(project, "configure", enabled = false)
                assertTrue(result.getBoolean("successful"), result.toString())
                assertEquals(result.getJSONObject("settings").getLong("revision") - 1,
                    result.getJSONObject("before").getLong("revision"))
            } }
            tasks.forEach { it.get() }
            assertEquals(24L, store().load(project).revision)
        } finally { executor.shutdownNow() }
    }
}
