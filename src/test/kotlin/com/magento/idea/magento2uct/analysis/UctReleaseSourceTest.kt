/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.analysis

import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.testFramework.DumbModeTestUtils
import com.magento.idea.magento2plugin.PhysicalMagentoTestCase
import com.sun.net.httpserver.HttpServer
import org.junit.Assert.assertThrows
import java.net.InetSocketAddress
import java.nio.file.Files
import java.security.MessageDigest

class UctReleaseSourceTest : PhysicalMagentoTestCase() {
    private fun index(bytes: ByteArray): UctReleaseIndex {
        val archive = Files.createTempFile("uct-release-", ".zip")
        try {
            Files.write(archive, bytes)
            return UctReleaseSource().index(project, "2.4.9", archive, "a".repeat(64), {}, {})
        } finally { Files.deleteIfExists(archive) }
    }

    fun testUsesAllThreeExistingProcessorsAndExcludesTestsAndDependencies() {
        val prepared = index(UctReleaseFixture.archive("2.4.9"))
        val sample = "\\Magento\\Catalog\\Model\\Sample"
        assertEquals(2, prepared.processedFiles)
        assertTrue(prepared.existence[sample] == true)
        assertTrue(prepared.api[sample] == true)
        assertTrue(prepared.deprecation.keys.any { it.contains("oldMethod") })
        assertFalse(prepared.existence.keys.any { it.contains("PRIVATE_VALUE") || it.contains("hidden") || it.contains("secret") || it.contains("__construct") })
        assertTrue(prepared.api.keys.any { it.contains("PUBLIC_VALUE") })
        assertEquals(prepared, UctReleaseIndex.read(prepared.json()))
    }

    fun testReleasePreparationDoesNotRequireProjectIndexes() {
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            assertEquals(2, index(UctReleaseFixture.archive("2.4.9")).processedFiles)
        }
    }

    fun testRejectsMismatchedReleaseMalformedPhpAndIncompleteArchive() {
        assertThrows(IllegalArgumentException::class.java) { index(UctReleaseFixture.archive("2.4.9", metadataVersion = "2.4.8")) }
        assertThrows(IllegalArgumentException::class.java) {
            index(UctReleaseFixture.archive("2.4.9", mapOf("app/code/Magento/Catalog/Model/Broken.php" to "<?php class {")))
        }
        assertThrows(IllegalArgumentException::class.java) {
            index(UctReleaseFixture.archive("2.4.9", mapOf("lib/internal/Magento/Framework/AppInterface.php" to "<?php // missing framework interface")))
        }
        assertThrows(Exception::class.java) { index("not a ZIP".toByteArray()) }
    }

    fun testRejectsTraversalAndInvalidVersionBeforeDownloading() {
        assertThrows(IllegalArgumentException::class.java) {
            index(UctReleaseFixture.archive("2.4.9", mapOf("../outside.php" to "<?php class Outside {}")))
        }
        for (version in listOf("latest", "../../tmp", "2.4.9/other")) {
            assertThrows(IllegalArgumentException::class.java) { UctReleaseSource.url(version) }
        }
    }

    fun testHttpDownloadReportsByteAndFileProgressAndKeepsArchiveChecksum() {
        val bytes = UctReleaseFixture.archive("2.4.9")
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/release") { exchange ->
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        val archive = Files.createTempFile("uct-download-", ".zip")
        try {
            val progress = arrayListOf<UctPreparationProgress>()
            val source = UctReleaseSource { "http://127.0.0.1:${server.address.port}/release" }
            val prepared = source.prepare(project, "2.4.9", archive, {}, progress::add)
            assertEquals(MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }, prepared.archiveSha256)
            assertTrue(progress.any { it.phase == "downloading" && it.downloadedBytes == bytes.size.toLong() })
            assertEquals(2, progress.last().processedFiles)
            assertEquals(2, progress.last().totalFiles)
            assertEquals(UctReleaseSource.url("2.4.9"), prepared.sourceUrl)
        } finally { server.stop(0); Files.deleteIfExists(archive) }
    }

    fun testHttpFailureAndCancellationCannotProduceARelease() {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/missing") { exchange -> exchange.sendResponseHeaders(404, -1); exchange.close() }
        val bytes = UctReleaseFixture.archive("2.4.9")
        server.createContext("/release") { exchange ->
            exchange.sendResponseHeaders(200, bytes.size.toLong()); exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        val archive = Files.createTempFile("uct-failure-", ".zip")
        try {
            assertThrows(Exception::class.java) {
                UctReleaseSource { "http://127.0.0.1:${server.address.port}/missing" }.prepare(project, "2.4.9", archive, {}, {})
            }
            var cancel = false
            assertThrows(ProcessCanceledException::class.java) {
                UctReleaseSource { "http://127.0.0.1:${server.address.port}/release" }.prepare(project, "2.4.9", archive,
                    { if (cancel) throw ProcessCanceledException() }, { if (it.downloadedBytes > 0) cancel = true })
            }
        } finally { server.stop(0); Files.deleteIfExists(archive) }
    }
    fun testPreparedDeprecationDoesNotDependOnOpenProjectParentClasses() {
        val bytes = UctReleaseFixture.archive("2.4.9", mapOf(
            "app/code/Magento/Catalog/Model/ParentBlock.php" to """
                <?php namespace Magento\Catalog\Model;
                class ParentBlock { public function escapeHtml() {} }
            """.trimIndent(),
            "app/code/Magento/Catalog/Model/ChildBlock.php" to """
                <?php namespace Magento\Catalog\Model;
                class ChildBlock extends ParentBlock { public function escapeHtml() {} }
            """.trimIndent()
        ))
        val before = index(bytes)
        myFixture.addFileToProject("vendor/magento/catalog/Model/ParentBlock.php", """
            <?php namespace Magento\Catalog\Model;
            /** @deprecated */ class ParentBlock {
                /** @deprecated */ public function escapeHtml() {}
            }
        """.trimIndent())
        com.intellij.psi.PsiDocumentManager.getInstance(project).commitAllDocuments()
        val after = index(bytes)
        assertEquals(before.existence, after.existence)
        assertEquals(before.api, after.api)
        assertEquals(before.deprecation, after.deprecation)
    }

}
