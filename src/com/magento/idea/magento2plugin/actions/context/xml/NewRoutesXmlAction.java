/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.context.xml;

import com.intellij.ide.fileTemplates.actions.AttributesDefaults;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.context.AbstractContextAction;
import com.magento.idea.magento2plugin.magento.files.RoutesXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class NewRoutesXmlAction extends AbstractContextAction {

    public static final String ACTION_NAME = "Magento 2 Routes File";
    public static final String ACTION_DESCRIPTION = "Create a new Magento 2 routes.xml file";

    /**
     * New routes.xml file generation action constructor.
     */
    public NewRoutesXmlAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, new RoutesXml());
    }

    @Override
    protected boolean isVisible(
            final @NotNull GetMagentoModuleUtil.MagentoModuleData moduleData,
            final @NotNull PsiDirectory targetDirectory,
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
                Areas.frontend.toString()
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
        defaults.addPredefined("HAS_ROUTE_DATA", String.valueOf(true));
        defaults.addPredefined("MODULE_NAME", moduleData.getName());

        if (Areas.adminhtml.toString().equals(targetDirectory.getName())) {
            defaults.addPredefined("ROUTER_ID", RoutesXml.ROUTER_ID_ADMIN);
        } else if (Package.moduleBaseAreaDir.equals(targetDirectory.getName())
                || Areas.frontend.toString().equals(targetDirectory.getName())) {
            defaults.addPredefined("ROUTER_ID", RoutesXml.ROUTER_ID_STANDARD);
        }

        return defaults;
    }
}
