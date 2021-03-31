/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentDataProviderData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.UiComponentDataProviderFile;
import com.magento.idea.magento2plugin.magento.files.queries.GetListQueryFile;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class UiComponentDataProviderGenerator extends PhpFileGenerator {

    private final UiComponentDataProviderData data;
    private final String moduleName;

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
        this(data, moduleName, project, true);
    }

    /**
     * Ui component grid data provider constructor.
     *
     * @param data UiComponentGridDataProviderData
     * @param moduleName String
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public UiComponentDataProviderGenerator(
            final @NotNull UiComponentDataProviderData data,
            final @NotNull String moduleName,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project, checkFileAlreadyExists);
        this.data = data;
        this.moduleName = moduleName;
    }

    @Override
    protected AbstractPhpFile initFile() {
        return new UiComponentDataProviderFile(moduleName, data.getName(), data.getPath());
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
                .appendProperty("NAMESPACE", file.getNamespace())
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
