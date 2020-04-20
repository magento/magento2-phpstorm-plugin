/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.magento.idea.magento2plugin.actions.generation.data.CronjobClassData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.CronjobTemplate;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.util.Properties;

public class CronjobClassGenerator extends FileGenerator {
    private CronjobClassData cronjobClassData;
    private Project project;
    private ValidatorBundle validatorBundle;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;

    /**
     * @param project
     * @param cronjobClassData
     */
    public CronjobClassGenerator(Project project, @NotNull CronjobClassData cronjobClassData) {
        super(project);
        this.project = project;
        this.cronjobClassData = cronjobClassData;

        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.validatorBundle = new ValidatorBundle();
    }

    /**
     *
     * @param actionName
     *
     * @return void
     */
    public PsiFile generate(String actionName) {
        PhpFile cronjobFile = createCronjobClass(actionName);

        if (cronjobFile == null) {
            String errorMessage = validatorBundle.message(
            "validator.file.cantBeCreated",
            "Cronjob Class"
            );

            throw new RuntimeException(errorMessage);
        }

        return cronjobFile;
    }

    /**
     *
     * @param attributes
     */
    protected void fillAttributes(Properties attributes) {
        String cronjobClassName = this.cronjobClassData.getClassName();
        String cronjobNamespace = this.cronjobClassData.getNamespace();

        attributes.setProperty("NAME", cronjobClassName);
        attributes.setProperty("NAMESPACE", cronjobNamespace);
    }

    /**
     * Generate Cronjob Class according to data model
     *
     * @param actionName
     *
     * @return PhpFile
     */
    private PhpFile createCronjobClass(String actionName) {
        String cronjobClassName = this.cronjobClassData.getClassName();
        String moduleName = this.cronjobClassData.getModuleName();
        String[] cronjobSubDirectories = this.cronjobClassData.getDirectory().split("/");
        PsiDirectory parentDirectory = ModuleIndex.getInstance(project).getModuleDirectoryByModuleName(moduleName);

        for (String cronjobSubDirectory: cronjobSubDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(parentDirectory, cronjobSubDirectory);
        }

        Properties attributes = getAttributes();

        PsiFile blockFile = fileFromTemplateGenerator.generate(
            new CronjobTemplate(cronjobClassName),
            attributes,
            parentDirectory,
            actionName
        );

        if (blockFile == null) {
            return null;
        }

        return (PhpFile) blockFile;
    }
}
