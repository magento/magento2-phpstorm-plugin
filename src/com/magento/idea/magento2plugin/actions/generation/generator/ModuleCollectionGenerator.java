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
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.CollectionPhp;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.swing.JOptionPane;

public class ModuleCollectionGenerator extends FileGenerator {
    private final CollectionData collectionData;
    private final Project project;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final GetFirstClassOfFile getFirstClassOfFile;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;

    /**
     * Generates new Collection PHP Class based on provided data.
     *
     * @param collectionData CollectionData
     * @param project Project
     */
    public ModuleCollectionGenerator(
            final CollectionData collectionData,
            final Project project
    ) {
        super(project);
        this.project = project;
        this.collectionData = collectionData;
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    /**
     * Generates collection model class.
     *
     * @param actionName Action name
     * @return PsiFile
     */
    public PsiFile generate(final String actionName) {
        final PsiFile[] collectionFiles = new PsiFile[1];

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PhpClass collection = GetPhpClassByFQN.getInstance(project).execute(
                    getCollectionModelFqn()
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
     * Get controller module.
     *
     * @return String
     */
    public String getModuleName() {
        return collectionData.getModuleName();
    }

    private String getCollectionModelFqn() {
        return collectionData.getCollectionFqn();
    }

    private PhpClass createClass(final String actionName) {
        PsiDirectory parentDirectory = ModuleIndex.getInstance(project)
                .getModuleDirectoryByModuleName(getModuleName());
        final PsiFile modelFile;

        final String[] collectionDirectories = collectionData.getCollectionDirectory().split(
            File.separator
        );
        for (final String directory: collectionDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(
                parentDirectory, directory
            );
        }

        final Properties attributes = getAttributes();
        modelFile = fileFromTemplateGenerator.generate(
                new CollectionPhp(collectionData.getCollectionName()),
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
        attributes.setProperty("NAME", collectionData.getCollectionName());
        attributes.setProperty("NAMESPACE", collectionData.getCollectionNamespace());

        attributes.setProperty("DB_NAME", collectionData.getDbTableName());
        attributes.setProperty("MODEL", PhpClassGeneratorUtil.getNameFromFqn(
                collectionData.getModelName())
        );
        attributes.setProperty("RESOURCE_MODEL", PhpClassGeneratorUtil.getNameFromFqn(
                collectionData.getResourceModelName())
        );
        final List<String> uses = getUses();
        uses.add(collectionData.getResourceModelFqn());
        uses.add(collectionData.getModelFqn());

        attributes.setProperty(
                "EXTENDS",
                PhpClassGeneratorUtil.getNameFromFqn(CollectionPhp.ABSTRACT_COLLECTION)
        );

        attributes.setProperty("USES", PhpClassGeneratorUtil.formatUses(uses));
    }

    private List<String> getUses() {
        return new ArrayList<>(Arrays.asList(
                CollectionPhp.ABSTRACT_COLLECTION
        ));
    }
}
