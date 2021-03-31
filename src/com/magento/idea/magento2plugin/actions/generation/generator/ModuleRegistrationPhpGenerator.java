/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.ModuleRegistrationPhpData;
import com.magento.idea.magento2plugin.actions.generation.generator.data.ModuleDirectoriesData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.magento.files.RegistrationPhp;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class ModuleRegistrationPhpGenerator extends FileGenerator {

    private final ModuleRegistrationPhpData moduleRegistrationPhpData;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;

    /**
     * Construct generator.
     *
     * @param moduleRegistrationPhpData ModuleRegistrationPhpData
     * @param project Project
     */
    public ModuleRegistrationPhpGenerator(
            final @NotNull ModuleRegistrationPhpData moduleRegistrationPhpData,
            final Project project
    ) {
        super(project);
        this.moduleRegistrationPhpData = moduleRegistrationPhpData;
        this.fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        this.directoryGenerator = DirectoryGenerator.getInstance();
    }

    /**
     * Generate file.
     *
     * @param actionName String
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final String actionName) {
        if (moduleRegistrationPhpData.getCreateModuleDirs()) {
            final ModuleDirectoriesData moduleDirectoriesData = directoryGenerator
                    .createOrFindModuleDirectories(
                            moduleRegistrationPhpData.getPackageName(),
                            moduleRegistrationPhpData.getModuleName(),
                            moduleRegistrationPhpData.getBaseDir()
                    );
            return fileFromTemplateGenerator.generate(
                    RegistrationPhp.getInstance(),
                    getAttributes(),
                    moduleDirectoriesData.getModuleDirectory(),
                    actionName
            );
        }
        return fileFromTemplateGenerator.generate(
                RegistrationPhp.getInstance(),
                getAttributes(),
                moduleRegistrationPhpData.getBaseDir(),
                actionName
        );
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        attributes.setProperty("PACKAGE", moduleRegistrationPhpData.getPackageName());
        attributes.setProperty("MODULE_NAME", moduleRegistrationPhpData.getModuleName());
    }
}
