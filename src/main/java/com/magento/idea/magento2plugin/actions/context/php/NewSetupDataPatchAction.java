/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.context.php;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.context.CustomGeneratorContextAction;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewSetupDataPatchDialog;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import org.jetbrains.annotations.NotNull;

public class NewSetupDataPatchAction extends CustomGeneratorContextAction {

    public static final String ACTION_NAME = "Magento 2 Setup Data Patch";
    public static final String ACTION_DESCRIPTION = "Create a new Magento 2 Setup Data Patch";
    public static final String SETUP_DIRECTORY = "Setup";
    public static final String PATCH_DIRECTORY = "Patch";
    public static final String DATA_DIRECTORY = "Data";

    public NewSetupDataPatchAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION);
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent event) {
        final GetMagentoModuleUtil.MagentoModuleData moduleData = getModuleData();

        if (event.getProject() == null || moduleData == null || getDirectory() == null) {
            return;
        }
        final String[] module = moduleData.getName().split(Package.vendorModuleNameSeparator);

        if (module.length != 2) { //NOPMD
            return;
        }
        final PsiDirectory rootDirectory = moduleData.getModuleDir().findSubdirectory(
                SETUP_DIRECTORY
        );

        if (rootDirectory == null) {
            return;
        }
        NewSetupDataPatchDialog.open(event.getProject(), rootDirectory, module[0], module[1]);
    }

    @Override
    protected boolean isVisible(
            final @NotNull GetMagentoModuleUtil.MagentoModuleData moduleData,
            final PsiDirectory targetDirectory,
            final PsiFile targetFile
    ) {
        if (!moduleData.getType().equals(ComponentType.module)) {
            return false;
        }

        if (SETUP_DIRECTORY.equals(targetDirectory.getName())) {
            return moduleData.getModuleDir().equals(targetDirectory.getParentDirectory());
        }

        if (PATCH_DIRECTORY.equals(targetDirectory.getName())) {
            final PsiDirectory setupDirCandidate = targetDirectory.getParentDirectory();

            return setupDirCandidate != null
                    && SETUP_DIRECTORY.equals(setupDirCandidate.getName())
                    && moduleData.getModuleDir().equals(setupDirCandidate.getParentDirectory());
        }

        if (DATA_DIRECTORY.equals(targetDirectory.getName())) {
            final PsiDirectory patchDirCandidate = targetDirectory.getParentDirectory();

            if (patchDirCandidate == null) {
                return false;
            }
            final PsiDirectory setupDirCandidate = patchDirCandidate.getParentDirectory();

            return setupDirCandidate != null
                    && PATCH_DIRECTORY.equals(patchDirCandidate.getName())
                    && SETUP_DIRECTORY.equals(setupDirCandidate.getName())
                    && moduleData.getModuleDir().equals(setupDirCandidate.getParentDirectory());
        }

        return false;
    }
}
