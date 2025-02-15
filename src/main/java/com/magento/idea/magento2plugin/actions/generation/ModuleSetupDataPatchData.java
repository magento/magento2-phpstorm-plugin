/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;

public class ModuleSetupDataPatchData {

    private final String packageName;
    private final String moduleName;
    private final PsiDirectory baseDir;
    private final String className;

    /**
     * Constructor.
     *
     * @param packageName String
     * @param moduleName String
     * @param baseDir PsiDirectory
     */
    public ModuleSetupDataPatchData(
            final @NotNull String packageName,
            final @NotNull String moduleName,
            final @NotNull PsiDirectory baseDir,
            final @NotNull String className
    ) {
        this.packageName = packageName;
        this.moduleName = moduleName;
        this.baseDir = baseDir;
        this.className = className;
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

    public @NotNull String getClassName() {
        return className;
    }
}
