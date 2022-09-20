/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.dialog.OverrideLayoutInThemeDialog;
import com.magento.idea.magento2plugin.magento.files.LayoutXml;
import org.jetbrains.annotations.NotNull;

public class OverrideLayoutInThemeAction extends OverrideFileAction {

    public static final String ACTION_NAME = "Override this layout in a project theme";
    public static final String ACTION_DESCRIPTION = "Override layout in project theme";

    public OverrideLayoutInThemeAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent event) {
        final Project project = event.getProject();

        if (project == null || psiFile == null) {
            return;
        }
        OverrideLayoutInThemeDialog.open(project, psiFile);
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

        if (!(file instanceof XmlFile)) {
            return false;
        }

        if (!LayoutXml.PARENT_DIR.equals(file.getContainingDirectory().getName())
                && !LayoutXml.PAGE_LAYOUT_DIR.equals(file.getContainingDirectory().getName())) {
            return false;
        }

        return isFileInModuleOrTheme(file, project);
    }
}
