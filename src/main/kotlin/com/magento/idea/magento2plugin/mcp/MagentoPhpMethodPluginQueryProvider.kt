/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project

/** Registered only when both PHP and MCP support are available. */
class MagentoPhpMethodPluginQueryProvider : MagentoMethodPluginQueryProvider {
    override fun findPluginsForMethod(project: Project, className: String, methodName: String): String =
        MagentoPhpDiQueries.findPluginsForMethod(project, className, methodName)
}
