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
import com.magento.idea.magento2plugin.magento.packages.OverridableFileType;
import com.magento.idea.magento2plugin.magento.packages.Package;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

public class OverrideTemplateInThemeAction extends OverrideFileInThemeAction {

    public static final String ACTION_NAME = "Override this file in a project theme";
    public static final String ACTION_TEMPLATE_DESCRIPTION =
            "Override template file in project theme";
    public static final String ACTION_STYLES_DESCRIPTION = "Override styles file in project theme";
    public static final String ACTION_JS_DESCRIPTION = "Override javascript file in project theme";

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

        if (virtualFile == null
                || virtualFile.isDirectory()
                || virtualFile.getCanonicalPath() == null) {
            return false;
        }
        final String fileExtension = virtualFile.getExtension();

        if (fileExtension == null) {
            return false;
        }

        if (!OverridableFileType.getOverwritableFileExtensions().contains(fileExtension)) {
            return false;
        }

        if (OverridableFileType.isFileJS(fileExtension)
                && !virtualFile.getCanonicalPath().contains(Package.libWebRoot)
                && !Arrays.asList(virtualFile.getCanonicalPath()
                .split(Package.V_FILE_SEPARATOR)).contains("web")) {
            return false;
        }

        return OverridableFileType.isFileJS(fileExtension)
                || isFileInModuleOrTheme(file, project);
    }
}
