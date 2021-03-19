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
import com.magento.idea.magento2plugin.actions.generation.data.CollectionData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.CollectionModelFile;
import com.magento.idea.magento2plugin.magento.files.ModelFile;
import com.magento.idea.magento2plugin.magento.files.ResourceModelFile;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

public class ModuleCollectionGenerator extends FileGenerator {

    private final CollectionData data;
    private final Project project;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final GetFirstClassOfFile getFirstClassOfFile;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final CollectionModelFile file;

    /**
     * Generates new Collection PHP Class based on provided data.
     *
     * @param data CollectionData
     * @param project Project
     */
    public ModuleCollectionGenerator(
            final @NotNull CollectionData data,
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
        file = new CollectionModelFile(data.getCollectionName());
    }

    /**
     * Generates collection model class.
     *
     * @param actionName Action name
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final @NotNull String actionName) {
        final PsiFile[] collectionFiles = new PsiFile[1];

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PhpClass collection = GetPhpClassByFQN.getInstance(project).execute(
                    file.getNamespaceBuilder(
                            data.getModuleName(),
                            data.getCollectionDirectory()
                    ).getClassFqn()
            );

            if (collection != null) {
                final String errorMessage = this.validatorBundle.message(
                        "validator.file.alreadyExists",
                        "Collection Class"
                );
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        commonBundle.message("common.error"),
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            collection = createClass(actionName);

            if (collection == null) {
                final String errorMessage = this.validatorBundle.message(
                        "validator.file.cantBeCreated",
                        "Collection Class"
                );
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        commonBundle.message("common.error"),
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            collectionFiles[0] = collection.getContainingFile();
        });

        return collectionFiles[0];
    }

    /**
     * Create collection class.
     *
     * @param actionName String
     *
     * @return PhpClass
     */
    private PhpClass createClass(final @NotNull String actionName) {
        final PsiDirectory parentDirectory = ModuleIndex.getInstance(project)
                .getModuleDirectoryByModuleName(data.getModuleName());
        final PsiFile modelFile;

        final PsiDirectory collectionDirectory = directoryGenerator.findOrCreateSubdirectories(
                parentDirectory,
                file.getDirectory(data.getCollectionDirectory())
        );
        final Properties attributes = getAttributes();

        modelFile = fileFromTemplateGenerator.generate(
                file,
                attributes,
                collectionDirectory,
                actionName
        );

        if (modelFile == null) {
            return null;
        }

        return getFirstClassOfFile.execute((PhpFile) modelFile);
    }

    /**
     * Fill collection model file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        final PhpClassTypesBuilder phpClassTypesBuilder = new PhpClassTypesBuilder();

        final ResourceModelFile resourceModelFile =
                new ResourceModelFile(data.getResourceModelName());
        final ModelFile modelFile = new ModelFile(data.getModelName());

        phpClassTypesBuilder.appendProperty("NAME", data.getCollectionName())
                .appendProperty(
                        "NAMESPACE",
                        file.getNamespaceBuilder(
                                data.getModuleName(),
                                data.getCollectionDirectory()
                        ).getNamespace()
                )
                .appendProperty("DB_NAME", data.getDbTableName())
                .appendProperty("MODEL", data.getModelName())
                .appendProperty("RESOURCE_MODEL", data.getResourceModelName())
                .append("EXTENDS", CollectionModelFile.ABSTRACT_COLLECTION)
                .append(
                        "RESOURCE_MODEL",
                        resourceModelFile.getNamespaceBuilder(
                                data.getModuleName()
                        ).getClassFqn(),
                        ResourceModelFile.ALIAS
                )
                .append(
                        "MODEL",
                        modelFile.getNamespaceBuilder(
                                data.getModuleName()
                        ).getClassFqn(),
                        ModelFile.ALIAS
                )
                .mergeProperties(attributes);

        attributes.setProperty(
                "USES",
                PhpClassGeneratorUtil.formatUses(
                        phpClassTypesBuilder.getUses()
                )
        );
    }
}
