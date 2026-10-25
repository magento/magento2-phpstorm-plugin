/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.startup;

import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class CheckIfMagentoPathIsValidActivityTest extends BasePlatformTestCase {

    public void testMissingInstallationDisablesSupportAndOffersSettings() {
        final Settings settings = Settings.getInstance(getProject());
        settings.pluginEnabled = true;
        settings.magentoPath = null;
        final List<Notification> notifications = new ArrayList<>();
        getProject().getMessageBus().connect(getTestRootDisposable()).subscribe(
                Notifications.TOPIC,
                new Notifications() {
                    @Override
                    public void notify(final @NotNull Notification notification) {
                        if ("Magento Notifications".equals(notification.getGroupId())) {
                            notifications.add(notification);
                        }
                    }
                }
        );

        try {
            new CheckIfMagentoPathIsValidActivity().runActivity(getProject());
            PlatformTestUtil.dispatchAllEventsInIdeEventQueue();

            assertFalse(settings.pluginEnabled);
            assertSize(1, notifications);
            assertEquals(
                    "Magento 2 support is disabled. Please configure Magento 2 installation path.",
                    notifications.getFirst().getContent()
            );
            assertSize(1, notifications.getFirst().getActions());
            assertEquals(
                    "Show settings",
                    notifications.getFirst().getActions().getFirst().getTemplateText()
            );
        } finally {
            notifications.forEach(Notification::expire);
        }
    }

    public void testConfiguredInstallationRemainsEnabled() {
        final Settings settings = Settings.getInstance(getProject());
        settings.pluginEnabled = true;
        settings.magentoPath = "/configured/magento";

        new CheckIfMagentoPathIsValidActivity().runActivity(getProject());

        assertTrue(settings.pluginEnabled);
        assertEquals("/configured/magento", settings.magentoPath);
    }
}
