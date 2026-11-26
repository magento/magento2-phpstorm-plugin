/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlFile
import com.magento.idea.magento2plugin.project.Settings
import com.magento.idea.magento2plugin.util.PhpNameUtil
import java.io.File
import java.nio.file.Paths

internal object MagentoMcpSupport {
    const val MAX_MATCHES = 20
    const val MAX_FILE_MATCHES = 10
    const val INDEXES_NOT_READY_MESSAGE = "Indexes are not ready yet. Wait for indexing to finish and retry."

    fun validateProject(project: Project, requireSmartMode: Boolean = true): String? {
        if (!Settings.isEnabled(project)) {
            return MagentoMcpSetupGuidance.disabled(project)
        }
        if (requireSmartMode && DumbService.getInstance(project).isDumb) {
            return INDEXES_NOT_READY_MESSAGE
        }
        return null
    }

    fun addSection(lines: MutableList<String>, title: String, values: List<String>) {
        if (values.isEmpty()) {
            return
        }
        if (lines.isNotEmpty()) {
            lines += ""
        }
        lines += title
        lines += values.take(MAX_MATCHES)
    }

    fun findXmlFilesByName(project: Project, fileName: String): List<XmlFile> {
        val scope = GlobalSearchScope.allScope(project)
        return FilenameIndex.getVirtualFilesByName(fileName, scope)
            .mapNotNull { PsiManager.getInstance(project).findFile(it) as? XmlFile }
    }

    fun normalizeFqn(value: String?): String {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            return ""
        }
        return PhpNameUtil.toPresentableFQN(trimmed.removePrefix("\\"))
    }

    fun presentableFqn(value: String?): String? {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            return null
        }
        return PhpNameUtil.toPresentableFQN(trimmed.removePrefix("\\"))
    }

    fun prioritizeMatches(values: Collection<String>, query: String): List<String> {
        val normalizedQuery = query.trim().lowercase()
        return values
            .distinct()
            .sortedWith(compareBy<String>({ matchRank(it, normalizedQuery) }, { it.lowercase() }))
            .filter { fuzzyMatch(it, normalizedQuery) }
    }

    fun fuzzyMatch(candidate: String, query: String): Boolean {
        val normalizedCandidate = candidate.lowercase()
        val normalizedQuery = query.lowercase()
        return normalizedCandidate == normalizedQuery
            || normalizedCandidate.startsWith(normalizedQuery)
            || normalizedCandidate.contains(normalizedQuery)
    }

    fun pluginKindOrder(kind: String): Int = when (kind) {
        "before" -> 0
        "around" -> 1
        "after" -> 2
        else -> 3
    }

    fun configScopeOrder(scope: String): Int = when (scope) {
        "global" -> 0
        "frontend" -> 1
        "adminhtml" -> 2
        else -> 3
    }

    /**
     * Area of a view file: the folder after `app/design/`, the area in a `theme-<area>-…` package name, or the
     * folder after a module's `view/`. Null when the path does not say, as for theme packages named otherwise.
     */
    fun viewArea(path: String): String? {
        val segments = path.replace('\\', '/').split('/')
        val design = segments.indices.firstOrNull { i ->
            i + 5 < segments.size && segments[i] == "app" && segments[i + 1] == "design" &&
                segments[i + 2] in VIEW_AREAS
        }
        if (design != null) return segments[design + 2]

        val view = segments.indices.lastOrNull { i ->
            i + 2 < segments.size && segments[i] == "view" && segments[i + 1] in VIEW_AREAS &&
                (segments[i + 2] == "requirejs-config.js" ||
                    (i + 3 < segments.size && segments[i + 2] in THEME_MODULE_FOLDERS))
        }
        if (view != null) return segments[view + 1]

        return segments.asReversed().firstNotNullOfOrNull { THEME_PACKAGE.matchEntire(it)?.groupValues?.get(1) }
    }

    /** Module named by a theme override folder, such as `Magento_Catalog` in `…/Magento_Catalog/layout/x.xml`. */
    fun themeOverrideModule(path: String): String? {
        val segments = path.replace('\\', '/').split('/')
        return segments.indices.firstOrNull { index ->
            index + 1 < segments.size && MODULE_FOLDER.matches(segments[index]) &&
                (segments[index + 1] in THEME_MODULE_FOLDERS || segments[index + 1] == "requirejs-config.js")
        }?.let { segments[it] }
    }

    private val VIEW_AREAS = setOf("frontend", "adminhtml", "base")
    private val THEME_PACKAGE = Regex("theme-(frontend|adminhtml)-.+")
    private val MODULE_FOLDER = Regex("[A-Z][A-Za-z0-9]*_[A-Z][A-Za-z0-9]*")
    private val THEME_MODULE_FOLDERS = setOf("layout", "page_layout", "templates", "ui_component", "web", "email")

    /** Line starts of one file, built once so that each tag costs a binary search. */
    class LineIndex(text: CharSequence) {
        private val starts = buildList {
            add(0)
            for (index in text.indices) if (text[index] == '\n') add(index + 1)
        }

        /** One-based line of a character offset in this file. */
        fun lineAt(offset: Int): Int {
            val found = starts.binarySearch(offset)
            return if (found >= 0) found + 1 else -found - 1
        }

        /** One-based line of an element in this file. */
        fun lineOf(element: PsiElement): Int = lineAt(element.textOffset)
    }

    fun relativePath(project: Project, virtualFile: VirtualFile): String {
        val projectRoot = projectRoot(project)
        if (projectRoot != null) {
            VfsUtilCore.getRelativePath(virtualFile, projectRoot, '/')?.let { return it }
        }

        val basePath = project.basePath
        if (basePath == null) {
            return virtualFile.path
        }

        return try {
            Paths.get(basePath).relativize(Paths.get(virtualFile.path)).toString()
                .replace(File.separatorChar, '/')
        } catch (_: Throwable) {
            virtualFile.path
        }
    }

    fun determineConfigScope(filePath: String, fileName: String): String {
        // The leading slash lets a module at the project root match: its files start with `etc/`.
        val normalized = "/" + filePath.replace('\\', '/')
        val suffix = "/$fileName"
        val etcIndex = normalized.lastIndexOf("/etc/")
        if (etcIndex == -1) {
            return "global"
        }

        val tail = normalized.substring(etcIndex + "/etc/".length)
        return when {
            tail == fileName -> "global"
            tail.endsWith(suffix) -> tail.removeSuffix(suffix)
            else -> "global"
        }
    }

    fun projectRoot(project: Project): VirtualFile? {
        return ProjectRootManager.getInstance(project).contentRoots.firstOrNull() ?: project.projectFile?.parent
    }

    private fun matchRank(candidate: String, query: String): Int {
        val normalized = candidate.lowercase()
        return when {
            normalized == query -> 0
            normalized.startsWith(query) -> 1
            normalized.contains(query) -> 2
            else -> 3
        }
    }
}
