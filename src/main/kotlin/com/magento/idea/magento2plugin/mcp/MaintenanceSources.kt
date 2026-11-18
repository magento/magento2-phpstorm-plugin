/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import org.json.JSONArray
import java.nio.file.Files
import java.nio.file.Path

internal object MaintenanceSources {
    fun validPath(value: String): Boolean = value.isNotBlank() && value.length <= 4096 &&
        value.none { it.isISOControl() || it == '\\' } && !value.startsWith('/') &&
        !Regex("^[A-Za-z]:").containsMatchIn(value) && value.split('/').none { it in setOf("", ".", "..") }

    private data class ModuleList(val enabled: Set<String>, val additions: Set<String>)
    private val moduleLists = LinkedHashMap<Path, ModuleList>(16, 0.75f, true)

    /** A repeated check/verify pair for the same config diff should report the same additions. */
    @Synchronized
    fun observeModules(root: Path, configChanged: Boolean): Set<String> {
        val file = MagentoCliEnvironment.resolveProjectPath(root, "app/etc/config.php")
        if (file == null || !Files.isRegularFile(file) || Files.size(file) > 1024 * 1024) {
            moduleLists.remove(root)
            return emptySet()
        }
        val current = enabledModules(Files.readString(file))
        if (current == null) {
            moduleLists.remove(root)
            return emptySet()
        }
        val previous = moduleLists[root]
        if (previous?.enabled == current) return if (configChanged) previous.additions else emptySet()
        val added = previous?.let { current - it.enabled }.orEmpty()
        if (moduleLists.size >= 32 && previous == null) moduleLists.remove(moduleLists.keys.first())
        moduleLists[root] = ModuleList(current, added)
        return added
    }

    // Read only Magento's literal exported modules array. Computed PHP is indeterminate.
    fun enabledModules(config: String): Set<String>? {
        val section = Regex("""['"]modules['"]\s*=>\s*\[([^\[\]]*)]""").find(config)?.groupValues?.get(1) ?: return null
        val entry = Regex("""\s*['"]([A-Za-z][A-Za-z0-9]*_[A-Za-z][A-Za-z0-9_]*)['"]\s*=>\s*([01])\s*""")
        val enabled = mutableSetOf<String>()
        for (line in section.split(',').filter(String::isNotBlank)) {
            val match = entry.matchEntire(line) ?: return null
            if (match.groupValues[2] == "1") enabled += match.groupValues[1]
        }
        return enabled
    }

    private val layout = Regex("(?:^|/)(?:layout|page_layout)/[^/]+\\.xml$")
    private val translation = Regex("(?:^|/)(?:i18n|app/i18n/[^/]+/[^/]+)/[^/]+\\.csv$")
    private val patch = Regex("(?:^|/)Setup/Patch/(?:Schema|Data)/.+\\.php$")
    private val module = Regex("^app/code/([^/]+)/([^/]+)/")
    fun cacheType(path: String): String? = when {
        layout.containsMatchIn(path) -> "layout"
        path.endsWith("/etc/config.xml") -> "config"
        translation.containsMatchIn(path) -> "translate"
        else -> null
    }

    fun checkId(path: String): String? = when {
        cacheType(path) != null -> "cache_clean"
        path.endsWith("/etc/db_schema.xml") || path.endsWith("/etc/module.xml") || patch.containsMatchIn(path) ||
            path == "app/etc/config.php" -> "setup_upgrade"
        path in listOf("composer.lock", "composer.json") -> "composer_install"
        path == ".magento.env.yaml" || path.matches(Regex("m2-hotfixes/[^/]+\\.patch")) -> "ece_patches"
        else -> null
    }

    fun find(check: String, runtime: MaintenanceRuntime, paths: List<String>, enabled: List<String>): List<MaintenanceFinding> {
        val findings = mutableListOf<MaintenanceFinding>()
        val newModules = if (check == "setup_upgrade") (enabled + observeModules(runtime.root, "app/etc/config.php" in paths))
            .distinct().sorted() else emptyList()
        if (check == "composer_install" && ("composer.json" in paths || "composer.lock" in paths) &&
            Files.notExists(runtime.root.resolve("composer.lock"))) {
            return listOf(MaintenancePackages.missingLock())
        }
        for (path in paths.filter { checkId(it) == check }.sorted()) {
            val mod = module.find(path)?.let { "${it.groupValues[1]}_${it.groupValues[2]}" }
            when (check) {
                "setup_upgrade" -> {
                    if (path.endsWith("/etc/module.xml")) {
                        val file = MagentoCliEnvironment.resolveProjectPath(runtime.root, path)
                        if (file != null && Files.isRegularFile(file) && Files.size(file) <= 131072 &&
                            !Regex("""\bsetup_version\s*=""").containsMatchIn(Files.readString(file))) continue
                    }
                    if (path == "app/etc/config.php" && newModules.isNotEmpty()) continue
                    val listChange = path == "app/etc/config.php"
                    findings += MaintenanceFinding(if (listChange) "module_list_changed" else "setup_source_changed", "advisory",
                        mod ?: Regex("^vendor/([^/]+/[^/]+)/").find(path)?.groupValues?.get(1) ?: path, "Changed file: $path", if (listChange) "Compare the enabled modules with the previous deployment."
                        else "Verify setup:db:status in the selected runtime.", module = mod)
                }
                "cache_clean" -> {
                    val type = cacheType(path)!!
                    findings += MaintenanceFinding("source_cache_type", "advisory", type, "Changed file: $path",
                        "If the edited content is stale, clean the $type cache.", runtime.suggestion("bin/magento", "cache:clean", type))
                }
                else -> findings += MaintenanceFinding("maintenance_source_changed", "advisory", path,
                    "Changed file: $path", "Verify $check in the selected environment.")
            }
        }
        if (check == "setup_upgrade") for (name in newModules) findings += MaintenanceFinding(
            "module_enabled", "advisory", name, "The module-list change enables $name. DB validators only cover inspected modules.",
            "Confirm the module enablement deployment and run its setup workflow if still pending.", module = name)
        return findings
    }

    fun cache(runtime: MaintenanceRuntime, sources: List<MaintenanceFinding>): List<MaintenanceFinding> {
        val raw = JSONArray(runtime.php("cache"))
        val types = (0 until raw.length()).map(raw::getString).distinct().sorted()
        if (types.any { !it.matches(Regex("[a-zA-Z0-9_][a-zA-Z0-9_.-]*")) })
            throw MaintenanceUnavailable("cache_ids_unknown", "Magento returned an unexpected cache type ID.")
        val invalidated = types.map { type -> MaintenanceFinding("cache_invalidated", "required", type,
            "TypeListInterface::getInvalidated() returned $type.", "Clean the reported cache types, then verify.",
            runtime.suggestion("bin/magento", "cache:clean", *types.toTypedArray())) }
        return (invalidated + sources.filter { it.item !in types }).ifEmpty { listOf(MaintenanceFinding(
            "no_invalidations", "clear", "enabled cache types", "Magento returned no invalidated types.", "No cache command.")) }
    }
}
