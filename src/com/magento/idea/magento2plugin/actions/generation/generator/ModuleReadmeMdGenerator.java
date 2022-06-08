/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.ModuleReadmeMdData;
import com.magento.idea.magento2plugin.actions.generation.generator.data.ModuleDirectoriesData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.magento.files.ModuleReadmeMd;
import com.magento.idea.magento2plugin.magento.files.RegistrationPhp;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class ModuleReadmeMdGenerator extends FileGenerator {
    private final ModuleReadmeMdData moduleReadmeMdData;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;

    /**
     * Construct generator.
     *
     * @param moduleReadmeMdData ModuleReadmeFileData
     * @param project Project
     */
    public ModuleReadmeMdGenerator(
            final @NotNull ModuleReadmeMdData moduleReadmeMdData,
            final Project project
    ) {
        super(project);
        this.moduleReadmeMdData = moduleReadmeMdData;
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
        if (moduleReadmeMdData.hasCreateModuleDirs()) {
            final ModuleDirectoriesData moduleDirectoriesData = directoryGenerator
                    .createOrFindModuleDirectories(
                            moduleReadmeMdData.getPackageName(),
                            moduleReadmeMdData.getModuleName(),
                            moduleReadmeMdData.getBaseDir()
                    );
            return fileFromTemplateGenerator.generate(
                    ModuleReadmeMd.getInstance(),
                    getAttributes(),
                    moduleDirectoriesData.getModuleDirectory(),
                    actionName
            );
        }
        return fileFromTemplateGenerator.generate(
                RegistrationPhp.getInstance(),
                getAttributes(),
                moduleReadmeMdData.getBaseDir(),
                actionName
        );
    }

    /**
     * Fill template properties.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final Properties attributes) {
        attributes.setProperty("PACKAGE", moduleReadmeMdData.getPackageName());
        attributes.setProperty("MODULE_NAME", moduleReadmeMdData.getModuleName());
    }
}
