/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.data.GetListQueryModelData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.CollectionModelFile;
import com.magento.idea.magento2plugin.magento.files.EntityDataMapperFile;
import com.magento.idea.magento2plugin.magento.files.queries.GetListQueryFile;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class GetListQueryModelGenerator extends PhpFileGenerator {

    private final GetListQueryModelData data;

    /**
     * Query model generator Constructor.
     *
     * @param data QueryModelData
     * @param project Project
     */
    public GetListQueryModelGenerator(
            final @NotNull GetListQueryModelData data,
            final @NotNull Project project
    ) {
        this(data, project, true);
    }

    /**
     * Query model generator Constructor.
     *
     * @param data QueryModelData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public GetListQueryModelGenerator(
            final @NotNull GetListQueryModelData data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project, checkFileAlreadyExists);
        this.data = data;
    }

    @Override
    protected AbstractPhpFile initFile() {
        return new GetListQueryFile(data.getModuleName(), data.getEntityName());
    }

    /**
     * Fill file property attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        final PhpClassTypesBuilder phpClassTypesBuilder = new PhpClassTypesBuilder();
        final CollectionModelFile collectionModelFile =
                new CollectionModelFile(
                        data.getModuleName(),
                        data.getCollectionName(),
                        data.getModelName()
                );
        final NamespaceBuilder collectionNamespace =
                collectionModelFile.getNamespaceBuilder();

        phpClassTypesBuilder
                .appendProperty("ENTITY_NAME", data.getEntityName())
                .appendProperty("NAMESPACE", file.getNamespace())
                .appendProperty("CLASS_NAME", GetListQueryFile.CLASS_NAME)
                .append(
                        "ENTITY_COLLECTION_TYPE",
                        collectionNamespace.getClassFqn()
                )
                .append(
                        "ENTITY_COLLECTION_FACTORY_TYPE",
                        collectionNamespace.getClassFqn().concat("Factory")
                )
                .append(
                        "ENTITY_DATA_MAPPER_TYPE",
                        new EntityDataMapperFile(
                                data.getModuleName(),
                                data.getEntityName()
                        ).getClassFqn()
                )
                .append(
                        "COLLECTION_PROCESSOR_TYPE",
                        FrameworkLibraryType.COLLECTION_PROCESSOR.getType()
                )
                .append(
                        "SEARCH_CRITERIA_BUILDER_TYPE",
                        FrameworkLibraryType.SEARCH_CRITERIA_BUILDER.getType()
                )
                .append(
                        "SEARCH_CRITERIA_TYPE",
                        FrameworkLibraryType.SEARCH_CRITERIA.getType()
                )
                .append(
                        "SEARCH_RESULT_TYPE",
                        FrameworkLibraryType.SEARCH_RESULT.getType()
                )
                .append(
                        "SEARCH_RESULT_FACTORY_TYPE",
                        FrameworkLibraryType.SEARCH_RESULT.getFactory()
                )
                .mergeProperties(attributes);

        attributes.setProperty(
                "USES",
                PhpClassGeneratorUtil.formatUses(phpClassTypesBuilder.getUses())
        );
    }
}
