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
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.ID
import com.magento.idea.magento2plugin.stubs.indexes.js.KnockoutTemplateIndex
import com.magento.idea.magento2plugin.stubs.indexes.ui.DisplayAreaIndex
import com.magento.idea.magento2plugin.stubs.indexes.ui.GetRegionUsageIndex
import com.magento.idea.magento2plugin.stubs.indexes.ui.UiComponentComponentDeclarationIndex
import com.magento.idea.magento2plugin.stubs.indexes.ui.UiComponentTemplateDeclarationIndex
import com.magento.idea.magento2plugin.util.magento.js.MagentoJsParser
import com.magento.idea.magento2plugin.util.magento.js.RequireJsPathResolver
import com.magento.idea.magento2plugin.util.magento.ui.TemplatePathResolver
import java.io.IOException

internal object MagentoJsQueries {
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
        val candidates: List<String>
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
            records += InspectionRecord(
                kind = decl.kind,
                key = decl.value,
                path = decl.filePath,
                line = decl.line,
                area = MagentoMcpSupport.viewArea(decl.filePath),
                fields = linkedMapOf(
                    "value" to decl.value,
                    "candidates" to decl.candidates
                ),
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
            val parsed = MagentoJsParser.parseRequireJsConfig(text)
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
        val lineCache = hashMapOf<String, Pair<String, MagentoMcpSupport.LineIndex>>()

        fun findLine(file: VirtualFile, relativePath: String, needle: String): Int? {
            val (text, lineIndex) = lineCache.getOrPut(relativePath) {
                val content = readText(file) ?: return null
                content to MagentoMcpSupport.LineIndex(content)
            }
            val idx = text.indexOf(needle)
            return if (idx >= 0) lineIndex.lineAt(idx) else null
        }

        fun <V> collectFromIndex(
            indexId: ID<String, V>,
            kind: String,
            resolveCandidates: (String) -> List<String>
        ) {
            val allKeys = try {
                indexKeys?.keys(indexId, project) ?: fileIndex.getAllKeys(indexId, project)
            } catch (_: Throwable) {
                emptyList()
            }
            val matchedKeys = MagentoMcpSupport.prioritizeMatches(allKeys, query)
            for (key in matchedKeys) {
                ProgressManager.checkCanceled()
                val candidates = resolveCandidates(key)
                val files = fileIndex.getContainingFiles(indexId, key, scope)
                    .sortedBy { MagentoMcpSupport.relativePath(project, it) }
                for (file in files) {
                    ProgressManager.checkCanceled()
                    val relPath = MagentoMcpSupport.relativePath(project, file)
                    val line = findLine(file, relPath, key)
                    results += UiUsageDecl(
                        kind = kind,
                        value = key,
                        filePath = relPath,
                        line = line,
                        candidates = candidates
                    )
                }
            }
        }

        collectFromIndex(UiComponentComponentDeclarationIndex.KEY, "ui_component_declaration") { key ->
            jsResolver.findJsFiles(project, key)
                .map { MagentoMcpSupport.relativePath(project, it) }
                .distinct()
                .sorted()
        }
        collectFromIndex(UiComponentTemplateDeclarationIndex.KEY, "ui_template_declaration") { key ->
            templateResolver.findTemplateFiles(project, key)
                .map { MagentoMcpSupport.relativePath(project, it) }
                .distinct()
                .sorted()
        }
        collectFromIndex(KnockoutTemplateIndex.KEY, "knockout_template") { key ->
            templateResolver.findTemplateFiles(project, key)
                .map { MagentoMcpSupport.relativePath(project, it) }
                .distinct()
                .sorted()
        }
        collectFromIndex(DisplayAreaIndex.KEY, "display_area") { emptyList() }
        collectFromIndex(GetRegionUsageIndex.KEY, "get_region") { emptyList() }

        return results
            .distinctBy { Triple(it.kind, it.value, it.filePath) }
            .sortedWith(compareBy({ it.kind }, { it.value }, { it.filePath }, { it.line ?: 0 }))
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
