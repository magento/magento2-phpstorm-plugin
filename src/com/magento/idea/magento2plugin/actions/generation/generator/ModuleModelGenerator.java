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
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.ModelFile;
import com.magento.idea.magento2plugin.magento.files.ResourceModelFile;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

public class ModuleModelGenerator extends FileGenerator {
    private final ModelData data;
    private final Project project;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final GetFirstClassOfFile getFirstClassOfFile;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final ModelFile file;

    /**
     * Generates new Model PHP Class based on provided data.
     *
     * @param data ModelData
     * @param project Project
     */
    public ModuleModelGenerator(
            final @NotNull ModelData data,
            final @NotNull Project project
    ) {
        super(project);
        this.project = project;
        this.data = data;
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
        file = new ModelFile(data.getModelName());
    }

    /**
     * Generates model class.
     *
     * @param actionName Action name
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final @NotNull String actionName) {
        final PsiFile[] modelFiles = new PsiFile[1];

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PhpClass model = GetPhpClassByFQN.getInstance(project).execute(
                    file.getNamespaceBuilder(data.getModuleName()).getClassFqn()
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
     * Create model class.
     *
     * @param actionName String
     *
     * @return PhpClass
     */
    private PhpClass createModelClass(final @NotNull String actionName) {
        PsiDirectory parentDirectory = ModuleIndex.getInstance(project)
                .getModuleDirectoryByModuleName(data.getModuleName());
        final PsiFile modelFile;

        parentDirectory = directoryGenerator.findOrCreateSubdirectory(
                parentDirectory, ModelFile.MODEL_DIRECTORY
        );

        final Properties attributes = getAttributes();
        modelFile = fileFromTemplateGenerator.generate(
                file,
                attributes,
                parentDirectory,
                actionName
        );

        if (modelFile == null) {
            return null;
        }

        return getFirstClassOfFile.execute((PhpFile) modelFile);
    }

    /**
     * Fill model file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        final PhpClassTypesBuilder phpClassTypesBuilder = new PhpClassTypesBuilder();
        final ResourceModelFile resourceModelFile = new ResourceModelFile(data.getResourceName());

        phpClassTypesBuilder
                .appendProperty("NAME", data.getModelName())
                .appendProperty(
                        "NAMESPACE",
                        file.getNamespaceBuilder(data.getModuleName()).getNamespace()
                )
                .appendProperty("DB_NAME", data.getDbTableName())
                .append(
                        "RESOURCE_MODEL",
                        resourceModelFile.getNamespaceBuilder(data.getModuleName()).getClassFqn(),
                        ResourceModelFile.ALIAS
                )
                .appendProperty(
                        "EXTENDS",
                        PhpClassGeneratorUtil.getNameFromFqn(ModelFile.ABSTRACT_MODEL)
                )
                .append("ABSTRACT_MODEL", ModelFile.ABSTRACT_MODEL)
                .mergeProperties(attributes);

        attributes.setProperty("USES",
                PhpClassGeneratorUtil.formatUses(phpClassTypesBuilder.getUses()));
    }
}
