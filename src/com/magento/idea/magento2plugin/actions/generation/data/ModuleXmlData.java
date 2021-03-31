/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import com.intellij.psi.PsiDirectory;
import java.util.List;

@SuppressWarnings({"PMD.DataClass"})
public class ModuleXmlData {
    private final String packageName;
    private final String moduleName;
    private final String setupVersion;
    private final PsiDirectory baseDir;
    private final List<String> moduleSequences;
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
            final List<String> moduleSequences,
            final boolean createModuleDirs
    ) {
        this.packageName = packageName;
        this.moduleName = moduleName;
        this.setupVersion = setupVersion;
        this.baseDir = baseDir;
        this.moduleSequences = moduleSequences;
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

    public List<String> getModuleSequences() {
        return moduleSequences;
    }

    public boolean isCreateModuleDirs() {
        return this.createModuleDirs;
    }
}
