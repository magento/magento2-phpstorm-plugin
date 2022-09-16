/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.context.xml;

import com.intellij.ide.fileTemplates.actions.AttributesDefaults;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.context.AbstractContextAction;
import com.magento.idea.magento2plugin.magento.files.ModuleEventsXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class NewEventsXmlAction extends AbstractContextAction {

    public static final String ACTION_NAME = "Magento 2 Events File";
    public static final String ACTION_DESCRIPTION = "Create a new Magento 2 events.xml file";

    /**
     * New events.xml file generation action constructor.
     */
    public NewEventsXmlAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, new ModuleEventsXml());
    }

    @Override
    protected boolean isVisible(
            final @NotNull GetMagentoModuleUtil.MagentoModuleData moduleData,
            final PsiDirectory targetDirectory,
            final PsiFile targetFile
    ) {
        final PsiDirectory configDir = moduleData.getConfigDir();
        final PsiDirectory globalScopeDir = getGlobalScopeDir(targetDirectory);

        if (configDir == null || globalScopeDir == null) {
            return false;
        }

        final List<String> allowedDirectories = Arrays.asList(
                Package.moduleBaseAreaDir,
                Areas.adminhtml.toString(),
                Areas.crontab.toString(),
                Areas.frontend.toString(),
                Areas.graphql.toString(),
                Areas.webapi_rest.toString(),
                Areas.webapi_soap.toString()
        );

        return allowedDirectories.contains(targetDirectory.getName())
                && globalScopeDir.equals(configDir)
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
