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
import com.magento.idea.magento2plugin.actions.generation.data.ModelData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.ModelPhp;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.swing.JOptionPane;

public class ModuleModelGenerator extends FileGenerator {
    private final ModelData modelData;
    private final Project project;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final GetFirstClassOfFile getFirstClassOfFile;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;

    /**
     * Generates new Model PHP Class based on provided data.
     *
     * @param modelData ModelData
     * @param project Project
     */
    public ModuleModelGenerator(
            final ModelData modelData,
            final Project project
    ) {
        super(project);
        this.project = project;
        this.modelData = modelData;
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    /**
     * Generates model class.
     *
     * @param actionName Action name
     * @return PsiFile
     */
    public PsiFile generate(final String actionName) {
        final PsiFile[] modelFiles = new PsiFile[1];

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PhpClass model = GetPhpClassByFQN.getInstance(project).execute(
                    getModelFqn()
            );

            if (model != null) {
                final String errorMessage = this.validatorBundle.message(
                        "validator.file.alreadyExists",
                        "Model Class"
                );
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        commonBundle.message("common.error"),
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            model = createModelClass(actionName);

            if (model == null) {
                final String errorMessage = this.validatorBundle.message(
                        "validator.file.cantBeCreated",
                        "Model Class"
                );
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        commonBundle.message("common.error"),
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            modelFiles[0] = model.getContainingFile();
        });

        return modelFiles[0];
    }

    /**
     * Get module.
     *
     * @return String
     */
    public String getModuleName() {
        return modelData.getModuleName();
    }

    private String getModelFqn() {
        return modelData.getFqn();
    }

    private PhpClass createModelClass(final String actionName) {
        PsiDirectory parentDirectory = ModuleIndex.getInstance(project)
                .getModuleDirectoryByModuleName(getModuleName());
        final PsiFile modelFile;

        parentDirectory = directoryGenerator.findOrCreateSubdirectory(
                parentDirectory, ModelPhp.MODEL_DIRECTORY
        );

        final Properties attributes = getAttributes();
        modelFile = fileFromTemplateGenerator.generate(
                new ModelPhp(modelData.getModelName()),
                attributes,
                parentDirectory,
                actionName
        );

        if (modelFile == null) {
            return null;
        }

        return getFirstClassOfFile.execute((PhpFile) modelFile);
    }

    protected void fillAttributes(final Properties attributes) {
        attributes.setProperty("NAME", modelData.getModelName());
        attributes.setProperty("NAMESPACE", modelData.getNamespace());

        attributes.setProperty("DB_NAME", modelData.getDbTableName());
        attributes.setProperty("RESOURCE_MODEL", modelData.getResourceName());
        final List<String> uses = getUses();

        attributes.setProperty(
                "EXTENDS",
                PhpClassGeneratorUtil.getNameFromFqn(ModelPhp.ABSTRACT_MODEL)
        );

        attributes.setProperty("USES", PhpClassGeneratorUtil.formatUses(uses));
    }

    private List<String> getUses() {
        return new ArrayList<>(Arrays.asList(
                ModelPhp.ABSTRACT_MODEL,
                modelData.getResourceModelFqn()
        ));
    }
}
