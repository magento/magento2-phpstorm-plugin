/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.analysis

import com.magento.idea.magento2uct.util.php.MagentoTypeEscapeUtil
import com.magento.idea.magento2uct.versioning.UctVersionState
import java.io.InputStream
import java.io.ObjectInputStream

/** Orders Magento releases numerically, with prereleases before releases and patches after them. */
internal object UctVersions : Comparator<String> {
    private val pattern = Regex("(\\d+)\\.(\\d+)\\.(\\d+)(?:-(alpha|beta|rc|p)(\\d+))?", RegexOption.IGNORE_CASE)

    private fun parts(version: String): List<Int> {
        val match = pattern.matchEntire(version)
            ?: throw IllegalArgumentException("Invalid Magento version: $version")
        val rank = when (match.groupValues[4].lowercase()) {
            "alpha" -> -3
            "beta" -> -2
            "rc" -> -1
            "p" -> 1
            else -> 0
        }
        return (1..3).map { match.groupValues[it].toInt() } + rank + (match.groupValues[5].toIntOrNull() ?: 0)
    }

    override fun compare(first: String, second: String): Int {
        val left = parts(first)
        val right = parts(second)
        return left.indices.firstNotNullOfOrNull { left[it].compareTo(right[it]).takeIf { value -> value != 0 } } ?: 0
    }
}

/** Immutable catalog of bundled history and verified full release snapshots. */
class UctIndexCatalog internal constructor(
    private val existence: Map<String, Map<String, Boolean>>,
    private val api: Map<String, Map<String, Boolean>>,
    private val deprecation: Map<String, Map<String, Boolean>>,
    private val releases: Map<String, UctReleaseIndex> = emptyMap()
) {
    val supportedVersions: List<String> = (existence.keys.intersect(api.keys).intersect(deprecation.keys) + releases.keys)
        .sortedWith(UctVersions)

    internal fun withReleases(prepared: Map<String, UctReleaseIndex>): UctIndexCatalog =
        UctIndexCatalog(existence, api, deprecation, prepared.toMap())

    fun isPrepared(version: String): Boolean = version in releases

    fun requiresCurrentCoverage(currentVersion: String?, targetVersion: String, ignoreCurrentVersion: Boolean): Boolean =
        ignoreCurrentVersion || (currentVersion != null && (isPrepared(targetVersion) || targetVersion !in existence.keys.intersect(api.keys).intersect(deprecation.keys)))

    fun validateVersions(currentVersion: String?, targetVersion: String, ignoreCurrentVersion: Boolean) {
        require(targetVersion in supportedVersions) {
            "Compatibility indexes are unavailable for targetVersion `$targetVersion`. Use mode `status` for supportedVersions."
        }
        if (currentVersion != null) {
            require(UctVersions.compare(currentVersion, targetVersion) <= 0) { "currentVersion must not exceed targetVersion." }
        }
        require(!requiresCurrentCoverage(currentVersion, targetVersion, ignoreCurrentVersion) || currentVersion in supportedVersions) {
            "This analysis requires compatibility data for currentVersion. Use mode `prepare` for the baseline release."
        }
    }

    fun snapshot(currentVersion: String?, targetVersion: String, ignoreCurrentVersion: Boolean): UctVersionState {
        validateVersions(currentVersion, targetVersion, ignoreCurrentVersion)
        val target = stateAt(targetVersion)
        val current = if (ignoreCurrentVersion) stateAt(currentVersion!!) else null
        return Snapshot(target, current)
    }

    private data class Entry(val active: Boolean, val version: String)
    private data class State(
        val existence: Map<String, Entry>,
        val api: Map<String, Entry>,
        val deprecation: Map<String, Entry>
    )

    private fun stateAt(version: String): State {
        val release = releases[version] ?: return State(
            merge(existence, version), merge(api, version), merge(deprecation, version)
        )
        // A full snapshot resets all flags. It must not inherit stale @api/@deprecated flags.
        // Missing releases between snapshots do not establish the first change release.
        val observed = "$version (observed snapshot; first change release unknown)"
        val known = (merge(existence, version).keys + releases.values
            .filter { UctVersions.compare(it.version, version) <= 0 }.flatMap { it.existence.keys })
            .filter { it.startsWith("\\Magento\\") }
        fun entries(values: Map<String, Boolean>) = values.mapValues { Entry(it.value, observed) }
        return State(
            known.associateWith { Entry(release.existence[it] == true, observed) },
            entries(release.api), entries(release.deprecation)
        )
    }

    private fun merge(history: Map<String, Map<String, Boolean>>, target: String): Map<String, Entry> {
        val state = HashMap<String, Entry>()
        history.keys.filter { UctVersions.compare(it, target) <= 0 }.sortedWith(UctVersions).forEach { version ->
            history.getValue(version).forEach { (symbol, active) ->
                val key = "\\" + symbol.trim().removePrefix("\\")
                if (state[key]?.active != active) state[key] = Entry(active, version)
            }
        }
        return state
    }

    private class Snapshot(private val target: State, private val current: State?) : UctVersionState {
        override fun isPresentInCodebase(fqn: String): Boolean = normalize(fqn) in target.existence
        override fun isExists(fqn: String): Boolean {
            val key = normalize(fqn)
            return target.existence[key]?.active != false || current?.existence?.get(key)?.active == false
        }
        override fun isDeprecated(fqn: String): Boolean {
            val key = normalize(fqn)
            return target.deprecation[key]?.active == true && current?.deprecation?.get(key)?.active != true
        }
        override fun getDeprecatedInVersion(fqn: String): String = target.deprecation[normalize(fqn)]?.version.orEmpty()
        override fun getRemovedInVersion(fqn: String): String = target.existence[normalize(fqn)]?.version.orEmpty()
        override fun isApi(fqn: String): Boolean {
            val key = normalize(fqn)
            return target.existence[key]?.active != true || target.api[key]?.active == true ||
                (current?.existence?.get(key)?.active == true && current.api[key]?.active != true)
        }
    }

    companion object {
        private val bundled by lazy { load { UctIndexCatalog::class.java.getResourceAsStream(it) } }

        @JvmStatic
        fun bundled(): UctIndexCatalog = bundled

        internal fun load(resource: (String) -> InputStream?): UctIndexCatalog {
            fun read(path: String): Map<*, *>? = resource(path)?.use { stream ->
                ObjectInputStream(stream).use { input ->
                    input.readObject() as? Map<*, *> ?: error("Invalid UCT index: $path")
                }
            }
            fun entries(raw: Map<*, *>, path: String): Map<String, Boolean> = raw.entries.associate { (key, value) ->
                require(key is String && value is Boolean) { "Invalid UCT index entry: $path" }
                key to value
            }
            fun history(path: String): Map<String, Map<String, Boolean>> {
                val raw = read(path) ?: error("Missing UCT index: $path")
                return raw.entries.associate { (key, value) ->
                    require(key is String && value is Map<*, *>) { "Invalid UCT version index: $path" }
                    UctVersions.compare(key, key)
                    key to entries(value, path)
                }
            }
            val existence = history("/uct/existence/indexes.EXISTENCE.idc")
            val api = history("/uct/api/indexes.API_COVERAGE.idc")
            val versions = existence.keys + api.keys
            val deprecation = versions.mapNotNull { version ->
                val path = "/uct/deprecation/indexes.v$version.DEPRECATION.idc"
                read(path)?.let { version to entries(it, path) }
            }.toMap()
            require("2.3.0" in existence && "2.3.0" in api && "2.3.0" in deprecation) {
                "Missing UCT 2.3.0 baseline indexes."
            }
            return UctIndexCatalog(existence, api, deprecation)
        }

        private fun normalize(fqn: String): String = MagentoTypeEscapeUtil.escape("\\" + fqn.trim().removePrefix("\\"))
    }
}
