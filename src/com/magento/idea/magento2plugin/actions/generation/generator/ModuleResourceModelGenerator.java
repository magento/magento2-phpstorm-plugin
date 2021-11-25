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
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.ResourceModelPhp;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.swing.JOptionPane;

public class ModuleResourceModelGenerator extends FileGenerator {
    private final ResourceModelData resourceModelData;
    private final Project project;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final GetFirstClassOfFile getFirstClassOfFile;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;

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
    }

    /**
     * Generates resource model class.
     *
     * @param actionName Action name
     * @return PsiFile
     */
    public PsiFile generate(final String actionName) {
        final PsiFile[] resourceModelFiles = new PsiFile[1];

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PhpClass resourceModel = GetPhpClassByFQN.getInstance(project).execute(
                    getResourceModelFqn()
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
     * Get module.
     *
     * @return String
     */
    public String getModuleName() {
        return resourceModelData.getModuleName();
    }

    private String getResourceModelFqn() {
        return resourceModelData.getFqn();
    }

    private PhpClass createClass(final String actionName) {
        PsiDirectory parentDirectory = ModuleIndex.getInstance(project)
                .getModuleDirectoryByModuleName(getModuleName());
        final PsiFile modelFile;

        final String[] resourceModelDirectories = ResourceModelPhp.RESOURCE_MODEL_DIRECTORY.split(
            File.separator
        );
        for (final String directory: resourceModelDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(
                parentDirectory, directory
            );
        }

        final Properties attributes = getAttributes();
        modelFile = fileFromTemplateGenerator.generate(
                new ResourceModelPhp(resourceModelData.getResourceModelName()),
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
        attributes.setProperty("NAME", resourceModelData.getResourceModelName());
        attributes.setProperty("NAMESPACE", resourceModelData.getNamespace());

        attributes.setProperty("DB_NAME", resourceModelData.getDbTableName());
        attributes.setProperty("ENTITY_ID_COLUMN", PhpClassGeneratorUtil.getNameFromFqn(
                resourceModelData.getEntityIdColumn())
        );
        final List<String> uses = getUses();

        attributes.setProperty(
                "EXTENDS",
                PhpClassGeneratorUtil.getNameFromFqn(ResourceModelPhp.ABSTRACT_DB)
        );

        attributes.setProperty("USES", PhpClassGeneratorUtil.formatUses(uses));
    }

    private List<String> getUses() {
        return new ArrayList<>(Arrays.asList(
                ResourceModelPhp.ABSTRACT_DB
        ));
    }
}
