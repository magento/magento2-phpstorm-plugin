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
import org.jetbrains.annotations.NotNull;
import java.util.Properties;

public class ModuleRegistrationPhpGenerator extends FileGenerator {

    private final ModuleRegistrationPhpData moduleRegistrationPhpData;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;

    public ModuleRegistrationPhpGenerator(@NotNull ModuleRegistrationPhpData moduleRegistrationPhpData, Project project) {
        super(project);
        this.moduleRegistrationPhpData = moduleRegistrationPhpData;
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.directoryGenerator = DirectoryGenerator.getInstance();
    }

    public PsiFile generate(String actionName) {
        if (moduleRegistrationPhpData.getCreateModuleDirs()) {
            ModuleDirectoriesData moduleDirectoriesData = directoryGenerator.createOrFindModuleDirectories(moduleRegistrationPhpData.getPackageName(), moduleRegistrationPhpData.getModuleName(), moduleRegistrationPhpData.getBaseDir());
            return fileFromTemplateGenerator.generate(RegistrationPhp.getInstance(), getAttributes(), moduleDirectoriesData.getModuleDirectory(), actionName);
        } else {
            return fileFromTemplateGenerator.generate(RegistrationPhp.getInstance(), getAttributes(), moduleRegistrationPhpData.getBaseDir(), actionName);
        }
    }

    protected void fillAttributes(Properties attributes) {
        attributes.setProperty("PACKAGE", moduleRegistrationPhpData.getPackageName());
        attributes.setProperty("MODULE_NAME", moduleRegistrationPhpData.getModuleName());
    }
}
