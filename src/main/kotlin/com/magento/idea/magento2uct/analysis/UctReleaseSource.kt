/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.analysis

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.io.HttpRequests
import com.jetbrains.php.lang.PhpFileType
import com.magento.idea.magento2uct.versioning.processors.ApiCoverageIndexProcessor
import com.magento.idea.magento2uct.versioning.processors.DeprecationIndexProcessor
import com.magento.idea.magento2uct.versioning.processors.ExistenceIndexProcessor
import org.json.JSONObject
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.zip.ZipFile

internal data class UctPreparationProgress(
    val phase: String = "queued",
    val downloadedBytes: Long = 0,
    val totalBytes: Long? = null,
    val processedFiles: Int = 0,
    val totalFiles: Int = 0
)

/** Uses only the requested official release; HTTP failures never select another version. */
internal class UctReleaseSource(private val endpoint: (String) -> String = ::url) {
    fun prepare(project: Project, version: String, archive: Path, check: () -> Unit,
                progress: (UctPreparationProgress) -> Unit): UctReleaseIndex {
        UctVersions.compare(version, version)
        val digest = MessageDigest.getInstance("SHA-256")
        HttpRequests.request(endpoint(version)).connectTimeout(10000).readTimeout(10000).connect { request ->
            val total = request.connection.contentLengthLong.takeIf { it >= 0 }
            require(total == null || total <= MAX_ARCHIVE_BYTES) { "Release archive exceeds 512 MiB." }
            var downloaded = 0L
            progress(UctPreparationProgress("downloading", totalBytes = total))
            request.inputStream.use { input ->
                Files.newOutputStream(archive).use { output ->
                    val buffer = ByteArray(65536)
                    while (true) {
                        check()
                        val count = input.read(buffer)
                        if (count < 0) break
                        downloaded += count
                        require(downloaded <= MAX_ARCHIVE_BYTES) { "Release archive exceeds 512 MiB." }
                        digest.update(buffer, 0, count)
                        output.write(buffer, 0, count)
                        progress(UctPreparationProgress("downloading", downloaded, total))
                    }
                }
            }
        }
        check()
        val checksum = digest.digest().joinToString("") { "%02x".format(it) }
        return index(project, version, archive, checksum, check, progress)
    }

    internal fun index(project: Project, version: String, archive: Path, checksum: String,
                       check: () -> Unit, progress: (UctPreparationProgress) -> Unit): UctReleaseIndex {
        val existence = ExistenceIndexProcessor()
        val api = ApiCoverageIndexProcessor()
        val deprecation = DeprecationIndexProcessor()
        ZipFile(archive.toFile()).use { zip ->
            val entries = zip.entries().asSequence().toList()
            require(entries.size <= 200000) { "Too many release archive entries." }
            // No archive paths are extracted or added to the IDE project.
            val prefix = "magento2-$version/"
            require(entries.all { it.name.startsWith(prefix) && it.name.removePrefix(prefix).split('/').none { part -> part == ".." } }) {
                "Unexpected release archive layout."
            }
            val composer = zip.getEntry(prefix + "composer.json") ?: error("Release archive has no composer.json.")
            require(composer.size in 1..MAX_FILE_BYTES) { "Invalid release metadata size." }
            val metadata = JSONObject(zip.getInputStream(composer).use { String(it.readAllBytes(), Charsets.UTF_8) })
            require(metadata.getString("name") == "magento/magento2ce" && metadata.getString("version") == version) {
                "Release archive metadata does not match requested Magento Open Source version $version."
            }
            require(zip.getEntry(prefix + "app/code/Magento/Catalog/etc/module.xml") != null) { "Missing Magento core modules." }
            val php = entries.filter { !it.isDirectory && runtimePhp(it.name.removePrefix(prefix)) }
            require(php.isNotEmpty() && php.size <= 50000) { "Invalid release PHP file count." }
            var expanded = 0L
            php.forEachIndexed { index, entry ->
                check()
                require(entry.size in 1..MAX_FILE_BYTES) { "Invalid release PHP file size: ${entry.name}" }
                expanded += entry.size
                require(expanded <= MAX_ARCHIVE_BYTES) { "Release PHP content exceeds 512 MiB." }
                val text = zip.getInputStream(entry).use { String(it.readAllBytes(), Charsets.UTF_8) }
                ReadAction.computeBlocking<Unit, RuntimeException> {
                    check()
                    val file = PsiFileFactory.getInstance(project).createFileFromText(
                        entry.name.substringAfterLast('/'), PhpFileType.INSTANCE, text
                    )
                    require(PsiTreeUtil.findChildOfType(file, PsiErrorElement::class.java) == null) {
                        "Cannot parse release PHP file: ${entry.name}. Update the IDE PHP parser before retrying."
                    }
                    existence.process(file)
                    api.process(file)
                    deprecation.process(file)
                }
                progress(UctPreparationProgress("indexing_release", Files.size(archive), Files.size(archive), index + 1, php.size))
            }
            fun core(values: Map<String, Boolean>) = values.filterKeys { it.startsWith("\\Magento\\") }
            return UctReleaseIndex(version, url(version), checksum, php.size,
                core(existence.snapshot()), core(api.snapshot()), core(deprecation.snapshot()))
        }
    }

    companion object {
        private const val MAX_ARCHIVE_BYTES = 512L * 1024 * 1024
        private const val MAX_FILE_BYTES = 8L * 1024 * 1024

        fun url(version: String): String {
            UctVersions.compare(version, version)
            return "https://codeload.github.com/magento/magento2/zip/refs/tags/$version"
        }

        internal fun runtimePhp(path: String): Boolean = path.endsWith(".php") &&
            (path.startsWith("app/code/Magento/") || path.startsWith("lib/internal/Magento/") || path.startsWith("setup/src/Magento/")) &&
            path.split('/').none { it.equals("test", true) || it.equals("tests", true) }
    }
}
