/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import com.intellij.psi.PsiDirectory;

public class ModuleReadmeMdData {
    private final String packageName;
    private final String moduleName;
    private final PsiDirectory baseDir;
    private final boolean createModuleDirs;

    /**
     * Constructor.
     *
     * @param packageName String
     * @param moduleName String
     * @param baseDir PsiDirectory
     * @param createModuleDirs boolean
     */
    public ModuleReadmeMdData(
            final String packageName,
            final String moduleName,
            final PsiDirectory baseDir,
            final boolean createModuleDirs
    ) {
        this.packageName = packageName;
        this.moduleName = moduleName;
        this.baseDir = baseDir;
        this.createModuleDirs = createModuleDirs;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public PsiDirectory getBaseDir() {
        return this.baseDir;
    }

    public boolean hasCreateModuleDirs() {
        return this.createModuleDirs;
    }
}
