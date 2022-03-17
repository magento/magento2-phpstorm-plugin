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
import com.magento.idea.magento2plugin.actions.generation.dialog.OverrideLayoutInThemeDialog;
import com.magento.idea.magento2plugin.magento.files.LayoutXml;
import com.magento.idea.magento2plugin.magento.files.UiComponentGridXmlFile;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import org.jetbrains.annotations.NotNull;

public class OverrideLayoutInThemeAction extends AnAction {

    public static final String ACTION_NAME = "Override this layout in a project theme";
    public static final String ACTION_DESCRIPTION = "Override layout in project theme";
    private PsiFile psiFile;

    public OverrideLayoutInThemeAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
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

        if (!UiComponentGridXmlFile.FILE_EXTENSION
                .equals(psiFile.getVirtualFile().getExtension())) {
            return false;
        }

        if (!LayoutXml.PARENT_DIR.equals(psiFile.getContainingDirectory().getName())
                && !LayoutXml.PAGE_LAYOUT_DIR.equals(psiFile.getContainingDirectory().getName())) {
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
        OverrideLayoutInThemeDialog.open(event.getProject(), this.psiFile);
    }
}
