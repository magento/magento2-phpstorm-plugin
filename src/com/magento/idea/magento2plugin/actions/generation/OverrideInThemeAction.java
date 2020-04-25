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
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.ComponentType;
import com.magento.idea.magento2plugin.util.magento.GetComponentNameByDirectory;
import com.magento.idea.magento2plugin.util.magento.GetComponentTypeByName;
import org.jetbrains.annotations.NotNull;

public class OverrideInThemeAction extends DumbAwareAction {
    public static String ACTION_NAME = "Override in theme...";
    public static String ACTION_DESCRIPTION = "Override template in project theme.";
    private PsiFile psiFile;

    public OverrideInThemeAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
    }

    public void update(@NotNull AnActionEvent event) {
        boolean status = false;
        Project project = event.getData(PlatformDataKeys.PROJECT);
        psiFile = event.getData(PlatformDataKeys.PSI_FILE);

        if (Settings.isEnabled(project)) {
            try {
                status = isOverrideAllowed(
                        psiFile.getVirtualFile(),
                        project
                    );
            } catch (NullPointerException e) {
                // Ignore
            }
        }

        this.setStatus(event, status);
    }

    private boolean isOverrideAllowed(VirtualFile file, Project project) throws NullPointerException {
        if (file.isDirectory()) {
            return false;
        }

        boolean isAllowed = false;

        GetComponentNameByDirectory getComponentNameByDirectory = GetComponentNameByDirectory.getInstance(project);
        String componentType = GetComponentTypeByName.execute(getComponentNameByDirectory
                .execute(psiFile.getContainingDirectory()));

        if (componentType.equals(ComponentType.TYPE_MODULE)) {
            isAllowed = file.getPath().contains("view");
        } else if (componentType.equals(ComponentType.TYPE_THEME)) {
            isAllowed = true;
        }

        return isAllowed;
    }

    private void setStatus(AnActionEvent event, boolean status) {
        event.getPresentation().setVisible(status);
        event.getPresentation().setEnabled(status);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        OverrideInThemeDialog.open(e.getProject(), this.psiFile);
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }
}
