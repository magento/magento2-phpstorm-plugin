/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.groups;

import com.intellij.ide.actions.NonTrivialActionGroup;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader.LazyIcon;
import javax.swing.Icon;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.util.IsClickedDirectoryInsideProject;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectory;
import org.jetbrains.annotations.NotNull;

public class NewModuleFileGroup extends NonTrivialActionGroup {
    public NewModuleFileGroup() {
        this.getTemplatePresentation().setIcon(new LazyIcon() {
            @NotNull
            protected Icon compute() {
                return MagentoIcons.MODULE;
            }
        });
    }

    public void update(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
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
        if (Settings.isEnabled(project) && moduleName != null) {
            event.getPresentation().setVisible(true);
            return;
        }
        event.getPresentation().setVisible(false);
    }
}
