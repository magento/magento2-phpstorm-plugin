/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;

public class ModuleReadmeMdData {

    private final String packageName;
    private final String moduleName;
    private final PsiDirectory baseDir;

    /**
     * Constructor.
     *
     * @param packageName String
     * @param moduleName String
     * @param baseDir PsiDirectory
     */
    public ModuleReadmeMdData(
            final @NotNull String packageName,
            final @NotNull String moduleName,
            final @NotNull PsiDirectory baseDir
    ) {
        this.packageName = packageName;
        this.moduleName = moduleName;
        this.baseDir = baseDir;
    }

    public @NotNull String getPackageName() {
        return packageName;
    }

    public @NotNull String getModuleName() {
        return moduleName;
    }

    public @NotNull PsiDirectory getBaseDir() {
        return baseDir;
    }
}
