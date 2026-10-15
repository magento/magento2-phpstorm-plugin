/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.updates

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Files

class CloudSecurityPatchesTest {
    @Rule @JvmField val temporary = TemporaryFolder()
    private val root get() = temporary.root.toPath()
    private val fixture = CloudPatchesFixture { path, body ->
        val file = root.resolve(path)
        Files.createDirectories(file.parent)
        Files.writeString(file, body)
    }
    private val commerce = InstalledMagento(MagentoDistribution.COMMERCE, "2.4.8-p5")
    private val advisory get() = JSONObject("""{"id":"APSB26-153","isolatedPatches":[{"id":"2026-oct","appliesTo":["2.4.8-p5"]}]}""")
    private fun covered(product: InstalledMagento = commerce, item: JSONObject = advisory) =
        LocalSecurityPatches.inspect(root).covers(item, product)

    @Test fun `reported Commerce 248p5 with B2B 153 requires all applied Cloud monthly patches`() {
        fixture.install()
        assertFalse(covered())
        for (component in listOf("ce", "ee", "b2b")) {
            fixture.apply(component)
            assertEquals(component, component == "b2b", covered())
        }
        fixture.apply("ee", applied = false)
        assertFalse(covered())
    }

    @Test fun `Open Source only needs CE and Commerce without B2B needs CE and EE`() {
        fixture.install(b2b = false)
        fixture.apply("ce")
        assertTrue(covered(commerce.copy(distribution = MagentoDistribution.OPEN_SOURCE)))
        assertFalse(covered())
        fixture.apply("ee")
        assertTrue(covered())
    }

    @Test fun `package installation configuration and historical applied status cannot suppress the advisory`() {
        fixture.install()
        fixture.write(".magento.app.yaml", "name: application")
        fixture.write(".magento.env.yaml", "stage: {build: {QUALITY_PATCHES: [MCLOUD-15634]}}")
        fixture.write("var/log/patch.log", "MCLOUD-15634 | Patch for Oct 2026 monthly release | Applied")
        assertFalse(covered())
    }

    @Test fun `missing mappings or incompatible package constraints leave the warning visible`() {
        fixture.install()
        fixture.applyAll()
        for (constraint in listOf("2.4.8-p4", "2.4.8-p6", ">=2.4.8-p6 <2.4.9", "2.4.7-p10 || 2.4.8-p4")) {
            fixture.mapping("ce", constraint)
            assertFalse(constraint, covered())
        }
        fixture.mapping("ce", "2.4.8-p5")
        assertTrue(covered())
        fixture.mappings.remove("magento/magento2-ee-base")
        fixture.saveMappings()
        assertFalse(covered())
    }

    @Test fun `mapping constraints support exact versions comparisons unions and inclusive ranges`() {
        fixture.install()
        fixture.applyAll()
        for (constraint in listOf("=2.4.8-p5", "v2.4.8-p5", ">=2.4.8-p4 <2.4.8-p6", ">=2.4.8-p5, <2.4.9",
            "2.4.8-p5 - 2.4.8-p7", "2.4.7-p10 || 2.4.8-p5")) {
            fixture.mapping("ce", constraint)
            assertTrue(constraint, covered())
        }
    }

    @Test fun `unapplied variants for other package versions do not count as required fixes`() {
        fixture.install()
        fixture.applyAll()
        val variants = fixture.mappings.getJSONObject("magento/magento2-base").getJSONObject("Patch for Oct 2026 monthly release")
        variants.put("2.4.7-p10", "MCLOUD-15634__Oct_2026_monthly_release_ce__2.4.7-p10.patch")
        variants.put("2.4.8-p6", "MCLOUD-15634__Oct_2026_monthly_release_ce__2.4.8-p6.patch")
        fixture.saveMappings()
        assertTrue(covered())
    }

    @Test fun `an invalid monthly mapping cannot be hidden by another applied mapping`() {
        fixture.install()
        fixture.applyAll()
        val definitions = fixture.mappings.getJSONObject("magento/magento2-base")
        for (invalid in listOf("not a version map", JSONObject(), JSONObject().put("2.4.8-p5", 42))) {
            definitions.put("Patch for October 2026 monthly release follow-up", invalid)
            fixture.saveMappings()
            assertFalse(invalid.toString(), covered())
        }
    }

    @Test fun `unverifiable mappings diffs and targets keep the advisory visible`() {
        fixture.install()
        fixture.applyAll()
        val source = fixture.source("b2b")
        Files.delete(root.resolve(source))
        assertFalse(covered())
        fixture.write(source, "GIT binary patch\nunsupported")
        assertFalse(covered())
        fixture.write(source, fixture.diff("b2b"))
        Files.delete(root.resolve(fixture.target("b2b")))
        assertFalse(covered())
        fixture.apply("b2b")
        fixture.mapping("b2b", "unrecognized constraint")
        assertFalse(covered())
        fixture.write(fixture.directory + "/patches.json", "invalid")
        assertFalse(covered())
    }

    @Test fun `every applicable mapping and every file in its diff must be applied`() {
        fixture.install()
        fixture.applyAll()
        val second = "MCLOUD-15635__Oct_2026_monthly_release_ce__2.4.8-p5.patch"
        fixture.mappings.getJSONObject("magento/magento2-base").put("Patch for Oct 2026 monthly release follow-up",
            JSONObject().put("2.4.8-p5", second))
        fixture.saveMappings()
        fixture.write(fixture.directory + "/patches/" + second, fixture.diff("ce", "vendor/magento/framework/Second.php"))
        assertFalse(covered())
        fixture.write("vendor/magento/framework/Second.php", "context\nsafe-ce\nend\n")
        assertTrue(covered())
        fixture.write(fixture.source("ce"), fixture.diff("ce") + fixture.diff("ce", "vendor/magento/framework/Third.php"))
        fixture.write("vendor/magento/framework/Third.php", "context\nunsafe-ce\nend\n")
        assertFalse(covered())
    }

    @Test fun `installed Composer metadata overrides locked B2B package versions`() {
        fixture.install()
        fixture.applyAll()
        fixture.write("vendor/composer/installed.json", """{"packages":[
            {"name":"magento/magento2-b2b-base","version":"1.5.4"},
            {"name":"magento/extension-b2b","version":"1.5.4"}]}""")
        assertFalse(covered())
    }

    @Test fun `the mapped base package version takes precedence over the product version`() {
        fixture.install()
        fixture.applyAll()
        fixture.write("vendor/composer/installed.json", """[{"name":"magento/magento2-base","version":"2.4.8-p4"}]""")
        assertFalse(covered())
        fixture.write("vendor/composer/installed.json", """[{"name":"magento/magento2-base","version":"v2.4.8-p5"}]""")
        assertTrue(covered())
    }

    @Test fun `B2B base package alone is sufficient to require its fixes`() {
        fixture.install()
        fixture.write("composer.lock", """{"packages":[{"name":"magento/magento2-b2b-base","version":"1.5.3"}]}""")
        fixture.apply("ce")
        fixture.apply("ee")
        assertFalse(covered())
        fixture.apply("b2b")
        assertTrue(covered())
    }

    @Test fun `month and mapping source must match rather than trusting a loose Cloud filename`() {
        fixture.install()
        fixture.applyAll()
        assertFalse(covered(item = JSONObject(advisory.toString().replace("2026-oct", "2026-sep"))))
        fixture.write(fixture.directory + "/patches.json", "{}")
        assertFalse(covered())
    }

    @Test fun `monthly Cloud filenames identify the month when the mapping uses a generic title`() {
        fixture.install()
        fixture.applyAll()
        val definitions = fixture.mappings.getJSONObject("magento/magento2-base")
        definitions.put("Monthly security update", definitions.remove("Patch for Oct 2026 monthly release"))
        fixture.saveMappings()
        assertTrue(covered())
    }

    @Test fun `custom vendor directory and package relative Cloud diffs are supported and watched`() {
        fixture.vendor = "packages"
        fixture.install()
        fixture.write("composer.json", """{"config":{"vendor-dir":"packages"}}""")
        fixture.applyAll()
        fixture.write(fixture.source("ce"), fixture.diff("ce", "Example.php"))
        fixture.write("packages/magento/magento2-base/Example.php", "context\nsafe-ce\nend\n")
        assertTrue(covered())
        val watched = LocalSecurityPatches.inspect(root).watchedPaths
        // Watched paths use forward slashes on every platform, matching VFS event paths.
        for (relative in listOf(fixture.directory + "/patches.json", "packages/magento/magento2-base/Example.php")) {
            val expected = root.resolve(relative).toString().replace('\\', '/')
            assertTrue("Missing watched path: $expected", watched.contains(expected))
        }
    }
}

/** Minimal Cloud Patches 1.1.22 layout using package -> title -> constraint -> filename mappings. */
internal class CloudPatchesFixture(private val writer: (String, String) -> Unit) {
    var vendor = "vendor"
    val directory get() = "$vendor/magento/magento-cloud-patches"
    val mappings = JSONObject()
    private val packages = mapOf("ce" to "magento/magento2-base", "ee" to "magento/magento2-ee-base", "b2b" to "magento/magento2-b2b-base")
    private fun version(component: String) = if (component == "b2b") "1.5.3" else "2.4.8-p5"
    private fun filename(component: String) = "MCLOUD-15634__Oct_2026_monthly_release_${component}__${version(component)}.patch"
    fun source(component: String) = "$directory/patches/${filename(component)}"
    fun target(component: String) = "$vendor/magento/framework/Cloud-$component.php"
    fun write(path: String, body: String) = writer(path, body.trimIndent())
    fun saveMappings() = write("$directory/patches.json", mappings.toString())
    fun mapping(component: String, constraint: String = version(component)) {
        mappings.put(packages.getValue(component), JSONObject().put("Patch for Oct 2026 monthly release",
            JSONObject().put(constraint, filename(component))))
        saveMappings()
    }
    fun diff(component: String, target: String = target(component)) =
        "--- a/$target\n+++ b/$target\n@@ -1,3 +1,3 @@\n context\n-unsafe-$component\n+safe-$component\n end\n"
    fun install(b2b: Boolean = true) {
        write("composer.lock", """{"packages":[
            {"name":"magento/product-enterprise-edition","version":"2.4.8-p5"},
            {"name":"magento/magento2-base","version":"2.4.8-p5"},
            {"name":"magento/magento-cloud-patches","version":"1.1.22"}
            ${if (b2b) """,{"name":"magento/extension-b2b","version":"1.5.3"},{"name":"magento/magento2-b2b-base","version":"1.5.3"}""" else ""}
        ]}""")
        for (component in packages.keys) {
            mapping(component)
            write(source(component), diff(component))
        }
    }
    fun apply(component: String, applied: Boolean = true) = write(target(component), "context\n${if (applied) "safe" else "unsafe"}-$component\nend\n")
    fun applyAll() = packages.keys.forEach { apply(it) }
}
