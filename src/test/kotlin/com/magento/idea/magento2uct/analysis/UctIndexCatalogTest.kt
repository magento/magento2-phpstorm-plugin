/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.analysis

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

class UctIndexCatalogTest {
    @Test
    fun ordersNumericVersionsPrereleasesAndPatches() {
        val versions = listOf("2.4.10", "2.4.9-p10", "2.4.9-p2", "2.4.9", "2.4.9-rc1", "2.4.9-beta10", "2.4.9-beta2")
        assertEquals(versions.reversed(), versions.sortedWith(UctVersions))
        assertThrows(IllegalArgumentException::class.java) { UctVersions.compare("latest", "2.4.3") }
    }

    @Test
    fun reportsOnlyVersionsWithAllThreeIndexFamilies() {
        val catalog = UctIndexCatalog(
            mapOf("2.4.2" to emptyMap(), "2.4.3" to emptyMap()),
            mapOf("2.4.2" to emptyMap(), "2.4.3" to emptyMap()),
            mapOf("2.4.2" to emptyMap())
        )
        assertEquals(listOf("2.4.2"), catalog.supportedVersions)
        assertThrows(IllegalArgumentException::class.java) { catalog.snapshot(null, "2.4.3", false) }
        assertThrows(IllegalArgumentException::class.java) { catalog.snapshot("2.4.3", "2.4.2", false) }
        assertThrows(IllegalArgumentException::class.java) { catalog.snapshot(null, "2.4.2", true) }
    }

    @Test
    fun snapshotsKeepTheirVersionsAndSuppressOnlyExistingIssues() {
        val catalog = fixtureCatalog()
        val before = catalog.snapshot(null, "2.4.2", false)
        val after = catalog.snapshot(null, "2.4.3", false)
        val delta = catalog.snapshot("2.4.2", "2.4.3", true)
        assertTrue(before.isExists("Magento\\Test\\Removed"))
        assertFalse(after.isExists("Magento\\Test\\Removed"))
        assertFalse(delta.isExists("Magento\\Test\\Removed"))
        assertEquals("2.4.3", after.getRemovedInVersion("Magento\\Test\\Removed"))
        assertFalse(before.isDeprecated("Magento\\Test\\Deprecated"))
        assertTrue(after.isDeprecated("Magento\\Test\\Deprecated"))
        assertTrue(delta.isDeprecated("Magento\\Test\\Deprecated"))
        assertFalse(after.isApi("Magento\\Test\\Internal"))
        assertTrue(delta.isApi("Magento\\Test\\Internal"))
        assertTrue(before.isExists("Magento\\Test\\Removed"))
        assertTrue(after.isExists("Foo\\Bar\\Unrelated"))
        assertTrue(after.isApi("Foo\\Bar\\Unrelated"))
        assertFalse(after.isPresentInCodebase("Foo\\Bar\\Unrelated"))
    }

    @Test
    fun removalAndRestorationFollowVersionOrder() {
        val symbol = "\\Magento\\Test\\Restored"
        val history = mapOf("2.4.10" to mapOf(symbol to true), "2.4.9" to mapOf(symbol to false), "2.4.8" to mapOf(symbol to true))
        val catalog = UctIndexCatalog(history, history, history.mapValues { emptyMap() })
        assertFalse(catalog.snapshot(null, "2.4.9", false).isExists(symbol))
        assertTrue(catalog.snapshot(null, "2.4.10", false).isExists(symbol))
    }

    @Test
    fun resourceLoaderRejectsMissingBaselineAndCorruptIndexes() {
        assertThrows(IllegalStateException::class.java) { UctIndexCatalog.load { null } }
        val bytes = ByteArrayOutputStream().also { out ->
            ObjectOutputStream(out).use { it.writeObject(mapOf("2.4.3" to mapOf("symbol" to true))) }
        }.toByteArray()
        assertThrows(IllegalArgumentException::class.java) { UctIndexCatalog.load { ByteArrayInputStream(bytes) } }
        val corrupt = ByteArrayOutputStream().also { out ->
            ObjectOutputStream(out).use { it.writeObject(mapOf("2.3.0" to mapOf("symbol" to "true"))) }
        }.toByteArray()
        assertThrows(IllegalArgumentException::class.java) { UctIndexCatalog.load { ByteArrayInputStream(corrupt) } }
    }

    @Test
    fun bundledResourcesProvideUsableOfflineCoverage() {
        val catalog = UctIndexCatalog.bundled()
        assertTrue(catalog.supportedVersions.contains("2.3.0"))
        assertTrue(catalog.supportedVersions.contains("2.4.3"))
        assertNotNull(catalog.snapshot("2.3.0", "2.4.3", false))
        assertThrows(IllegalArgumentException::class.java) { catalog.snapshot(null, "99.0.0", false) }
    }

    companion object {
        internal fun fixtureCatalog(): UctIndexCatalog {
            val existing = listOf("Removed", "Deprecated", "Internal").associate { "\\Magento\\Test\\$it" to true }
            return UctIndexCatalog(
                mapOf("2.4.2" to existing, "2.4.3" to mapOf("\\Magento\\Test\\Removed" to false)),
                mapOf("2.4.2" to existing.filterKeys { !it.endsWith("Internal") }, "2.4.3" to emptyMap()),
                mapOf("2.4.2" to emptyMap(), "2.4.3" to mapOf("\\Magento\\Test\\Deprecated" to true))
            )
        }
    }
    @Test
    fun unrelatedPreparationCannotChangeSnapshotOrIdentity() {
        val removed = "\\Magento\\Test\\BaselineOnly"
        val unrelated = "\\Magento\\Test\\Unrelated"
        val baseline = UctReleaseFixture.index("2.4.8-p5", setOf(removed))
        val target = UctReleaseFixture.index("2.4.9")
        val catalog = fixtureCatalog().withReleases(mapOf(baseline.version to baseline, target.version to target))
        val expanded = catalog.withReleases(mapOf(baseline.version to baseline, target.version to target,
            "2.4.8-p4" to UctReleaseFixture.index("2.4.8-p4", setOf(unrelated))))
        val before = catalog.snapshot(baseline.version, target.version, false)
        val after = expanded.snapshot(baseline.version, target.version, false)
        assertFalse(before.isExists(removed))
        assertFalse(after.isExists(removed))
        assertFalse(before.isPresentInCodebase(unrelated))
        assertFalse(after.isPresentInCodebase(unrelated))
        assertEquals(catalog.identity(baseline.version, target.version, false).toString(),
            expanded.identity(baseline.version, target.version, false).toString())
        val changed = catalog.withReleases(mapOf(baseline.version to baseline, target.version to UctReleaseFixture.index("2.4.9", setOf(removed))))
        assertNotEquals(catalog.identity(baseline.version, target.version, false).getString("indexRevision"),
            changed.identity(baseline.version, target.version, false).getString("indexRevision"))
    }

    @Test
    fun catalogDefensivelyCopiesMutableInputs() {
        val entries = mutableMapOf("\\Magento\\Test\\Stable" to true)
        val history = mapOf("2.4.3" to entries)
        val catalog = UctIndexCatalog(history, history, history)
        val identity = catalog.identity(null, "2.4.3", false).toString()
        entries.clear()
        assertTrue(catalog.snapshot(null, "2.4.3", false).isDeprecated("\\Magento\\Test\\Stable"))
        assertEquals(identity, catalog.identity(null, "2.4.3", false).toString())
    }

}
