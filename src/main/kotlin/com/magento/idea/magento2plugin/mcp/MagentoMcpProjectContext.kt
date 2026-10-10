/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.magento.idea.magento2plugin.project.Settings
import org.json.JSONObject
import java.nio.file.Path

/** Local, index-independent context for agents; never runs Composer or discovers remote releases. */
internal object MagentoMcpProjectContext {
    private const val PLUGIN_ID = "com.magento.idea.magento2plugin"
    fun magentoRoot(project: Project): Path? {
        val base = project.basePath?.let(Path::of) ?: return null
        val configured = Settings.getMagentoPath(project) ?: return base.normalize()
        val path = Path.of(configured)
        return (if (path.isAbsolute) path else base.resolve(path)).normalize()
    }

    fun identity(project: Project): JSONObject = JSONObject()
        .put("projectPath", project.basePath ?: JSONObject.NULL)
        .put("magentoRoot", magentoRoot(project)?.toString() ?: JSONObject.NULL)
        .put("pluginVersion", PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID))?.version ?: JSONObject.NULL)
        .put("ideVersion", ApplicationInfo.getInstance().fullVersion)

}
