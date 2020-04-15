/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.generation.php;

import com.magento.idea.magento2plugin.project.Settings;
import java.util.List;

public class MagentoProjectGeneratorSettings {
    private final Settings.State myMagentoState;
    private final String packageName;
    private final String moduleName;
    private final String moduleDescription;
    private final String composerPackageName;
    private final String moduleVersion;
    private final List<String> moduleLicenses;

    public MagentoProjectGeneratorSettings(
            Settings.State state,
            String packageName,
            String moduleName,
            String moduleDescription,
            String composerPackageName,
            String moduleVersion,
            List<String> moduleLicenses
    ) {
        this.myMagentoState = state;
        this.packageName = packageName;
        this.moduleName = moduleName;
        this.moduleDescription = moduleDescription;
        this.composerPackageName = composerPackageName;
        this.moduleVersion = moduleVersion;
        this.moduleLicenses = moduleLicenses;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getModuleDescription() {
        return moduleDescription;
    }

    public String getComposerPackageName() {
        return composerPackageName;
    }

    public String getModuleVersion() {
        return moduleVersion;
    }

    public List<String> getModuleLicenses() {
        return moduleLicenses;
    }

    public Settings.State getMagentoState() {
        return this.myMagentoState;
    }
}
