/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.startup;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.startup.ProjectActivity;
import com.magento.idea.magento2plugin.init.ConfigurationManager;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.MagentoBasePathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CheckIfMagentoPathIsValidActivity implements StartupActivity, ProjectActivity {

    public void registerSettings(final @NotNull Project project) {
        final Settings settings = Settings.getInstance(project);
        final String path = settings.magentoPath;
        if (settings.pluginEnabled && (path == null || path.isEmpty())) {
            if (MagentoBasePathUtil.isMagentoFolderValid(project.getBasePath())) {
                settings.setMagentoPath(project.getBasePath());
                return;
            }
            settings.pluginEnabled = false;
            ConfigurationManager.suggestToConfigureMagentoPath(project);
        }
    }

    @Override
    public void runActivity(final @NotNull Project project) {
        registerSettings(project);
    }

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        registerSettings(project);
        return null;
    }
}
