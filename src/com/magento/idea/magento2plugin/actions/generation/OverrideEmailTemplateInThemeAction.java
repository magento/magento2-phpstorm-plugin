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
import com.magento.idea.magento2plugin.actions.generation.dialog.OverrideTemplateInThemeDialog;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import org.jetbrains.annotations.NotNull;

public class OverrideEmailTemplateInThemeAction extends OverrideFileInThemeAction {

    public static final String ACTION_NAME = "Override email template in a project theme";
    public static final String ACTION_DESCRIPTION = "Override email template file in project theme";
    public static final String EMAIL_DIRECTORY = "email";
    public static final String HTML = "html";

    public OverrideEmailTemplateInThemeAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
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

        if (virtualFile == null) {
            return false;
        }
        final String fileExtension = virtualFile.getExtension();

        if (fileExtension == null) {
            return false;
        }

        if (!HTML.equals(fileExtension)) {
            return false;
        }

        return isEmailTemplateFile(virtualFile);
    }

    private boolean isEmailTemplateFile(final @NotNull VirtualFile virtualFile) {
        final VirtualFile directory = virtualFile.getParent();

        if (directory == null) {
            return false;
        }

        if (EMAIL_DIRECTORY.equals(directory.getName())) {
            return Areas.getAreaByString(directory.getParent().getName()) != null;
        } else {
            return isEmailTemplateFile(directory);
        }
    }
}
