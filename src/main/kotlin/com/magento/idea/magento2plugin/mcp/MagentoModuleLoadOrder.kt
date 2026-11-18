/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

/** Reads the ordered modules map from Magento's generated, literal PHP return array without executing PHP. */
internal object MagentoModuleLoadOrder {
    private val tokens = Regex("""<\?php\b|\?>|=>|'(?:\\.|[^'\\])*+'|"(?:\\.|[^"\\])*+"|//[^\r\n]*|\#[^\r\n]*|/\*[\s\S]*?\*/|\s+|[\w.-]+|.""")
    private val moduleName = Regex("[A-Za-z0-9]+_[A-Za-z0-9_]+")

    fun parse(source: String): Map<String, Boolean>? {
        val input = tokens.findAll(source).map { it.value }
            .filterNot { it.isBlank() || it.startsWith("//") || it.startsWith("#") || it.startsWith("/*") }.iterator()
        fun next() = if (input.hasNext()) input.next() else null
        fun endOfArray(open: String?): String? = when (open) {
            "[" -> "]"
            "array" -> if (next() == "(") ")" else null
            else -> null
        }
        if (next() != "<?php" || next() != "return") return null
        val outerEnd = endOfArray(next()) ?: return null
        var result: Map<String, Boolean>? = null
        var key = next()
        while (key != outerEnd) {
            if (key == null || key.length < 2 || key.first() !in "\"'" || next() != "=>") return null
            val value = next() ?: return null
            if (key == "'modules'" || key == "\"modules\"") {
                if (result != null) return null
                val end = endOfArray(value) ?: return null
                val modules = linkedMapOf<String, Boolean>()
                var entry = next()
                while (entry != end) {
                    if (entry == null || entry.length < 2 || entry.first() !in "\"'") return null
                    val name = entry.substring(1, entry.length - 1)
                    if (!moduleName.matches(name) || name in modules || next() != "=>") return null
                    modules[name] = when (next()) {
                        "1", "true" -> true
                        "0", "false" -> false
                        else -> return null
                    }
                    entry = next()
                    if (entry != end) {
                        if (entry != ",") return null
                        entry = next()
                    }
                }
                result = modules
            } else {
                // Consume an unrelated literal value, including nested arrays. Decline dynamic config.
                val ends = ArrayDeque<String>()
                var token = value
                while (true) {
                    when {
                        token == "[" || token == "array" -> ends.addLast(endOfArray(token) ?: return null)
                        token == "]" || token == ")" -> if (ends.removeLastOrNull() != token) return null
                        token.first() in "\"'" || token in listOf("=>", ",", "true", "false", "null")
                            || token.toDoubleOrNull() != null -> Unit
                        else -> return null
                    }
                    if (ends.isEmpty()) break
                    token = next() ?: return null
                }
            }
            key = next()
            if (key != outerEnd) {
                if (key != ",") return null
                key = next()
            }
        }
        if (next() != ";") return null
        val trailing = next()
        return result.takeIf { trailing == null || (trailing == "?>" && !input.hasNext()) }
    }
}
