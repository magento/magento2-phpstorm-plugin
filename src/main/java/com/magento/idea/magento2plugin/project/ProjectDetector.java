/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectConfigurator;
import com.magento.idea.magento2plugin.indexes.IndexManager;
import com.magento.idea.magento2plugin.magento.packages.MagentoComponentManager;
import com.magento.idea.magento2plugin.util.magento.MagentoBasePathUtil;
import com.magento.idea.magento2plugin.util.magento.MagentoVersionUtil;
import javax.swing.event.HyperlinkEvent;
import org.jetbrains.annotations.NotNull;

public class ProjectDetector implements DirectoryProjectConfigurator {
    @Override
    public void configureProject(
            final @NotNull Project project,
            final @NotNull VirtualFile baseDir,
            final @NotNull Ref<Module> moduleRef,
            final boolean newProject
    ) {
        StartupManager.getInstance(project).runWhenProjectIsInitialized(() -> {
            DumbService.getInstance(project).smartInvokeLater(() -> {
                if (!MagentoBasePathUtil.isMagentoFolderValid(baseDir.getPath())) {
                    return;
                }
                final Notification notification = new Notification("Magento", "Magento",
                        "<a href='enable'>Enable</a> Magento support for this project?",
                        NotificationType.INFORMATION, new NotificationListener.Adapter() {
                            @Override
                            public void hyperlinkActivated(
                                    final @NotNull Notification notification,
                                    final @NotNull HyperlinkEvent event
                            ) {
                                Settings settings = Settings.getInstance(project);
                                settings.pluginEnabled = true;
                                settings.mftfSupportEnabled = true;
                                settings.magentoPath = project.getBasePath();
                                settings.magentoVersion = MagentoVersionUtil.get(
                                        project,
                                        project.getBasePath()
                                );
                                IndexManager.manualReindex();
                                MagentoComponentManager.getInstance(project).flushModules();
                                notification.expire();
                            }
                        });
                Notifications.Bus.notify(notification, project);
            });
        });
    }
}
