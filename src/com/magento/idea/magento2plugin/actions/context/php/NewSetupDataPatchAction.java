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
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class NewSetupDataPatchAction extends CustomGeneratorContextAction {

    public static final String ACTION_NAME = "Magento 2 Setup Data Patch";
    public static final String ACTION_DESCRIPTION = "Create a new Magento 2 Setup Data Patch";
    public static final String ROOT_DIRECTORY = "Setup";
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
        final PsiDirectory rooDirectory = moduleData.getModuleDir().findSubdirectory(
                ROOT_DIRECTORY
        );

        NewSetupDataPatchDialog.open(event.getProject(), rooDirectory, module[0], module[1]);
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

        if (ROOT_DIRECTORY.equals(targetDirectory.getName())) {
            return Objects.requireNonNull(targetDirectory.getParentDirectory()).getName().equals(
                    moduleData.getModuleDir().getName()
            );
        }

        if (PATCH_DIRECTORY.equals(targetDirectory.getName())) {
            return ROOT_DIRECTORY.equals(Objects.requireNonNull(
                    targetDirectory.getParentDirectory()).getName()
            );
        }

        if (DATA_DIRECTORY.equals(targetDirectory.getName())) {
            final PsiDirectory parentDirectory = Objects.requireNonNull(
                    targetDirectory.getParentDirectory()
            );

            if (PATCH_DIRECTORY.equals(parentDirectory.getName())) {
                return ROOT_DIRECTORY.equals(Objects.requireNonNull(
                        parentDirectory.getParentDirectory()).getName()
                );
            }
        }

        return false;
    }
}
