/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewModuleDialog;
import com.magento.idea.magento2plugin.actions.generation.util.IsClickedDirectoryInsideProject;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NewModuleAction extends com.intellij.openapi.actionSystem.AnAction {
    public static String ACTION_NAME = "Magento 2 Module";
    public static String ACTION_DESCRIPTION = "Create a new Magento 2 module";

    NewModuleAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
    }
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
        if (view != null) {
            Project project = CommonDataKeys.PROJECT.getData(dataContext);
            if (project != null) {
                PsiDirectory initialBaseDir = view.getOrChooseDirectory();
                if (initialBaseDir != null) {
                    this.invoke(project, initialBaseDir, this.getFile(dataContext), view, CommonDataKeys.EDITOR.getData(dataContext));
                }
            }
        }
    }

    public void invoke(@NotNull Project project, @NotNull PsiDirectory initialBaseDir, @Nullable PsiFile file, @Nullable IdeView view, @Nullable Editor editor) {
        NewModuleDialog.open(project, initialBaseDir, file, view, editor);
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }

    @NotNull
    private String getActionName() {
        return this.getTemplatePresentation().getText();
    }

    public PsiFile getFile(DataContext dataContext) {
        return CommonDataKeys.PSI_FILE.getData(dataContext);
    }

    public void update(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);

        if (Settings.isEnabled(project)) {
            String magentoPath = Settings.getMagentoPath(project);
            if (magentoPath == null) {
                event.getPresentation().setVisible(false);
                return;
            }
            PsiElement psiElement = event.getData(PlatformDataKeys.PSI_ELEMENT);
            if (!(psiElement instanceof PsiDirectory)) {
                event.getPresentation().setVisible(false);
                return;
            }

            if(!IsClickedDirectoryInsideProject.getInstance().execute(project, (PsiDirectory) psiElement)) {
                event.getPresentation().setVisible(false);
                return;
            }

            GetModuleNameByDirectory getModuleName = GetModuleNameByDirectory.getInstance(project);
            String moduleName = getModuleName.execute((PsiDirectory) psiElement);
            if (moduleName == null) {
                event.getPresentation().setVisible(true);
                return;
            }
        }

        event.getPresentation().setVisible(false);
    }
}
