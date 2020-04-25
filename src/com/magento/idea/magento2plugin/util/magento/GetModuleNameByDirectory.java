/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.util.RegExUtil;

public class GetModuleNameByDirectory {
    private static GetModuleNameByDirectory INSTANCE = null;
    private Project project;

    public static GetModuleNameByDirectory getInstance(Project project) {
        if (null == INSTANCE) {
            INSTANCE = new GetModuleNameByDirectory();
        }
        INSTANCE.project = project;
        return INSTANCE;
    }

    public String execute(PsiDirectory psiDirectory) {
        GetComponentNameByDirectory getComponentNameByDirectory =
                GetComponentNameByDirectory.getInstance(project);
        String componentName = getComponentNameByDirectory.execute(psiDirectory);

        return componentName.matches(RegExUtil.Magento.MODULE_NAME) ? componentName : null;
    }
}
