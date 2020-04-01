/**
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
import com.intellij.openapi.vfs.*;
import com.intellij.platform.DirectoryProjectConfigurator;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.indexes.IndexManager;
import com.magento.idea.magento2plugin.php.module.MagentoComponentManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;


public class ProjectDetector implements DirectoryProjectConfigurator {
    @Override
    public void configureProject(@NotNull Project project, @NotNull VirtualFile baseDir, @NotNull Ref<Module> moduleRef, boolean newProject) {
        StartupManager.getInstance(project).runWhenProjectIsInitialized(() -> {
            DumbService.getInstance(project).smartInvokeLater(() -> {
                if (null == VfsUtil.findRelativeFile(project.getBaseDir(), "app", "etc", "di.xml")) {
                    return;
                }
                Notification notification = new Notification("Magento", "Magento",
                        "<a href='enable'>Enable</a> Magento support for this project?",
                        NotificationType.INFORMATION, new NotificationListener.Adapter() {
                    @Override
                    public void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent event) {
                        Settings.getInstance(project).pluginEnabled = true;
                        IndexManager.manualReindex();
                        MagentoComponentManager.getInstance(project).flushModules();
                        notification.expire();
                    }
                }
                );
                Notifications.Bus.notify(notification, project);
            });
        });
    }
}
