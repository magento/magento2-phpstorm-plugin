/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.DumbModeTestUtils
import com.magento.idea.magento2plugin.BaseProjectTestCase
import com.magento.idea.magento2plugin.indexes.ModuleIndex
import com.magento.idea.magento2plugin.project.Settings
import org.json.JSONObject

class MagentoInspectionResultsTest : BaseProjectTestCase() {
    private fun query(type: String, parameters: JSONObject): JSONObject =
        JSONObject(MagentoInspectionCommands.query(project, type, parameters.toString()))

    private fun events(parameters: JSONObject = JSONObject()) =
        query("observers_for_event", JSONObject(parameters.toMap()).put("eventName", "acme_paging").put("format", "json"))

    /** The directory of the fixture module Foo_Bar2, as the tool reports it. */
    private fun moduleDirectory(): String = query("module", JSONObject().put("moduleName", "Foo_Bar2").put("format", "json"))
        .getJSONArray("records").getJSONObject(0).getString("path")

    /** 25 events with one observer each, plus one event with 23 observers split over global and adminhtml. */
    private fun addEvents(): String {
        val module = moduleDirectory()
        val global = (0 until 25).joinToString("\n") { index ->
            "<event name=\"acme_paging_%02d\"><observer name=\"single_$index\" instance=\"Foo\\Bar2\\Observer\\Single\"/></event>".format(index)
        } + "\n<event name=\"acme_paging_big\">" + (0 until 15).joinToString("") {
            "<observer name=\"big_%02d\" instance=\"Foo\\Bar2\\Observer\\Big\"/>".format(it)
        } + "</event>"
        val admin = "<event name=\"acme_paging_big\">" + (15 until 23).joinToString("\n") {
            "<observer name=\"big_%02d\" instance=\"Foo\\Bar2\\Observer\\Big\" disabled=\"true\"/>".format(it)
        } + "</event>"
        myFixture.addFileToProject("$module/etc/events.xml", "<config>\n$global\n</config>")
        myFixture.addFileToProject("$module/etc/adminhtml/events.xml", "<config>\n$admin\n</config>")
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        return module
    }

    fun testPagesReturnEveryObserverOnceInAFixedOrder() {
        val module = addEvents()
        val text = MagentoEventQueries.findObserversForEvent(project, "acme_paging")
        assertTrue(text, text.endsWith(MagentoInspectionResults.TEXT_TRUNCATED_HINT))

        val seen = mutableListOf<String>()
        var cursor: String? = null
        var pages = 0
        do {
            val page = events(JSONObject().put("limit", 7).apply { cursor?.let { put("cursor", it) } })
            assertEquals(page.toString(), "ok", page.getString("status"))
            assertEquals(48, page.getInt("total"))
            val records = page.getJSONArray("records")
            for (index in 0 until records.length()) {
                val record = records.getJSONObject(index)
                seen += "${record.getString("key")}/${record.getString("observerName")}"
                assertEquals("Foo_Bar2", record.getString("module"))
                assertTrue(record.getString("path").startsWith("$module/etc/"))
                assertTrue(record.getInt("line") > 1)
            }
            cursor = page.optString("nextCursor").takeIf { !page.isNull("nextCursor") }
            assertEquals(cursor == null, page.getBoolean("complete"))
            pages++
            assertTrue("pagination does not end", pages <= 7)
        } while (cursor != null)
        assertEquals(7, pages)
        assertEquals(48, seen.size)
        assertEquals(seen.toSet().size, seen.size)
        val single = events(JSONObject().put("limit", 100))
        assertEquals(seen, (0 until 48).map { index ->
            single.getJSONArray("records").getJSONObject(index).let { "${it.getString("key")}/${it.getString("observerName")}" }
        })
        assertEquals((0 until 23).map { "acme_paging_big/big_%02d".format(it) }.toSet(),
            seen.filter { it.startsWith("acme_paging_big/") }.toSet())
    }

    fun testFiltersNarrowRecordsAndRejectWhatDoesNotApply() {
        val module = addEvents()
        val admin = events(JSONObject().put("area", "adminhtml").put("limit", 100))
        assertEquals(8, admin.getInt("total"))
        assertEquals("adminhtml", admin.getJSONObject("scope").getString("area"))
        assertEquals(40, events(JSONObject().put("area", "global").put("limit", 100)).getInt("total"))
        assertEquals(48, events(JSONObject().put("module", "Foo_Bar2").put("path", "$module/etc/")).getInt("total"))
        assertEquals(8, events(JSONObject().put("path", "./$module/etc/adminhtml")).getInt("total"))
        val none = events(JSONObject().put("module", "Missing_Module"))
        assertEquals("no_matches", none.getString("status"))
        assertEquals(0, none.getJSONArray("records").length())

        val areaOnModules = query("module", JSONObject().put("moduleName", "Foo").put("format", "json").put("area", "global"))
        assertEquals("invalid_request", areaOnModules.getString("status"))
        assertTrue(areaOnModules.getString("message").contains("Supported filters: `path`"))
        for ((key, value) in listOf("limit" to 0, "limit" to 101, "path" to "/etc", "path" to "../x", "path" to ".",
            "path" to "C:/x", "cursor" to "garbage")) {
            val result = events(JSONObject().put(key, value))
            assertEquals("$key=$value: $result", "invalid_request", result.getString("status"))
            assertEquals(0, result.getInt("total"))
            assertEquals(0, result.getJSONArray("records").length())
        }
        assertEquals(8, events(JSONObject().put("path", "$module//etc/./adminhtml/")).getInt("total"))
        val xml = MagentoInspectionCommands.query(project, "observers_for_event", """{"eventName":"acme_paging","format":"xml"}""")
        assertTrue(xml, xml.contains("format must be `text` or `json`"))
        val missing = query("observers_for_event", JSONObject().put("format", "json"))
        assertEquals("invalid_request", missing.getString("status"))
        assertTrue(missing.getString("message").contains("`eventName`"))
        val layout = query("layout_entities", JSONObject().put("name", "default").put("format", "json"))
        assertEquals("invalid_request", layout.getString("status"))
        val schema = MagentoInspectionCommands.detailedSchema("module")
        assertTrue(schema, schema.contains("filters path") && schema.contains("path: project-relative"))
        assertTrue(schema, schema.contains("A module at the project root has path `.`, which no path filter selects"))
        assertFalse(schema, schema.contains("area: global"))
    }

    fun testTextOutputIsUnchangedAndRejectsJsonOnlyOptions() {
        addEvents()
        val text = MagentoInspectionCommands.query(project, "observers_for_event", """{"eventName":"acme_paging_01"}""")
        assertTrue(text, text.startsWith("Found 1 event match(es) for \"acme_paging_01\"."))
        assertFalse(text, text.contains(MagentoInspectionResults.TEXT_TRUNCATED_HINT))
        val limited = MagentoInspectionCommands.query(project, "observers_for_event", """{"eventName":"acme_paging","limit":5}""")
        assertTrue(limited, limited.contains("`limit` require `\"format\":\"json\"`"))
    }

    fun testTextRepliesStayAsBefore() {
        // Git may check the file out with CRLF on Windows; the replies always use LF.
        val expected = java.io.File("src/test/testData/mcp/inspect-text-replies.txt").readText().replace("\r\n", "\n")
        val cases = Regex("^### (\\S+) (.*)$", RegexOption.MULTILINE).findAll(expected).map { it.groupValues[1] to it.groupValues[2] }
        val actual = StringBuilder()
        for ((type, parameters) in cases) {
            actual.append("### $type $parameters\n").append(MagentoInspectionCommands.query(project, type, parameters)).append("\n")
        }
        assertEquals(9, cases.count())
        assertEquals(expected, actual.toString())
    }

    fun testCursorReuseFollowsTheResultItWasIssuedFor() {
        addEvents()
        val admin = events(JSONObject().put("area", "adminhtml").put("limit", 3))
        val reusedWithoutFilter = events(JSONObject().put("limit", 3).put("cursor", admin.getString("nextCursor")))
        assertEquals("stale_cursor", reusedWithoutFilter.getString("status"))
        assertEquals(0, reusedWithoutFilter.getJSONArray("records").length())
        assertTrue(reusedWithoutFilter.isNull("nextCursor"))
        val first = events(JSONObject().put("limit", 5))
        val wider = events(JSONObject().put("limit", 7).put("cursor", first.getString("nextCursor")))
        assertEquals(5, wider.getInt("offset"))
        assertEquals(7, wider.getInt("returned"))
        val parts = String(java.util.Base64.getUrlDecoder().decode(first.getString("nextCursor"))).split(':')
        val pastEnd = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("v1:999:${parts[2]}".toByteArray())
        assertEquals("invalid_request", events(JSONObject().put("cursor", pastEnd)).getString("status"))
    }

    fun testNestedModuleOwnsItsOwnFiles() {
        val outer = moduleDirectory()
        myFixture.addFileToProject("$outer/Nested/registration.php", """
            <?php \Magento\Framework\Component\ComponentRegistrar::register(\Magento\Framework\Component\ComponentRegistrar::MODULE, 'Foo_Nested', __DIR__);
        """.trimIndent())
        myFixture.addFileToProject("$outer/Nested/etc/module.xml", "<config><module name=\"Foo_Nested\"/></config>")
        myFixture.addFileToProject("$outer/Nested/etc/events.xml",
            "<config><event name=\"acme_nested\"><observer name=\"inner\" instance=\"Foo\\Nested\\Observer\"/></event></config>")
        myFixture.addFileToProject("$outer/etc/events.xml",
            "<config><event name=\"acme_nested\"><observer name=\"outer\" instance=\"Foo\\Bar2\\Observer\"/></event></config>")
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        val result = query("observers_for_event", JSONObject().put("eventName", "acme_nested").put("format", "json"))
        val owners = (0 until result.getJSONArray("records").length()).associate {
            result.getJSONArray("records").getJSONObject(it).let { record -> record.getString("observerName") to record.getString("module") }
        }
        assertEquals(mapOf("inner" to "Foo_Nested", "outer" to "Foo_Bar2"), owners)
        assertEquals(1, query("observers_for_event", JSONObject().put("eventName", "acme_nested").put("format", "json")
            .put("module", "Foo_Nested")).getInt("total"))
    }

    fun testCursorFromChangedDeclarationsIsStale() {
        val module = addEvents()
        val first = events(JSONObject().put("limit", 5))
        myFixture.addFileToProject("$module/etc/frontend/events.xml",
            "<config><event name=\"acme_paging_new\"><observer name=\"late\" instance=\"Foo\\Bar2\\Observer\\Late\"/></event></config>")
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        val stale = events(JSONObject().put("limit", 5).put("cursor", first.getString("nextCursor")))
        assertEquals("stale_cursor", stale.getString("status"))
    }

    fun testDiConfigAndModuleRecords() {
        val di = query("di_config", JSONObject().put("className", "Magento\\Theme\\Block\\Html\\Topmenu").put("format", "json"))
        assertEquals(di.toString(), "ok", di.getString("status"))
        val records = (0 until di.getJSONArray("records").length()).map { di.getJSONArray("records").getJSONObject(it) }
        val plugin = records.single { it.getString("kind") == "plugin" && it.getString("name") == "catalogTopmenu" }
        assertEquals("Magento\\Catalog\\Plugin\\Block\\Topmenu", plugin.getString("type"))
        assertTrue(plugin.getString("path").endsWith("vendor/magento/module-catalog/etc/di.xml"))
        assertTrue(records.any { it.getString("kind") == "type" })
        val module = query("module", JSONObject().put("moduleName", "Foo_Bar2").put("format", "json"))
        val record = module.getJSONArray("records").getJSONObject(0)
        assertEquals("Foo_Bar2", record.getString("module"))
        assertEquals(true, record.getBoolean("editable"))
        assertTrue(record.getString("etc").endsWith("/etc"))
    }

    private fun records(result: JSONObject): List<JSONObject> =
        (0 until result.getJSONArray("records").length()).map { result.getJSONArray("records").getJSONObject(it) }

    private fun addFiles(vararg files: Pair<String, String>) {
        for ((path, text) in files) myFixture.addFileToProject(path, text)
        PsiDocumentManager.getInstance(project).commitAllDocuments()
    }

    fun testModuleAtProjectRootOwnsItsRecordsAndAreas() {
        val observer = { name: String -> "<config><event name=\"acme_root_event\"><observer name=\"$name\" instance=\"Acme\\Root\\Observer\"/></event></config>" }
        addFiles(
            "etc/module.xml" to "<config><module name=\"Acme_Root\"/></config>",
            "etc/events.xml" to observer("root_global"),
            "etc/frontend/events.xml" to observer("root_frontend"),
            "etc/adminhtml/events.xml" to observer("root_adminhtml"),
            "etc/di.xml" to "<config><preference for=\"Acme\\Root\\Api\\Service\" type=\"Acme\\Root\\Model\\Service\"/></config>",
            "etc/frontend/di.xml" to "<config><type name=\"Acme\\Root\\Api\\Service\"><plugin name=\"root_plugin\" type=\"Acme\\Root\\Plugin\"/></type></config>"
        )
        fun rootEvents(parameters: JSONObject = JSONObject()) =
            query("observers_for_event", parameters.put("eventName", "acme_root_event").put("format", "json"))
        val observers = records(rootEvents())
        assertEquals(mapOf("etc/events.xml" to "global", "etc/frontend/events.xml" to "frontend",
            "etc/adminhtml/events.xml" to "adminhtml"), observers.associate { it.getString("path") to it.getString("area") })
        assertTrue(observers.toString(), observers.all { it.optString("module") == "Acme_Root" })
        assertEquals(3, rootEvents(JSONObject().put("module", "Acme_Root")).getInt("total"))
        assertEquals(listOf("root_frontend"), records(rootEvents(JSONObject().put("area", "frontend"))).map { it.getString("observerName") })
        assertEquals(listOf("root_adminhtml"), records(rootEvents(JSONObject().put("area", "adminhtml"))).map { it.getString("observerName") })
        assertEquals(listOf("root_adminhtml"), records(rootEvents(JSONObject().put("module", "Acme_Root")
            .put("path", "etc/adminhtml"))).map { it.getString("observerName") })

        fun di(parameters: JSONObject = JSONObject()) =
            query("di_config", parameters.put("className", "Acme\\Root\\Api\\Service").put("format", "json"))
        val declarations = records(di())
        assertEquals(listOf("preference@etc/di.xml/global", "plugin@etc/frontend/di.xml/frontend", "type@etc/frontend/di.xml/frontend"),
            declarations.map { "${it.getString("kind")}@${it.getString("path")}/${it.getString("area")}" })
        assertTrue(declarations.toString(), declarations.all { it.optString("module") == "Acme_Root" })
        assertEquals(3, di(JSONObject().put("module", "Acme_Root")).getInt("total"))
        assertEquals(setOf("type", "plugin"), records(di(JSONObject().put("module", "Acme_Root").put("area", "frontend")))
            .map { it.getString("kind") }.toSet())

        val module = records(query("module", JSONObject().put("moduleName", "Acme_Root").put("format", "json"))).single()
        assertEquals(".", module.getString("path"))
        assertEquals("Acme_Root", module.getString("module"))
        assertEquals("etc", module.getString("etc"))
        assertTrue(module.toString(), module.getBoolean("editable"))
        assertTrue(module.toString(), module.getBoolean("preferred"))
        val text = MagentoInspectionCommands.query(project, "module", """{"moduleName":"Acme_Root"}""")
        assertTrue(text, text.contains("\npath: .\neditable: yes\n"))
    }

    fun testRootModuleDoesNotClaimOtherPackages() {
        val declaration = "<config><preference for=\"Acme\\Root\\Shared\" type=\"Acme\\Root\\SharedImpl\"/></config>"
        addFiles(
            "etc/module.xml" to "<config><module name=\"Acme_Root\"/></config>",
            "etc/di.xml" to declaration,
            "vendor/x/y/app/etc/di.xml" to declaration,
            "app/etc/di.xml" to declaration,
            "packages/acme/composer-package/composer.json" to "{}",
            "packages/acme/composer-package/etc/di.xml" to declaration,
            "packages/acme/registered/registration.php" to "<?php",
            "packages/acme/registered/etc/di.xml" to declaration,
            "VENDOR/x/y/etc/di.xml" to declaration
        )
        val result = query("di_config", JSONObject().put("className", "Acme\\Root\\Shared").put("format", "json").put("limit", 100))
        assertEquals(mapOf(
            "etc/di.xml" to "Acme_Root",
            "vendor/x/y/app/etc/di.xml" to null,
            "app/etc/di.xml" to null,
            "packages/acme/composer-package/etc/di.xml" to null,
            "packages/acme/registered/etc/di.xml" to null,
            "VENDOR/x/y/etc/di.xml" to if (SystemInfo.isFileSystemCaseSensitive) "Acme_Root" else null
        ), modulesByPath(result))

        // A path built from the project base path names a file outside the project root: it does not resolve under
        // the root, or it starts with `../` even when it leads back into the root.
        val root = MagentoMcpSupport.projectRoot(project)!!
        val options = MagentoInspectionResults.options(JSONObject().put("format", "json"), setOf("module", "area", "path"))
        val outside = JSONObject(MagentoInspectionResults.respond(project, "di_config", "x", listOf(
            InspectionRecord("type", "x", "outside/a/etc/di.xml", 1, "global", emptyMap()),
            InspectionRecord("type", "x", "../${root.name}/etc/di.xml", 1, "global", emptyMap())
        ), options))
        assertEquals(mapOf("outside/a/etc/di.xml" to null, "../${root.name}/etc/di.xml" to null), modulesByPath(outside))
    }

    private fun modulesByPath(result: JSONObject): Map<String, String?> =
        records(result).associate { it.getString("path") to (if (it.isNull("module")) null else it.getString("module")) }

    fun testRootModuleHonoursTheComposerVendorDirectory() {
        val declaration = "<config><preference for=\"Acme\\Root\\Shared\" type=\"Acme\\Root\\SharedImpl\"/></config>"
        val composer = MagentoMcpSupport.projectRoot(project)!!.findChild("composer.json")!!
        WriteAction.runAndWait<java.io.IOException> {
            VfsUtil.saveText(composer, "{\"name\": \"acme/root\", \"config\": {\"vendor-dir\": \"./lib/deps/\"}}")
        }
        addFiles(
            "etc/module.xml" to "<config><module name=\"Acme_Root\"/></config>",
            "etc/di.xml" to declaration,
            "lib/deps/x/y/etc/di.xml" to declaration
        )
        val result = query("di_config", JSONObject().put("className", "Acme\\Root\\Shared").put("format", "json"))
        assertEquals(mapOf("etc/di.xml" to "Acme_Root", "lib/deps/x/y/etc/di.xml" to null), modulesByPath(result))
    }

    /**
     * The contract that lets owner lookups report `indexing`. The reply path checks dumb mode before it reads the
     * module index, so a race between that check and the lookup cannot be reproduced end to end.
     */
    fun testIndexedModuleDirectoriesThrowWhileIndexing() {
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            try {
                ModuleIndex(project).getIndexedModuleDirectories("Foo_Bar2")
                fail("expected IndexNotReadyException")
            } catch (_: IndexNotReadyException) {
                // The caller reports `indexing` instead of caching an incomplete owner map.
            }
        }
    }

    fun testDiConfigOrdersDeclarationsOnOneLineByName() {
        addFiles("app/code/Acme/Order/etc/di.xml" to "<config><type name=\"Acme\\Order\\Same\">" +
            "<plugin name=\"zeta\" type=\"Acme\\Order\\Zeta\"/><plugin name=\"alpha\" type=\"Acme\\Order\\Alpha\"/></type></config>")
        val result = query("di_config", JSONObject().put("className", "Acme\\Order\\Same").put("format", "json"))
        assertEquals(listOf("plugin/alpha", "plugin/zeta", "type/Acme\\Order\\Same"),
            records(result).map { "${it.getString("kind")}/${it.getString("name")}" })
    }

    fun testEveryIndexedCopyOfAModuleOwnsItsFiles() {
        val observer = { name: String -> "<config><event name=\"acme_twin_event\"><observer name=\"$name\" instance=\"Acme\\Twin\\Observer\"/></event></config>" }
        addFiles(
            "app/code/Acme/Twin/etc/module.xml" to "<config><module name=\"Acme_Twin\"/></config>",
            "app/code/Acme/Twin/etc/events.xml" to observer("code_copy"),
            "vendor/acme/module-twin/etc/module.xml" to "<config><module name=\"Acme_Twin\"/></config>",
            "vendor/acme/module-twin/etc/events.xml" to observer("vendor_copy")
        )
        fun names(parameters: JSONObject) = records(query("observers_for_event",
            parameters.put("eventName", "acme_twin_event").put("format", "json").put("module", "Acme_Twin"))).map { it.getString("observerName") }
        assertEquals(listOf("code_copy", "vendor_copy"), names(JSONObject()))
        assertEquals(listOf("code_copy"), names(JSONObject().put("path", "app/code/Acme/Twin")))
        assertEquals(listOf("vendor_copy"), names(JSONObject().put("path", "vendor/acme/module-twin")))

        fun modules(parameters: JSONObject = JSONObject()) =
            records(query("module", parameters.put("moduleName", "Acme_Twin").put("format", "json")))
        val copies = modules()
        assertEquals(setOf("app/code/Acme/Twin", "vendor/acme/module-twin"), copies.map { it.getString("path") }.toSet())
        assertEquals(2, copies.size)
        assertTrue(copies.toString(), copies.all { it.getString("module") == "Acme_Twin" })
        assertEquals(listOf(true, false), copies.map { it.getBoolean("preferred") })
        for (path in listOf("app/code/Acme/Twin", "vendor/acme/module-twin")) {
            assertEquals(listOf(path), modules(JSONObject().put("path", path)).map { it.getString("path") })
        }
        val text = MagentoInspectionCommands.query(project, "module", """{"moduleName":"Acme_Twin"}""")
        assertTrue(text, text.startsWith("Found 2 Magento module match(es)"))
        assertEquals(text, 1, text.split("preferred: yes").size - 1)
        assertEquals(text, 1, text.split("preferred: no").size - 1)
    }

    fun testDiConfigOrdersEveryDeclarationKindTogether() {
        addFiles(
            "app/code/Acme/Order/etc/adminhtml/di.xml" to """
                <config>
                    <type name="Acme\Order\Target">
                        <plugin name="admin_plugin" type="Acme\Order\AdminPlugin"/>
                    </type>
                    <preference for="Acme\Order\Target" type="Acme\Order\AdminTarget"/>
                </config>
            """.trimIndent(),
            "app/code/Acme/Order/etc/di.xml" to """
                <config>
                    <virtualType name="Acme\Order\Virtual" type="Acme\Order\Target"/>
                    <preference for="Acme\Order\TargetInterface" type="Acme\Order\Target"/>
                    <type name="Acme\Order\Target">
                        <plugin name="global_plugin" type="Acme\Order\GlobalPlugin"/>
                    </type>
                </config>
            """.trimIndent()
        )
        val result = query("di_config", JSONObject().put("className", "Acme\\Order\\Target").put("format", "json").put("limit", 100))
        val admin = "app/code/Acme/Order/etc/adminhtml/di.xml"
        val global = "app/code/Acme/Order/etc/di.xml"
        assertEquals(listOf("type@$admin:2", "plugin@$admin:3", "preference@$admin:5",
            "virtualType@$global:2", "preference@$global:3", "type@$global:4", "plugin@$global:5"),
            records(result).map { "${it.getString("kind")}@${it.getString("path")}:${it.getInt("line")}" })
    }

    fun testIndexingAndDisabledSupportAreStatuses() {
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            assertEquals("indexing", events().getString("status"))
        }
        Settings.getInstance(project).pluginEnabled = false
        try {
            assertEquals("unavailable", events().getString("status"))
        } finally {
            Settings.getInstance(project).pluginEnabled = true
        }
    }
}
