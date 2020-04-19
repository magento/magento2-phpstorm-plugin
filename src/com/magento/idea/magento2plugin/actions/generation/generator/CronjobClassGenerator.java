/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.CronjobClassData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.CronjobTemplate;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
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
        String errorTitle = "Error";
//        String cronjobFqn = this.getCronjobFqn();
//        PhpClass cronjobClass = GetPhpClassByFQN.getInstance(project).execute(cronjobFqn);
//
//        // todo: move it to validator
//        if (cronjobClass != null) {
//            String errorMessage = validatorBundle.message("validator.file.alreadyExists", "Block Class");
//            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);
//
//            return null;
//        }

        PhpFile cronjobFile = createCronjobClass(actionName);

        // todo: throw an exception and remove validation
        if (cronjobFile == null) {
            String errorMessage = validatorBundle.message("validator.file.cantBeCreated", "Cronjob Class");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return null;
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
     * @param String actionName
     *
     * @return PhpFile
     */
    private PhpFile createCronjobClass(String actionName) {
        String cronjobClassName = this.cronjobClassData.getClassName();
        String moduleName = this.cronjobClassData.getModuleName();
        String[] cronjobSubDirectories = this.cronjobClassData.getDirectory().split(File.separator);
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
