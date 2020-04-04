/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.data;

import com.intellij.psi.PsiDirectory;
import java.util.List;

public class ModuleComposerJsonData {
    private final String packageName;
    private final String moduleName;
    private PsiDirectory baseDir;
    private final String moduleDescription;
    private final String composerPackageName;
    private final String moduleVersion;
    private final List moduleLicense;

    public ModuleComposerJsonData(
        String packageName,
        String moduleName,
        PsiDirectory baseDir,
        String moduleDescription,
        String composerPackageName,
        String moduleVersion,
        List moduleLicense
    ) {
        this.packageName = packageName;
        this.moduleName = moduleName;
        this.baseDir = baseDir;
        this.moduleDescription = moduleDescription;
        this.composerPackageName = composerPackageName;
        this.moduleVersion = moduleVersion;
        this.moduleLicense = moduleLicense;
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

    public String getModuleDescription() {
        return this.moduleDescription;
    }

    public String getComposerPackageName() {
        return this.composerPackageName;
    }

    public String getModuleVersion() {
        return this.moduleVersion;
    }

    public List getModuleLicense() {
        return this.moduleLicense;
    }
}
