/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlFile
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.PhpLangUtil
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.magento.idea.magento2plugin.project.Settings
import java.io.File
import java.nio.file.Paths

internal object MagentoMcpSupport {
    const val MAX_MATCHES = 20
    const val MAX_FILE_MATCHES = 10

    fun validateProject(project: Project, requireSmartMode: Boolean = true): String? {
        if (!Settings.isEnabled(project)) {
            return "Magento plugin support is disabled for this project."
        }
        if (requireSmartMode && DumbService.getInstance(project).isDumb) {
            return "Indexes are not ready yet. Wait for indexing to finish and retry."
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
        return PhpLangUtil.toPresentableFQN(trimmed.removePrefix("\\"))
    }

    fun presentableFqn(value: String?): String? {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            return null
        }
        return PhpLangUtil.toPresentableFQN(trimmed.removePrefix("\\"))
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

    fun resolveTargetClasses(phpIndex: PhpIndex, className: String): List<PhpClass> {
        val classes = phpIndex.getClassesByFQN(className).toMutableList()
        classes += phpIndex.getInterfacesByFQN(className)
        return classes.distinctBy { it.fqn }
    }

    fun collectClassHierarchy(phpClass: PhpClass, fqns: MutableSet<String>) {
        fqns += phpClass.presentableFQN
        for (parent in phpClass.supers) {
            val parentFqn = parent.fqn.removePrefix("\\")
            if (fqns.add(parentFqn)) {
                collectClassHierarchy(parent, fqns)
            }
        }
    }

    fun getPluginKind(pluginMethodName: String, targetMethodName: String): String? {
        val capitalizedMethod = targetMethodName.replaceFirstChar { it.uppercase() }
        return when {
            pluginMethodName == "before$capitalizedMethod" -> "before"
            pluginMethodName == "around$capitalizedMethod" -> "around"
            pluginMethodName == "after$capitalizedMethod" -> "after"
            else -> null
        }
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
        val normalized = filePath.replace('\\', '/')
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

    data class PluginMethodMatch(
        val targetFqn: String,
        val data: PluginDeclarationRecord,
        val pluginClass: PhpClass,
        val method: Method,
        val kind: String
    ) {
        val sortOrder: Int
            get() = data.sortOrder

        val scope: String
            get() = data.scope
    }
}
