/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project
import com.magento.idea.magento2plugin.project.Settings
import org.json.JSONObject

internal object MaintenanceDesktop {
    fun execute(project: Project, mode: String, checks: List<String>, files: List<String>, enabled: List<String>,
                devMode: String, cancel: () -> Unit): JSONObject {
        val root = MagentoCliToolQueries.projectRootPath(project)
        if (root == null || !Settings.isEnabled(project) || Settings.getMagentoPath(project).isNullOrBlank()) return MaintenanceService.error("invalid_configuration",
            "Configure and enable Magento integration first.")
        return MaintenanceService(root, MagentoCliToolQueries.resolveConfiguredMagentoRootRelativePath(project),
            Settings.getMcpCliToolCandidates(project)).execute(mode, checks, files, enabled, devMode, cancel)
    }
}
