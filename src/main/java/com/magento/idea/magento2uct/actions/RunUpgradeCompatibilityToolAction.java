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
import com.magento.idea.magento2uct.execution.DefaultExecutor;
import com.magento.idea.magento2uct.execution.process.DefaultAnalysisHandler;
import com.magento.idea.magento2uct.settings.UctSettingsService;
import org.jetbrains.annotations.NotNull;

public class RunUpgradeCompatibilityToolAction extends AnAction {

    public static final String ACTION_NAME = "Run The Upgrade Compatibility Tool";
    public static final String ACTION_DESCRIPTION = "Magento 2 Upgrade Compatibility Tool";

    /**
     * An action constructor.
     */
    public RunUpgradeCompatibilityToolAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, AllIcons.Actions.Execute);
    }

    @Override
    public void update(final @NotNull AnActionEvent event) {
        setIsAvailableForEvent(event, false);
        final Project project = event.getProject();

        if (project == null || !Settings.isEnabled(project)) {
            return;
        }
        final UctSettingsService uctSettingsService = UctSettingsService.getInstance(project);
        setIsAvailableForEvent(event, true);

        if (!uctSettingsService.isEnabled()) {
            event.getPresentation().setEnabled(false);
        }
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent event) {
        final Project project = event.getProject();

        if (project == null) {
            return;
        }
        final DefaultExecutor executor = new DefaultExecutor(
                project,
                new DefaultAnalysisHandler(project)
        );
        executor.run();
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
