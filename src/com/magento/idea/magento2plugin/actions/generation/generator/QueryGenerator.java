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
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentDataProviderData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.UiComponentDataProviderFile;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

public class QueryGenerator extends FileGenerator {

    private final UiComponentDataProviderData data;
    private final Project project;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final String moduleName;
    private final GetFirstClassOfFile getFirstClassOfFile;
    private final UiComponentDataProviderFile file;

    /**
     * Ui component grid data provider constructor.
     *
     * @param data UiComponentGridDataProviderData
     * @param moduleName String
     * @param project Project
     */
    public QueryGenerator(
            final UiComponentDataProviderData data,
            final String moduleName,
            final Project project
    ) {
        super(project);
        this.data = data;
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
        this.project = project;
        this.moduleName = moduleName;
        file = new UiComponentDataProviderFile(moduleName, data.getName(), data.getPath());
    }

    @Override
    public PsiFile generate(final @NotNull String actionName) {
        final PsiFile[] dataProviderFiles = new PsiFile[1];

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PhpClass dataProvider = GetPhpClassByFQN.getInstance(project).execute(
                    file.getClassFqn()
            );

            if (dataProvider != null) {
                final String errorMessage = this.validatorBundle.message(
                        "validator.file.alreadyExists",
                        "DataProvider Class"
                );
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        commonBundle.message("common.error"),
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            dataProvider = createDataProviderClass(actionName);

            if (dataProvider == null) {
                final String errorMessage = this.validatorBundle.message(
                        "validator.file.cantBeCreated",
                        "DataProvider Class"
                );
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        commonBundle.message("common.error"),
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            dataProviderFiles[0] = dataProvider.getContainingFile();
        });

        return dataProviderFiles[0];
    }

    /**
     * Create data provider file.
     *
     * @param actionName String
     *
     * @return PhpClass
     */
    private PhpClass createDataProviderClass(final @NotNull String actionName) {
        final PsiDirectory parentDirectory = new ModuleIndex(project)
                .getModuleDirectoryByModuleName(this.moduleName);

        if (parentDirectory == null) {
            return null;
        }
        final PsiDirectory dataProviderDirectory =
                directoryGenerator.findOrCreateSubdirectories(parentDirectory, data.getPath());

        final PsiFile dataProviderFile = fileFromTemplateGenerator.generate(
                new UiComponentDataProviderFile(moduleName, data.getName(), data.getPath()),
                getAttributes(),
                dataProviderDirectory,
                actionName
        );

        if (dataProviderFile == null) {
            return null;
        }

        return getFirstClassOfFile.execute((PhpFile) dataProviderFile);
    }

    /**
     * Fill file property attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        attributes.setProperty("NAMESPACE", file.getNamespace());
        attributes.setProperty("CLASS_NAME", data.getName());
    }
}
