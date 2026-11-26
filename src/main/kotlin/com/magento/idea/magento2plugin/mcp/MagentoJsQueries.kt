/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.ID
import com.magento.idea.magento2plugin.stubs.indexes.js.KnockoutTemplateIndex
import com.magento.idea.magento2plugin.stubs.indexes.ui.DisplayAreaIndex
import com.magento.idea.magento2plugin.stubs.indexes.ui.GetRegionUsageIndex
import com.magento.idea.magento2plugin.stubs.indexes.ui.UiComponentComponentDeclarationIndex
import com.magento.idea.magento2plugin.stubs.indexes.ui.UiComponentTemplateDeclarationIndex
import com.magento.idea.magento2plugin.stubs.indexes.ui.data.UiComponentNavigationData
import com.magento.idea.magento2plugin.util.magento.js.JsPsiSupportProvider
import com.magento.idea.magento2plugin.util.magento.js.MagentoJsParser
import com.magento.idea.magento2plugin.util.magento.js.RequireJsPathResolver
import com.magento.idea.magento2plugin.util.magento.ui.TemplatePathResolver
import java.io.IOException

internal object MagentoJsQueries {
    private val COMPONENT_INDEX_KEY_PREFIXES = listOf("componentName:", "componentJsPath:")
    private val TEMPLATE_INDEX_KEY_PREFIXES = listOf("templatePath:", "componentName:", "componentJsPath:")

    private data class MixinDecl(
        val target: String,
        val mixin: String,
        val enabled: Boolean,
        val filePath: String,
        val line: Int,
        val targetFiles: List<String>,
        val mixinFiles: List<String>
    )

    private data class MapDecl(
        val context: String,
        val alias: String,
        val target: String,
        val filePath: String,
        val line: Int,
        val targetFiles: List<String>
    )

    private data class PathDecl(
        val alias: String,
        val target: String,
        val filePath: String,
        val line: Int,
        val targetFiles: List<String>
    )

    private data class UiUsageDecl(
        val kind: String,
        val value: String,
        val filePath: String,
        val line: Int?,
        val candidates: List<String>,
        val componentName: String? = null,
        val componentJsPath: String? = null
    )

    /**
     * Searches `requirejs-config.js` declarations (`mixins`, `map`, `paths`) and JS/XML/HTML UI component &
     * Knockout template declarations (`component`, `template`, `displayArea`, `getRegion`) without requiring
     * the JetBrains JavaScript or PHP plugins.
     */
    fun findJsConfig(project: Project, name: String, indexKeys: MagentoIndexKeys? = null): String {
        val query = name.trim()
        if (query.isEmpty()) {
            return "Provide a RequireJS module path, alias, mixin, UI component, or Knockout template name."
        }

        val (mixins, maps, paths) = collectRequireJsDeclarations(project, query)
        val usages = collectUiUsages(project, query, indexKeys)

        val lines = mutableListOf<String>()
        if (mixins.isNotEmpty()) {
            lines += "mixins"
            for (decl in mixins.take(MagentoMcpSupport.MAX_MATCHES)) {
                val status = if (decl.enabled) "enabled" else "disabled"
                lines += "${decl.target} -> mixin=${decl.mixin} ($status) file=${decl.filePath}:${decl.line}"
            }
        }

        if (maps.isNotEmpty()) {
            if (lines.isNotEmpty()) lines += ""
            lines += "map"
            for (decl in maps.take(MagentoMcpSupport.MAX_MATCHES)) {
                lines += "[${decl.context}] ${decl.alias} -> ${decl.target} file=${decl.filePath}:${decl.line}"
            }
        }

        if (paths.isNotEmpty()) {
            if (lines.isNotEmpty()) lines += ""
            lines += "paths"
            for (decl in paths.take(MagentoMcpSupport.MAX_MATCHES)) {
                lines += "${decl.alias} -> ${decl.target} file=${decl.filePath}:${decl.line}"
            }
        }

        if (usages.isNotEmpty()) {
            if (lines.isNotEmpty()) lines += ""
            lines += "ui components and templates"
            for (decl in usages.take(MagentoMcpSupport.MAX_MATCHES)) {
                val loc = if (decl.line != null) "${decl.filePath}:${decl.line}" else decl.filePath
                lines += "${decl.kind}: ${decl.value} -> file=$loc"
            }
        }

        if (lines.isEmpty()) {
            return "No RequireJS configurations or JS UI component/template declarations matched \"$query\"."
        }

        val truncated = mixins.size > MagentoMcpSupport.MAX_MATCHES ||
            maps.size > MagentoMcpSupport.MAX_MATCHES ||
            paths.size > MagentoMcpSupport.MAX_MATCHES ||
            usages.size > MagentoMcpSupport.MAX_MATCHES
        val hint = if (truncated) "\n\n${MagentoInspectionResults.TEXT_TRUNCATED_HINT}" else ""
        return "JS configuration and UI declarations for \"$query\"\n\n${lines.joinToString("\n")}$hint"
    }

    fun jsConfigRecords(project: Project, name: String, indexKeys: MagentoIndexKeys? = null): List<InspectionRecord> {
        val query = name.trim()
        val (mixins, maps, paths) = collectRequireJsDeclarations(project, query)
        val usages = collectUiUsages(project, query, indexKeys)

        val records = mutableListOf<InspectionRecord>()
        for (decl in mixins) {
            records += InspectionRecord(
                kind = "js_mixin",
                key = decl.target,
                path = decl.filePath,
                line = decl.line,
                area = MagentoMcpSupport.viewArea(decl.filePath),
                fields = linkedMapOf(
                    "target" to decl.target,
                    "mixin" to decl.mixin,
                    "enabled" to decl.enabled,
                    "targetFiles" to decl.targetFiles,
                    "mixinFiles" to decl.mixinFiles
                ),
                moduleHint = MagentoMcpSupport.themeOverrideModule(decl.filePath)
            )
        }
        for (decl in maps) {
            records += InspectionRecord(
                kind = "requirejs_map",
                key = decl.alias,
                path = decl.filePath,
                line = decl.line,
                area = MagentoMcpSupport.viewArea(decl.filePath),
                fields = linkedMapOf(
                    "context" to decl.context,
                    "alias" to decl.alias,
                    "target" to decl.target,
                    "targetFiles" to decl.targetFiles
                ),
                moduleHint = MagentoMcpSupport.themeOverrideModule(decl.filePath)
            )
        }
        for (decl in paths) {
            records += InspectionRecord(
                kind = "requirejs_path",
                key = decl.alias,
                path = decl.filePath,
                line = decl.line,
                area = MagentoMcpSupport.viewArea(decl.filePath),
                fields = linkedMapOf(
                    "alias" to decl.alias,
                    "target" to decl.target,
                    "targetFiles" to decl.targetFiles
                ),
                moduleHint = MagentoMcpSupport.themeOverrideModule(decl.filePath)
            )
        }
        for (decl in usages) {
            val fields = linkedMapOf<String, Any?>(
                "value" to decl.value,
                "candidates" to decl.candidates
            )
            decl.componentName?.let { fields["componentName"] = it }
            decl.componentJsPath?.let { fields["componentJsPath"] = it }
            records += InspectionRecord(
                kind = decl.kind,
                key = decl.value,
                path = decl.filePath,
                line = decl.line,
                area = MagentoMcpSupport.viewArea(decl.filePath),
                fields = fields,
                moduleHint = MagentoMcpSupport.themeOverrideModule(decl.filePath)
            )
        }
        return records
    }

    private fun collectRequireJsDeclarations(
        project: Project,
        query: String
    ): Triple<List<MixinDecl>, List<MapDecl>, List<PathDecl>> {
        val mixins = mutableListOf<MixinDecl>()
        val maps = mutableListOf<MapDecl>()
        val paths = mutableListOf<PathDecl>()
        val jsResolver = RequireJsPathResolver.getInstance()
        val jsProvider = JsPsiSupportProvider.getInstance()
        val psiManager = if (jsProvider != null) PsiManager.getInstance(project) else null
        val resolvedJsCache = hashMapOf<String, List<String>>()

        fun resolveJsCandidates(jsPath: String): List<String> = resolvedJsCache.getOrPut(jsPath) {
            jsResolver.findJsFiles(project, jsPath)
                .map { MagentoMcpSupport.relativePath(project, it) }
                .distinct()
                .sorted()
        }

        val configFiles = FilenameIndex.getVirtualFilesByName("requirejs-config.js", GlobalSearchScope.allScope(project))
            .sortedBy { MagentoMcpSupport.relativePath(project, it) }

        for (file in configFiles) {
            ProgressManager.checkCanceled()
            val text = readText(file) ?: continue
            val parsed = (if (jsProvider != null && psiManager != null) {
                psiManager.findFile(file)?.let { jsProvider.parseRequireJsConfig(it) }
            } else {
                null
            }) ?: MagentoJsParser.parseRequireJsConfig(text)
            if (parsed.mixinEntries.isEmpty() && parsed.mapEntries.isEmpty() && parsed.pathEntries.isEmpty()) continue
            val filePath = MagentoMcpSupport.relativePath(project, file)
            val lineIndex = MagentoMcpSupport.LineIndex(text)

            for (entry in parsed.mixinEntries) {
                if (MagentoMcpSupport.fuzzyMatch(entry.target, query) ||
                    MagentoMcpSupport.fuzzyMatch(entry.mixin, query)
                ) {
                    mixins += MixinDecl(
                        target = entry.target,
                        mixin = entry.mixin,
                        enabled = entry.enabled,
                        filePath = filePath,
                        line = lineIndex.lineAt(entry.keyOffset),
                        targetFiles = resolveJsCandidates(entry.target),
                        mixinFiles = resolveJsCandidates(entry.mixin)
                    )
                }
            }

            for (entry in parsed.mapEntries) {
                if (MagentoMcpSupport.fuzzyMatch(entry.alias, query) ||
                    MagentoMcpSupport.fuzzyMatch(entry.target, query) ||
                    MagentoMcpSupport.fuzzyMatch(entry.context, query)
                ) {
                    maps += MapDecl(
                        context = entry.context,
                        alias = entry.alias,
                        target = entry.target,
                        filePath = filePath,
                        line = lineIndex.lineAt(entry.keyOffset),
                        targetFiles = resolveJsCandidates(entry.target)
                    )
                }
            }

            for (entry in parsed.pathEntries) {
                if (MagentoMcpSupport.fuzzyMatch(entry.alias, query) ||
                    MagentoMcpSupport.fuzzyMatch(entry.target, query)
                ) {
                    paths += PathDecl(
                        alias = entry.alias,
                        target = entry.target,
                        filePath = filePath,
                        line = lineIndex.lineAt(entry.keyOffset),
                        targetFiles = resolveJsCandidates(entry.target)
                    )
                }
            }
        }

        return Triple(
            mixins.sortedWith(compareBy({ it.target }, { it.mixin }, { it.filePath }, { it.line })),
            maps.sortedWith(compareBy({ it.alias }, { it.target }, { it.filePath }, { it.line })),
            paths.sortedWith(compareBy({ it.alias }, { it.target }, { it.filePath }, { it.line }))
        )
    }

    private fun collectUiUsages(
        project: Project,
        query: String,
        indexKeys: MagentoIndexKeys?
    ): List<UiUsageDecl> {
        val results = mutableListOf<UiUsageDecl>()
        val scope = GlobalSearchScope.allScope(project)
        val fileIndex = FileBasedIndex.getInstance()
        val jsResolver = RequireJsPathResolver.getInstance()
        val templateResolver = TemplatePathResolver.getInstance()
        val virtualFileManager = VirtualFileManager.getInstance()
        val lineCache = hashMapOf<String, Pair<String, MagentoMcpSupport.LineIndex>>()
        val jsCandidateCache = hashMapOf<String, List<String>>()
        val templateCandidateCache = hashMapOf<String, List<String>>()

        fun resolveJsCandidates(path: String): List<String> = jsCandidateCache.getOrPut(path) {
            jsResolver.findJsFiles(project, path)
                .map { MagentoMcpSupport.relativePath(project, it) }
                .distinct()
                .sorted()
        }

        fun resolveTemplateCandidates(path: String): List<String> = templateCandidateCache.getOrPut(path) {
            templateResolver.findTemplateFiles(project, path)
                .map { MagentoMcpSupport.relativePath(project, it) }
                .distinct()
                .sorted()
        }

        fun fileLineIndex(file: VirtualFile, relativePath: String): Pair<String, MagentoMcpSupport.LineIndex>? {
            return lineCache[relativePath] ?: run {
                val content = readText(file) ?: return@run null
                val entry = content to MagentoMcpSupport.LineIndex(content)
                lineCache[relativePath] = entry
                entry
            }
        }

        fun lineAtOffset(file: VirtualFile, relativePath: String, offset: Int): Int? {
            if (offset < 0) return null
            val (content, lineIndex) = fileLineIndex(file, relativePath) ?: return null
            return lineIndex.lineAt(offset.coerceAtMost(content.length))
        }

        fun findLine(file: VirtualFile, relativePath: String, needle: String): Int? {
            val (text, lineIndex) = fileLineIndex(file, relativePath) ?: return null
            val idx = text.indexOf(needle)
            return if (idx >= 0) lineIndex.lineAt(idx) else null
        }

        fun <V> readKeys(indexId: ID<String, V>): Collection<String> =
            indexKeys?.keys(indexId, project) ?: fileIndex.getAllKeys(indexId, project)

        fun collectNavigationDeclarations(
            indexId: ID<String, Set<UiComponentNavigationData>>,
            kind: String,
            prefixes: List<String>,
            valueSelector: (UiComponentNavigationData) -> String,
            candidatesSelector: (UiComponentNavigationData) -> List<String>
        ) {
            val matchedRawKeys = matchingRawKeys(readKeys(indexId), query, prefixes)
            for (rawKey in matchedRawKeys) {
                ProgressManager.checkCanceled()
                for (declSet in fileIndex.getValues(indexId, rawKey, scope)) {
                    for (decl in declSet) {
                        ProgressManager.checkCanceled()
                        val file = virtualFileManager.findFileByUrl(decl.fileUrl) ?: continue
                        val relPath = MagentoMcpSupport.relativePath(project, file)
                        val value = valueSelector(decl)
                        val line = lineAtOffset(file, relPath, decl.valueOffset) ?: findLine(file, relPath, value)
                        results += UiUsageDecl(
                            kind = kind,
                            value = value,
                            filePath = relPath,
                            line = line,
                            candidates = candidatesSelector(decl),
                            componentName = decl.componentName,
                            componentJsPath = decl.componentJsPath
                        )
                    }
                }
            }
        }

        collectNavigationDeclarations(
            indexId = UiComponentComponentDeclarationIndex.KEY,
            kind = "ui_component_declaration",
            prefixes = COMPONENT_INDEX_KEY_PREFIXES,
            valueSelector = { decl -> decl.componentJsPath?.takeIf { it.isNotBlank() } ?: decl.value },
            candidatesSelector = { decl ->
                decl.componentJsPath?.takeIf { it.isNotBlank() }?.let(::resolveJsCandidates) ?: emptyList()
            }
        )
        collectNavigationDeclarations(
            indexId = UiComponentTemplateDeclarationIndex.KEY,
            kind = "ui_template_declaration",
            prefixes = TEMPLATE_INDEX_KEY_PREFIXES,
            valueSelector = { decl -> decl.value },
            candidatesSelector = { decl -> resolveTemplateCandidates(decl.value) }
        )

        val matchedKnockoutKeys = MagentoMcpSupport.prioritizeMatches(readKeys(KnockoutTemplateIndex.KEY), query)
        for (key in matchedKnockoutKeys) {
            ProgressManager.checkCanceled()
            val candidates = resolveTemplateCandidates(key)
            val files = fileIndex.getContainingFiles(KnockoutTemplateIndex.KEY, key, scope)
                .sortedBy { MagentoMcpSupport.relativePath(project, it) }
            for (file in files) {
                ProgressManager.checkCanceled()
                val relPath = MagentoMcpSupport.relativePath(project, file)
                val line = findLine(file, relPath, key)
                results += UiUsageDecl(
                    kind = "knockout_template",
                    value = key,
                    filePath = relPath,
                    line = line,
                    candidates = candidates
                )
            }
        }

        collectNavigationDeclarations(
            indexId = DisplayAreaIndex.KEY,
            kind = "display_area",
            prefixes = emptyList(),
            valueSelector = { decl -> decl.value },
            candidatesSelector = { emptyList() }
        )
        collectNavigationDeclarations(
            indexId = GetRegionUsageIndex.KEY,
            kind = "get_region",
            prefixes = emptyList(),
            valueSelector = { decl -> decl.value },
            candidatesSelector = { emptyList() }
        )

        return results
            .distinctBy { listOf(it.kind, it.value, it.filePath, it.line) }
            .sortedWith(compareBy({ it.kind }, { it.value }, { it.filePath }, { it.line ?: 0 }))
    }

    private fun matchingRawKeys(
        allKeys: Collection<String>,
        query: String,
        prefixes: List<String>
    ): List<String> {
        if (prefixes.isEmpty()) {
            return MagentoMcpSupport.prioritizeMatches(allKeys, query)
        }
        val byPayload = linkedMapOf<String, MutableList<String>>()
        for (rawKey in allKeys) {
            val payload = stripPrefix(rawKey, prefixes)
            byPayload.getOrPut(payload) { mutableListOf() }.add(rawKey)
        }
        return MagentoMcpSupport.prioritizeMatches(byPayload.keys, query)
            .flatMap { payload -> byPayload[payload].orEmpty() }
    }

    private fun stripPrefix(key: String, prefixes: List<String>): String {
        for (prefix in prefixes) {
            if (key.startsWith(prefix)) {
                return key.substring(prefix.length)
            }
        }
        return key
    }

    private fun readText(file: VirtualFile): String? {
        val doc = FileDocumentManager.getInstance().getCachedDocument(file)
        if (doc != null) {
            return doc.text
        }
        return try {
            VfsUtilCore.loadText(file)
        } catch (_: IOException) {
            null
        }
    }
}
