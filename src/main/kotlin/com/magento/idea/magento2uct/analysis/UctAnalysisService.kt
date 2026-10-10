/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.analysis

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VFileProperty
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.magento.idea.magento2uct.execution.scanner.ModuleScanner
import com.magento.idea.magento2uct.execution.scanner.filter.ExcludeMagentoBundledFilter
import com.magento.idea.magento2uct.inspections.UctAnalysisContext
import com.magento.idea.magento2uct.inspections.UctInspectionManager
import com.magento.idea.magento2uct.packages.IssueSeverityLevel
import com.magento.idea.magento2uct.packages.SupportedIssue
import com.magento.idea.magento2uct.util.inspection.FilterDescriptorResultsUtil
import java.util.function.Consumer
import java.nio.file.Path

data class UctAnalysisRequest @JvmOverloads constructor(
    val paths: List<String>,
    val currentVersion: String?,
    val targetVersion: String,
    val minimumSeverity: IssueSeverityLevel,
    val ignoreCurrentVersion: Boolean,
    val explainSuppressed: Boolean = false
)

data class UctFinding(
    val filePath: String,
    val line: Int,
    val column: Int,
    val message: String,
    val issue: SupportedIssue
)

data class UctAnalysisProgress(val processedFiles: Int, val totalFiles: Int)
data class UctAnalysisResult(
    val findings: List<UctFinding>, val processedFiles: Int, val modules: Int, val themes: Int,
    val analysisIdentity: String? = null,
    val fileTypeCounts: Map<String, Int> = emptyMap(),
    val testFiles: Int = 0,
    val suppressedByRule: Map<Int, Int>? = null,
    val suppressedBySeverity: Map<String, Int>? = null
)

/** Shared scan engine. It does not open windows, save documents, write reports, or change settings. */
class UctAnalysisService @JvmOverloads constructor(
    private val project: Project,
    private val catalog: UctIndexCatalog = project.getService(UctReleasePreparation::class.java).catalog()
) {
    companion object {
        /** Caller holds read access. Keep MCP file validation and scan coverage identical. */
        internal fun isSupportedFile(project: Project, file: VirtualFile): Boolean =
            SupportedIssue.getSupportedFileTypes().any { it.isInstance(PsiManager.getInstance(project).findFile(file)) }
    }

    private data class Component(val directory: VirtualFile, val theme: Boolean)

    fun analyze(
        request: UctAnalysisRequest,
        cancellation: Runnable,
        progress: Consumer<UctAnalysisProgress>
    ): UctAnalysisResult {
        val context = UctAnalysisContext(
            request.minimumSeverity,
            catalog.snapshot(request.currentVersion, request.targetVersion, request.ignoreCurrentVersion)
        )
        require(!request.explainSuppressed || request.ignoreCurrentVersion) {
            "explainSuppressed requires ignoreCurrentVersion=true."
        }
        val fullContext = if (request.explainSuppressed) UctAnalysisContext(request.minimumSeverity,
            catalog.snapshot(request.currentVersion, request.targetVersion, false)) else null
        val identity = catalog.identity(request.currentVersion, request.targetVersion, request.ignoreCurrentVersion).toString()
        val suppressedByRule = sortedMapOf<Int, Int>()
        val suppressedBySeverity = sortedMapOf<String, Int>()
        val fileTypeCounts = sortedMapOf<String, Int>()
        var testFiles = 0
        fun check() {
            cancellation.run()
            ProgressManager.checkCanceled()
            if (project.isDisposed) throw ProcessCanceledException()
            check(!DumbService.isDumb(project)) { "Indexes are not ready. Wait for indexing to finish and retry." }
        }
        fun <T> read(action: () -> T): T {
            check()
            return ReadAction.computeBlocking<T, RuntimeException> { check(); action() }
        }

        val components = linkedMapOf<String, Component>()
        val files = linkedMapOf<String, VirtualFile>()
        val roots = request.paths.distinct().map { path ->
            LocalFileSystem.getInstance().findFileByNioFile(Path.of(path))
                ?: throw IllegalArgumentException("Analysis path does not exist: $path")
        }
        val excluded = ExcludeMagentoBundledFilter()
        val directories = ArrayDeque(roots.filter { it.isDirectory })
        roots.filterNot { it.isDirectory }.forEach { files[it.path] = it }
        val visited = hashSetOf<String>()
        while (directories.isNotEmpty()) {
            val directory = directories.removeFirst()
            if (!visited.add(directory.path) || directory.`is`(VFileProperty.SYMLINK)) continue
            require(visited.size <= 10000) { "Scan exceeds 10000 directories. Choose a narrower scope." }
            read {
                val psiDirectory = PsiManager.getInstance(project).findDirectory(directory)
                    ?: error("Analysis directory is unavailable: ${directory.path}")
                val found = ModuleScanner(psiDirectory).scanDirectory(psiDirectory)
                if (found.isEmpty()) {
                    directories.addAll(directory.children.filter { it.isDirectory })
                } else {
                    found.filterNot { excluded.isExcluded(it) }.forEach {
                        components[directory.path] = Component(directory, it.type.toString() == "theme")
                    }
                }
            }
        }
        require(components.isNotEmpty() || files.isNotEmpty()) { "No supported PHP, PHTML, XML, or HTML files or custom Magento components found in the requested scope. JavaScript, images, and other unsupported files are not scanned. Select a supported file or a custom module/theme directory." }

        val pending = ArrayDeque(components.values.map { it.directory })
        visited.clear()
        while (pending.isNotEmpty()) {
            val directory = pending.removeFirst()
            if (!visited.add(directory.path) || directory.`is`(VFileProperty.SYMLINK)) continue
            require(visited.size <= 10000 && files.size <= 10000) { "Scan exceeds 10000 files or directories. Choose a narrower scope." }
            read {
                directory.children.filterNot { it.`is`(VFileProperty.SYMLINK) }.forEach {
                    if (it.isDirectory) pending.add(it) else files[it.path] = it
                }
            }
        }
        require(files.size <= 10000) { "Scan exceeds 10000 files. Choose a narrower scope." }
        val supported = files.values.filter { file ->
            read {
                isSupportedFile(project, file)
            }
        }.sortedBy { it.path }
        require(supported.isNotEmpty()) { "No supported PHP, PHTML, XML, or HTML files found in the requested scope." }
        val findings = arrayListOf<UctFinding>()
        progress.accept(UctAnalysisProgress(0, supported.size))
        supported.forEachIndexed { index, virtualFile ->
            val fileFindings = read {
                val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                    ?: error("Analysis file is unavailable: ${virtualFile.path}")
                val documentManager = PsiDocumentManager.getInstance(project)
                val document = documentManager.getDocument(psiFile)
                check(document == null || documentManager.isCommitted(document)) {
                    "Document changes are not committed for ${virtualFile.path}. Retry after the IDE finishes processing edits."
                }
                val holder = UctInspectionManager(project).run(psiFile, context)
                    ?: error("Unsupported analysis file: ${virtualFile.path}")
                val type = virtualFile.extension?.lowercase()?.takeIf { it in setOf("php", "phtml", "xml", "html") }
                    ?: if (psiFile is com.jetbrains.php.lang.psi.PhpFile) "php" else "xml"
                fileTypeCounts[type] = (fileTypeCounts[type] ?: 0) + 1
                val relative = Path.of(project.basePath!!).relativize(Path.of(virtualFile.path))
                if (relative.any { it.toString().lowercase() in setOf("test", "tests", "fixture", "fixtures", "_files") }) testFiles++
                if (fullContext != null) {
                    val full = UctInspectionManager(project).run(psiFile, fullContext)!!
                    fun key(descriptor: com.intellij.codeInspection.ProblemDescriptor, source: com.magento.idea.magento2uct.inspections.UctProblemsHolder) =
                        Triple(descriptor.psiElement.textRange.startOffset, source.getIssue(descriptor).code, descriptor.descriptionTemplate)
                    val retained = holder.results.map { key(it, holder) }.toSet()
                    full.results.distinctBy { key(it, full) }.filterNot { key(it, full) in retained }.forEach {
                        val issue = full.getIssue(it)
                        suppressedByRule[issue.code] = (suppressedByRule[issue.code] ?: 0) + 1
                        val severity = issue.level.name.lowercase()
                        suppressedBySeverity[severity] = (suppressedBySeverity[severity] ?: 0) + 1
                    }
                    require(suppressedByRule.values.sum() <= 50000) { "Scan exceeds 50000 suppressed findings. Choose a narrower scope." }
                }
                FilterDescriptorResultsUtil.filter(holder).map { descriptor ->
                    val offset = descriptor.psiElement.textRange.startOffset
                    val line = document?.getLineNumber(offset) ?: descriptor.lineNumber
                    val column = if (document == null) 1 else offset - document.getLineStartOffset(line) + 1
                    UctFinding(
                        virtualFile.path, line + 1, column,
                        descriptor.descriptionTemplate.replaceFirst(Regex("^\\[\\d+][ \\t]*"), ""),
                        holder.getIssue(descriptor)
                    )
                }
            }
            findings.addAll(fileFindings)
            require(findings.size <= 50000) { "Scan exceeds 50000 findings. Choose a narrower scope." }
            progress.accept(UctAnalysisProgress(index + 1, supported.size))
        }
        check()
        return UctAnalysisResult(
            findings.sortedWith(compareBy({ it.filePath }, { it.line }, { it.column }, { it.issue.code }, { it.message })),
            supported.size, components.values.count { !it.theme }, components.values.count { it.theme },
            identity, fileTypeCounts.toMap(), testFiles,
            if (request.explainSuppressed) suppressedByRule.toMap() else null,
            if (request.explainSuppressed) suppressedBySeverity.toMap() else null
        )
    }
}
