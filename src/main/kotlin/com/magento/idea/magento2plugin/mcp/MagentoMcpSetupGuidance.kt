/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project
import org.json.JSONObject

/** Read-only recovery guidance, including when a client exposes only the JetBrains router. */
internal object MagentoMcpSetupGuidance {
    fun disabled(project: Project): String = withNextStep(project, "Magento plugin support is disabled for this project.")

    fun missingRoot(project: Project): String = withNextStep(project, "Magento root path is not configured for this project.")

    private fun withNextStep(project: Project, message: String): String = buildString {
        appendLine(message)
        appendLine("Next step: discover the project configuration tool's arguments (read-only; does not change project settings).")
        appendLine("Direct: call magento_intelligence with " +
            JSONObject().put("tool", "configure_magento_project"))
        append("Router: call execute_tool with " + JSONObject()
            .put("command", "magento_intelligence --tool configure_magento_project")
            .put("projectPath", project.basePath ?: "<current IDE project directory>"))
    }
}
