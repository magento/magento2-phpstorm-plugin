/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.startup;

import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.startup.StartupActivity;
import com.magento.idea.magento2plugin.init.ConfigurationManager;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.MagentoBasePathUtil;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CheckIfMagentoPathIsValidActivity implements StartupActivity, ProjectActivity {
    private static final PluginId PHP_PLUGIN_ID = PluginId.getId("com.jetbrains.php");

    @Override
    public void runActivity(final @NotNull Project project) {
        registerSettings(project);
    }

    @Nullable
    @Override
    public Object execute(@NotNull final Project project,
                          @NotNull final Continuation<? super Unit> continuation) {
        registerSettings(project);
        return null;
    }

    private void registerSettings(final @NotNull Project project) {
        registerNonPhpPhtmlSupport();
        final Settings settings = Settings.getInstance(project);
        final String path = Settings.getMagentoPath(project);
        if (settings.pluginEnabled && (path == null || path.isEmpty())) {
            if (MagentoBasePathUtil.isMagentoFolderValid(project.getBasePath())) {
                settings.setMagentoPath(project.getBasePath());
            } else {
                settings.pluginEnabled = false;
                ConfigurationManager.suggestToConfigureMagentoPath(project);
            }
        }
        DeferredProjectOpenActions.getInstance(project).runPendingActions();
    }

    private void registerNonPhpPhtmlSupport() {
        if (PluginManagerCore.isLoaded(PHP_PLUGIN_ID)) {
            return;
        }
        final FileTypeManager fileTypeManager = FileTypeManager.getInstance();

        if (fileTypeManager.getFileTypeByExtension("phtml") == HtmlFileType.INSTANCE) {
            return;
        }
        fileTypeManager.associateExtension(HtmlFileType.INSTANCE, "phtml");
    }
}
