/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.DataModelInterfaceData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.DataModelInterfaceFile;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

public class DataModelInterfaceGenerator extends FileGenerator {

    private final Project project;
    private final DataModelInterfaceData data;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final GetFirstClassOfFile getFirstClassOfFile;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final DataModelInterfaceFile file;

    /**
     * DTO interface generator constructor.
     *
     * @param data DataModelInterfaceData
     * @param project Project
     */
    public DataModelInterfaceGenerator(
            final @NotNull DataModelInterfaceData data,
            final @NotNull Project project
    ) {
        super(project);
        this.project = project;
        this.data = data;
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
        file = new DataModelInterfaceFile(data.getName());
    }

    @Override
    public PsiFile generate(final String actionName) {
        final PsiFile[] files = new PsiFile[1];

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PhpClass modelInterface = GetPhpClassByFQN.getInstance(project).execute(
                    file.getNamespaceBuilder(data.getModuleName()).getClassFqn()
            );

            if (modelInterface == null) {
                modelInterface = createInterface(actionName);

                if (modelInterface == null) {
                    final String errorMessage = this.validatorBundle.message(
                            "validator.file.cantBeCreated",
                            "Data Model Interface"
                    );
                    JOptionPane.showMessageDialog(
                            null,
                            errorMessage,
                            commonBundle.message("common.error"),
                            JOptionPane.ERROR_MESSAGE
                    );
                } else {
                    files[0] = modelInterface.getContainingFile();
                }
            } else {
                final String errorMessage = this.validatorBundle.message(
                        "validator.file.alreadyExists",
                        "Data Model Interface"
                );
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        commonBundle.message("common.error"),
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        return files[0];
    }

    /**
     * Create DTO interface file.
     *
     * @param actionName String
     *
     * @return PhpClass
     */
    private PhpClass createInterface(final @NotNull String actionName) {
        final PsiDirectory parentDirectory = new ModuleIndex(project)
                .getModuleDirectoryByModuleName(data.getModuleName());
        final PsiDirectory dtoInterfaceDirectory = directoryGenerator
                .findOrCreateSubdirectories(parentDirectory, DataModelInterfaceFile.DIRECTORY);

        final PsiFile dtoInterfaceFile = fileFromTemplateGenerator.generate(
                file,
                getAttributes(),
                dtoInterfaceDirectory,
                actionName
        );

        return dtoInterfaceFile == null ? null : getFirstClassOfFile.execute(
                (PhpFile) dtoInterfaceFile
        );
    }

    /**
     * Fill DTO interface file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        attributes.setProperty("NAME", data.getName());
        attributes.setProperty("NAMESPACE",
                file.getNamespaceBuilder(data.getModuleName()).getNamespace());
        attributes.setProperty("PROPERTIES", data.getProperties());
    }
}
