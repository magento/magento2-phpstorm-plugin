/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewCLICommandDialog;
import org.jetbrains.annotations.NotNull;

public class NewCLICommandAction extends AnAction {
    public static String ACTION_NAME = "Magento 2 CLI Command";
    public static String ACTION_DESCRIPTION = "Create a new Magento 2 CLI Command";

    NewCLICommandAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);

        if (view == null) {
            return;
        }

        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return;
        }

        PsiDirectory directory = view.getOrChooseDirectory();
        if (directory == null) {
            return;
        }

        NewCLICommandDialog.open(project, directory);
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }
}