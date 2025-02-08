/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.json.psi.JsonFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.magento.idea.magento2plugin.actions.generation.data.ModuleComposerJsonData;
import com.magento.idea.magento2plugin.actions.generation.generator.data.ModuleDirectoriesData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.ComposerJson;
import java.util.List;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ModuleComposerJsonGenerator extends FileGenerator {

    private static final String ANY_VERSION = "*";
    private final ModuleComposerJsonData moduleComposerJsonData;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;
    private final ModuleIndex moduleIndex;

    /**
     * Constructor.
     *
     * @param moduleComposerJsonData ModuleComposerJsonData
     * @param project Project
     */
    public ModuleComposerJsonGenerator(
            final @NotNull ModuleComposerJsonData moduleComposerJsonData,
            final Project project
    ) {
        super(project);
        this.moduleComposerJsonData = moduleComposerJsonData;
        this.fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.moduleIndex = new ModuleIndex(project);
    }

    @Override
    public PsiFile generate(final String actionName) {
        if (moduleComposerJsonData.getCreateModuleDirs()) {
            final ModuleDirectoriesData moduleDirectoriesData =
                    directoryGenerator.createOrFindModuleDirectories(
                        moduleComposerJsonData.getPackageName(),
                        moduleComposerJsonData.getModuleName(),
                        moduleComposerJsonData.getBaseDir()
            );
            return fileFromTemplateGenerator.generate(
                    ComposerJson.getInstance(),
                    getAttributes(),
                    moduleDirectoriesData.getModuleDirectory(),
                    actionName
            );
        }
        return fileFromTemplateGenerator.generate(
                ComposerJson.getInstance(),
                getAttributes(),
                moduleComposerJsonData.getBaseDir(),
                actionName
        );
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        attributes.setProperty("PACKAGE", moduleComposerJsonData.getPackageName());
        attributes.setProperty("MODULE_NAME", moduleComposerJsonData.getModuleName());
        attributes.setProperty("MODULE_DESCRIPTION", moduleComposerJsonData.getModuleDescription());
        attributes.setProperty(
                "COMPOSER_PACKAGE_NAME",
                moduleComposerJsonData.getComposerPackageName()
        );
        attributes.setProperty("MODULE_VERSION", moduleComposerJsonData.getModuleVersion());
        attributes.setProperty(
                "LICENSE",
                this.getLicensesString(moduleComposerJsonData.getModuleLicense())
        );
        attributes.setProperty(
                "DEPENDENCIES",
                this.getDependenciesString(moduleComposerJsonData.getModuleDependencies())
        );
    }

    protected String getLicensesString(final List licensesList) {
        String license = "[\n";
        final Object[] licenses = licensesList.toArray();

        for (int i = 0; i < licenses.length; i++) {
            license = license.concat("\"");
            license = license.concat(licenses[i].toString());
            license = license.concat("\"");

            if (licenses.length != i + 1) {
                license = license.concat(",");
            }

            license = license.concat("\n");
        }

        license = license.concat("\n]");

        return license;
    }

    private String getDependenciesString(final List dependenciesList) {
        String result = "";
        final Object[] dependencies = dependenciesList.toArray();
        result = result.concat(ComposerJson.DEFAULT_DEPENDENCY);
        final boolean noDependency =
                dependencies.length == 1 && ComposerJson.NO_DEPENDENCY_LABEL.equals(
                        dependencies[0]
                );
        if (dependencies.length == 0 || noDependency) {
            result = result.concat("\n");
        } else {
            result = result.concat(",\n");
        }

        if (noDependency) {
            return result;
        }

        for (int i = 0; i < dependencies.length; i++) {
            final String dependency = dependencies[i].toString();
            final Pair<String, String> dependencyData = getDependencyData(dependency);
            if (!dependencyData.getFirst().isEmpty()) {
                result = result.concat("\"");
                result = result.concat(dependencyData.getFirst());
                result = result.concat("\"");
                result = result.concat(": \"" + dependencyData.getSecond() + "\"");
            }

            if (!dependencyData.getFirst().isEmpty() && dependencies.length != i + 1) {
                result = result.concat(",");
                result = result.concat("\n");
            }

        }

        return result;
    }

    private Pair<String, String> getDependencyData(
            final String dependency
    ) {
        String version = "";
        String moduleName = "";

        try {
            final PsiDirectory moduleDir = moduleIndex.getModuleDirectoryByModuleName(dependency);

            if (moduleDir == null) {
                return Pair.create("", "");
            }
            final PsiFile virtualFile = moduleDir.findFile(ComposerJson.FILE_NAME);

            if (virtualFile != null) { //NOPMD
                final VirtualFile composerJsonVirtualFile = virtualFile.getVirtualFile();

                if (composerJsonVirtualFile.exists()) {
                    final PsiFile composerJsonFile = PsiManager.getInstance(project)
                            .findFile(composerJsonVirtualFile);
                    if (!(composerJsonFile instanceof JsonFile)) {
                        return Pair.create("", "");
                    }
                    final JSONParser parser = new JSONParser();
                    final Object obj = parser.parse(
                            composerJsonFile.getText()
                    );
                    final JSONObject jsonObject = (JSONObject) obj;

                    if (jsonObject.get("name") == null) {
                        return Pair.create("", "");
                    }
                    moduleName = jsonObject.get("name").toString().trim();
                    version = jsonObject.get("version") == null
                            ? ANY_VERSION : jsonObject.get("version").toString();

                    if (!ANY_VERSION.equals(version)) {
                        final int minorVersionSeparator = version.lastIndexOf('.');
                        version = new StringBuilder(version)
                                .replace(minorVersionSeparator + 1, version.length(), ANY_VERSION)
                                .toString();
                    }
                }
            } else {
                return Pair.create("", "");
            }
        } catch (ParseException exception) { //NOPMD
            // It's fine
        }

        return Pair.create(moduleName, version);
    }
}
