/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentGridData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentGridToolbarData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.GetCodeTemplateUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.GridActionColumnFile;
import com.magento.idea.magento2plugin.magento.files.UiComponentGridXml;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.magento.packages.database.ColumnAttributes;
import com.magento.idea.magento2plugin.magento.packages.database.TableColumnTypes;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class UiComponentGridXmlGenerator extends FileGenerator {
    public static final String TRUE = "true";
    private final UiComponentGridData uiComponentGridData;
    private final DirectoryGenerator directoryGenerator;
    private final ModuleIndex moduleIndex;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final GetCodeTemplateUtil getCodeTemplateUtil;

    /**
     * UI component grid XML generator constructor.
     *
     * @param uiComponentGridData UiComponentGridData
     * @param project Project
     */
    public UiComponentGridXmlGenerator(
            final UiComponentGridData uiComponentGridData,
            final Project project
    ) {
        super(project);

        this.uiComponentGridData = uiComponentGridData;
        directoryGenerator = DirectoryGenerator.getInstance();
        moduleIndex = ModuleIndex.getInstance(project);
        fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        getCodeTemplateUtil = new GetCodeTemplateUtil(project);
    }

    @Override
    public PsiFile generate(final String actionName) {
        final String moduleName = this.uiComponentGridData.getModuleName();
        PsiDirectory uiComponentDirectory = this.moduleIndex.getModuleDirectoryByModuleName(
                moduleName
        );
        final String subdirectory = String.format(
                "%s/%s/%s",
                Package.moduleViewDir,
                this.uiComponentGridData.getArea(),
                Package.moduleViewUiComponentDir
        );

        for (final String directory: subdirectory.split(File.separator)) {
            uiComponentDirectory = directoryGenerator.findOrCreateSubdirectory(
                    uiComponentDirectory,
                    directory
            );
        }

        final UiComponentGridXml uiGridXml = new UiComponentGridXml(uiComponentGridData.getName());

        return this.fileFromTemplateGenerator.generate(
                uiGridXml,
                getAttributes(),
                uiComponentDirectory,
                actionName
        );
    }

    /**
     * Fill UI Component grid xml file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        attributes.setProperty("NAME", uiComponentGridData.getName());
        attributes.setProperty("ID_FIELD_NAME", uiComponentGridData.getIdFieldName());
        attributes.setProperty("ID_FIELD_NAME", uiComponentGridData.getIdFieldName());
        attributes.setProperty("PROVIDER_CLASS", uiComponentGridData.getProviderClassName());
        attributes.setProperty("ACL", uiComponentGridData.getAcl());
        final UiComponentGridToolbarData toolbarData = uiComponentGridData.getGridToolbarData();

        if (toolbarData.isAddToolbar()) {
            attributes.setProperty("TOOLBAR", TRUE);

            if (toolbarData.isAddBookmarks()) {
                attributes.setProperty("BOOKMARKS", TRUE);
            }

            if (toolbarData.isAddColumnsControls()) {
                attributes.setProperty("COLUMNS_CONTROLS", TRUE);
            }

            if (toolbarData.isAddFulltextSearch()) {
                attributes.setProperty("FULLTEXT_SEARCH", TRUE);
            }

            if (toolbarData.isAddListingFilters()) {
                attributes.setProperty("LISTING_FILTERS", TRUE);
            }

            if (toolbarData.isAddListingPaging()) {
                attributes.setProperty("LISTING_PAGING", TRUE);
            }
        }

        prepareColumnsProperties(attributes);
    }

    /**
     * Prepare columns properties.
     *
     * @param attributes Properties
     */
    private void prepareColumnsProperties(final @NotNull Properties attributes) {
        final List<Map<String, String>> columnsProperties = uiComponentGridData.getColumns();
        final List<String> columnsTextList = new LinkedList<>();

        for (final Map<String, String> columnProperties : columnsProperties) {
            final String columnName = columnProperties.get(ColumnAttributes.NAME.getName());

            if (columnName.equals(uiComponentGridData.getIdFieldName())) {
                continue;
            }

            final String columnLabel = Arrays.stream(
                    columnName.replaceAll("_", " ").split(" ")
            ).map(
                    s -> s.substring(0, 1).toUpperCase(Locale.getDefault()) + s.substring(1)
            ).collect(Collectors.joining(" "));

            final TableColumnTypes columnType =
                    TableColumnTypes.getByValue(
                            columnProperties.get(ColumnAttributes.TYPE.getName())
                    );

            String columnFilter;

            if (columnType == null) {
                columnFilter = "text";
            } else {
                columnFilter = mapColumnTypeToFilterType(columnType);
            }

            try {
                columnsTextList.add(
                        getCodeTemplateUtil.execute(
                                UiComponentGridXml.COLUMN_TEMPLATE,
                                fillColumnCodeTemplateAttributes(
                                        columnName,
                                        columnLabel,
                                        columnFilter
                                )
                        ).trim()
                );
            } catch (IOException exception) {
                return;
            }
        }

        if (!columnsTextList.isEmpty()) {
            attributes.setProperty("COLUMNS", String.join("\n", columnsTextList));
        }

        final NamespaceBuilder actionColumnNamespace = new NamespaceBuilder(
                uiComponentGridData.getModuleName(),
                GridActionColumnFile.CLASS_NAME,
                GridActionColumnFile.DIRECTORY
        );
        attributes.setProperty("ACTION_COLUMN", actionColumnNamespace.getClassFqn());
    }

    /**
     * Fill column code template attributes.
     *
     * @param name String
     * @param label String
     * @param filter String
     *
     * @return Properties
     */
    private Properties fillColumnCodeTemplateAttributes(
            final @NotNull String name,
            final @NotNull String label,
            final @NotNull String filter

    ) {
        final Properties properties = new Properties();

        properties.setProperty("COLUMN_NAME", name);
        properties.setProperty("COLUMN_LABEL", label);
        properties.setProperty("COLUMN_FILTER", filter);

        return properties;
    }

    /**
     * Map column type to filter UI Component type.
     *
     * @param type TableColumnTypes
     *
     * @return String
     */
    @SuppressWarnings("PMD.CyclomaticComplexity")
    private @NotNull String mapColumnTypeToFilterType(final @NotNull TableColumnTypes type) {
        switch (type) {
            case BOOLEAN:
                return "select";
            case DATE:
                return "dateRange";
            case DATETIME:
                return "datetimeRange";
            case INT:
            case BIGINT:
            case TINYINT:
            case SMALLINT:
            case DECIMAL:
            case DOUBLE:
            case FLOAT:
                return "textRange";
            default:
                return "text";
        }
    }
}
