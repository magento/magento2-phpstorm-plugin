/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project
import com.magento.idea.magento2plugin.project.MagentoProjectConfigurationService
import com.magento.idea.magento2plugin.project.MagentoProjectConfigurationService.ConfigurationResult
import com.magento.idea.magento2plugin.project.MagentoProjectConfigurationService.ConfigurationState
import com.magento.idea.magento2plugin.project.MagentoProjectConfigurationService.InstallationType
import com.magento.idea.magento2plugin.util.magento.MagentoVersionUtil

internal object MagentoProjectConfigurationCommands {

    fun help(): String {
        val lines = mutableListOf<String>()
        lines += "Magento Project Configuration Tool"
        lines += "Inspect and configure Magento project integration and root path in the current IDE project."
        lines += ""
        lines += "Actions / Modes:"
        lines += "  • action=status: Read-only inspection of Magento project integration, root path, installation type, and indexing readiness."
        lines += "  • action=configure (default): Configure Magento root path, enable/disable integration, and control indexing exclusions."
        lines += "  • action=help: Show this discoverable help and schema documentation."
        lines += ""
        lines += "Parameters:"
        lines += "  • action (string, optional, default \"configure\"): One of \"status\", \"configure\", or \"help\"."
        lines += "  • magentoPath (string, optional): Project-relative (e.g. \"src\" or \".\") or absolute path to the Magento directory. Required when enabling integration."
        lines += "  • enabled (boolean, optional): true to enable Magento integration, false to disable."
        lines += "  • excludeGeneratedFiles (boolean, optional): Controls whether generated and runtime files are excluded from indexing. Enabled by default when first enabled; pass false to opt out."
        lines += "  • projectPath (string, optional): Intended IDE project directory path to verify against the active project."
        lines += ""
        lines += "Guidance:"
        lines += "  - Calling configure before Magento integration is enabled or during IDE indexing is fully supported."
        lines += "  - Relative paths are resolved against the IDE project base path and normalized."
        lines += "  - Supported partial checkouts and source checkouts are preserved; missing dependencies or unknown versions are reported explicitly."
        lines += "  - Repeated identical requests are idempotent and do not trigger needless reindexing."
        return lines.joinToString("\n")
    }

    fun getStatus(project: Project): String {
        val service = MagentoProjectConfigurationService.getInstance(project)
        val state = service.currentState
        val lines = mutableListOf<String>()

        lines += "Magento Project Integration Status"
        lines += "Integration enabled: ${if (state.enabled) "yes" else "no"}"
        lines += "Configured Magento path: ${state.configuredPath ?: "(not configured)"}"
        lines += "Resolved Magento path: ${state.resolvedPath ?: "(not configured)"}"

        val installation = state.installation
        lines += "Installation type: ${installation.type.displayName}"
        val versionText = when {
            installation.version.isNullOrBlank() || MagentoVersionUtil.DEFAULT_VERSION.equals(installation.version, ignoreCase = true) -> "unknown"
            else -> installation.version
        }
        lines += "Platform version: $versionText"
        if (!installation.edition.isNullOrBlank()) {
            lines += "Platform edition: ${installation.edition}"
        }
        lines += "Root composer.json: ${if (installation.composerJsonExists) "present" else "missing"}"
        lines += "Framework directory: ${if (installation.frameworkExists) "present" else "missing"}"

        if (installation.missingDependencies.isNotEmpty()) {
            lines += "Missing dependencies: ${installation.missingDependencies.joinToString(", ")}"
        }

        val exclusionText = when (state.excludeGeneratedFiles) {
            true -> "enabled"
            false -> "disabled (opt-out)"
            null -> if (state.enabled) "enabled (default)" else "not configured"
        }
        lines += "Indexing exclusions: $exclusionText"
        lines += "Pending index refresh: ${if (state.indexingPending) "yes" else "no"}"
        lines += "Next usable action: ${service.determineNextAction(state)}"

        return lines.joinToString("\n")
    }

    fun configure(
        project: Project,
        action: String = "configure",
        magentoPath: String = "",
        enabled: Boolean? = null,
        excludeGeneratedFiles: Boolean? = null,
        projectPath: String = ""
    ): String {
        val normalizedAction = action.trim().lowercase()

        if (normalizedAction == "help" || normalizedAction == "schema" || normalizedAction == "detailed_schema") {
            return help()
        }

        if (normalizedAction == "status") {
            return getStatus(project)
        }

        if (normalizedAction != "configure" && normalizedAction.isNotBlank()) {
            return """
                Result: error
                Message: Unsupported action '$action'. Supported actions are: status, configure, help.
                Next usable action: Call configure_magento_project with action="status", action="configure", or action="help".
            """.trimIndent()
        }

        val isConfigArgProvided = magentoPath.isNotBlank() || enabled != null || excludeGeneratedFiles != null
        if (!isConfigArgProvided) {
            return getStatus(project)
        }

        val service = MagentoProjectConfigurationService.getInstance(project)
        val result = service.configure(
            magentoPath.ifBlank { null },
            enabled,
            excludeGeneratedFiles,
            projectPath.ifBlank { null }
        )

        return formatResult(service, result)
    }

    private fun formatResult(
        service: MagentoProjectConfigurationService,
        result: ConfigurationResult
    ): String {
        val lines = mutableListOf<String>()
        lines += "Magento Project Configuration"
        lines += "Result: ${if (result.success) "success" else "error"}"
        lines += "Message: ${result.message}"
        lines += ""

        lines += "Previous State:"
        lines += "  Integration enabled: ${if (result.previousState.enabled) "yes" else "no"}"
        lines += "  Configured path: ${result.previousState.configuredPath ?: "(none)"}"
        lines += "  Indexing exclusions: ${formatExclusionState(result.previousState)}"

        lines += ""
        if (result.success) {
            lines += "Current State:"
            lines += "  Integration enabled: ${if (result.currentState.enabled) "yes" else "no"}"
            lines += "  Configured path: ${result.currentState.configuredPath ?: "(none)"}"
            lines += "  Resolved path: ${result.currentState.resolvedPath ?: "(none)"}"
            lines += "  Installation type: ${result.currentState.installation.type.displayName}"
            val currentVersionText = when {
                result.currentState.installation.version.isNullOrBlank() || MagentoVersionUtil.DEFAULT_VERSION.equals(result.currentState.installation.version, ignoreCase = true) -> "unknown"
                else -> result.currentState.installation.version
            }
            lines += "  Platform version: $currentVersionText"
            if (!result.currentState.installation.edition.isNullOrBlank()) {
                lines += "  Platform edition: ${result.currentState.installation.edition}"
            }
            lines += "  Indexing exclusions: ${formatExclusionState(result.currentState)}"

            lines += ""
            lines += "Status:"
            val recognized = result.currentState.installation.type == InstallationType.RECOGNIZED_APPLICATION
            lines += "  Installation recognized: ${if (recognized) "yes" else "no"}"
            lines += "  Framework directory: ${if (result.currentState.installation.frameworkExists) "present" else "missing"}"
            val deps = if (result.currentState.installation.missingDependencies.isEmpty()) {
                "complete"
            } else {
                "missing: " + result.currentState.installation.missingDependencies.joinToString(", ")
            }
            lines += "  Composer dependencies: $deps"
            lines += "  Pending index refresh: ${if (result.currentState.indexingPending || result.indexRefreshRequested) "yes" else "no"}"
        } else {
            lines += "Current State:"
            lines += "  (unchanged - prior settings preserved)"
            lines += ""
            lines += "Validation Failures:"
            for (error in result.validationErrors) {
                lines += "  • $error"
            }
        }

        lines += ""
        lines += "Next usable action: ${result.nextUsableAction}"
        return lines.joinToString("\n")
    }

    private fun formatExclusionState(state: ConfigurationState): String = when (state.excludeGeneratedFiles) {
        true -> "enabled"
        false -> "disabled (opt-out)"
        null -> if (state.enabled) "enabled (default)" else "disabled"
    }
}
