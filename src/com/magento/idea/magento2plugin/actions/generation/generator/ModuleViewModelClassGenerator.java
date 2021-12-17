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
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.ViewModelPhp;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

public class ModuleViewModelClassGenerator extends FileGenerator {
    private final ViewModelFileData viewModelFileData;
    private final Project project;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;

    /**
     * Constructor.
     *
     * @param viewModelFileData ViewModelFileData
     * @param project Project
     */
    public ModuleViewModelClassGenerator(
            final @NotNull ViewModelFileData viewModelFileData,
            final Project project
    ) {
        super(project);

        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        this.viewModelFileData = viewModelFileData;
        this.project = project;
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    /**
     * Generate a View Model File.
     *
     * @param actionName String
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final String actionName) {
        final PhpClass block = GetPhpClassByFQN.getInstance(project).execute(getViewModelFqn());
        final String errorTitle = commonBundle.message("common.error");

        if (block != null) {
            final String errorMessage = validatorBundle.message(
                    "validator.file.alreadyExists",
                    "View Model"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return null;
        }

        final PhpFile viewModelFile = createViewModelClass(actionName);
        if (viewModelFile == null) {
            final String errorMessage = validatorBundle.message(
                    "validator.file.cantBeCreated",
                    "View Model"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return null;
        }

        return viewModelFile;
    }

    @NotNull
    private String getViewModelFqn() {
        return viewModelFileData.getNamespace()
                + Package.fqnSeparator
                + viewModelFileData.getViewModelClassName();
    }

    private PhpFile createViewModelClass(final String actionName) {
        PsiDirectory parentDirectory = new ModuleIndex(project)
                .getModuleDirectoryByModuleName(getViewModelModule());

        if (parentDirectory == null) {
            return null;
        }
        final String[] viewModelDirectories = viewModelFileData.getViewModelDirectory()
                .split(File.separator);
        for (final String viewModelDirectory: viewModelDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(
                    parentDirectory,
                    viewModelDirectory
            );
        }

        final Properties attributes = getAttributes();
        final PsiFile viewModelFile = fileFromTemplateGenerator.generate(
                ViewModelPhp.getInstance(viewModelFileData.getViewModelClassName()),
                attributes,
                parentDirectory,
                actionName
        );
        if (viewModelFile == null) {
            return null;
        }
        return (PhpFile) viewModelFile;
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        final String viewModelClassName = viewModelFileData.getViewModelClassName();
        attributes.setProperty("NAME", viewModelClassName);
        attributes.setProperty("NAMESPACE", viewModelFileData.getNamespace());
        attributes.setProperty("USE", ViewModelPhp.INTERFACE_FQN);
        attributes.setProperty("IMPLEMENTS", ViewModelPhp.INTERFACE_NAME);
    }

    public String getViewModelModule() {
        return viewModelFileData.getViewModelModule();
    }
}
