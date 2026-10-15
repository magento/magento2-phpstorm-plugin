/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.updates

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Files
import java.time.Instant

class LocalSecurityPatchesTest {
    @Rule @JvmField val temporary = TemporaryFolder()
    private val root get() = temporary.root.toPath()
    private val installed = InstalledMagento(MagentoDistribution.OPEN_SOURCE, "2.4.8-p5")
    private val now = Instant.parse("2026-10-15T12:00:00Z")
    private val catalog = """{"data":{"2.4.8-p5":{"version":"2.4.8-p5","releaseDate":"2026-05-01"}}}"""
    private val advisory get() = JSONObject("""{"id":"APSB26-153","publishedDate":"2026-10-13","severity":"critical","priority":1,
        "isolatedPatches":[{"id":"VULN-12345","appliesTo":["2.4.8-p5"]}]}""")

    private fun write(path: String, body: String) {
        val file = root.resolve(path)
        Files.createDirectories(file.parent)
        Files.writeString(file, body.trimIndent())
    }

    private fun composer(extra: String, plugin: String = "cweagans/composer-patches", config: String = "{}") {
        write("composer.json", """{"require":{"$plugin":"*"},"config":$config,"extra":$extra}""")
    }

    private fun covered(item: JSONObject = advisory, product: InstalledMagento = installed) =
        LocalSecurityPatches.inspect(root).covers(item, product)

    private fun diff(file: String = "vendor/magento/framework/Example.php", old: String = "unsafe", new: String = "safe") =
        "--- a/$file\n+++ b/$file\n@@ -1,3 +1,3 @@\n context\n-$old\n+$new\n end\n"

    @Test fun `recognizes Composer compact expanded and Vaimo source declarations`() {
        val definitions = listOf(
            """{"VULN-12345 security fix":"https://example.test/fix.patch"}""",
            """[{"description":"Security fix","url":"https://example.test/VULN-12345.patch","sha256":"abc"}]""",
            """{"Security fix":{"source":"https://example.test/VULN-12345.patch"}}"""
        )
        for (plugin in listOf("cweagans/composer-patches", "vaimo/composer-patches")) {
            for (definition in definitions) {
                composer("""{"patches":{"magento/framework":$definition}}""", plugin)
                assertTrue("$plugin $definition", covered())
            }
        }
    }

    @Test fun `Netresearch title and url declarations are recognized`() {
        composer("""{"patches":{"magento/framework":[{"title":"VULN-12345","url":"https://example.test/fix.patch"}]}}""",
            "netresearch/composer-patches-plugin")
        assertTrue(covered())
    }

    @Test fun `recognizes external files in both formats and configuration locations`() {
        for (option in listOf(""""patches-file":"config/security.json"""", """"patches-file":["config/security.json"]""",
            """"composer-patches":{"patches-file":"config/security.json"}""", """"patcher":{"file":"config/security.json"}""")) {
            composer("{$option}")
            for (body in listOf("""{"patches":{"magento/framework":{"Security fix":"https://example.test/VULN-12345.patch"}}}""",
                """{"magento/framework":{"Security fix":"https://example.test/VULN-12345.patch"}}""")) {
                write("config/security.json", body)
                assertTrue("$option $body", covered())
            }
        }
    }

    @Test fun `patch lock is authoritative over declarations`() {
        composer("""{"patches":{"magento/framework":{"VULN-12345":"https://example.test/fix.patch"}}}""")
        write("patches.lock.json", """{"_hash":"test","patches":{"magento/framework":[{"description":"Other fix","url":"other.patch"}]}}""")
        assertFalse(covered())
        write("patches.lock.json", """{"_hash":"test","patches":{"magento/framework":[{"description":"Fix","url":"https://example.test/VULN-12345.patch"}]}}""")
        assertTrue(covered())
    }

    @Test fun `default patches json is discovered without an extra configuration section`() {
        write("composer.json", """{"require":{"cweagans/composer-patches":"^2.0"}}""")
        write("patches.json", """{"patches":{"magento/framework":{"VULN-12345":"https://example.test/fix.patch"}}}""")
        assertTrue(covered())
    }

    @Test fun `disabled Composer resolvers and wildcard plugin prohibition do not count definitions`() {
        write("patches.json", """{"patches":{"magento/framework":{"VULN-12345":"https://example.test/fix.patch"}}}""")
        composer("""{"composer-patches":{"disable-resolvers":["\\cweagans\\Composer\\Resolver\\PatchesFile"]}}""")
        assertFalse(covered())
        composer("{}", config = """{"allow-plugins":{"*":false}}""")
        assertFalse(covered())
    }

    @Test fun `Vaimo embedded metadata supports default search and skip directives`() {
        composer("{}", "vaimo/composer-patches")
        write("patches/fix.patch", "@package magento/framework\n@ticket VULN-12345\n" + diff())
        assertTrue(covered())
        write("patches/fix.patch", "@package magento/framework\n@ticket VULN-12345\n@skip\n" + diff())
        assertFalse(covered())
    }

    @Test fun `dependency patch definitions support centralized patch packages`() {
        composer("{}")
        write("composer.lock", """{"packages":[{"name":"company/security-patches","version":"1.0.0","extra":{
            "patches":{"magento/framework":{"VULN-12345":"https://example.test/security.patch"}}}}]}""")
        assertTrue(covered())
    }

    @Test fun `reads applied records from Composer one and two and custom vendor directory`() {
        for (vendor in listOf("vendor", "packages")) {
            write("composer.json", """{"config":{"vendor-dir":"$vendor"}}""")
            val pkg = """{"name":"magento/framework","extra":{"patches_applied":{"Security fix":"https://example.test/VULN-12345.patch"}}}"""
            for (body in listOf("[$pkg]", """{"packages":[$pkg],"dev":false}""")) {
                write("$vendor/composer/installed.json", body)
                assertTrue(covered())
            }
        }
    }

    @Test fun `Composer lock applied records do not require a patcher declaration`() {
        write("composer.lock", """{"packages":[{"name":"magento/framework","extra":{"patches_applied":{"VULN-12345":"https://example.test/fix.patch"}}}]}""")
        assertTrue(covered())
    }

    @Test fun `installed metadata supersedes historical applied records in composer lock`() {
        write("composer.lock", """{"packages":[{"name":"magento/framework","extra":{"patches_applied":{"VULN-12345":"https://example.test/fix.patch"}}}]}""")
        write("vendor/composer/installed.json", """{"packages":[{"name":"magento/framework","extra":{"patches_applied":{}}}]}""")
        assertFalse(covered())
    }

    @Test fun `disabled patch plugins and unrelated Composer strings cannot hide advisories`() {
        val extra = """{"patches":{"magento/framework":{"VULN-12345":"https://example.test/fix.patch"}}}"""
        for (config in listOf("""{"allow-plugins":false}""", """{"allow-plugins":{"cweagans/composer-patches":false}}""")) {
            composer(extra, config = config)
            assertFalse(covered())
        }
        composer(extra.replace("{\"patches\"", "{\"patcher\":false,\"patches\""), "vaimo/composer-patches")
        assertFalse(covered())
        write("composer.json", """{"description":"VULN-12345 applied","extra":$extra}""")
        assertFalse(covered())
    }

    @Test fun `ignored dependency patches do not count as configured`() {
        composer("""{"patches-ignore":{"company/patches":{"magento/framework":{"Security fix":"https://example.test/VULN-12345.patch"}}}}""")
        write("composer.lock", """{"packages":[{"name":"company/patches","extra":{"patches":{"magento/framework":{"Security fix":"https://example.test/VULN-12345.patch"}}}}]}""")
        assertFalse(covered())
    }

    @Test fun `Cloud YAML uses actual build QUALITY_PATCHES including inline lists and anchors`() {
        for (yaml in listOf(
            "stage:\n  build:\n    QUALITY_PATCHES:\n      - 'VULN-12345' # security\n",
            "stage: {build: {QUALITY_PATCHES: [VULN-12345]}}",
            "patches: &patches [VULN-12345]\nstage:\n  build:\n    QUALITY_PATCHES: *patches\n"
        )) {
            write(".magento.env.yaml", yaml)
            assertTrue(yaml, covered())
        }
    }

    @Test fun `comments unrelated YAML fields wrong stages and malformed files do not suppress`() {
        for (yaml in listOf("# QUALITY_PATCHES: [VULN-12345]", "description: VULN-12345",
            "stage: {deploy: {QUALITY_PATCHES: [VULN-12345]}}", "stage: [invalid: :")) {
            write(".magento.env.yaml", yaml)
            assertFalse(yaml, covered())
        }
    }

    @Test fun `Cloud hotfixes count as deployment configuration but loose files do not`() {
        write("m2-hotfixes/VULN-12345.patch", diff())
        assertFalse(covered())
        write(".magento.app.yaml", "name: application\nhooks:\n  build: php ./vendor/bin/ece-tools build\n")
        assertTrue(covered())
        Files.delete(root.resolve(".magento.app.yaml"))
        write("composer.lock", """{"packages":[{"name":"magento/ece-tools","version":"2002.2.0"}]}""")
        assertTrue(covered())
    }

    @Test fun `Cloud ignores diff extension for automatic hotfix application`() {
        write(".magento.app.yaml", "name: application")
        write("m2-hotfixes/VULN-12345.diff", diff())
        assertFalse(covered())
    }

    @Test fun `manual patch git apply and committed changes are detected by complete post images`() {
        write("patches/security/VULN-12345.patch", diff())
        write("vendor/magento/framework/Example.php", "context\nunsafe\nend\n")
        assertFalse(covered())
        write("vendor/magento/framework/Example.php", "context\nsafe\nend\n")
        assertTrue(covered())
        write("vendor/magento/framework/Example.php", "context\nunsafe\nend\n")
        assertFalse(covered())
    }

    @Test fun `every file and hunk must be patched`() {
        write("VULN-12345.patch", diff() + diff("app/code/Test/Other.php"))
        write("vendor/magento/framework/Example.php", "context\nsafe\nend\n")
        write("app/code/Test/Other.php", "context\nunsafe\nend\n")
        assertFalse(covered())
        write("app/code/Test/Other.php", "context\nsafe\nend\n")
        assertTrue(covered())
    }

    @Test fun `Composer package relative diffs detect applied code and disprove stale records`() {
        composer("""{"patches":{"magento/framework":{"VULN-12345":"patches/fix.patch"}}}""")
        write("patches/fix.patch", diff("Example.php"))
        write("vendor/magento/framework/Example.php", "context\nsafe\nend\n")
        assertTrue(covered())
        write("vendor/magento/framework/Example.php", "context\nunsafe\nend\n")
        assertFalse(covered())
    }

    @Test fun `QPT and bundled Cloud diffs require actual application`() {
        for (directory in listOf("quality-patches", "magento-cloud-patches")) {
            write("vendor/magento/$directory/patches/VULN-12345.patch", diff())
        }
        assertFalse(covered())
        write("vendor/magento/framework/Example.php", "context\nsafe\nend\n")
        assertTrue(covered())
    }

    @Test fun `historical logs patch reports and patch tool installation are insufficient`() {
        write("var/log/patch.log", "VULN-12345 has been applied\nVULN-12345 has been reverted")
        write("vendor/magento/framework/PATCHES.txt", "VULN-12345")
        write("composer.lock", """{"packages":[{"name":"magento/quality-patches","version":"1.1.0"}]}""")
        assertFalse(covered())
    }

    @Test fun `patch IDs are matched with boundaries and applicability`() {
        composer("""{"patches":{"magento/framework":{"Other fix":"https://example.test/VULN-123456.patch"}}}""")
        assertFalse(covered())
        composer("""{"patches":{"magento/framework":{"Security fix":"https://example.test/VULN-12345__2.4.8.patch"}}}""")
        assertTrue(covered())
        val wrongBase = JSONObject(advisory.toString().replace("2.4.8-p5", "2.4.7-p10"))
        assertFalse(covered(wrongBase))
    }

    @Test fun `covering one of several required isolated patches cannot hide the advisory`() {
        composer("""{"patches":{"magento/framework":{"Fix":"https://example.test/VULN-12345.patch"}}}""")
        val item = advisory
        item.getJSONArray("isolatedPatches").put(JSONObject("""{"id":"VULN-67890","appliesTo":["2.4.8-p5"]}"""))
        assertFalse(covered(item))
        composer("""{"patches":{"magento/framework":{"Fix":"https://example.test/VULN-12345.patch","Other fix":"https://example.test/VULN-67890.patch"}}}""")
        assertTrue(covered(item))
    }

    @Test fun `monthly Adobe filenames match the exact release and require CE and EE for Commerce`() {
        val monthly = JSONObject(advisory.toString().replace("VULN-12345", "2026-oct"))
        write(".magento.app.yaml", "name: application")
        write("m2-hotfixes/248p5-2026-10-001-CE.patch", diff())
        assertTrue(covered(monthly))
        val commerce = installed.copy(distribution = MagentoDistribution.COMMERCE)
        assertFalse(covered(monthly, commerce))
        write("m2-hotfixes/248p5-2026-10-002-EE.patch", diff())
        assertTrue(covered(monthly, commerce))
        Files.delete(root.resolve("m2-hotfixes/248p5-2026-10-001-CE.patch"))
        write("m2-hotfixes/247p10-2026-10-001-CE.patch", diff())
        assertFalse(covered(monthly))
    }

    @Test fun `a later monthly patch cannot hide a missing earlier patch`() {
        write(".magento.app.yaml", "name: application")
        write("m2-hotfixes/248p5-2026-11-001-CE.patch", diff())
        assertFalse(covered(JSONObject(advisory.toString().replace("VULN-12345", "2026-oct"))))
    }

    @Test fun `Commerce monthly coverage includes the installed B2B version`() {
        val monthly = JSONObject(advisory.toString().replace("VULN-12345", "2026-oct"))
        write("composer.lock", """{"packages":[{"name":"magento/extension-b2b","version":"1.5.3"}]}""")
        write(".magento.app.yaml", "name: application")
        for (component in listOf("CE", "EE")) write("m2-hotfixes/248p5-2026-10-001-$component.patch", diff())
        val commerce = installed.copy(distribution = MagentoDistribution.COMMERCE)
        assertFalse(covered(monthly, commerce))
        write("m2-hotfixes/153-2026-10-001-B2B.patch", diff())
        assertTrue(covered(monthly, commerce))
    }

    @Test fun `CVT cached diffs verify current code and registry requires every sequence and prerequisite`() {
        val monthly = JSONObject(advisory.toString().replace("VULN-12345", "2026-oct"))
        write("var/patch_metadata/.patch_registry_cache.json", """{"_areas":{"CE":{"detection":"base_version"}},"patches":{
            "248p5-2026-09-001-CE":{"area":"CE","applies_to":["2.4.8-p5"],"requires":[]},
            "248p5-2026-10-001-CE":{"area":"CE","applies_to":["2.4.8-p5"],"requires":["248p5-2026-09-001-CE"]},
            "248p5-2026-10-002-CE":{"area":"CE","applies_to":["2.4.8-p5"],"requires":["248p5-2026-10-001-CE"]}
        }}""")
        write("vendor/magento/framework/Example.php", "context\nsafe\nend\n")
        val directory = "var/patch_metadata/.patch_diffs"
        write("$directory/248p5-2026-10-001-CE.diff", diff())
        assertFalse(covered(monthly))
        write("$directory/248p5-2026-10-002-CE.diff", diff())
        assertFalse(covered(monthly))
        write("$directory/248p5-2026-09-001-CE.diff", diff())
        assertTrue(covered(monthly))
        write("vendor/magento/framework/Example.php", "context\nunsafe\nend\n")
        assertFalse(covered(monthly))
    }

    @Test fun `fixed product releases suppress stale feed vulnerabilities only on their own line`() {
        val item = advisory.put("fixedIn", "2.4.8-p4")
        assertTrue(covered(item))
        item.put("fixedIn", "2.4.7-p4")
        assertFalse(covered(item))
    }

    @Test fun `evaluation recomputes severity count and fingerprint from uncovered advisories`() {
        composer("""{"patches":{"magento/framework":{"Fix":"https://example.test/VULN-12345.patch"}}}""")
        val low = JSONObject("""{"id":"APSB-OTHER","severity":"low","publishedDate":"2026-10-13","isolatedPatches":[]}""")
        val security = JSONObject().put("data", JSONObject().put("distribution", installed.distribution.apiId)
            .put("version", installed.version).put("status", "vulnerable").put("vulnerableTo", JSONArray().put(advisory).put(low))).toString()
        val result = UpdateEvaluator.evaluate(installed, catalog, security, now, LocalSecurityPatches.inspect(root))
        assertEquals(1, result.advisoryCount)
        assertEquals(UpdateSeverity.LOW, result.severity)
        assertFalse(result.urgent)
        assertFalse(result.findings.single().message.contains("APSB26-153"))
        assertTrue(result.findings.single().message.contains("APSB-OTHER"))
    }

    @Test fun `shared feed cache evaluates each project and stale results against current local state`() {
        var unavailable = false
        var calls = 0
        val security = JSONObject().put("data", JSONObject().put("distribution", installed.distribution.apiId)
            .put("version", installed.version).put("status", "vulnerable").put("vulnerableTo", JSONArray().put(advisory))).toString()
        val feed = MagentoUpdateFeed({ url ->
            calls++
            if (unavailable) throw java.io.IOException("offline")
            if (url.endsWith("/security")) security else catalog
        }, { now })
        val url = MagentoUpdateSettings.DEFAULT_FEED_URL
        assertTrue(feed.check(url, installed).assessment.actionable)
        composer("""{"patches":{"magento/framework":{"Fix":"https://example.test/VULN-12345.patch"}}}""")
        val patched = LocalSecurityPatches.inspect(root)
        assertFalse(feed.check(url, installed, false, patched).assessment.actionable)
        assertEquals(2, calls)
        assertTrue(feed.check(url, installed).assessment.actionable)
        unavailable = true
        val stale = feed.check(url, installed, true, patched)
        assertTrue(stale.stale)
        assertFalse(stale.assessment.actionable)
    }

    @Test fun `malformed and missing metadata leaves the advisory visible`() {
        for (path in listOf("composer.json", "composer.lock", "patches.lock.json", "vendor/composer/installed.json")) write(path, "invalid VULN-12345")
        assertFalse(covered())
    }
}
