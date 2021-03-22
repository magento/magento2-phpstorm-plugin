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
import com.magento.idea.magento2plugin.actions.generation.data.DataModelData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.DataModelFile;
import com.magento.idea.magento2plugin.magento.files.DataModelInterfaceFile;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

public class DataModelGenerator extends FileGenerator {

    private final Project project;
    private final DataModelData data;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final GetFirstClassOfFile getFirstClassOfFile;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final DataModelFile file;

    /**
     * Data model generator constructor.
     *
     * @param project Project
     * @param data DataModelData
     */
    public DataModelGenerator(
            final @NotNull Project project,
            final @NotNull DataModelData data
    ) {
        super(project);
        this.project = project;
        this.data = data;
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
        file = new DataModelFile(data.getName());
    }

    @Override
    public PsiFile generate(final @NotNull String actionName) {
        final PsiFile[] files = new PsiFile[1];

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PhpClass dataModel = GetPhpClassByFQN.getInstance(project).execute(
                    file.getNamespaceBuilder(data.getModuleName()).getClassFqn()
            );

            if (dataModel == null) {
                dataModel = createModel(actionName);

                if (dataModel == null) {
                    final String errorMessage = this.validatorBundle.message(
                            "validator.file.cantBeCreated",
                            "Data Model"
                    );
                    JOptionPane.showMessageDialog(
                            null,
                            errorMessage,
                            commonBundle.message("common.error"),
                            JOptionPane.ERROR_MESSAGE
                    );
                } else {
                    files[0] = dataModel.getContainingFile();
                }
            } else {
                final String errorMessage = this.validatorBundle.message(
                        "validator.file.alreadyExists",
                        "Data Model"
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
     * Create model class.
     *
     * @param actionName String
     *
     * @return PhpClass
     */
    private PhpClass createModel(final @NotNull String actionName) {
        final PsiDirectory parentDirectory = ModuleIndex.getInstance(project)
                .getModuleDirectoryByModuleName(data.getModuleName());
        final PsiDirectory dataModelDirectory = directoryGenerator.findOrCreateSubdirectories(
                parentDirectory,
                DataModelFile.DIRECTORY
        );

        final PsiFile dataModelFile = fileFromTemplateGenerator.generate(
                file,
                getAttributes(),
                dataModelDirectory,
                actionName
        );

        return dataModelFile == null ? null : getFirstClassOfFile.execute((PhpFile) dataModelFile);
    }

    /**
     * Fill data model file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        final PhpClassTypesBuilder phpClassTypesBuilder = new PhpClassTypesBuilder();

        phpClassTypesBuilder
                .appendProperty(
                        "NAMESPACE",
                        file.getNamespaceBuilder(data.getModuleName()).getNamespace()
                )
                .appendProperty("NAME", data.getName())
                .appendProperty("PROPERTIES", data.getProperties())
                .appendProperty("HASINTERFACE", Boolean.toString(data.hasInterface()))
                .append("EXTENDS", DataModelFile.DATA_OBJECT);

        if (data.hasInterface()) {
            phpClassTypesBuilder.append(
                    "IMPLEMENTS",
                    new DataModelInterfaceFile(
                            data.getInterfaceName()
                    ).getNamespaceBuilder(data.getModuleName()).getClassFqn());
        }

        phpClassTypesBuilder.mergeProperties(attributes);

        attributes.setProperty(
                "USES",
                PhpClassGeneratorUtil.formatUses(phpClassTypesBuilder.getUses())
        );
    }
}
