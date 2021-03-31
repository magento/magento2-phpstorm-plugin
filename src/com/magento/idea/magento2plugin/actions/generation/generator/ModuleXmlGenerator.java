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
import java.util.List;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

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
        this.fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
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

        final String sequences = this.getSequencesString(moduleXmlData.getModuleSequences());
        if (!sequences.isEmpty()) {
            attributes.setProperty(
                    "SEQUENCES",
                    sequences
            );
        }
    }

    private String getSequencesString(final List sequences) {
        String result = "";
        final Object[] dependencies = sequences.toArray();
        final boolean noDependency = dependencies.length == 1 && dependencies[0]
                .equals(ModuleXml.NO_SEQUENCES_LABEL);

        if (noDependency) {
            return result;
        }

        for (final Object o : dependencies) {
            result = result.concat(String.format("<module name=\"%s\"/>", o.toString()));
        }

        return result;
    }
}
