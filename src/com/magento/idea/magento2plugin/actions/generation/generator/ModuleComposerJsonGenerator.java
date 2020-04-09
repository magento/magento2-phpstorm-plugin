/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.ModuleComposerJsonData;
import com.magento.idea.magento2plugin.actions.generation.generator.data.ModuleDirectoriesData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.ComposerJson;
import com.magento.idea.magento2plugin.util.CamelCaseToHyphen;
import org.jetbrains.annotations.NotNull;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Properties;
import java.util.List;

public class ModuleComposerJsonGenerator extends FileGenerator {

    private final ModuleComposerJsonData moduleComposerJsonData;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;
    private final CamelCaseToHyphen camelCaseToHyphen;
    private final Project project;
    private final ModuleIndex moduleIndex;

    public ModuleComposerJsonGenerator(@NotNull ModuleComposerJsonData moduleComposerJsonData, Project project) {
        super(project);
        this.moduleComposerJsonData = moduleComposerJsonData;
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.camelCaseToHyphen = CamelCaseToHyphen.getInstance();
        this.project = project;
        this.moduleIndex = ModuleIndex.getInstance(project);
    }

    public PsiFile generate(String actionName) {
        if (moduleComposerJsonData.getCreateModuleDirs()) {
            ModuleDirectoriesData moduleDirectoriesData = directoryGenerator.createOrFindModuleDirectories(moduleComposerJsonData.getPackageName(), moduleComposerJsonData.getModuleName(), moduleComposerJsonData.getBaseDir());
            return fileFromTemplateGenerator.generate(ComposerJson.getInstance(), getAttributes(), moduleDirectoriesData.getModuleDirectory(), actionName);
        } else {
            return fileFromTemplateGenerator.generate(ComposerJson.getInstance(), getAttributes(), moduleComposerJsonData.getBaseDir(), actionName);
        }
    }

    protected void fillAttributes(Properties attributes) {
        attributes.setProperty("PACKAGE", moduleComposerJsonData.getPackageName());
        attributes.setProperty("MODULE_NAME", moduleComposerJsonData.getModuleName());
        attributes.setProperty("MODULE_DESCRIPTION", moduleComposerJsonData.getModuleDescription());
        attributes.setProperty("COMPOSER_PACKAGE_NAME", moduleComposerJsonData.getComposerPackageName());
        attributes.setProperty("MODULE_VERSION", moduleComposerJsonData.getModuleVersion());
        attributes.setProperty("LICENSE", this.getLicensesString(moduleComposerJsonData.getModuleLicense()));
        attributes.setProperty("DEPENDENCIES", this.getDependenciesString(moduleComposerJsonData.getModuleDependencies()));
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

    private String getDependenciesString(List dependenciesList) {
        String result = "";
        Object[] dependencies = dependenciesList.toArray();
        result = result.concat(ComposerJson.DEFAULT_DEPENDENCY);
        if (dependencies.length == 0) {
            result = result.concat("\n");
        } else {
            result = result.concat(",\n");
        }

        for (int i = 0; i < dependencies.length; i++) {
            String dependency = dependencies[i].toString();
            result = result.concat("\"");
            result = result.concat(
                    camelCaseToHyphen.convert(dependency).replace("_-", "/")
            );
            result = result.concat("\"");
            result = result.concat(": \"" + getDependencyVersion(dependency) + "\"");

            if (dependencies.length != (i + 1)) {
                result = result.concat(",");
            }

            result = result.concat("\n");
        }

        return result;
    }

    private String getDependencyVersion(String dependency) {
        String version = "*";
        try {
            VirtualFile virtualFile = moduleIndex.getModuleDirectoryByModuleName(dependency)
                    .findFile(ComposerJson.FILE_NAME)
                    .getVirtualFile();
            if (virtualFile.exists()) {
                JsonElement jsonElement = new JsonParser().parse(new FileReader(virtualFile.getPath()));
                JsonElement versionJsonElement = jsonElement.getAsJsonObject().get("version");
                if (versionJsonElement != null) {
                    version = versionJsonElement.getAsString();
                    int minorVersionSeparator = version.lastIndexOf(".");
                    version = new StringBuilder(version)
                            .replace(minorVersionSeparator + 1, version.length(),"*")
                            .toString();
                }
            }
        } catch (FileNotFoundException e) {
            // It's fine
        }
        return version;
    }
}
