/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.data.CollectionData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.CollectionModelFile;
import com.magento.idea.magento2plugin.magento.files.ModelFile;
import com.magento.idea.magento2plugin.magento.files.ResourceModelFile;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class ModuleCollectionGenerator extends PhpFileGenerator {

    private final CollectionData data;

    /**
     * Generates new Collection PHP Class based on provided data.
     *
     * @param data CollectionData
     * @param project Project
     */
    public ModuleCollectionGenerator(
            final @NotNull CollectionData data,
            final @NotNull Project project
    ) {
        this(data, project, true);
    }

    /**
     * Generates new Collection PHP Class based on provided data.
     *
     * @param data CollectionData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public ModuleCollectionGenerator(
            final @NotNull CollectionData data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project, checkFileAlreadyExists);
        this.data = data;
    }

    @Override
    protected AbstractPhpFile initFile() {
        return new CollectionModelFile(
                data.getModuleName(),
                data.getCollectionName(),
                data.getCollectionDirectory()
        );
    }

    /**
     * Fill collection model file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        final PhpClassTypesBuilder phpClassTypesBuilder = new PhpClassTypesBuilder();

        final ResourceModelFile resourceModelFile =
                new ResourceModelFile(data.getModuleName(), data.getResourceModelName());
        final ModelFile modelFile = new ModelFile(data.getModuleName(), data.getModelName());

        phpClassTypesBuilder.appendProperty("NAME", data.getCollectionName())
                .appendProperty("NAMESPACE", file.getNamespaceBuilder().getNamespace())
                .appendProperty("DB_NAME", data.getDbTableName())
                .appendProperty("MODEL", data.getModelName())
                .appendProperty("RESOURCE_MODEL", data.getResourceModelName())
                .append("EXTENDS", CollectionModelFile.ABSTRACT_COLLECTION)
                .append(
                        "RESOURCE_MODEL",
                        resourceModelFile.getClassFqn(),
                        ResourceModelFile.ALIAS
                )
                .append(
                        "MODEL",
                        modelFile.getClassFqn(),
                        ModelFile.ALIAS
                )
                .mergeProperties(attributes);

        attributes.setProperty(
                "USES",
                PhpClassGeneratorUtil.formatUses(
                        phpClassTypesBuilder.getUses()
                )
        );
    }
}
