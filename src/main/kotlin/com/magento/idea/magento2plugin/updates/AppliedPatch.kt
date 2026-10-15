/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.updates

import java.nio.file.Path

/** Read-only unified-diff post-image check, shared by Composer, QPT, Cloud and manual patches. */
internal object AppliedPatch {
    private val header = Regex("@@ -(\\d+)(?:,(\\d+))? \\+(\\d+)(?:,(\\d+))? @@.*")

    /** null means the diff/target is unavailable or unsupported; false means an unpatched post-image. */
    fun isApplied(body: String, root: Path, read: (Path) -> String?, packageRoot: Path? = null): Boolean? {
        val lines = body.replace("\r\n", "\n").lines()
        var index = 0
        var files = 0
        var changed = false
        var applied = true
        while (index < lines.size) {
            if (!lines[index].startsWith("--- ")) { index++; continue }
            val oldPath = lines[index++].removePrefix("--- ").substringBefore('\t')
            if (index >= lines.size || !lines[index].startsWith("+++ ")) return null
            val newPath = lines[index++].removePrefix("+++ ").substringBefore('\t')
            // Quoted paths, binary diffs and deletions require a richer patch engine.
            if (newPath == "/dev/null" || newPath.startsWith('"')) return null
            val relative = newPath.removePrefix("b/")
            if (relative.startsWith('/') || relative.split('/').contains("..")) return null
            val candidates = listOfNotNull(root.resolve(relative), packageRoot?.resolve(relative)).distinct()
            val target = candidates.firstNotNullOfOrNull { path -> read(path)?.replace("\r\n", "\n") } ?: return null
            val content = target.lines()
            var hunks = 0
            var previousEnd = 0
            while (index < lines.size && lines[index].startsWith("@@ ")) {
                val match = header.matchEntire(lines[index++]) ?: return null
                val oldCount = match.groupValues[2].ifEmpty { "1" }.toIntOrNull() ?: return null
                val start = match.groupValues[3].toIntOrNull()?.minus(1) ?: return null
                val newCount = match.groupValues[4].ifEmpty { "1" }.toIntOrNull() ?: return null
                var removed = 0
                var added = 0
                val postImage = mutableListOf<String>()
                var hunkChanged = false
                while (index < lines.size && (removed < oldCount || added < newCount)) {
                    val line = lines[index++]
                    when (line.firstOrNull()) {
                        ' ' -> { removed++; added++; postImage.add(line.substring(1)) }
                        '-' -> { removed++; hunkChanged = true }
                        '+' -> { added++; postImage.add(line.substring(1)); hunkChanged = true }
                        '\\' -> if (line != "\\ No newline at end of file") return null
                        else -> return null
                    }
                    if (removed > oldCount || added > newCount) return null
                }
                if (removed != oldCount || added != newCount || !hunkChanged || start < 0) return null
                if (postImage.isEmpty()) return null
                // Search after preceding hunks to allow unrelated edits shifting line numbers.
                val expected = start.coerceAtLeast(previousEnd)
                val position = if (expected >= 0 && expected + postImage.size <= content.size &&
                    content.subList(expected, expected + postImage.size) == postImage) expected else
                    (previousEnd..(content.size - postImage.size)).firstOrNull {
                        content.subList(it, it + postImage.size) == postImage
                    }
                if (position == null) applied = false else previousEnd = position + postImage.size
                changed = true
                hunks++
                if (index < lines.size && lines[index] == "\\ No newline at end of file") index++
            }
            if (hunks == 0 || oldPath.isBlank()) return null
            files++
        }
        if (lines.any { it.startsWith("GIT binary patch") || it.startsWith("Binary files ") }) return null
        return if (files > 0 && changed) applied else null
    }
}
