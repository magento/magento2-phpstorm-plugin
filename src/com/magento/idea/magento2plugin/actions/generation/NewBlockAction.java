/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Method;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewBlockDialog;
import com.magento.idea.magento2plugin.actions.generation.generator.code.PluginMethodsGenerator;
import com.magento.idea.magento2plugin.magento.files.RegistrationPhp;
import com.magento.idea.magento2plugin.project.Settings;
import org.jetbrains.annotations.NotNull;

public class NewBlockAction extends AnAction {
    public static String ACTION_NAME = "Magento 2 Block";
    public static String ACTION_DESCRIPTION = "Create a new Magento 2 block";

    NewBlockAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
    }

    /**
     * Triggers when new item is clicked
     */
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

        NewBlockDialog.open(project, directory);
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }
}

