/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.DumbAwareAction;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MagentoNotificationUtil {
    private MagentoNotificationUtil() {
    }

    public static void suggestToConfigureMagentoPath(final @NotNull Project project) {
        final String message = "Magento 2 support is disabled. "
                + "Please configure Magento 2 installation path.";
        final Function<Notification, AnAction> showSettingsAction = notification -> new DumbAwareAction(
                "Show settings"
        ) {
            @Override
            public void actionPerformed(final @NotNull AnActionEvent event) {
                notification.expire();
                ShowSettingsUtil.getInstance().showSettingsDialog(
                        project, MagentoNotificationUtil::isMagentoSettings, null
                );
            }
        };
        showPopup(project, message, showSettingsAction);
    }

    static boolean isMagentoSettings(final Configurable configurable) {
        if (!(configurable instanceof SearchableConfigurable searchable)) {
            return false;
        }
        final String id = searchable.getId();
        return "Magento2.SettingsForm".equals(id) || "settings.php.frameworks".equals(id);
    }

    @SafeVarargs
    private static void showPopup(
            final @Nullable Project project,
            final @NotNull String message,
            final @NotNull Function<Notification, AnAction>... actions
    ) {
        final Runnable runnable = () -> notifyGlobally(
                project,
                "Magento 2 and Adobe Commerce",
                message,
                NotificationType.INFORMATION,
                actions
        );
        ApplicationManager.getApplication().invokeLater(runnable, ModalityState.nonModal());
    }

    @SafeVarargs
    public static void notifyGlobally(
            final @Nullable Project project,
            final @NotNull String title,
            final @NotNull String message,
            final @NotNull NotificationType notificationType,
            final @NotNull Function<Notification, AnAction>... actions
    ) {
        final Notification notification = NotificationGroupManager.getInstance()
                .getNotificationGroup("Magento Notifications")
                .createNotification(title, message, notificationType);

        for (final Function<Notification, AnAction> generator : actions) {
            notification.addAction(generator.apply(notification));
        }

        notification.notify(project);
    }
}
