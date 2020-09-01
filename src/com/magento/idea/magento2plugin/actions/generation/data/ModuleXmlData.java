/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import com.intellij.psi.PsiDirectory;

public class ModuleXmlData {
    private final String packageName;
    private final String moduleName;
    private final String moduleVersion;
    private PsiDirectory baseDir;
    private boolean createModuleDirs;

    /**
     * Constructor ModuleXmlData.
     *
     * @param packageName Package name
     * @param moduleName Module name
     * @param moduleVersion Module version
     * @param baseDir Base directory
     * @param createModuleDirs Create module Dirs
     */
    public ModuleXmlData(
            String packageName,
            String moduleName,
            String moduleVersion,
            PsiDirectory baseDir,
            boolean createModuleDirs
    ) {
        this.packageName = packageName;
        this.moduleName = moduleName;
        this.moduleVersion = moduleVersion;
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

    public String getModuleVersion() {
        return this.moduleVersion;
    }

    public boolean getCreateModuleDirs() {
        return this.createModuleDirs;
    }
}
