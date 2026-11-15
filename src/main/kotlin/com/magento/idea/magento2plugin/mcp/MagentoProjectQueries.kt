/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project
import com.magento.idea.magento2plugin.project.Settings

internal object MagentoProjectQueries {
    /**
     * Returns the Magento root path configured in project settings.
     */
    fun getMagentoRootPath(project: Project): String {
        val configuredPath = Settings.getMagentoPath(project)?.trim().orEmpty()
        if (configuredPath.isEmpty()) {
            return MagentoMcpSetupGuidance.missingRoot(project)
        }

        return "Configured Magento root path: $configuredPath"
    }
}
