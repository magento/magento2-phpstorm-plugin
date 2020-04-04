/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.data;

import com.intellij.psi.PsiDirectory;

import java.util.List;

public class ModuleXmlData {
    private final String packageName;
    private final String moduleName;
    private PsiDirectory baseDir;

    public ModuleXmlData(
        String packageName,
        String moduleName,
        PsiDirectory baseDir
    ) {
        this.packageName = packageName;
        this.moduleName = moduleName;
        this.baseDir = baseDir;
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
}
