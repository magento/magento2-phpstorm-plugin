/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewModuleDialog;
import com.magento.idea.magento2plugin.actions.generation.util.IsClickedDirectoryInsideProject;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import com.magento.idea.magento2plugin.util.magento.MagentoBasePathUtil;
import org.jetbrains.annotations.NotNull;

public class NewModuleAction extends com.intellij.openapi.actionSystem.AnAction {
    public static final String ACTION_NAME = "Magento 2 Module";
    public static final String ACTION_DESCRIPTION = "Create a new Magento 2 Module";

    /**
     * Constructor.
     */
    public NewModuleAction() {
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
        if (project != null) {
            final PsiDirectory initialBaseDir = view.getOrChooseDirectory();
            if (initialBaseDir != null) {
                this.invoke(project, initialBaseDir);
            }
        }
    }

    public void invoke(final @NotNull Project project, final @NotNull PsiDirectory initialBaseDir) {
        NewModuleDialog.open(project, initialBaseDir);
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }

    /**
     * Inherit doc.
     *
     * @param event AnActionEvent
     */
    @Override
    public void update(final AnActionEvent event) {
        final Project project = event.getData(PlatformDataKeys.PROJECT);

        if (project == null) {
            return;
        }

        if (Settings.isEnabled(project)) {
            final String magentoPath = Settings.getMagentoPath(project);
            if (magentoPath == null) {
                event.getPresentation().setVisible(false);
                return;
            }
            final PsiElement psiElement = event.getData(PlatformDataKeys.PSI_ELEMENT);
            if (!(psiElement instanceof PsiDirectory)) {
                event.getPresentation().setVisible(false);
                return;
            }

            if (!IsClickedDirectoryInsideProject.getInstance().execute(
                    project,
                    (PsiDirectory) psiElement)
            ) {
                event.getPresentation().setVisible(false);
                return;
            }

            final String moduleName = GetModuleNameByDirectoryUtil
                    .execute((PsiDirectory) psiElement, project);
            if (moduleName == null) {
                final String sourceDirPath = ((PsiDirectory) psiElement).getVirtualFile().getPath();
                final boolean isCustomCodeSourceDirValid =
                        MagentoBasePathUtil.isCustomCodeSourceDirValid(sourceDirPath);
                final boolean isCustomVendorDirValid =
                        MagentoBasePathUtil.isCustomVendorDirValid(sourceDirPath);

                if (!isCustomCodeSourceDirValid && !isCustomVendorDirValid) { //NOPMD
                    event.getPresentation().setVisible(false);
                    return;
                }
                event.getPresentation().setVisible(true);
                return;
            }
        }

        event.getPresentation().setVisible(false);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
