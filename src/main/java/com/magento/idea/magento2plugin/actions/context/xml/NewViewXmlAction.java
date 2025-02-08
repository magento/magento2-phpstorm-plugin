/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.context.xml;

import com.intellij.ide.fileTemplates.actions.AttributesDefaults;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.context.AbstractContextAction;
import com.magento.idea.magento2plugin.magento.files.ModuleViewXmlFile;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import org.jetbrains.annotations.NotNull;

public class NewViewXmlAction extends AbstractContextAction {

    public static final String ACTION_NAME = "Magento 2 View File";
    public static final String ACTION_DESCRIPTION = "Create a new Magento 2 view.xml file";

    /**
     * New view.xml file generation action constructor.
     */
    public NewViewXmlAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, new ModuleViewXmlFile());
    }

    @Override
    protected boolean isVisible(
            final @NotNull GetMagentoModuleUtil.MagentoModuleData moduleData,
            final @NotNull PsiDirectory targetDirectory,
            final PsiFile targetFile
    ) {
        final PsiDirectory configDir = moduleData.getConfigDir();

        if (configDir == null) {
            return false;
        }

        return targetDirectory.getName().equals(Package.moduleBaseAreaDir)
                && targetDirectory.equals(configDir)
                && moduleData.getType().equals(ComponentType.module);
    }


    @Override
    protected AttributesDefaults getProperties(
            final @NotNull AttributesDefaults defaults,
            final @NotNull GetMagentoModuleUtil.MagentoModuleData moduleData,
            final PsiDirectory targetDirectory,
            final PsiFile targetFile
    ) {
        return defaults;
    }
}
