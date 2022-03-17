/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.CopyMagentoPath;
import com.magento.idea.magento2plugin.actions.generation.dialog.OverrideTemplateInThemeDialog;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import org.jetbrains.annotations.NotNull;

public class OverrideTemplateInThemeAction extends AnAction {

    public static final String ACTION_NAME = "Override this in a project theme";
    public static final String ACTION_TEMPLATE_DESCRIPTION = "Override template in project theme";
    public static final String ACTION_STYLES_DESCRIPTION = "Override styles in project theme";
    public static final String LESS_FILE_EXTENSION = "less";
    private PsiFile psiFile;

    public OverrideTemplateInThemeAction() {
        super(ACTION_NAME, ACTION_TEMPLATE_DESCRIPTION, MagentoIcons.MODULE);
    }

    /**
     * Action entry point.
     *
     * @param event AnActionEvent
     */
    @Override
    public void update(final @NotNull AnActionEvent event) {
        boolean status = false;
        final Project project = event.getData(PlatformDataKeys.PROJECT);
        psiFile = event.getData(PlatformDataKeys.PSI_FILE);

        if (Settings.isEnabled(project)) {
            try {
                status = isOverrideAllowed(
                        psiFile.getVirtualFile(),
                        project
                    );
            } catch (NullPointerException e) { //NOPMD
                // Ignore
            }
        }

        this.setStatus(event, status);
    }

    private boolean isOverrideAllowed(final VirtualFile file, final Project project) {
        if (file.isDirectory()) {
            return false;
        }
        final String fileExtension = psiFile.getVirtualFile().getExtension();

        if (!CopyMagentoPath.PHTML_EXTENSION.equals(fileExtension)
                && !LESS_FILE_EXTENSION.equals(fileExtension)) {
            return false;
        }
        boolean isAllowed = false;
        final GetMagentoModuleUtil.MagentoModuleData moduleData =
                GetMagentoModuleUtil.getByContext(psiFile.getContainingDirectory(), project);

        if (moduleData.getType().equals(ComponentType.module)) {
            isAllowed = file.getPath().contains(Package.moduleViewDir);
        } else if (moduleData.getType().equals(ComponentType.theme)) {
            isAllowed = true;
        }

        return isAllowed;
    }

    private void setStatus(final AnActionEvent event, final boolean status) {
        event.getPresentation().setVisible(status);
        event.getPresentation().setEnabled(status);
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent event) {
        OverrideTemplateInThemeDialog.open(event.getProject(), this.psiFile);
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }
}
