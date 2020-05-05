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
import com.magento.idea.magento2plugin.actions.generation.data.ViewModelFileData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.ViewModelPhp;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import com.magento.idea.magento2plugin.magento.packages.File;
import java.util.Properties;

public class ModuleViewModelClassGenerator extends FileGenerator {
    private ViewModelFileData viewModelFileData;
    private Project project;
    private ValidatorBundle validatorBundle;
    private CommonBundle commonBundle;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;

    public ModuleViewModelClassGenerator(@NotNull ViewModelFileData viewModelFileData, Project project) {
        super(project);
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.viewModelFileData = viewModelFileData;
        this.project = project;
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    public PsiFile generate(String actionName) {
        PhpClass block = GetPhpClassByFQN.getInstance(project).execute(getViewModelFqn());
        String errorTitle = commonBundle.message("common.error");

        if (block != null) {
            String errorMessage = validatorBundle.message("validator.file.alreadyExists", "View Model");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return null;
        }

        PhpFile viewModelFile = createViewModelClass(actionName);
        if (viewModelFile == null) {
            String errorMessage = validatorBundle.message("validator.file.cantBeCreated", "View Model");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return null;
        }

        return viewModelFile;
    }

    @NotNull
    private String getViewModelFqn() {
        return viewModelFileData.getNamespace() + Package.fqnSeparator + viewModelFileData.getViewModelClassName();
    }

    private PhpFile createViewModelClass(String actionName) {
        PsiDirectory parentDirectory = ModuleIndex.getInstance(project)
                .getModuleDirectoryByModuleName(getViewModelModule());
        String[] viewModelDirectories = viewModelFileData.getViewModelDirectory().split(File.separator);
        for (String viewModelDirectory: viewModelDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(parentDirectory, viewModelDirectory);
        }

        Properties attributes = getAttributes();
        PsiFile viewModelFile = fileFromTemplateGenerator.generate(ViewModelPhp.getInstance(viewModelFileData.getViewModelClassName()), attributes, parentDirectory, actionName);
        if (viewModelFile == null) {
            return null;
        }
        return (PhpFile) viewModelFile;
    }

    protected void fillAttributes(Properties attributes) {
        String viewModelClassName = viewModelFileData.getViewModelClassName();
        attributes.setProperty("NAME", viewModelClassName);
        attributes.setProperty("NAMESPACE", viewModelFileData.getNamespace());
        attributes.setProperty("USE", ViewModelPhp.INTERFACE_FQN);
        attributes.setProperty("IMPLEMENTS", ViewModelPhp.INTERFACE_NAME);
    }

    public String getViewModelModule() {
        return viewModelFileData.getViewModelModule();
    }
}
