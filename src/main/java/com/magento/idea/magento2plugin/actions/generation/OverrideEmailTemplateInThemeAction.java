/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.dialog.OverrideTemplateInThemeDialog;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
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
        final PsiFile psiFile = getTargetFile(event);

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

        if (virtualFile == null || !HTML.equals(virtualFile.getExtension())) {
            return false;
        }
        final GetMagentoModuleUtil.MagentoModuleData moduleData =
                GetMagentoModuleUtil.getByContext(file.getContainingDirectory(), project);

        if (moduleData == null || moduleData.getType() != ComponentType.module
                || moduleData.getViewDir() == null) {
            return false;
        }
        final String relativePath = VfsUtilCore.getRelativePath(
                virtualFile, moduleData.getViewDir().getVirtualFile(), '/'
        );

        if (relativePath == null) {
            return false;
        }
        final String[] path = relativePath.split("/");

        return path.length >= 3 && EMAIL_DIRECTORY.equals(path[1])
                && (Areas.frontend.toString().equals(path[0])
                || Areas.adminhtml.toString().equals(path[0])
                || Areas.base.toString().equals(path[0]));
    }
}
