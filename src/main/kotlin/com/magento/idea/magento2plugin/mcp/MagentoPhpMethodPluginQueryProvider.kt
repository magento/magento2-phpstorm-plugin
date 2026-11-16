/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project

/**
 * Registered only when both PHP and MCP support are available. Internal because it returns internal
 * records; the class is still public in bytecode, so the extension point can create it.
 */
internal class MagentoPhpMethodPluginQueryProvider : MagentoMethodPluginQueryProvider, MagentoMethodPluginRecordProvider {
    override fun findPluginsForMethod(project: Project, className: String, methodName: String): String =
        MagentoPhpDiQueries.findPluginsForMethod(project, className, methodName)

    override fun pluginRecords(project: Project, className: String, methodName: String): List<InspectionRecord> =
        MagentoPhpDiQueries.pluginRecords(project, className, methodName)
}
