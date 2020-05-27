/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewMenuItemDialog;

@SuppressWarnings({"PMD.FieldNamingConventions"})
public class NewMenuItemAction extends AnAction {
    public static String ACTION_NAME = "Magento 2 Menu Item";
    public static String ACTION_DESCRIPTION = "Create a new Magento 2 Menu Item";

    public NewMenuItemAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
    }

    @Override
    public void actionPerformed(
            @org.jetbrains.annotations.NotNull
            final AnActionEvent event
    ) {
        final DataContext context = event.getDataContext();
        final IdeView view = LangDataKeys.IDE_VIEW.getData(context);
        if (view == null) {
            return;
        }

        final Project project = CommonDataKeys.PROJECT.getData(context);
        if (project == null) {
            return;
        }

        final PsiDirectory directory = view.getOrChooseDirectory();
        if (directory == null) {
            return;
        }

        NewMenuItemDialog.open(project, directory);
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }
}
