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
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.UiComponentDataProviderFile;
import com.magento.idea.magento2plugin.magento.files.queries.GetListQueryFile;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

public class UiComponentDataProviderGenerator extends FileGenerator {

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
    public UiComponentDataProviderGenerator(
            final @NotNull UiComponentDataProviderData data,
            final @NotNull String moduleName,
            final @NotNull Project project
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
        file = new UiComponentDataProviderFile(data.getName());
    }

    /**
     * Generate UiComponent data provider class.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final @NotNull String actionName) {
        final PsiFile[] dataProviderFiles = new PsiFile[1];

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PhpClass dataProvider = GetPhpClassByFQN.getInstance(project).execute(
                    file.getNamespaceBuilder(moduleName, data.getPath()).getClassFqn()
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
     * Generate data provider class.
     *
     * @param actionName String
     *
     * @return PhpClass
     */
    private PhpClass createDataProviderClass(final @NotNull String actionName) {
        final PsiDirectory parentDirectory = new ModuleIndex(project)
                .getModuleDirectoryByModuleName(this.moduleName);
        final PsiDirectory fileDirectory =
                directoryGenerator.findOrCreateSubdirectories(parentDirectory, data.getPath());

        final PsiFile dataProviderFile = fileFromTemplateGenerator.generate(
                file,
                getAttributes(),
                fileDirectory,
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
        final PhpClassTypesBuilder phpClassTypesBuilder = new PhpClassTypesBuilder();

        phpClassTypesBuilder
                .appendProperty("NAMESPACE",
                        file.getNamespaceBuilder(moduleName, data.getPath()).getNamespace())
                .appendProperty("CLASS_NAME", data.getName())
                .appendProperty("HAS_GET_LIST_QUERY", "false")
                .append("EXTENDS", UiComponentDataProviderFile.DEFAULT_DATA_PROVIDER);

        if (data.getEntityIdFieldName() != null && data.getEntityName() != null) {
            phpClassTypesBuilder.appendProperty("ENTITY_ID", data.getEntityIdFieldName());

            phpClassTypesBuilder
                    .appendProperty("HAS_GET_LIST_QUERY", "true")
                    .append(
                            "GET_LIST_QUERY_TYPE",
                            new GetListQueryFile(
                                    data.getEntityName()
                            ).getNamespaceBuilder(moduleName).getClassFqn()
                    )
                    .append("REPORTING_TYPE", FrameworkLibraryType.REPORTING.getType())
                    .append("SEARCH_CRITERIA_BUILDER",
                            FrameworkLibraryType.API_SEARCH_CRITERIA_BUILDER.getType())
                    .append("REQUEST_TYPE", FrameworkLibraryType.REQUEST.getType())
                    .append("FILTER_BUILDER", FrameworkLibraryType.FILTER_BUILDER.getType())
                    .append("SEARCH_RESULT_FACTORY",
                            UiComponentDataProviderFile.SEARCH_RESULT_FACTORY);
        }
        phpClassTypesBuilder.mergeProperties(attributes);

        attributes.setProperty("USES",
                PhpClassGeneratorUtil.formatUses(phpClassTypesBuilder.getUses()));
    }
}
