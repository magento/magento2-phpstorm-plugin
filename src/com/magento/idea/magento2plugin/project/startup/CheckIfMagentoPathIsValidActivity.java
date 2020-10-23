/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.startup;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.magento.idea.magento2plugin.init.ConfigurationManager;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.MagentoBasePathUtil;
import org.jetbrains.annotations.NotNull;

public class CheckIfMagentoPathIsValidActivity implements StartupActivity {

    @Override
    public void runActivity(final @NotNull Project project) {
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
}
