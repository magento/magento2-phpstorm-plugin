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
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.util.IsClickedDirectoryInsideProject;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

public class NewModuleFileGroup extends NonTrivialActionGroup {

    /**
     * Group for generate module file actions.
     */
    public NewModuleFileGroup() {
        super();

        this.getTemplatePresentation().setIcon(new LazyIcon() {
            @NotNull
            @Override
            protected Icon compute() {
                return MagentoIcons.MODULE;
            }
        });
    }

    @Override
    public void update(final AnActionEvent event) {
        final PsiElement psiElement = event.getData(PlatformDataKeys.PSI_ELEMENT);
        if (!(psiElement instanceof PsiDirectory)) {
            event.getPresentation().setVisible(false);
            return;
        }

        final Project project = event.getData(PlatformDataKeys.PROJECT);

        if (project == null
                || !Settings.isEnabled(project)
                || !IsClickedDirectoryInsideProject.getInstance()
                .execute(project, (PsiDirectory) psiElement)) {
            event.getPresentation().setVisible(false);
            return;
        }

        final String moduleName = GetModuleNameByDirectoryUtil
                .execute((PsiDirectory) psiElement, project);

        if (moduleName != null) {
            final PsiDirectory moduleDirectory = new ModuleIndex(project)
                    .getModuleDirectoryByModuleName(moduleName);

            if (moduleDirectory != null
                    && GetMagentoModuleUtil.isDirectoryInEditableModule(moduleDirectory)) {
                event.getPresentation().setVisible(true);
                return;
            }
        }

        event.getPresentation().setVisible(false);
    }
}
