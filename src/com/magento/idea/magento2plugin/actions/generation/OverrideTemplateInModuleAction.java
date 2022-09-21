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
import com.magento.idea.magento2plugin.actions.generation.dialog.OverrideTemplateInModuleDialog;
import com.magento.idea.magento2plugin.magento.packages.OverridableFileType;
import org.jetbrains.annotations.NotNull;

public class OverrideTemplateInModuleAction extends OverrideFileAction {

    public static final String ACTION_NAME = "Override this file in a module";
    public static final String ACTION_TEMPLATE_DESCRIPTION =
            "Override template file in a module";

    public OverrideTemplateInModuleAction() {
        super(ACTION_NAME, ACTION_TEMPLATE_DESCRIPTION, MagentoIcons.MODULE);
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent event) {
        final Project project = event.getProject();

        if (project == null || psiFile == null) {
            return;
        }

        OverrideTemplateInModuleDialog.open(project, psiFile);
    }

    @Override
    protected boolean isOverrideAllowed(
            final @NotNull PsiFile file,
            final @NotNull Project project
    ) {
        final VirtualFile virtualFile = file.getVirtualFile();

        if (virtualFile == null
                || virtualFile.isDirectory()
                || virtualFile.getCanonicalPath() == null
                || !this.isFileInModule(file, project)
                || virtualFile.getExtension() == null
        ) {
            return false;
        }

        return OverridableFileType.isFilePhtml(virtualFile.getExtension());
    }
}
