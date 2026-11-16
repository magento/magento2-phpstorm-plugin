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

class CliEnvironmentTest {
    @TempDir lateinit var directory: Path
    private fun file(root: Path, name: String, body: String, executable: Boolean = false): Path {
        val file = root.resolve(name)
        Files.createDirectories(file.parent)
        Files.writeString(file, body)
        if (executable) assertTrue(file.toFile().setExecutable(true))
        return file
    }
    private fun service() = ProjectService(SettingsStore(directory.resolve("settings")))

    @Test fun `docker wrappers have priority without executing scripts or changing files`() {
        val root = Files.createDirectories(directory.resolve("project")).toRealPath()
        file(root, "compose.yaml", "services: {}")
        file(root, "src/bin/magento", "#!/bin/sh\ntouch ../executed", true)
        file(root, "bin/magento", "#!/bin/sh\ntouch executed", true)
        service().configure(root, "configure", "src", true)
        val result = service().describeEnvironment(root)
        val text = result.getString("description")
        assertEquals("ok", result.getString("status"))
        assertTrue(text.contains("./bin/magento\n  type: Magento CLI wrapper\n  selection: preferred"), text)
        assertTrue(text.contains("fallback for ./bin/magento"), text)
        assertTrue(text.contains("working directory: $root"), text)
        assertFalse(result.getBoolean("runtimeProbed"))
        assertFalse(result.getBoolean("configurationWritten"))
        assertFalse(Files.exists(root.resolve("executed")))
        assertEquals(result.toString(), service().describeEnvironment(root).toString())
    }

    @Test fun `without compose discovery uses the configured Magento directory`() {
        val root = Files.createDirectories(directory.resolve("project")).toRealPath()
        file(root, "bin/magento", "#!/bin/sh", true)
        file(root, "src/bin/magento", "#!/bin/sh", true)
        service().configure(root, "configure", "src", true)
        val text = service().describeEnvironment(root).getString("description")
        assertTrue(text.contains("./src/bin/magento"))
        assertFalse(text.contains("fallback for ./bin/magento"))
        assertTrue(text.contains("Preferred CLI directory: ${root.resolve("src")}"))
    }

    @Test fun `DDEV overrides and malformed updates are reflected without exporting secrets`() {
        val root = Files.createDirectories(directory.resolve("project")).toRealPath()
        Files.createDirectories(root.resolve("src"))
        file(root, ".ddev/config.yaml", "name: base\ndocroot: src/pub\nweb_environment: [PRIVATE_TOKEN=never-export]\n")
        file(root, ".ddev/config.local.yaml", "name: commerce-local\nworking_dir: {web: '/var/www/html/src with spaces'}\n")
        service().configure(root, "configure", "src", true)
        val initial = service().describeEnvironment(root).getString("description")
        assertTrue(initial.contains("commerce-local"), initial)
        assertTrue(initial.contains("/var/www/html/src with spaces"), initial)
        assertTrue(initial.contains("your terminal"), initial)
        assertFalse(initial.contains("PhpStorm"))
        assertFalse(initial.contains("magento_magerun"))
        assertFalse(initial.contains("magento_skills"))
        assertFalse(initial.contains("never-export"))
        file(root, ".ddev/config.local.yaml", "name: [invalid-private-value")
        val changed = service().describeEnvironment(root).getString("description")
        assertTrue(changed.contains("invalid_configuration"))
        assertFalse(changed.contains("invalid-private-value"))
    }

    @Test fun `custom candidates persist and empty discovery is a successful description`() {
        val root = Files.createDirectories(directory.resolve("project")).toRealPath()
        service().configure(root, "configure", ".", true, listOf("tools/custom"))
        assertTrue(service().describeEnvironment(root).getString("description").contains("No project-local CLI wrappers"))
        file(root, "tools/custom", "#!/bin/sh", true)
        assertTrue(service().describeEnvironment(root).getString("description").contains("./tools/custom"))
    }

    @Test fun `outside wrapper symlinks are omitted`() {
        val root = Files.createDirectories(directory.resolve("project")).toRealPath()
        val outside = file(directory, "outside/cli", "#!/bin/sh", true)
        Files.createDirectories(root.resolve("bin"))
        Files.createSymbolicLink(root.resolve("bin/magento"), outside)
        service().configure(root, "configure", ".", true)
        assertTrue(service().describeEnvironment(root).getString("description").contains("No project-local CLI wrappers"))
    }

    @Test fun `Magerun candidates do not advertise tools absent from the HCE host`() {
        val root = Files.createDirectories(directory.resolve("project")).toRealPath()
        file(root, "bin/n98-magerun2", "<?php throw new Exception('do not run');")
        service().configure(root, "configure", ".", true)
        val description = service().describeEnvironment(root).getString("description")
        assertTrue(description.contains("./bin/n98-magerun2"))
        assertTrue(description.contains("unavailable in this host"))
        assertFalse(description.contains("magento_magerun"))
        assertFalse(description.contains("magento_skills"))
    }
}
