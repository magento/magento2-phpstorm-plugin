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
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

public abstract class OverrideFileAction extends AnAction {

    protected PsiFile psiFile;

    /**
     * Override file in theme action constructor.
     *
     * @param actionName String
     * @param actionDescription String
     * @param module Icon
     */
    public OverrideFileAction(
            final String actionName,
            final String actionDescription,
            final Icon module
    ) {
        super(actionName, actionDescription, module);
    }

    /**
     * Action entry point.
     *
     * @param event AnActionEvent
     */
    @Override
    public void update(final @NotNull AnActionEvent event) {
        setStatus(event, false);
        final Project project = event.getData(PlatformDataKeys.PROJECT);
        final PsiFile targetFile = event.getData(PlatformDataKeys.PSI_FILE);

        if (project == null || targetFile == null) {
            return;
        }

        if (Settings.isEnabled(project) && isOverrideAllowed(targetFile, project)) {
            setStatus(event, true);
            psiFile = targetFile;
        }
    }

    /**
     * Implement this method to specify if override allowed for particular file types.
     *
     * @param file PsiFile
     * @param project Project
     *
     * @return boolean
     */
    protected abstract boolean isOverrideAllowed(
            final @NotNull PsiFile file,
            final @NotNull Project project
    );

    /**
     * Check if file has a path specific to the Magento 2 module or theme.
     *
     * @param file PsiFile
     * @param project Project
     *
     * @return boolean
     */
    protected boolean isFileInModuleOrTheme(
            final @NotNull PsiFile file,
            final @NotNull Project project
    ) {
        final GetMagentoModuleUtil.MagentoModuleData moduleData =
                GetMagentoModuleUtil.getByContext(file.getContainingDirectory(), project);

        if (moduleData == null) {
            return false;
        }
        final VirtualFile virtualFile = file.getVirtualFile();

        if (moduleData.getType().equals(ComponentType.module)) {
            return virtualFile.getPath().contains("/" + Package.moduleViewDir + "/");
        } else {
            return moduleData.getType().equals(ComponentType.theme);
        }
    }

    /**
     * Checks if file has a path specific to the Magento 2 module.
     *
     * @param file PsiFile
     * @param project Project
     *
     * @return boolean
     */
    protected boolean isFileInModule(
            final @NotNull PsiFile file,
            final @NotNull Project project
    ) {
        final GetMagentoModuleUtil.MagentoModuleData moduleData =
                GetMagentoModuleUtil.getByContext(file.getContainingDirectory(), project);

        return moduleData != null && moduleData.getType().equals(ComponentType.module);
    }

    private void setStatus(final AnActionEvent event, final boolean status) {
        event.getPresentation().setVisible(status);
        event.getPresentation().setEnabled(status);
    }
}
