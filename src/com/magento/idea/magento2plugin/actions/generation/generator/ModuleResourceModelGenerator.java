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
import com.magento.idea.magento2plugin.actions.generation.data.ResourceModelData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.ResourceModelFile;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.List;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

public class ModuleResourceModelGenerator extends FileGenerator {

    private final ResourceModelData resourceModelData;
    private final Project project;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final GetFirstClassOfFile getFirstClassOfFile;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final ResourceModelFile file;

    /**
     * Generates new Resource Model PHP Class based on provided data.
     *
     * @param resourceModelData ResourceModelData
     * @param project Project
     */
    public ModuleResourceModelGenerator(
            final ResourceModelData resourceModelData,
            final Project project
    ) {
        super(project);
        this.project = project;
        this.resourceModelData = resourceModelData;
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
        file = new ResourceModelFile(resourceModelData.getResourceModelName());

    }

    /**
     * Generates resource model class.
     *
     * @param actionName Action name
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final @NotNull String actionName) {
        final PsiFile[] resourceModelFiles = new PsiFile[1];

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PhpClass resourceModel = GetPhpClassByFQN.getInstance(project).execute(
                    file.getNamespaceBuilder(resourceModelData.getModuleName()).getClassFqn()
            );

            if (resourceModel != null) {
                final String errorMessage = this.validatorBundle.message(
                        "validator.file.alreadyExists",
                        "Resource Model Class"
                );
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        commonBundle.message("common.error"),
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            resourceModel = createClass(actionName);

            if (resourceModel == null) {
                final String errorMessage = this.validatorBundle.message(
                        "validator.file.cantBeCreated",
                        "Resource Model Class"
                );
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        commonBundle.message("common.error"),
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            resourceModelFiles[0] = resourceModel.getContainingFile();
        });

        return resourceModelFiles[0];
    }

    /**
     * Create resource model class.
     *
     * @param actionName String
     *
     * @return PhpClass
     */
    private PhpClass createClass(final @NotNull String actionName) {
        PsiDirectory parentDirectory = ModuleIndex.getInstance(project)
                .getModuleDirectoryByModuleName(resourceModelData.getModuleName());
        final PsiFile modelFile;

        final String[] resourceModelDirectories = ResourceModelFile.RESOURCE_MODEL_DIRECTORY.split(
            File.separator
        );
        for (final String directory: resourceModelDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(
                parentDirectory, directory
            );
        }

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
     * Fill resource model file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        final PhpClassTypesBuilder phpClassTypesBuilder = new PhpClassTypesBuilder();

        phpClassTypesBuilder
                .appendProperty("NAME", resourceModelData.getResourceModelName())
                .appendProperty(
                        "NAMESPACE",
                        file.getNamespaceBuilder(resourceModelData.getModuleName()).getNamespace()
                )
                .appendProperty("DB_NAME", resourceModelData.getDbTableName())
                .appendProperty("ENTITY_ID_COLUMN", resourceModelData.getEntityIdColumn())
                .append("EXTENDS", ResourceModelFile.ABSTRACT_DB)
                .mergeProperties(attributes);

        final List<String> uses = phpClassTypesBuilder.getUses();
        attributes.setProperty("USES", PhpClassGeneratorUtil.formatUses(uses));
    }
}
