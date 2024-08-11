/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.actions;

import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2uct.ui.ReindexDialog;
import org.jetbrains.annotations.NotNull;

public class ReindexVersionedIndexesAction extends AnAction {

    public static final String ACTION_NAME = "Reindex the UCT versioned indexes";
    public static final String ACTION_DESCRIPTION =
            "Action provides reindexing feature for the internal UCT framework";

    /**
     * Action constructor.
     */
    public ReindexVersionedIndexesAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, AllIcons.Actions.Execute);
    }

    @Override
    public void update(final @NotNull AnActionEvent event) {
        setIsAvailableForEvent(event, false);
        final Project project = event.getProject();

        if (project == null
                || !Settings.isEnabled(project)
                || !ApplicationManager.getApplication().isInternal()) {
            return;
        }
        setIsAvailableForEvent(event, true);
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent event) {
        final Project project = event.getProject();
        final IdeView ideView = LangDataKeys.IDE_VIEW.getData(event.getDataContext());

        if (project == null || ideView == null) {
            return;
        }
        final PsiDirectory directory = ideView.getOrChooseDirectory();

        if (directory == null) {
            return;
        }
        ReindexDialog.open(project, directory);
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
