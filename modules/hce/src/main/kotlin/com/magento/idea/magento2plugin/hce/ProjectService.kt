/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.hce

import com.magento.idea.magento2plugin.mcp.MagentoCliEnvironment
import org.json.JSONObject
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/** Project configuration and read-only environment discovery; never executes project code. */
class ProjectService(private val store: SettingsStore) {
    fun configure(project: Path, action: String = "status", magentoPath: String = "", enabled: Boolean? = null,
                  cliToolCandidates: List<String>? = null, excludeGeneratedFiles: Boolean? = null): JSONObject = respond {
        if (action == "help") return@respond ok().put("actions", listOf("help", "status", "configure"))
            .put("settings", listOf("magentoPath", "enabled", "cliToolCandidates"))
            .put("unsupported", listOf("excludeGeneratedFiles"))
        require(action in setOf("status", "configure")) { "action must be help, status, or configure." }
        val root = store.projectRoot(project)
        if (action == "status") return@respond status(root, store.load(root))
        if (excludeGeneratedFiles != null) return@respond failure("unsupported_capability",
            "HCE indexing exclusion configuration is not supported by this module; no settings were changed.")
        var before: ProjectSettings? = null
        val after = store.update(root) { current ->
            before = current
            val target = if (magentoPath.isBlank()) current.magentoPath else resolveRoot(root, magentoPath).toString()
            val active = enabled ?: current.enabled
            require(!active || target != null) { "magentoPath is required when enabling Magento integration." }
            target?.let { resolveRoot(root, it) }
            val candidates = cliToolCandidates?.let { validateCandidates(root, it) } ?: current.cliToolCandidates
            current.copy(enabled = active, magentoPath = target, cliToolCandidates = candidates)
        }
        status(root, after).put("before", requireNotNull(before).json()).put("configurationWritten", true)
    }

    fun root(project: Path): JSONObject = respond {
        val state = store.load(project)
        if (state.magentoPath == null) failure("unconfigured", "Configure the Magento root first.")
        else ok().put("projectPath", state.projectPath).put("magentoPath", state.magentoPath)
            .put("enabled", state.enabled).put("configurationWritten", false)
    }

    fun describeEnvironment(project: Path): JSONObject = respond {
        val root = store.projectRoot(project)
        val state = store.load(root)
        if (!state.enabled) return@respond failure("disabled", "Magento integration is disabled; use configure_magento_project.")
        val target = state.magentoPath ?: return@respond failure("unconfigured", "Configure the Magento root first.")
        val magento = resolveRoot(root, target)
        val relative = root.relativize(magento).toString().replace('\\', '/').ifEmpty { null }
        ok().put("projectPath", root.toString()).put("magentoPath", magento.toString())
            .put("configurationRevision", state.revision).put("configurationWritten", false).put("runtimeProbed", false)
            .put("description", MagentoCliEnvironment.describeCliEnvironment(root, relative, state.cliToolCandidates,
                magerunAvailable = false, skillsAvailable = false))
    }

    private fun status(root: Path, state: ProjectSettings): JSONObject = ok()
        .put("projectPath", root.toString()).put("settings", state.json())
        .put("settingsFile", store.settingsFile(root).toAbsolutePath().toString())
        .put("configurationWritten", false).put("persistence", "host_file")
        .put("indexingExclusionsSupported", false)

    private fun resolveRoot(project: Path, value: String): Path {
        val path = project.resolve(value).normalize()
        require(path.startsWith(project) && Files.isDirectory(path)) { "Magento root must be an existing directory inside this project." }
        val real = path.toRealPath()
        require(real.startsWith(project)) { "Magento root cannot follow a symlink outside this project." }
        return real
    }

    private fun validateCandidates(root: Path, values: List<String>): List<String> {
        require(values.size <= 100) { "At most 100 CLI wrapper candidates are supported." }
        return values.map { value ->
            val candidate = value.trim().replace('\\', '/').removePrefix("./")
            require(candidate.isNotEmpty() && candidate.length <= 1024 && candidate.none(Char::isISOControl)
                && !Path.of(candidate).isAbsolute && candidate.split('/').none { it == ".." }
                && MagentoCliEnvironment.resolveProjectPath(root, candidate) != null) {
                "CLI wrapper candidates must be project-relative paths without parent traversal."
            }
            candidate
        }.distinct()
    }

    private fun respond(action: () -> JSONObject): JSONObject = try { action() }
        catch (failure: IllegalArgumentException) { failure("invalid_request", failure.message ?: "Invalid arguments.") }
        catch (failure: IllegalStateException) { failure("invalid_configuration", failure.message ?: "Invalid stored settings.") }
        catch (_: IOException) { failure("storage_error", "The project or Magento settings could not be read or written.") }

    private fun ok() = JSONObject().put("status", "ok").put("successful", true)
    private fun failure(status: String, message: String) = JSONObject().put("status", status)
        .put("successful", false).put("message", message).put("configurationWritten", false)
}
