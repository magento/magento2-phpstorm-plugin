/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.context.xml;

import com.intellij.ide.fileTemplates.actions.AttributesDefaults;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.context.AbstractContextAction;
import com.magento.idea.magento2plugin.magento.files.ModuleSystemXmlFile;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import org.jetbrains.annotations.NotNull;

public class NewSystemXmlAction extends AbstractContextAction {

    public static final String ACTION_NAME = "Magento 2 System File";
    public static final String ACTION_DESCRIPTION = "Create a new Magento 2 system.xml file";

    /**
     * New system.xml file generation action constructor.
     */
    public NewSystemXmlAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, new ModuleSystemXmlFile());
    }

    @Override
    protected boolean isVisible(
            final @NotNull GetMagentoModuleUtil.MagentoModuleData moduleData,
            final @NotNull PsiDirectory targetDirectory,
            final PsiFile targetFile
    ) {
        final PsiDirectory parentDir = targetDirectory.getParentDirectory();
        final PsiDirectory configDir = moduleData.getConfigDir();

        if (parentDir == null || configDir == null) {
            return false;
        }

        return targetDirectory.getName().equals(Areas.adminhtml.toString())
                && parentDir.equals(configDir)
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
