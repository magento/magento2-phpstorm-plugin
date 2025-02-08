/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.context.xml;

import com.intellij.ide.fileTemplates.actions.AttributesDefaults;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.context.AbstractContextAction;
import com.magento.idea.magento2plugin.magento.files.ModuleWidgetXmlFile;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import org.jetbrains.annotations.NotNull;

public class NewWidgetXmlAction extends AbstractContextAction {

    public static final String ACTION_NAME = "Magento 2 Widget File";
    public static final String ACTION_DESCRIPTION = "Create a new Magento 2 widget.xml file";

    /**
     * New widget.xml file generation action constructor.
     */
    public NewWidgetXmlAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, new ModuleWidgetXmlFile());
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
