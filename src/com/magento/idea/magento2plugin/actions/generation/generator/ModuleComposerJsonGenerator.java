/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.ModuleComposerJsonData;
import com.magento.idea.magento2plugin.actions.generation.generator.data.ModuleDirectoriesData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.magento.files.ComposerJson;
import org.jetbrains.annotations.NotNull;
import java.util.Properties;
import java.util.List;

public class ModuleComposerJsonGenerator extends FileGenerator {

    private final ModuleComposerJsonData moduleComposerJsonData;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;

    public ModuleComposerJsonGenerator(@NotNull ModuleComposerJsonData moduleComposerJsonData, Project project) {
        super(project);
        this.moduleComposerJsonData = moduleComposerJsonData;
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.directoryGenerator = DirectoryGenerator.getInstance();
    }

    public PsiFile generate(String actionName) {
        ModuleDirectoriesData moduleDirectoriesData = directoryGenerator.createOrFindModuleDirectories(moduleComposerJsonData.getPackageName(), moduleComposerJsonData.getModuleName(), moduleComposerJsonData.getBaseDir());
        return fileFromTemplateGenerator.generate(ComposerJson.getInstance(), getAttributes(), moduleDirectoriesData.getModuleDirectory(), actionName);
    }

    protected void fillAttributes(Properties attributes) {
        attributes.setProperty("PACKAGE", moduleComposerJsonData.getPackageName());
        attributes.setProperty("MODULE_NAME", moduleComposerJsonData.getModuleName());
        attributes.setProperty("MODULE_DESCRIPTION", moduleComposerJsonData.getModuleDescription());
        attributes.setProperty("COMPOSER_PACKAGE_NAME", moduleComposerJsonData.getComposerPackageName());
        attributes.setProperty("MODULE_VERSION", moduleComposerJsonData.getModuleVersion());
        attributes.setProperty("LICENSE", this.getLicensesString(moduleComposerJsonData.getModuleLicense()));
    }

    protected String getLicensesString(List licensesList) {
        String license = "[\n";
        Object[] licenses = licensesList.toArray();

        for (int i = 0; i < licenses.length; i++) {
            license = license.concat("\"");
            license = license.concat(licenses[i].toString());
            license = license.concat("\"");

            if (licenses.length != (i + 1)) license = license.concat(",");

            license = license.concat("\n");
        }

        license = license.concat("\n]");

        return license;
    }
}
