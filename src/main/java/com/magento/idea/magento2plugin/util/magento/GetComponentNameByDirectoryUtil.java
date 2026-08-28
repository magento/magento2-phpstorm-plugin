/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;

public final class GetComponentNameByDirectoryUtil {

    private GetComponentNameByDirectoryUtil() {}

    /**
     * Returns component name.
     *
     * @param psiDirectory PsiDirectory
     * @param project Project
     * @return String
     */
    public static String execute(final PsiDirectory psiDirectory, final Project project) {
        final GetMagentoModuleUtil.MagentoModuleData moduleData = GetMagentoModuleUtil
                .getByContext(psiDirectory, project);

        return moduleData == null ? null : moduleData.getName();
    }
}
