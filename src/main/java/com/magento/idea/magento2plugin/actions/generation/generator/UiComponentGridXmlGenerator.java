/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentGridData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentGridToolbarData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.GetCodeTemplateUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.GridActionColumnFile;
import com.magento.idea.magento2plugin.magento.files.UiComponentDataProviderFile;
import com.magento.idea.magento2plugin.magento.files.UiComponentGridXmlFile;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.magento.packages.database.ColumnAttributes;
import com.magento.idea.magento2plugin.magento.packages.database.TableColumnTypes;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;
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
    private final UiComponentGridData data;
    private final DirectoryGenerator directoryGenerator;
    private final ModuleIndex moduleIndex;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final GetCodeTemplateUtil getCodeTemplateUtil;

    /**
     * UI component grid XML generator constructor.
     *
     * @param data UiComponentGridData
     * @param project Project
     */
    public UiComponentGridXmlGenerator(
            final UiComponentGridData data,
            final Project project
    ) {
        super(project);
        this.data = data;
        directoryGenerator = DirectoryGenerator.getInstance();
        moduleIndex = new ModuleIndex(project);
        fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        getCodeTemplateUtil = new GetCodeTemplateUtil(project);
    }

    @Override
    public PsiFile generate(final String actionName) {
        final String moduleName = data.getModuleName();
        final PsiDirectory parentDirectory = moduleIndex.getModuleDirectoryByModuleName(
                moduleName
        );
        if (parentDirectory == null) {
            return null;
        }
        final String subdirectory = String.format(
                "%s/%s/%s",
                Package.moduleViewDir,
                this.data.getArea(),
                Package.moduleViewUiComponentDir
        );
        final PsiDirectory uiComponentDirectory =
                directoryGenerator.findOrCreateSubdirectories(parentDirectory, subdirectory);

        final UiComponentGridXmlFile file = new UiComponentGridXmlFile(data.getName());

        XmlFile gridXmlFile = (XmlFile) FileBasedIndexUtil.findModuleViewFile(
                file.getFileName(),
                Areas.getAreaByString(data.getArea()),
                moduleName,
                project,
                Package.moduleViewUiComponentDir
        );

        if (gridXmlFile == null) {
            gridXmlFile = (XmlFile) fileFromTemplateGenerator.generate(
                    new UiComponentGridXmlFile(data.getName()),
                    getAttributes(),
                    uiComponentDirectory,
                    actionName
            );
        }

        return gridXmlFile;
    }

    /**
     * Fill UI Component grid xml file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        final PhpClassTypesBuilder phpClassTypesBuilder = new PhpClassTypesBuilder();
        final String dataProviderClassName =
                new UiComponentDataProviderFile(
                        data.getModuleName(),
                        data.getDataProviderName(),
                        data.getDataProviderPath()
                ).getClassFqn();

        phpClassTypesBuilder
                .appendProperty("NAME", data.getName())
                .appendProperty("ID_FIELD_NAME", data.getIdFieldName())
                .appendProperty("PROVIDER_CLASS", dataProviderClassName)
                .appendProperty("ACL", data.getAcl());

        final UiComponentGridToolbarData toolbarData = data.getGridToolbarData();

        if (toolbarData.isAddToolbar()) {
            phpClassTypesBuilder.appendProperty("TOOLBAR", TRUE);

            if (toolbarData.isAddBookmarks()) {
                phpClassTypesBuilder.appendProperty("BOOKMARKS", TRUE);
            }

            if (toolbarData.isAddColumnsControls()) {
                phpClassTypesBuilder.appendProperty("COLUMNS_CONTROLS", TRUE);
            }

            if (toolbarData.isAddFulltextSearch()) {
                phpClassTypesBuilder.appendProperty("FULLTEXT_SEARCH", TRUE);
            }

            if (toolbarData.isAddListingFilters()) {
                phpClassTypesBuilder.appendProperty("LISTING_FILTERS", TRUE);
            }

            if (toolbarData.isAddListingPaging()) {
                phpClassTypesBuilder.appendProperty("LISTING_PAGING", TRUE);
            }
        }
        phpClassTypesBuilder.mergeProperties(attributes);

        prepareColumnsProperties(attributes);
    }

    /**
     * Prepare columns properties.
     *
     * @param attributes Properties
     */
    private void prepareColumnsProperties(final @NotNull Properties attributes) {
        final List<Map<String, String>> columnsProperties = data.getColumns();
        final List<String> columnsTextList = new LinkedList<>();

        for (final Map<String, String> columnProperties : columnsProperties) {
            final String columnName = columnProperties.get(ColumnAttributes.NAME.getName());

            if (columnName.equals(data.getIdFieldName())) {
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
                                UiComponentGridXmlFile.COLUMN_TEMPLATE,
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

        if (data.getEntityName() != null) {
            final GridActionColumnFile gridActionColumnFile =
                    new GridActionColumnFile(data.getModuleName(), data.getEntityName());
            attributes.setProperty("ACTION_COLUMN", gridActionColumnFile.getClassFqn());
        }
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
