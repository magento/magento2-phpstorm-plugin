/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.magento.idea.magento2plugin.project.Settings
import com.magento.idea.magento2plugin.project.indexing.MagentoIndexingExclusionService

internal object MagentoIndexingExclusionCommands {

    fun getStatus(project: Project): String {
        val service = MagentoIndexingExclusionService.getInstance(project)
        val settings = Settings.getInstance(project)
        val magentoPath = Settings.getMagentoPath(project)
        val pathValid = service.isMagentoPathConfiguredAndValid()

        if (!pathValid) {
            val pathStatus = if (magentoPath.isNullOrBlank()) "not configured" else "invalid ($magentoPath)"
            return "Exclusions cannot be applied: Magento installation path is $pathStatus. " +
                "Configure a valid Magento path before managing indexing exclusions."
        }

        val preview = service.previewExclusions(null, null)
        val lines = mutableListOf<String>()

        lines += "Magento Indexing Exclusions Status"
        lines += "Magento integration enabled: ${if (settings.pluginEnabled) "yes" else "no"}"
        lines += "Exclusion setting enabled: ${if (settings.isExcludeGeneratedFiles) "yes" else "no (opt-out)"}"
        lines += "Configured Magento path: $magentoPath"

        val additionalPaths = settings.getAdditionalExcludedPaths()
        if (additionalPaths.isEmpty()) {
            lines += "Additional project-relative paths: (none)"
        } else {
            lines += "Additional project-relative paths: ${additionalPaths.joinToString(", ")}"
        }

        val effective = preview.effectiveExclusions()
        lines += ""
        lines += "Effective Excluded Paths (${effective.size}):"
        if (effective.isEmpty()) {
            lines += "  (none)"
        } else {
            for (entry in effective) {
                val diskStatus = if (entry.existsOnDisk()) "exists" else "absent (monitored)"
                lines += "  • ${entry.relativePath()} — ${entry.reason()} [$diskStatus]"
            }
        }

        val skipped = preview.skippedEntries()
        if (skipped.isNotEmpty()) {
            lines += ""
            lines += "Skipped Candidates (${skipped.size}):"
            for (entry in skipped) {
                lines += "  • ${entry.path()} (${entry.reason()})"
            }
        }

        lines += ""
        lines += "Managed Exclusions: ${settings.getManagedExcludedFolders().size} folder URL(s)"
        val refreshPending = DumbService.getInstance(project).isDumb
        lines += "Pending index refresh: ${if (refreshPending) "yes" else "no"}"

        return lines.joinToString("\n")
    }

    fun configure(
        project: Project,
        action: String = "apply",
        enabled: Boolean? = null,
        additionalPaths: List<String>? = null
    ): String {
        val normalizedAction = action.trim().lowercase()
        val service = MagentoIndexingExclusionService.getInstance(project)

        when (normalizedAction) {
            "apply", "update" -> {
                val effectiveEnabled = enabled ?: Settings.getInstance(project).isExcludeGeneratedFiles
                val effectiveAdditional = additionalPaths ?: Settings.getInstance(project).getAdditionalExcludedPaths()

                if (!service.isMagentoPathConfiguredAndValid() && effectiveEnabled) {
                    return "Error: Magento installation path is not configured or is invalid. " +
                        "Configure a valid Magento path before exclusions can be applied."
                }

                val result = service.applyExclusions(effectiveEnabled, effectiveAdditional)
                return formatApplyResult(project, result, normalizedAction)
            }
            "reset" -> {
                val result = service.resetExclusions()
                val lines = mutableListOf<String>()
                lines += "Magento Indexing Exclusions Reset"
                lines += "Result: success"
                lines += "Removed managed exclusions: ${result.removedPaths().size}"
                for (path in result.removedPaths()) {
                    lines += "  - $path"
                }
                lines += "Pending index refresh: ${if (result.indexRefreshRequested()) "yes" else "no"}"
                return lines.joinToString("\n")
            }
            "preview", "status" -> {
                val effectiveEnabled = enabled ?: Settings.getInstance(project).isExcludeGeneratedFiles
                if (!service.isMagentoPathConfiguredAndValid() && effectiveEnabled) {
                    return "Error: Magento installation path is not configured or is invalid. " +
                        "Configure a valid Magento path before exclusions can be applied."
                }

                val preview = service.previewExclusions(enabled, additionalPaths)
                val lines = mutableListOf<String>()
                lines += "Magento Indexing Exclusions Preview (no changes applied)"
                lines += "Status: ${preview.statusMessage()}"
                lines += "Exclusion setting: ${if (preview.exclusionEnabled()) "enabled" else "disabled"}"
                lines += "Resolved paths to exclude (${preview.effectiveExclusions().size}):"
                for (entry in preview.effectiveExclusions()) {
                    val status = if (entry.existsOnDisk()) "exists" else "absent (monitored)"
                    lines += "  • ${entry.relativePath()} — ${entry.reason()} [$status]"
                }
                if (preview.skippedEntries().isNotEmpty()) {
                    lines += "Skipped entries:"
                    for (entry in preview.skippedEntries()) {
                        lines += "  • ${entry.path()} (${entry.reason()})"
                    }
                }
                lines += "URLs to add: ${preview.urlsToAdd().size}"
                lines += "URLs to remove: ${preview.urlsToRemove().size}"
                return lines.joinToString("\n")
            }
            else -> {
                return "Unknown action \"$action\". Supported actions are \"apply\", \"update\", \"reset\", and \"preview\"."
            }
        }
    }

    private fun formatApplyResult(
        project: Project,
        result: MagentoIndexingExclusionService.ExclusionApplyResult,
        actionName: String
    ): String {
        val lines = mutableListOf<String>()
        lines += "Magento Indexing Exclusions ${actionName.replaceFirstChar { it.uppercase() }}"
        lines += "Result: ${if (result.success()) "success" else "failure"}"
        lines += "Message: ${result.message()}"

        val added = result.addedPaths()
        lines += "Added exclusions (${added.size}):"
        if (added.isEmpty()) {
            lines += "  (none)"
        } else {
            for (p in added) {
                lines += "  + $p"
            }
        }

        val removed = result.removedPaths()
        lines += "Removed exclusions (${removed.size}):"
        if (removed.isEmpty()) {
            lines += "  (none)"
        } else {
            for (p in removed) {
                lines += "  - $p"
            }
        }

        val unchanged = result.unchangedPaths()
        if (unchanged.isNotEmpty()) {
            lines += "Unchanged exclusions (${unchanged.size}):"
            for (p in unchanged) {
                lines += "  = $p"
            }
        }

        val skipped = result.skippedEntries()
        if (skipped.isNotEmpty()) {
            lines += "Skipped candidates (${skipped.size}):"
            for (entry in skipped) {
                lines += "  • ${entry.path()} (${entry.reason()})"
            }
        }

        val effective = result.effectiveExclusions()
        lines += "Resolved effective exclusions (${effective.size}):"
        for (entry in effective) {
            lines += "  • ${entry.relativePath()} — ${entry.reason()}"
        }

        val refreshPending = result.indexRefreshRequested() || DumbService.getInstance(project).isDumb
        lines += "Pending index refresh: ${if (refreshPending) "yes" else "no"}"

        return lines.joinToString("\n")
    }
}
