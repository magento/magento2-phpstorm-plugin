/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.XmlSuppressionProvider
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlFile
import com.magento.idea.magento2plugin.inspections.xml.CacheableFalseInDefaultLayoutInspection
import com.magento.idea.magento2plugin.inspections.xml.ModuleDeclarationInModuleXmlInspection
import org.json.JSONArray
import org.json.JSONObject
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CancellationException

/** Bounded, explicitly scoped execution of existing local inspections; never applies quick fixes. */
internal object MagentoXmlDiagnostics {
    private data class Rule(val id: String, val severity: String, val fileName: String,
                            val parentName: String, val explanation: String,
                            val create: () -> LocalInspectionTool)
    private val rules = listOf(
        Rule("ModuleDeclarationInModuleXmlInspection", "error", "module.xml", "etc",
            "Checks the declared module name against its editable module directory; ignores dependency sequence names and vendor modules.",
            ::ModuleDeclarationInModuleXmlInspection),
        Rule("CacheableFalseInDefaultLayoutInspection", "warning", "default.xml", "layout",
            "Flags cacheable=false on block/referenceBlock in default.xml because it disables full-page caching across pages using this handle.",
            ::CacheableFalseInDefaultLayoutInspection)
    )
    private const val MAX_BYTES = 1_048_576
    private const val MAX_ELEMENTS = 50_000
    private const val MAX_FINDINGS = 1_000

    fun help(): String = JSONObject().put("tool", "magento_diagnostics")
        .put("modes", JSONArray(listOf("help", "check")))
        .put("scope", "One existing project-relative XML file; no recursive project scan. Read-only; quick fixes are never applied.")
        .put("rules", JSONArray(rules.map { JSONObject().put("ruleId", it.id).put("severity", it.severity)
            .put("explanation", it.explanation).put("fileName", it.fileName).put("parentDirectory", it.parentName) }))
        .put("limits", JSONObject().put("fileBytes", MAX_BYTES).put("psiElements", MAX_ELEMENTS)
            .put("collectedFindings", MAX_FINDINGS).put("returnedFindings", "limit: 1..100, default 25"))
        .put("coverage", "Only these selected inspections, using default rule severities and PSI suppression. No editor profile, schema validation, PHP semantics, merge-order or runtime validation. Files with syntax errors are reported as invalid_xml.").toString()

    fun failure(status: String, message: String): JSONObject = JSONObject().put("status", status)
        .put("successful", false).put("complete", false).put("readOnly", true).put("message", message)
        .put("diagnostics", JSONArray())

    /** Caller holds a read action. Resolve through the host's indexed VFS, not a desktop filesystem. */
    fun check(project: Project, filePath: String, ruleIds: List<String> = emptyList(), limit: Int = 25,
              checkCancelled: () -> Unit = ProgressManager::checkCanceled): JSONObject = guarded {
        validate(ruleIds, limit)
        val relative = Path.of(filePath)
        require(filePath.isNotBlank() && !relative.isAbsolute && relative.none { it.toString() == ".." }) {
            "filePath must be a project-relative file path without parent traversal."
        }
        if (DumbService.getInstance(project).isDumb) return@guarded failure("indexing", MagentoMcpSupport.INDEXES_NOT_READY_MESSAGE)
        checkCancelled()
        val root = Path.of(requireNotNull(project.basePath) { "Project path is unavailable." }).toRealPath()
        val target = root.resolve(relative).normalize()
        require(Files.isRegularFile(target) && target.toRealPath().startsWith(root)) { "File must exist inside the opened project." }
        val virtual = FilenameIndex.getVirtualFilesByName(target.fileName.toString(), GlobalSearchScope.allScope(project))
            .firstOrNull { Path.of(it.path).normalize() == target }
            ?: return@guarded failure("file_unavailable", "File is outside the indexed project scope; wait for indexing after updates.")
        if (virtual.length > MAX_BYTES) return@guarded failure("file_too_large", "File exceeds the $MAX_BYTES byte diagnostic limit.")
        val file = PsiManager.getInstance(project).findFile(virtual) as? XmlFile
            ?: return@guarded failure("unsupported_file", "Only XML PSI files are supported.")
        checkFile(file, ruleIds, limit, checkCancelled)
    }

    /** Kept separate so desktop regression tests exercise exactly the same inspection execution. */
    fun checkFile(file: XmlFile, ruleIds: List<String> = emptyList(), limit: Int = 25,
                  checkCancelled: () -> Unit = ProgressManager::checkCanceled): JSONObject = guarded {
        validate(ruleIds, limit)
        if (DumbService.getInstance(file.project).isDumb) return@guarded failure("indexing", MagentoMcpSupport.INDEXES_NOT_READY_MESSAGE)
        if (file.textLength > MAX_BYTES || file.virtualFile.length > MAX_BYTES) {
            return@guarded failure("file_too_large", "File exceeds the $MAX_BYTES byte diagnostic limit.")
        }
        val selected = rules.filter { ruleIds.isEmpty() || it.id in ruleIds }
        val applicable = selected.filter { file.name == it.fileName && file.virtualFile.parent?.name == it.parentName }
        val executions = applicable.map { rule ->
            val tool = rule.create()
            val holder = ProblemsHolder(InspectionManager.getInstance(file.project), file, false)
            Triple(rule, tool, holder)
        }
        val visitors = executions.map { (_, tool, holder) -> tool.buildVisitor(holder, false) }
        val pending = ArrayDeque<PsiElement>().apply { add(file) }
        var processed = 0
        var bounded = false
        while (pending.isNotEmpty()) {
            checkCancelled()
            if (processed++ >= MAX_ELEMENTS || executions.sumOf { it.third.resultCount } >= MAX_FINDINGS) {
                bounded = true
                break
            }
            val element = pending.removeLast()
            if (element is PsiErrorElement) return@guarded failure("invalid_xml", "XML contains syntax errors; diagnostics are incomplete.")
            visitors.forEach { element.accept(it) }
            // Reverse push preserves source order without recursive tree traversal.
            var child = element.lastChild
            while (child != null) { pending.add(child); child = child.prevSibling }
        }
        val document = file.viewProvider.document
            ?: return@guarded failure("file_unavailable", "Source document is unavailable.")
        val findings = executions.flatMap { (rule, _, holder) -> holder.results.mapNotNull { problem ->
            checkCancelled()
            val element = problem.psiElement ?: return@mapNotNull null
            if (XmlSuppressionProvider.isSuppressed(element, rule.id)) return@mapNotNull null
            val range = element.textRange
            val localRange = problem.textRangeInElement
            val start = range.startOffset + (localRange?.startOffset ?: 0)
            val end = range.startOffset + (localRange?.endOffset ?: range.length)
            fun position(offset: Int): JSONObject {
                val line = document.getLineNumber(offset)
                return JSONObject().put("line", line + 1).put("column", offset - document.getLineStartOffset(line) + 1)
                    .put("offset", offset)
            }
            JSONObject().put("ruleId", rule.id).put("severity", rule.severity)
                .put("message", problem.descriptionTemplate).put("explanation", rule.explanation)
                .put("path", MagentoMcpSupport.relativePath(file.project, file.virtualFile))
                .put("range", JSONObject().put("start", position(start)).put("end", position(end)))
        } }.sortedWith(compareBy({ it.getJSONObject("range").getJSONObject("start").getInt("offset") }, { it.getString("ruleId") }))
        val truncated = bounded || findings.size > limit
        JSONObject().put("status", if (truncated) "partial" else "ok").put("successful", !truncated)
            .put("complete", !truncated).put("readOnly", true).put("truncated", truncated)
            .put("processedElements", processed).put("collectedFindings", findings.size)
            .put("ruleIds", JSONArray(applicable.map { it.id }))
            .put("skippedRules", JSONArray((selected - applicable.toSet()).map { JSONObject().put("ruleId", it.id).put("reason", "not_applicable") }))
            .put("coverage", "Selected XML inspections only; default severities; no editor profile, schema or PHP semantic validation. Ranges are 1-based line/column and UTF-16 offsets; end is exclusive.")
            .put("diagnostics", JSONArray(findings.take(limit)))
    }

    private fun validate(ruleIds: List<String>, limit: Int) {
        require(limit in 1..100) { "limit must be between 1 and 100." }
        require(ruleIds.all { id -> rules.any { it.id == id } }) { "Unknown ruleId; use mode=help for supported rules. PHP inspections are unavailable." }
    }

    private inline fun guarded(action: () -> JSONObject): JSONObject = try { action() }
        catch (_: IndexNotReadyException) { failure("indexing", MagentoMcpSupport.INDEXES_NOT_READY_MESSAGE) }
        catch (_: ProcessCanceledException) { failure("cancelled", "Diagnostic execution was cancelled; results are incomplete.") }
        catch (_: CancellationException) { failure("cancelled", "Diagnostic execution was cancelled; results are incomplete.") }
        catch (error: IllegalArgumentException) { failure("invalid_request", error.message ?: "Invalid diagnostic request.") }
        catch (error: java.io.IOException) { failure("file_unavailable", error.message ?: "File is unavailable.") }
}
