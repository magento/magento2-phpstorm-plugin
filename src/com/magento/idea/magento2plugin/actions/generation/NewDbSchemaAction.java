/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewDbSchemaDialog;
import org.jetbrains.annotations.NotNull;

public class NewDbSchemaAction extends AnAction {

    public static final String ACTION_NAME = "Magento 2 Declarative Schema XML";
    public static final String ACTION_DESCRIPTION = "Create a new declarative schema XML";

    /**
     * Constructor.
     */
    public NewDbSchemaAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent event) {
        final DataContext dataContext = event.getDataContext();
        final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);

        if (view == null) {
            return;
        }
        final Project project = CommonDataKeys.PROJECT.getData(dataContext);

        if (project == null) {
            return;
        }
        final PsiDirectory directory = view.getOrChooseDirectory();

        if (directory == null) {
            return;
        }
        NewDbSchemaDialog.open(project, directory);
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
