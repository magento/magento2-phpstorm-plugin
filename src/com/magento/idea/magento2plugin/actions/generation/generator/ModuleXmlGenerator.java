/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.ModuleXmlData;
import com.magento.idea.magento2plugin.actions.generation.generator.data.ModuleDirectoriesData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.magento.files.ModuleXml;
import com.magento.idea.magento2plugin.magento.packages.Package;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Properties;

public class ModuleXmlGenerator extends FileGenerator {

    private final ModuleXmlData moduleXmlData;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;

    /**
     * Constructor.
     *
     * @param moduleXmlData ModuleXmlData
     * @param project Project
     */
    public ModuleXmlGenerator(
            final @NotNull ModuleXmlData moduleXmlData,
            final Project project
    ) {
        super(project);
        this.moduleXmlData = moduleXmlData;
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.directoryGenerator = DirectoryGenerator.getInstance();
    }

    @Override
    public PsiFile generate(final String actionName) {
        if (moduleXmlData.isCreateModuleDirs()) {
            final ModuleDirectoriesData moduleDirectoriesData = directoryGenerator
                    .createOrFindModuleDirectories(
                            moduleXmlData.getPackageName(),
                            moduleXmlData.getModuleName(),
                            moduleXmlData.getBaseDir()
                    );
            return fileFromTemplateGenerator.generate(
                    ModuleXml.getInstance(),
                    getAttributes(),
                    moduleDirectoriesData.getModuleEtcDirectory(),
                    actionName
            );
        }
        final PsiDirectory etcDirectory = directoryGenerator.findOrCreateSubdirectory(
                moduleXmlData.getBaseDir(),
                Package.moduleBaseAreaDir
        );
        return fileFromTemplateGenerator.generate(
                ModuleXml.getInstance(),
                getAttributes(),
                etcDirectory,
                actionName
        );
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        attributes.setProperty("PACKAGE", moduleXmlData.getPackageName());
        attributes.setProperty("MODULE_NAME", moduleXmlData.getModuleName());
        if (moduleXmlData.getSetupVersion() != null) {
            attributes.setProperty("SETUP_VERSION", moduleXmlData.getSetupVersion());
        }
        attributes.setProperty("SEQUENCES", this.getDependenciesString(moduleXmlData.getModuleDependencies()));
    }

    private String getDependenciesString(List dependenciesList) {
        String result = "";
        Object[] dependencies = dependenciesList.toArray();
        boolean noDependency = dependencies.length == 1 && dependencies[0].equals(ModuleXml.NO_DEPENDENCY_LABEL);

        if (noDependency) {
            return result;
        }

        for (int i = 0; i < dependencies.length; i++) {
            String dependency = dependencies[i].toString();
            result = result.concat("<module name=\"" + dependency + "\"/>");
        }

        return result;
    }
}
