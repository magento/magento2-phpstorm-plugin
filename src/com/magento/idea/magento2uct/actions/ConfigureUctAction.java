/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2uct.ui.ConfigurationDialog;
import org.jetbrains.annotations.NotNull;

public class ConfigureUctAction extends AnAction {

    public static final String ACTION_NAME = "Configure The Upgrade Compatibility Tool";
    public static final String ACTION_DESCRIPTION = "Magento 2 Upgrade Compatibility Tool Settings";

    /**
     * An action constructor.
     */
    public ConfigureUctAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, AllIcons.Actions.InlayGear);
    }

    @Override
    public void update(final @NotNull AnActionEvent event) {
        setIsAvailableForEvent(event, false);
        final Project project = event.getProject();

        if (project == null || !Settings.isEnabled(project)) {
            return;
        }
        setIsAvailableForEvent(event, true);
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent event) {
        final Project project = event.getProject();

        if (project == null) {
            return;
        }
        ConfigurationDialog.open(project);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    /**
     * Set is action available for event.
     *
     * @param event AnActionEvent
     * @param isAvailable boolean
     */
    private void setIsAvailableForEvent(
            final @NotNull AnActionEvent event,
            final boolean isAvailable
    ) {
        event.getPresentation().setVisible(isAvailable);
        event.getPresentation().setEnabled(isAvailable);
    }
}
