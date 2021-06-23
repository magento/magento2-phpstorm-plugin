/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.context.EntityCreatorContext;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentDataProviderData;
import com.magento.idea.magento2plugin.actions.generation.dialog.util.ClassPropertyFormatterUtil;
import com.magento.idea.magento2plugin.actions.generation.util.GenerationContextRegistry;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.UiComponentDataProviderFile;
import com.magento.idea.magento2plugin.magento.files.queries.GetListQueryFile;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import java.util.Objects;
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
        typesBuilder
                .append("NAMESPACE", file.getNamespace(), false)
                .append("CLASS_NAME", data.getName(), false)
                .append("HAS_GET_LIST_QUERY", "false", false)
                .append("EXTENDS", UiComponentDataProviderFile.DEFAULT_DATA_PROVIDER);

        if (data.getEntityIdFieldName() != null && data.getEntityName() != null) {
            final EntityCreatorContext context =
                    (EntityCreatorContext) GenerationContextRegistry.getInstance().getContext();
            Objects.requireNonNull(context);
            final String dtoTypeFqn = context.getUserData(EntityCreatorContext.DTO_TYPE);
            Objects.requireNonNull(dtoTypeFqn);
            typesBuilder.append(
                    "ENTITY_ID_REFERENCE",
                    ClassPropertyFormatterUtil.formatNameToConstant(
                            data.getEntityIdFieldName(),
                            dtoTypeFqn
                    ),
                    false
            );
            typesBuilder.append("DTO_TYPE", dtoTypeFqn);

            typesBuilder
                    .append("HAS_GET_LIST_QUERY", "true", false)
                    .append(
                            "GET_LIST_QUERY_TYPE",
                            new GetListQueryFile(
                                    moduleName,
                                    data.getEntityName(),
                                    data.isHasQueryInterface()
                            ).getClassFqn()
                    )
                    .append("REPORTING_TYPE", FrameworkLibraryType.REPORTING.getType())
                    .append("SEARCH_CRITERIA_BUILDER",
                            FrameworkLibraryType.API_SEARCH_CRITERIA_BUILDER.getType())
                    .append("REQUEST_TYPE", FrameworkLibraryType.REQUEST.getType())
                    .append("FILTER_BUILDER", FrameworkLibraryType.FILTER_BUILDER.getType())
                    .append("SEARCH_RESULT_FACTORY",
                            UiComponentDataProviderFile.SEARCH_RESULT_FACTORY);
        }
    }
}
