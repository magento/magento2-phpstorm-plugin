/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.context.md;

import com.intellij.ide.fileTemplates.actions.AttributesDefaults;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.context.AbstractContextAction;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.ModuleReadmeMdFile;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import org.jetbrains.annotations.NotNull;

public class NewReadmeMdAction extends AbstractContextAction {

    public static final String ACTION_NAME = "Magento 2 README File";
    public static final String ACTION_DESCRIPTION = "Create a new Magento 2 README file";

    public NewReadmeMdAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, new ModuleReadmeMdFile());
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
        final PsiDirectory moduleDirectory = new ModuleIndex(targetDirectory.getProject())
                .getModuleDirectoryByModuleName(moduleData.getName());
        final String magentoModuleName = moduleData
                .getName()
                .split(Package.vendorModuleNameSeparator)[1];

        return targetDirectory.getName().equals(magentoModuleName)
                && targetDirectory.equals(moduleDirectory);
    }

    @Override
    protected AttributesDefaults getProperties(
            final @NotNull AttributesDefaults defaults,
            final @NotNull GetMagentoModuleUtil.MagentoModuleData moduleData,
            final PsiDirectory targetDirectory,
            final PsiFile targetFile
    ) {
        final String[] templateData = moduleData.getName().split(Package.vendorModuleNameSeparator);
        defaults.addPredefined("PACKAGE", templateData[0]);
        defaults.addPredefined("MODULE_NAME", templateData[1]);

        return defaults;
    }
}
