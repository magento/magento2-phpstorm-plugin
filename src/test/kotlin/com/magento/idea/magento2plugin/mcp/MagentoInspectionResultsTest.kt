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
        for ((key, value) in listOf("limit" to 0, "limit" to 101, "path" to "/etc", "path" to "../x", "cursor" to "garbage")) {
            val result = events(JSONObject().put(key, value))
            assertEquals("$key=$value: $result", "invalid_request", result.getString("status"))
        }
        val xml = MagentoInspectionCommands.query(project, "observers_for_event", """{"eventName":"acme_paging","format":"xml"}""")
        assertTrue(xml, xml.contains("format must be `text` or `json`"))
        val missing = query("observers_for_event", JSONObject().put("format", "json"))
        assertEquals("invalid_request", missing.getString("status"))
        assertTrue(missing.getString("message").contains("`eventName`"))
        val layout = query("layout_entities", JSONObject().put("name", "default").put("format", "json"))
        assertEquals("invalid_request", layout.getString("status"))
    }

    fun testTextOutputIsUnchangedAndRejectsJsonOnlyOptions() {
        addEvents()
        val text = MagentoInspectionCommands.query(project, "observers_for_event", """{"eventName":"acme_paging_01"}""")
        assertTrue(text, text.startsWith("Found 1 event match(es) for \"acme_paging_01\"."))
        assertFalse(text, text.contains(MagentoInspectionResults.TEXT_TRUNCATED_HINT))
        val limited = MagentoInspectionCommands.query(project, "observers_for_event", """{"eventName":"acme_paging","limit":5}""")
        assertTrue(limited, limited.contains("`limit` require `\"format\":\"json\"`"))
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
