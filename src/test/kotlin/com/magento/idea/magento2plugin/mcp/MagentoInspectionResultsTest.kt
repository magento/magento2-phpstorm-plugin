/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.DumbModeTestUtils
import com.magento.idea.magento2plugin.BaseProjectTestCase
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
        val plugins = query("plugins_for_method", JSONObject().put("className", "A").put("methodName", "b").put("format", "json"))
        assertEquals("invalid_request", plugins.getString("status"))
        val schema = MagentoInspectionCommands.detailedSchema("module")
        assertTrue(schema, schema.contains("filters path") && schema.contains("path: project-relative"))
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
        val expected = java.io.File("src/test/testData/mcp/inspect-text-replies.txt").readText()
        val cases = Regex("^### (\\S+) (.*)$", RegexOption.MULTILINE).findAll(expected).map { it.groupValues[1] to it.groupValues[2] }
        val actual = StringBuilder()
        for ((type, parameters) in cases) {
            actual.append("### $type $parameters\n").append(MagentoInspectionCommands.query(project, type, parameters)).append("\n")
        }
        assertEquals(14, cases.count())
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

    fun testViewQueriesReturnEveryDeclaration() {
        val layout = query("layout_entities", JSONObject().put("name", "test_index").put("format", "json").put("limit", 100))
        assertEquals(layout.toString(), "ok", layout.getString("status"))
        val kinds = records(layout).map { it.getString("kind") }
        assertEquals(listOf("layout_handle", "layout_handle", "block", "block", "container", "container"), kinds)
        assertTrue(records(layout).all { it.getString("area") == "frontend" && it.getString("module") == "Magento_Catalog" })
        assertTrue(records(layout).filter { it.getString("kind") != "layout_handle" }.all { it.getInt("line") > 1 })
        assertEquals("no_matches", query("layout_entities", JSONObject().put("name", "test_index").put("format", "json")
            .put("area", "adminhtml")).getString("status"))

        val module = moduleDirectory()
        for (index in 0 until 12) {
            myFixture.addFileToProject("$module/view/adminhtml/layout/acme_page_$index.xml",
                "<page>\n<body>\n<block name=\"acme.repeated\" template=\"Foo_Bar2::t$index.phtml\"/>\n</body>\n</page>")
        }
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        val text = MagentoViewQueries.findLayoutEntities(project, "acme.repeated")
        assertTrue(text, text.endsWith(MagentoInspectionResults.TEXT_TRUNCATED_HINT))
        val repeated = query("layout_entities", JSONObject().put("name", "acme.repeated").put("format", "json")
            .put("limit", 100).put("area", "adminhtml"))
        val blocks = records(repeated).filter { it.getString("kind") == "block" && it.getString("key") == "acme.repeated" }
        assertEquals(12, blocks.size)
        assertEquals((0 until 12).map { "Foo_Bar2::t$it.phtml" }.toSet(), blocks.map { it.getString("template") }.toSet())
        assertTrue(blocks.all { it.getInt("line") == 3 && it.getString("module") == "Foo_Bar2" })

        val ui = query("ui_component", JSONObject().put("name", "recently_viewed").put("format", "json"))
        assertTrue(records(ui).map { it.getString("key") }.containsAll(listOf("recently_viewed", "recently_viewed_2")))
        assertTrue(records(ui).all { it.getString("area") == "frontend" && it.isNull("line") })

        val acl = query("acl_or_menu", JSONObject().put("identifier", "Magento_Catalog").put("format", "json").put("limit", 100))
        val aclRecords = records(acl)
        val resource = aclRecords.first { it.getString("kind") == "acl_resource" && it.getString("key") == "Magento_Catalog::test" }
        assertEquals("global", resource.getString("area"))
        assertTrue(resource.getJSONArray("tree").length() > 0)
        assertEquals(5, resource.getInt("line"))
        val menu = aclRecords.filter { it.getString("kind") == "menu_entry" }
        assertTrue(menu.isNotEmpty() && menu.all { it.getString("area") == "adminhtml" })
        assertEquals(aclRecords.indexOfFirst { it.getString("kind") == "menu_entry" },
            aclRecords.count { it.getString("kind") == "acl_resource" })
    }

    fun testViewAreaAndThemeOverrideModuleFollowThePath() {
        val cases = mapOf(
            "vendor/magento/module-catalog/view/frontend/layout/x.xml" to ("frontend" to null),
            "app/design/frontend/Acme/view/Magento_Catalog/layout/x.xml" to ("frontend" to "Magento_Catalog"),
            "vendor/magento/theme-adminhtml-backend/Magento_Backend/layout/x.xml" to ("adminhtml" to "Magento_Backend"),
            "vendor/hyva-themes/magento2-default-theme/Magento_Catalog/templates/x.phtml" to (null to "Magento_Catalog"),
            "app/code/Foo/Bar/etc/di.xml" to (null to null)
        )
        for ((path, expected) in cases) {
            assertEquals(path, expected.first, MagentoMcpSupport.viewArea(path))
            assertEquals(path, expected.second, MagentoMcpSupport.themeOverrideModule(path))
        }
    }

    fun testThemeOverridesAndSameLineDeclarationsAreRecords() {
        myFixture.addFileToProject("app/design/frontend/Acme/luma/Magento_Catalog/layout/acme_theme.xml",
            "<page><body><block name=\"acme.same\" template=\"a.phtml\"/><block name=\"acme.same\" template=\"b.phtml\"/></body></page>")
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        val result = query("layout_entities", JSONObject().put("name", "acme.same").put("format", "json")
            .put("module", "Magento_Catalog").put("area", "frontend"))
        val blocks = records(result).filter { it.getString("kind") == "block" }
        assertEquals(result.toString(), listOf("a.phtml", "b.phtml"), blocks.map { it.getString("template") })
        assertTrue(blocks.all { it.getInt("line") == 1 })
    }

    fun testUiAndAclTextSayWhenTheyWereCut() {
        val module = moduleDirectory()
        for (index in 0 until 21) {
            myFixture.addFileToProject("$module/view/adminhtml/ui_component/acme_grid_$index.xml", "<listing/>")
        }
        myFixture.addFileToProject("$module/etc/acl.xml", "<config><acl><resources>" +
            (0 until 21).joinToString("") { "<resource id=\"Foo_Bar2::acme_cut_$it\" title=\"T$it\"/>" } + "</resources></acl></config>")
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        val ui = MagentoViewQueries.findUiComponent(project, "acme_grid")
        assertTrue(ui, ui.endsWith(MagentoInspectionResults.TEXT_TRUNCATED_HINT))
        assertEquals(21, query("ui_component", JSONObject().put("name", "acme_grid").put("format", "json")).getInt("total"))
        val acl = MagentoViewQueries.findAclOrMenu(project, "acme_cut")
        assertTrue(acl, acl.endsWith(MagentoInspectionResults.TEXT_TRUNCATED_HINT))
        val aclJson = query("acl_or_menu", JSONObject().put("identifier", "acme_cut").put("format", "json").put("limit", 100))
        assertEquals(21, aclJson.getInt("total"))
        assertEquals(21, records(aclJson).map { it.getString("key") }.toSet().size)
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
