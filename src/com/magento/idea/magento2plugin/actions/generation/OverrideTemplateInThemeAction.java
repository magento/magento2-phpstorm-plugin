/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.CopyMagentoPath;
import com.magento.idea.magento2plugin.actions.generation.dialog.OverrideTemplateInThemeDialog;
import org.jetbrains.annotations.NotNull;

public class OverrideTemplateInThemeAction extends OverrideFileInThemeAction {

    public static final String ACTION_NAME = "Override this file in a project theme";
    public static final String ACTION_TEMPLATE_DESCRIPTION = "Override template in project theme";
    public static final String ACTION_STYLES_DESCRIPTION = "Override styles in project theme";
    public static final String LESS_FILE_EXTENSION = "less";

    public OverrideTemplateInThemeAction() {
        super(ACTION_NAME, ACTION_TEMPLATE_DESCRIPTION, MagentoIcons.MODULE);
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent event) {
        final Project project = event.getProject();

        if (project == null || psiFile == null) {
            return;
        }
        OverrideTemplateInThemeDialog.open(project, psiFile);
    }

    @Override
    protected boolean isOverrideAllowed(
            final @NotNull PsiFile file,
            final @NotNull Project project
    ) {
        final VirtualFile virtualFile = file.getVirtualFile();

        if (virtualFile == null || virtualFile.isDirectory()) {
            return false;
        }
        final String fileExtension = virtualFile.getExtension();

        if (!CopyMagentoPath.PHTML_EXTENSION.equals(fileExtension)
                && !LESS_FILE_EXTENSION.equals(fileExtension)) {
            return false;
        }

        return isFileInModuleOrTheme(file, project);
    }
}
