/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import com.intellij.psi.PsiDirectory;

@SuppressWarnings({"PMD.DataClass"})
public class ModuleXmlData {
    private final String packageName;
    private final String moduleName;
    private final String setupVersion;
    private final PsiDirectory baseDir;
    private final boolean createModuleDirs;

    /**
     * Constructor ModuleXmlData.
     *
     * @param packageName Package name
     * @param moduleName Module name
     * @param setupVersion Setup version
     * @param baseDir Base directory
     * @param createModuleDirs Create module Dirs
     */
    public ModuleXmlData(
            final String packageName,
            final String moduleName,
            final String setupVersion,
            final PsiDirectory baseDir,
            final boolean createModuleDirs
    ) {
        this.packageName = packageName;
        this.moduleName = moduleName;
        this.setupVersion = setupVersion;
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

    public String getSetupVersion() {
        return this.setupVersion;
    }

    public boolean isCreateModuleDirs() {
        return this.createModuleDirs;
    }
}
