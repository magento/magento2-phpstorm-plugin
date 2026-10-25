/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

/** Optional method/interception analysis; its shared contract contains no PHP PSI types. */
interface MagentoMethodPluginQueryProvider {
    fun findPluginsForMethod(project: Project, className: String, methodName: String): String

    companion object {
        val EP_NAME = ExtensionPointName.create<MagentoMethodPluginQueryProvider>(
            "com.magento.idea.magento2plugin.methodPluginQueryProvider"
        )
        const val UNAVAILABLE_MESSAGE =
            "Query `plugins_for_method` is unavailable: it requires the JetBrains PHP plugin. " +
                "Use `di_config` for XML declarations; it does not resolve PHP methods or inheritance."

        /** Checking extension registration does not instantiate the optional PHP provider. */
        fun isAvailable(): Boolean = EP_NAME.hasAnyExtensions()
    }
}
