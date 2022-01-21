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
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class CronjobClassGenerator extends FileGenerator {
    private final CronjobClassData cronjobClassData;
    private final Project project;
    private final ValidatorBundle validatorBundle;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;

    /**
     * Construct generator.
     *
     * @param project Project
     * @param cronjobClassData CronjobClassData
     */
    public CronjobClassGenerator(
            final Project project,
            final @NotNull CronjobClassData cronjobClassData
    ) {
        super(project);
        this.project = project;
        this.cronjobClassData = cronjobClassData;

        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        this.validatorBundle = new ValidatorBundle();
    }

    /**
     * Generate file from code template.
     *
     * @param actionName String
     *
     * @return void
     */
    @Override
    public PsiFile generate(final String actionName) {
        final PhpFile cronjobFile = createCronjobClass(actionName);

        if (cronjobFile == null) {
            final String errorMessage = validatorBundle.message(
                    "validator.file.cantBeCreated",
                    "Cronjob Class"
            );

            throw new RuntimeException(errorMessage);//NOPMD
        }

        return cronjobFile;
    }

    /**
     * Fill template attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final Properties attributes) {
        final String cronjobClassName = this.cronjobClassData.getClassName();
        final String cronjobNamespace = this.cronjobClassData.getNamespace();

        attributes.setProperty("NAME", cronjobClassName);
        attributes.setProperty("NAMESPACE", cronjobNamespace);
    }

    /**
     * Generate Cronjob Class according to data model.
     *
     * @param actionName String
     *
     * @return PhpFile
     */
    private PhpFile createCronjobClass(final String actionName) {
        final String moduleName = this.cronjobClassData.getModuleName();
        PsiDirectory parentDirectory = new ModuleIndex(project)
                .getModuleDirectoryByModuleName(moduleName);

        if (parentDirectory == null) {
            return null;
        }
        final String cronjobClassName = this.cronjobClassData.getClassName();
        final String[] cronjobSubDirectories = this.cronjobClassData.getDirectory().split("/");

        for (final String cronjobSubDirectory: cronjobSubDirectories) {
            parentDirectory = directoryGenerator
                    .findOrCreateSubdirectory(parentDirectory, cronjobSubDirectory);
        }

        final Properties attributes = getAttributes();

        final PsiFile blockFile = fileFromTemplateGenerator.generate(
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
