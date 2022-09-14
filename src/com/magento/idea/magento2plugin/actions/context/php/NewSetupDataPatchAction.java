/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.context.php;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.context.CustomGeneratorContextAction;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewSetupDataPatch.NewSetupDataPatchDialog;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
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
        final String[] templateData = moduleData.getName().split(Package.vendorModuleNameSeparator);

        if (templateData.length != 2) { //NOPMD
            return;
        }

        NewSetupDataPatchDialog.open(
                event.getProject(),
                getDirectory(),
                templateData[0],
                templateData[1]
        );
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
        final PsiDirectory parentDirFirst = targetDirectory.getParentDirectory();
        final PsiDirectory parentDirSecond = parentDirFirst != null ?
                parentDirFirst.getParentDirectory() : null;

        return targetDirectory.getName().equals(ROOT_DIRECTORY) ||
                (parentDirFirst != null && parentDirFirst.getName().equals(ROOT_DIRECTORY)) ||
                (parentDirSecond != null && parentDirSecond.getName().equals(ROOT_DIRECTORY));
    }
}
