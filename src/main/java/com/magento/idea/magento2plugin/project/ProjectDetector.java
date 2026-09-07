/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectConfigurator;
import com.magento.idea.magento2plugin.indexes.IndexManager;
import com.magento.idea.magento2plugin.magento.packages.MagentoComponentManager;
import com.magento.idea.magento2plugin.project.startup.DeferredProjectOpenActions;
import com.magento.idea.magento2plugin.util.magento.MagentoBasePathUtil;
import com.magento.idea.magento2plugin.util.magento.MagentoVersionUtil;
import org.jetbrains.annotations.NotNull;

public class ProjectDetector implements DirectoryProjectConfigurator {
    @Override
    public void configureProject(
            final @NotNull Project project,
            final @NotNull VirtualFile baseDir,
            final @NotNull Ref<Module> moduleRef,
            final boolean newProject
    ) {
        DeferredProjectOpenActions.getInstance(project).runAfterProjectOpened(() -> {
            DumbService.getInstance(project).smartInvokeLater(() -> {
                if (!MagentoBasePathUtil.isMagentoFolderValid(baseDir.getPath())) {
                    return;
                }
                final Notification notification = NotificationGroupManager.getInstance()
                        .getNotificationGroup("Magento Notifications")
                        .createNotification(
                                "Magento",
                                "Enable Magento support for this project?",
                                NotificationType.INFORMATION
                        );
                notification.addAction(NotificationAction.createSimpleExpiring("Enable", () -> {
                            Settings settings = Settings.getInstance(project);
                            settings.pluginEnabled = true;
                            settings.mftfSupportEnabled = true;
                            settings.setMagentoPath(project.getBasePath());
                            settings.magentoVersion = MagentoVersionUtil.get(
                                    project,
                                    project.getBasePath()
                            );
                            IndexManager.manualReindex();
                            MagentoComponentManager.getInstance(project).flushModules();
                        }));
                Notifications.Bus.notify(notification, project);
            });
        });
    }
}
