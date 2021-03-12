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
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.UiComponentDataProviderFile;
import com.magento.idea.magento2plugin.magento.files.queries.GetListQuery;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"PMD.OnlyOneReturn", "PMD.DataflowAnomalyAnalysis"})
public class UiComponentDataProviderGenerator extends FileGenerator {
    private final UiComponentDataProviderData uiComponentGridDataProviderData;
    private final Project project;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final String moduleName;
    private final GetFirstClassOfFile getFirstClassOfFile;

    /**
     * Ui component grid data provider constructor.
     *
     * @param uiComponentGridDataProviderData UiComponentGridDataProviderData
     * @param moduleName String
     * @param project Project
     */
    public UiComponentDataProviderGenerator(
            final @NotNull UiComponentDataProviderData uiComponentGridDataProviderData,
            final @NotNull String moduleName,
            final @NotNull Project project
    ) {
        super(project);
        this.uiComponentGridDataProviderData = uiComponentGridDataProviderData;
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
        this.project = project;
        this.moduleName = moduleName;
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
                    getDataProviderFqn()
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
     * Fill file property attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        attributes.setProperty("NAMESPACE", uiComponentGridDataProviderData.getNamespace());
        attributes.setProperty("CLASS_NAME", uiComponentGridDataProviderData.getName());

        if (uiComponentGridDataProviderData.getEntityIdFieldName() != null) {
            attributes.setProperty(
                    "ENTITY_ID",
                    uiComponentGridDataProviderData.getEntityIdFieldName()
            );
        }
        attributes.setProperty("HAS_GET_LIST_QUERY", "false");

        final List<String> uses = new LinkedList<>();

        uses.add(UiComponentDataProviderFile.DEFAULT_DATA_PROVIDER);
        attributes.setProperty(
                "EXTENDS",
                PhpClassGeneratorUtil.getNameFromFqn(
                        UiComponentDataProviderFile.DEFAULT_DATA_PROVIDER
                )
        );

        final PhpClass getListQueryFile = GetPhpClassByFQN.getInstance(project).execute(
                GetListQuery.getClassFqn(moduleName)
        );

        if (getListQueryFile == null) {
            attributes.setProperty("USES", PhpClassGeneratorUtil.formatUses(uses));
            return;
        }
        attributes.setProperty("HAS_GET_LIST_QUERY", "true");

        uses.add(FrameworkLibraryType.REPORTING.getType());
        attributes.setProperty("REPORTING_TYPE", FrameworkLibraryType.REPORTING.getTypeName());

        uses.add(FrameworkLibraryType.API_SEARCH_CRITERIA_BUILDER.getType());
        attributes.setProperty("SEARCH_CRITERIA_BUILDER",
                FrameworkLibraryType.API_SEARCH_CRITERIA_BUILDER.getTypeName());

        uses.add(FrameworkLibraryType.REQUEST.getType());
        attributes.setProperty("REQUEST_TYPE", FrameworkLibraryType.REQUEST.getTypeName());

        uses.add(FrameworkLibraryType.FILTER_BUILDER.getType());
        attributes.setProperty("FILTER_BUILDER", FrameworkLibraryType.FILTER_BUILDER.getTypeName());

        uses.add(UiComponentDataProviderFile.SEARCH_RESULT_FACTORY);
        attributes.setProperty("SEARCH_RESULT_FACTORY",
                PhpClassGeneratorUtil.getNameFromFqn(
                        UiComponentDataProviderFile.SEARCH_RESULT_FACTORY
                )
        );

        final @NotNull String getListQueryFqn = getListQueryFile.getPresentableFQN();

        uses.add(getListQueryFqn);
        attributes.setProperty("GET_LIST_QUERY_TYPE", PhpClassGeneratorUtil.getNameFromFqn(
                getListQueryFqn
        ));

        attributes.setProperty("USES", PhpClassGeneratorUtil.formatUses(uses));
    }

    /**
     * Generate data provider class.
     *
     * @param actionName String
     *
     * @return PhpClass
     */
    private PhpClass createDataProviderClass(final @NotNull String actionName) {
        PsiDirectory parentDirectory = ModuleIndex.getInstance(project)
                .getModuleDirectoryByModuleName(this.moduleName);
        final PsiFile dataProviderFile;
        final String[] dataProviderDirectories = uiComponentGridDataProviderData.getPath().split(
                File.separator
        );
        for (final String dataProviderDirectory: dataProviderDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(
                    parentDirectory, dataProviderDirectory
            );
        }

        final Properties attributes = getAttributes();

        dataProviderFile = fileFromTemplateGenerator.generate(
                UiComponentDataProviderFile.getInstance(
                        uiComponentGridDataProviderData.getName()
                ),
                attributes,
                parentDirectory,
                actionName
        );

        if (dataProviderFile == null) {
            return null;
        }

        return getFirstClassOfFile.execute((PhpFile) dataProviderFile);
    }

    /**
     * Get data provider class FQN.
     *
     * @return String
     */
    private String getDataProviderFqn() {
        return String.format(
                "%s%s%s",
                uiComponentGridDataProviderData.getNamespace(),
                Package.fqnSeparator,
                uiComponentGridDataProviderData.getName()
        );
    }
}
