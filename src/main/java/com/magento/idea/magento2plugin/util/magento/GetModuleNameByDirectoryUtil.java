/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GetModuleNameByDirectoryUtil {

    private GetModuleNameByDirectoryUtil() {}

    /**
     * Provides module name if directory belongs to one.
     *
     * @param psiDirectory PsiDirectory
     * @param project Project
     *
     * @return String
     */
    public static @Nullable String execute(
            final @Nullable PsiDirectory psiDirectory,
            final @NotNull Project project
    ) {
        // Gracefully handle null directory inputs from callers
        if (psiDirectory == null) {
            return null;
        }
        final GetMagentoModuleUtil.MagentoModuleData moduleData = GetMagentoModuleUtil
                .getByContext(psiDirectory, project);

        return moduleData == null ? null : moduleData.getName();
    }
}
