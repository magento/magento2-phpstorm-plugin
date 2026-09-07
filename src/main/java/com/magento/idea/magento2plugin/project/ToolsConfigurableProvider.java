/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableProvider;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ToolsConfigurableProvider extends ConfigurableProvider {
    private static final PluginId PHP_PLUGIN_ID = PluginId.getId("com.jetbrains.php");

    private final Project project;

    public ToolsConfigurableProvider(final @NotNull Project project) {
        this.project = project;
    }

    @Override
    public Configurable createConfigurable() {
        return new SettingsForm(project);
    }

    @Override
    public boolean canCreateConfigurable() {
        return !PluginManagerCore.isLoaded(PHP_PLUGIN_ID);
    }
}
