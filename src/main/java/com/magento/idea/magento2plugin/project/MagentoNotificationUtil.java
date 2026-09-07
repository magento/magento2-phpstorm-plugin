/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
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
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Magento2.SettingsForm");
            }
        };
        showPopup(project, message, showSettingsAction);
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
        final Notification notification = new Notification(
                "Magento 2 and Adobe Commerce",
                title,
                message,
                notificationType
        );

        for (final Function<Notification, AnAction> generator : actions) {
            notification.addAction(generator.apply(notification));
        }

        notification.notify(project);
    }
}
