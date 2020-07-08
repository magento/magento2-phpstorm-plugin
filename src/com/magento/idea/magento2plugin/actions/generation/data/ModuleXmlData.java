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
    private final List<String> moduleDependencies;
    private boolean createModuleDirs;

    public ModuleXmlData(
        String packageName,
        String moduleName,
        PsiDirectory baseDir,
        List<String> moduleDependencies,
        boolean createModuleDirs
    ) {
        this.packageName = packageName;
        this.moduleName = moduleName;
        this.baseDir = baseDir;
        this.moduleDependencies = moduleDependencies;
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

    public List<String> getModuleDependencies() {
        return moduleDependencies;
    }

    public boolean getCreateModuleDirs() {
        return this.createModuleDirs;
    }
}
