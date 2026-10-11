package com.magento.idea.magento2uct.analysis

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.time.Instant
import java.util.UUID

class UctRunHistoryTest {
    @Rule @JvmField val temporary = TemporaryFolder()

    @Test fun reportsAreBoundedAndTheNewestReportAlwaysSurvives() {
        val root = temporary.newFolder().toPath()
        val history = UctRunHistory(root)
        var last = ""
        repeat(UctRunHistory.MAX_REPORTS + 2) {
            last = UUID.randomUUID().toString()
            history.save(last, JSONObject().put("state", "completed"))
            assertTrue(history.info(last).getBoolean("saved"))
        }
        assertEquals(UctRunHistory.MAX_REPORTS.toLong(), Files.list(root).use { it.count() })
        assertNotNull(history.read(last))
        assertNull(history.read("../$last"))
    }

    @Test fun expiredReportsAreNotReturnedAfterRestart() {
        val root = temporary.newFolder().toPath()
        val time = Instant.parse("2026-10-10T12:00:00Z")
        val id = UUID.randomUUID().toString()
        UctRunHistory(root) { time }.save(id, JSONObject().put("state", "failed"))
        Files.setLastModifiedTime(root.resolve("$id.json"), FileTime.from(time))
        val reopened = UctRunHistory(root) { time.plusSeconds(31 * 86400L) }
        assertNull(reopened.read(id))
        assertFalse(reopened.info(id).getBoolean("saved"))
    }

    @Test fun exportFailuresAreVisibleAndDoNotInventAReportPath() {
        val root = temporary.newFile().toPath()
        val history = UctRunHistory(root)
        val id = UUID.randomUUID().toString()
        history.save(id, JSONObject().put("state", "completed"))
        val info = history.info(id)
        assertFalse(info.getBoolean("saved"))
        assertTrue(info.isNull("path"))
        assertTrue(info.getString("error").contains("export failed"))
    }
}
