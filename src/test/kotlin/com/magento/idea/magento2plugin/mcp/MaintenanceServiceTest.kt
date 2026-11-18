/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Files
import java.util.concurrent.CancellationException

class MaintenanceServiceTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun sharedFixtureMatrix() {
        val root = temporary.root.toPath().toRealPath()
        val magento = Files.createDirectory(root.resolve("src"))
        MaintenanceContract.prepare(root, magento)
        val process = MagentoMagerunProcess()
        val launcher = MagerunRunner { argv, cwd, cancel ->
            val command = if (java.io.File.separatorChar == '\\') listOf("python") + argv else argv
            process.run(command, cwd, cancel)
        }
        MaintenanceContract.run(root) { args ->
            fun strings(key: String) = args.optJSONArray(key)?.map { it as String }.orEmpty()
            MaintenanceService(root, "src", emptyList(), launcher).execute(args.getString("mode"), strings("checkIds"),
                strings("changedFiles"), strings("newlyEnabledModules"), args.optString("devMode", "dev")) {}
        }
    }

    @Test fun installedPhpReadsCloudPoolsAndContainerInventory() {
        val php = com.intellij.execution.configurations.PathEnvironmentVariableUtil.findInPath("php")
        org.junit.Assume.assumeNotNull(php)
        val root = temporary.root.toPath()
        val vendor = Files.createDirectories(root.resolve("custom vendor"))
        fun run(script: String, patches: String = """["Q-2","Q-MISSING"]"""): org.json.JSONObject {
            val source = javaClass.getResource("/maintenance/$script.php")!!.readText().substringAfter("<?php")
            val process = ProcessBuilder(php!!.absolutePath, "-r", source, "--", vendor.toString()).directory(root.toFile())
                .redirectErrorStream(true).apply { environment()["QUALITY_PATCHES"] = patches }.start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            assertEquals(output, 0, process.waitFor())
            return org.json.JSONObject(output)
        }
        Files.createDirectories(root.resolve("m2-hotfixes"))
        Files.writeString(root.resolve("m2-hotfixes/site.patch"), "fixture")
        val bootstrap = vendor.resolve("magento/magento-cloud-patches/bootstrap.php")
        Files.createDirectories(bootstrap.parent)
        Files.writeString(bootstrap, javaClass.getResource("/maintenance/cloud-bootstrap.php")!!.readText())
        assertEquals(setOf("CLOUD-1", "Q-2", "Q-DEP", "Q-LEAF", "Q-MISSING", "m2-hotfixes/site.patch"),
            run("patches").getJSONArray("items").toList().toSet())
        assertEquals(setOf("CLOUD-1", "Q-2", "Q-LEAF", "m2-hotfixes/site.patch"),
            run("patches", """["*"]""").getJSONArray("items").toList().toSet())
        Files.writeString(root.resolve("composer.lock"), """{"packages":[{"name":"acme/meta","version":"1.0"}]}""")
        assertTrue(run("inventory").getBoolean("missing"))
        Files.writeString(vendor.resolve("autoload.php"), "<?php")
        Files.createDirectories(vendor.resolve("composer"))
        Files.writeString(vendor.resolve("composer/installed.json"),
            """{"packages":[{"type":"metapackage","name":"acme/meta","version":"1.0"}]}""")
        val data = run("inventory")
        assertEquals(data.getJSONArray("locked").toString(), data.getJSONArray("installed").toString())
    }

    @Test fun layoutAdviceRefreshesACachedStorefrontResponseWithoutInvalidations() {
        val root = temporary.root.toPath().toRealPath()
        MaintenanceContract.prepare(root, root)
        val path = "app/code/Acme/News/view/frontend/layout/default.xml"
        val layout = root.resolve(path)
        Files.createDirectories(layout.parent)
        Files.writeString(layout, "<page>old</page>")
        val cache = mutableMapOf<String, String>()
        fun storefrontResponse() = cache.getOrPut("full_page") {
            cache.getOrPut("layout") { Files.readString(layout) }
        }
        assertEquals("<page>old</page>", storefrontResponse())
        Files.writeString(layout, "<page>new</page>")

        val service = MaintenanceService(root, ".", emptyList(), MagerunRunner { argv, _, _ ->
            assertTrue(argv.toString(), argv.any { it.contains("getInvalidated()") })
            MagerunProcessResult(0, "[]")
        })
        val findings = service.execute("verify", listOf("cache_clean"), listOf(path), emptyList(), "dev") {}
            .getJSONArray("findings").map { it as org.json.JSONObject }
        assertEquals(listOf("layout", "full_page"), findings.map { it.getString("item") })
        assertTrue(findings.all { it.getString("status") == "advisory" })
        for (finding in findings) {
            val command = finding.getJSONObject("suggestedCommand").getJSONArray("argv").toList().takeLast(3)
            assertEquals(listOf("bin/magento", "cache:clean", finding.getString("item")), command)
            cache.remove(finding.getString("item"))
            if (finding.getString("item") == "layout") {
                assertEquals("<page>old</page>", storefrontResponse())
            }
        }
        assertEquals("<page>new</page>", storefrontResponse())
    }

    @Test fun composeWithoutAnIdentifiedLauncherNeverProbesHostVendor() {
        val root = temporary.root.toPath().toRealPath()
        Files.writeString(root.resolve("compose.yaml"), "services: {}")
        Files.writeString(root.resolve("composer.lock"), "{}")
        val service = MaintenanceService(root, ".", emptyList(), MagerunRunner { _, _, _ -> error("Unexpected host probe") })
        val result = service.execute("verify", listOf("composer_install"), emptyList(), emptyList(), "dev") {}
        assertEquals("runtime_unavailable", result.getJSONArray("findings").getJSONObject(0).getString("reasonId"))
    }

    @Test fun truncatedAndTimedOutSetupResultsDoNotRecommendUpgrade() {
        val root = temporary.root.toPath().toRealPath()
        for (limited in listOf(true, false)) {
            val service = MaintenanceService(root, ".", emptyList(), MagerunRunner { _, _, _ ->
                MagerunProcessResult(2, "Data patches are not up to date", timedOut = !limited, outputLimited = limited)
            })
            val row = service.execute("verify", listOf("setup_upgrade"), emptyList(), emptyList(), "dev") {}
                .getJSONArray("findings").getJSONObject(0)
            assertEquals("unknown", row.getString("status"))
            assertFalse(row.has("suggestedCommand"))
        }
    }

    @Test fun unrecognizedMagentoWrapperDoesNotSelectHostPhp() {
        val root = temporary.root.toPath().toRealPath()
        val bin = Files.createDirectories(root.resolve("bin"))
        val wrapper = Files.writeString(bin.resolve("magento"), "#!/bin/sh\necho custom wrapper")
        wrapper.toFile().setExecutable(true)
        val service = MaintenanceService(root, ".", emptyList(), MagerunRunner { _, _, _ -> error("Unexpected fallback") })
        val result = service.execute("verify", listOf("cache_clean"), emptyList(), emptyList(), "dev") {}
        assertEquals("runtime_unavailable", result.getJSONArray("findings").getJSONObject(0).getString("reasonId"))
    }

    @Test fun cancelsBeforeStartingProcess() {
        val service = MaintenanceService(temporary.root.toPath(), ".", emptyList(),
            MagerunRunner { _, _, _ -> error("Unexpected probe") })
        assertThrows(CancellationException::class.java) {
            service.execute("verify", emptyList(), emptyList(), emptyList(), "dev") { throw CancellationException() }
        }
    }

    @Test fun rejectsEscapingAndAbsoluteChangedPaths() {
        val service = MaintenanceService(temporary.root.toPath(), ".", emptyList())
        for (path in listOf("../etc/config.xml", "/tmp/layout/default.xml", "C:/app/etc/config.php")) {
            val result = service.execute("check", emptyList(), listOf(path), emptyList(), "dev") {}
            assertEquals("invalid_request", result.getString("status"))
        }
    }
}
