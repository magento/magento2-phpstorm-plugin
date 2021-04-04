/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.data.DeleteEntityByIdCommandData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.ModelFile;
import com.magento.idea.magento2plugin.magento.files.ResourceModelFile;
import com.magento.idea.magento2plugin.magento.files.commands.DeleteEntityByIdCommandFile;
import com.magento.idea.magento2plugin.magento.packages.code.ExceptionType;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class DeleteEntityByIdCommandGenerator extends PhpFileGenerator {

    private final DeleteEntityByIdCommandData data;

    /**
     * Delete entity command generator constructor.
     *
     * @param data DeleteEntityCommandData
     * @param project Project
     */
    public DeleteEntityByIdCommandGenerator(
            final @NotNull DeleteEntityByIdCommandData data,
            final @NotNull Project project
    ) {
        this(data, project, true);
    }

    /**
     * Delete entity command generator constructor.
     *
     * @param data DeleteEntityCommandData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public DeleteEntityByIdCommandGenerator(
            final @NotNull DeleteEntityByIdCommandData data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project, checkFileAlreadyExists);
        this.data = data;
    }

    @Override
    protected AbstractPhpFile initFile() {
        return new DeleteEntityByIdCommandFile(data.getModuleName(), data.getEntityName());
    }

    /**
     * Fill delete command file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        final PhpClassTypesBuilder phpClassTypesBuilder = new PhpClassTypesBuilder();
        final ModelFile modelFile = new ModelFile(data.getModuleName(), data.getModelName());
        final String modelType = modelFile.getClassFqn();
        final String modelFactoryType = modelType.concat("Factory");
        final ResourceModelFile resourceFile =
                new ResourceModelFile(data.getModuleName(), data.getResourceModelName());

        phpClassTypesBuilder
                .appendProperty("ENTITY_NAME", data.getEntityName())
                .appendProperty("NAMESPACE", file.getNamespace())
                .appendProperty("CLASS_NAME", DeleteEntityByIdCommandFile.CLASS_NAME)
                .appendProperty("ENTITY_ID", data.getEntityId())
                .append("Exception", "Exception")
                .append("COULD_NOT_DELETE", ExceptionType.COULD_NOT_DELETE.getType())
                .append("NO_SUCH_ENTITY_EXCEPTION",
                        ExceptionType.NO_SUCH_ENTITY_EXCEPTION.getType())
                .append("LOGGER", FrameworkLibraryType.LOGGER.getType())
                .append("MODEL", modelType)
                .append("MODEL_FACTORY", modelFactoryType)
                .append("RESOURCE", resourceFile.getClassFqn())
                .mergeProperties(attributes);

        attributes.setProperty("USES",
                PhpClassGeneratorUtil.formatUses(phpClassTypesBuilder.getUses()));
    }
}
