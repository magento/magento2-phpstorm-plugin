/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.dialog.OverrideInThemeDialog;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.GetComponentNameByDirectoryUtil;
import com.magento.idea.magento2plugin.util.magento.GetComponentTypeByNameUtil;
import org.jetbrains.annotations.NotNull;

public class OverrideInThemeAction extends DumbAwareAction {
    public static String actionName = "Override this template in a project theme";
    public static String actionDescription = "Override in project theme";
    private PsiFile psiFile;

    public OverrideInThemeAction() {
        super(actionName, actionDescription, MagentoIcons.MODULE);
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

        boolean isAllowed = false;

        final String componentType = GetComponentTypeByNameUtil.execute(
                GetComponentNameByDirectoryUtil.execute(psiFile.getContainingDirectory(), project)
        );

        if (componentType.equals(ComponentType.module.toString())) {
            isAllowed = file.getPath().contains(Package.moduleViewDir);
        } else if (componentType.equals(ComponentType.theme.toString())) {
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
        OverrideInThemeDialog.open(event.getProject(), this.psiFile);
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }
}
