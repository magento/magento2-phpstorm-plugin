/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.data;

import com.intellij.psi.PsiDirectory;

public class ModuleRegistrationPhpData {
    private final String packageName;
    private final String moduleName;
    private PsiDirectory baseDir;
    private boolean createModuleDirs;

    public ModuleRegistrationPhpData(
        String packageName,
        String moduleName,
        PsiDirectory baseDir,
        boolean createModuleDirs
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

    public boolean getCreateModuleDirs() {
        return this.createModuleDirs;
    }
}
