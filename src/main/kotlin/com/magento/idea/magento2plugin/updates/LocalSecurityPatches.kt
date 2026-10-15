/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.updates

import org.json.JSONArray
import org.json.JSONObject
import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings
import com.intellij.openapi.progress.ProgressManager
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Project-local evidence; never put it in the application feed cache.
 *
 * Composer definitions and Cloud build configuration describe the deployment's intended patch set.
 * Applied Composer records and matching post-images also cover locally/manual applied fixes. A loose
 * patch, a tool dependency, or a historical log entry alone is not evidence of application.
 *
 * Formats: https://docs.cweagans.net/composer-patches/usage/defining-patches/
 * https://github.com/vaimo/composer-patches/blob/master/docs/USAGE_BASIC.md
 * https://github.com/netresearch/composer-patches-plugin
 * https://experienceleague.adobe.com/en/docs/commerce-on-cloud/user-guide/develop/upgrade/apply-patches
 */
internal data class LocalSecurityPatches(
    private val entries: List<Entry>,
    val watchedPaths: Set<String>,
    private val packageVersions: Map<String, String> = emptyMap(),
    private val registry: SecurityPatchRegistry? = null
) {
    internal data class Entry(val identity: String, val covered: Boolean)

    fun covers(advisory: JSONObject, installed: InstalledMagento): Boolean {
        val fixed = ReleaseVersion.parse(advisory.optString("fixedIn"))
        val current = ReleaseVersion.parse(installed.version)
        if (fixed != null && current != null && fixed.stable && fixed.line == current.line && current >= fixed) return true
        val patches = advisory.optJSONArray("isolatedPatches").objects().filter {
            it.optJSONArray("appliesTo").strings().contains(installed.version)
        }
        if (patches.isNotEmpty()) return patches.all { patch ->
            val id = patch.optString("id")
            monthlyCovered(id, installed) ?: (matches(id) ||
                (patches.size == 1 && matches(advisory.optString("id"))))
        }
        // An isolated patch built for a different base release cannot resolve this advisory.
        if (advisory.optJSONArray("isolatedPatches").objects().isNotEmpty()) return false
        return matches(advisory.optString("id"))
    }

    private fun matches(id: String): Boolean {
        if (id.isBlank()) return false
        val pattern = Regex("(?<![a-z0-9])${Regex.escape(id)}(?![a-z0-9])", RegexOption.IGNORE_CASE)
        val matching = entries.filter { pattern.containsMatchIn(it.identity) }
        return matching.isNotEmpty() && matching.all { it.covered }
    }

    private fun monthlyCovered(id: String, installed: InstalledMagento): Boolean? {
        val month = Regex("(20\\d{2})-([a-z]{3})", RegexOption.IGNORE_CASE).matchEntire(id) ?: return null
        val number = listOf("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec")
            .indexOf(month.groupValues[2].lowercase()).takeIf { it >= 0 }?.plus(1) ?: return false
        val date = "${month.groupValues[1]}-${number.toString().padStart(2, '0')}"
        registry?.covers(date, installed, packageVersions, ::matches)?.let { return it }
        val versions = mutableMapOf("CE" to installed.version)
        if (installed.distribution == MagentoDistribution.COMMERCE) versions["EE"] = installed.version
        packageVersions["magento/extension-b2b"]?.let { versions["B2B"] = it }
        val pattern = Regex("(?<![a-z0-9])([a-z0-9]+)-$date-\\d+-([a-z0-9]+)(?![a-z0-9])", RegexOption.IGNORE_CASE)
        val matching = entries.filter { pattern.containsMatchIn(it.identity) }
        return versions.all { (component, version) ->
            val compact = version.replace(".", "").replace("-p", "p")
            val componentPattern = Regex("(?<![a-z0-9])${Regex.escape(compact)}-$date-\\d+-$component(?![a-z0-9])", RegexOption.IGNORE_CASE)
            val required = matching.filter { componentPattern.containsMatchIn(it.identity) }
            required.isNotEmpty() && required.all { it.covered }
        }
    }

    companion object {
        val EMPTY = LocalSecurityPatches(emptyList(), emptySet())
        fun inspect(root: Path): LocalSecurityPatches = Inspector(root.toAbsolutePath().normalize()).inspect()

        fun isPatchPath(root: String, path: String): Boolean {
            val relative = path.replace('\\', '/').removePrefix(root.replace('\\', '/').trimEnd('/') + "/")
            return relative in setOf("composer.json", "composer.lock", "patches.lock.json", ".magento.env.yaml", ".magento.app.yaml") ||
                relative.endsWith(".patch") || relative.endsWith(".diff") || relative.endsWith("/composer/installed.json")
        }
    }

    private class Inspector(private val root: Path) {
        private val entries = mutableListOf<Entry>()
        private val watched = linkedSetOf<String>()
        private val visited = mutableSetOf<Path>()
        private val composer = json(root.resolve("composer.json")) ?: JSONObject()
        private val vendor = localPath(composer.optJSONObject("config")?.optString("vendor-dir")?.ifBlank { "vendor" } ?: "vendor") ?: root.resolve("vendor")
        private val ignored = mutableSetOf<String>()

        fun inspect(): LocalSecurityPatches {
            val lock = json(root.resolve("composer.lock"))
            val installed = read(vendor.resolve("composer/installed.json"))?.let { body ->
                parse { if (body.trimStart().startsWith("[")) JSONArray(body) else JSONObject(body).optJSONArray("packages") }
            }.objects()
            val packages = lock?.optJSONArray("packages").objects()
            // installed.json describes the current vendor tree and supersedes historical lock data.
            (packages + installed).associateBy { it.optString("name") }.values.forEach { pkg ->
                definitions(pkg.optJSONObject("extra")?.opt("patches_applied"), applied = true, grouped = false,
                    target = vendor.resolve(pkg.optString("name")))
            }
            val extra = composer.optJSONObject("extra") ?: JSONObject()
            val plugins = composer.optJSONObject("config")?.opt("allow-plugins")
            val names = (packages + installed).map { it.optString("name") } +
                (composer.optJSONObject("require")?.keySet() ?: emptySet()) +
                (composer.optJSONObject("require-dev")?.keySet() ?: emptySet())
            val patchers = names.filter { name ->
                name in setOf("cweagans/composer-patches", "vaimo/composer-patches", "netresearch/composer-patches-plugin") &&
                    plugins != false && (plugins !is JSONObject || (plugins.opt(name) ?: plugins.opt("*")) != false) &&
                    (name != "vaimo/composer-patches" || extra.opt("patcher") != false)
            }
            if (patchers.isNotEmpty()) {
                ignoredSources(extra.opt("patches-ignore"))
                val patchLock = json(root.resolve("patches.lock.json"))
                if (patchLock != null) definitions(patchLock.opt("patches")) else {
                    val disabled = extra.optJSONObject("composer-patches")?.optJSONArray("disable-resolvers").strings()
                    composerDefinitions(composer, root, "vaimo/composer-patches" in patchers, disabled)
                    if (disabled.none { it.endsWith("\\Dependencies") }) packages.forEach { pkg ->
                        composerDefinitions(pkg, vendor.resolve(pkg.optString("name")), "vaimo/composer-patches" in patchers)
                    }
                }
            }
            val cloud = packages.any { it.optString("name") in setOf("magento/ece-tools", "magento/magento-cloud-metapackage") } ||
                read(root.resolve(".magento.app.yaml")) != null
            val env = read(root.resolve(".magento.env.yaml"))?.let { body ->
                parse { Load(LoadSettings.builder().setCodePointLimit(MAX_BYTES).setMaxAliasesForCollections(50).build()).loadFromString(body) as? Map<*, *> }
            }
            val stage = env?.get("stage") as? Map<*, *>
            val build = stage?.get("build") as? Map<*, *>
            (build?.get("QUALITY_PATCHES") as? List<*>)?.filterIsInstance<String>()?.forEach { entries.add(Entry(it, true)) }
            scan(root.resolve("m2-hotfixes"), configured = cloud)
            scan(root.resolve("patches"))
            scan(root, depth = 1)
            // Tool packages ship diff files even when patches were applied outside Composer/Cloud.
            scan(vendor.resolve("magento/quality-patches/patches"))
            scan(vendor.resolve("magento/magento-cloud-patches/patches"))
            // CVT caches the exact diffs used by its dry-run detector here. Recheck the code now;
            // its registry/cache files alone never assert that a patch is applied.
            scan(root.resolve("var/patch_metadata/.patch_diffs"))
            val registry = json(root.resolve("var/patch_metadata/.patch_registry_cache.json"))?.let(SecurityPatchRegistry::from)
            val versions = packages.associate { it.optString("name") to it.optString("version").removePrefix("v") }
            return LocalSecurityPatches(entries.distinct().sortedWith(compareBy<Entry> { it.identity }.thenBy { it.covered }), watched, versions, registry)
        }

        private fun composerDefinitions(owner: JSONObject, directory: Path, vaimo: Boolean = false, disabled: List<String> = emptyList()) {
            val extra = owner.optJSONObject("extra") ?: JSONObject()
            if (disabled.none { it.endsWith("\\RootComposer") }) definitions(extra.opt("patches"), base = if (vaimo) directory else root)
            if (directory != root && !vaimo) return
            val patcher = extra.optJSONObject("patcher")
            val config = extra.optJSONObject("composer-patches")
            val files = extra.opt("patches-file") ?: config?.opt("patches-file") ?: patcher?.opt("file") ?: "patches.json"
            val paths = when (files) { is String -> listOf(files); is JSONArray -> files.strings(); else -> emptyList() }
            if (disabled.none { it.endsWith("\\PatchesFile") }) paths.forEach { file ->
                localPath(file, directory)?.let { path -> json(path)?.let { definitions(it.opt("patches") ?: it, base = directory) } }
            }
            val searches = extra.opt("patches-search") ?: patcher?.opt("search") ?: if (vaimo) "patches" else null
            val directories = when (searches) { is String -> listOf(searches); is JSONArray -> searches.strings(); else -> emptyList() }
            directories.forEach { path -> localPath(path, directory)?.let { scan(it, configured = true, embedded = true) } }
        }

        private fun ignoredSources(value: Any?) {
            when (value) {
                is JSONObject -> value.keySet().forEach { ignoredSources(value.opt(it)) }
                is JSONArray -> value.strings().forEach(ignored::add)
                is String -> ignored.add(value)
            }
        }

        private fun definitions(value: Any?, applied: Boolean = false, grouped: Boolean = true, base: Path = root, target: Path? = null) {
            when (value) {
                is JSONObject -> {
                    if (grouped) value.keySet().forEach { definitions(value.opt(it), applied, false, base, vendor.resolve(it)) }
                    else if (value.has("url") || value.has("source")) {
                        val source = value.optString("url", value.optString("source"))
                        entry(value.optString("description", value.optString("title")), source, applied, base, target)
                    } else value.keySet().forEach { description ->
                        when (val patch = value.opt(description)) {
                            is String -> entry(description, patch, applied, base, target)
                            is JSONObject -> if (patch.has("source") || patch.has("url")) {
                                entry(description, patch.optString("url", patch.optString("source")), applied, base, target)
                            }
                        }
                    }
                }
                is JSONArray -> value.objects().forEach { definitions(it, applied, false, base, target) }
            }
        }

        private fun entry(description: String, source: String, applied: Boolean, base: Path, target: Path?) {
            if (source.isBlank()) return
            if (!applied && source in ignored) return
            val path = localPath(source, base)
            val body = path?.let(::read)
            if (path != null && body != null) visited.add(path)
            // A present diff can disprove stale applied records or a failed local Composer install.
            val verified = body?.let { AppliedPatch.isApplied(it, root, ::read, target) }
            entries.add(Entry("$description $source", verified ?: true))
        }

        private fun scan(directory: Path, configured: Boolean = false, embedded: Boolean = false, depth: Int = 4) {
            if (!Files.isDirectory(directory) || !directory.normalize().startsWith(root)) return
            try {
                Files.walk(directory, depth).use { paths ->
                    paths.filter { Files.isRegularFile(it) && it.fileName.toString().let { name -> name.endsWith(".patch") || name.endsWith(".diff") } }
                        .limit(1000).forEach { path ->
                            ProgressManager.checkCanceled()
                            if (!visited.add(path)) return@forEach
                            val body = read(path) ?: return@forEach
                            val preamble = body.lineSequence().takeWhile { !it.startsWith("--- ") && !it.startsWith("diff ") }.joinToString(" ")
                            val enabled = !embedded || ((preamble.contains("@package ") || preamble.contains("@type bundle")) && !preamble.contains("@skip"))
                            val applied = AppliedPatch.isApplied(body, root, ::read) == true
                            entries.add(Entry("${path.fileName} $preamble", enabled && (applied || (configured && path.toString().endsWith(".patch")))))
                        }
                }
            } catch (_: IOException) { /* Incomplete evidence must not hide a warning. */ }
        }

        private fun localPath(value: String, base: Path = root): Path? = parse {
            if (value.isBlank() || value.contains("://")) null else base.resolve(value).normalize().takeIf { it.startsWith(root) }
        }

        private fun read(path: Path): String? {
            watched.add(path.toString().replace('\\', '/'))
            return parse {
                if (!path.normalize().startsWith(root) || !Files.isRegularFile(path)) null else
                    Files.newInputStream(path).use { stream ->
                        val bytes = stream.readNBytes(MAX_BYTES + 1)
                        if (bytes.size > MAX_BYTES) null else bytes.toString(Charsets.UTF_8)
                    }
            }
        }

        private fun json(path: Path): JSONObject? = read(path)?.let { parse { JSONObject(it) } }
    }
}

private const val MAX_BYTES = 4 * 1024 * 1024
private fun JSONArray?.objects(): List<JSONObject> = if (this == null) emptyList() else (0 until length()).mapNotNull { optJSONObject(it) }
private fun JSONArray?.strings(): List<String> = if (this == null) emptyList() else (0 until length()).mapNotNull { opt(it) as? String }
private inline fun <T> parse(block: () -> T): T? = try { block() } catch (exception: Exception) {
    MagentoUpdateFeed.rethrowCancellation(exception)
    null
}
