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
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.DataModel;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;

import javax.swing.JOptionPane;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class DataModelGenerator extends FileGenerator {
    private final Project project;
    private final DataModelData modelData;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final GetFirstClassOfFile getFirstClassOfFile;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;

    public DataModelGenerator(Project project, DataModelData modelData) {
        super(project);

        this.project = project;
        this.modelData = modelData;
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    @Override
    public PsiFile generate(String actionName) {
        final PsiFile[] files = new PsiFile[1];

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PhpClass model = GetPhpClassByFQN.getInstance(project).execute(
                    modelData.getFQN()
            );

            if (model != null) {
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
            } else {
                model = createModel(actionName);

                if (model == null) {
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
                    files[0] = model.getContainingFile();
                }
            }
        });

        return files[0];
    }

    @Override
    protected void fillAttributes(Properties attributes) {
        final List<String> uses = getUses();
        attributes.setProperty("NAMESPACE", modelData.getNamespace());
        attributes.setProperty("USES", PhpClassGeneratorUtil.formatUses(uses));
        attributes.setProperty("NAME", modelData.getName());
        attributes.setProperty(
                "EXTENDS",
                PhpClassGeneratorUtil.getNameFromFqn(DataModel.DATA_OBJECT)
        );
        attributes.setProperty(
                "IMPLEMENTS",
                PhpClassGeneratorUtil.getNameFromFqn(modelData.getInterfaceFQN())
        );
    }

    private List<String> getUses() {
        return Arrays.asList(
                DataModel.DATA_OBJECT,
                modelData.getInterfaceFQN()
        );
    }

    private PhpClass createModel(String actionName) {
        PsiDirectory parentDirectory = ModuleIndex.getInstance(project)
                .getModuleDirectoryByModuleName(modelData.getModuleName());
        final PsiFile interfaceFile;
        final Properties attributes = getAttributes();

        for (String directory: DataModel.DIRECTORY.split("/")) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(
                    parentDirectory, directory
            );
        }

        interfaceFile = fileFromTemplateGenerator.generate(
                new DataModel(modelData.getName()),
                attributes,
                parentDirectory,
                actionName
        );

        return interfaceFile == null ? null : getFirstClassOfFile.execute((PhpFile) interfaceFile);
    }
}
