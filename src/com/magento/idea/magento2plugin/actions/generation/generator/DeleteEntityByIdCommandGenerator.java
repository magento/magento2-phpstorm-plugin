/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.context.EntityCreatorContext;
import com.magento.idea.magento2plugin.actions.generation.data.DeleteEntityByIdCommandData;
import com.magento.idea.magento2plugin.actions.generation.dialog.util.ClassPropertyFormatterUtil;
import com.magento.idea.magento2plugin.actions.generation.util.GenerationContextRegistry;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.ModelFile;
import com.magento.idea.magento2plugin.magento.files.ResourceModelFile;
import com.magento.idea.magento2plugin.magento.files.commands.DeleteEntityByIdCommandFile;
import com.magento.idea.magento2plugin.magento.packages.code.ExceptionType;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import java.util.Objects;
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
        final ModelFile modelFile = new ModelFile(data.getModuleName(), data.getModelName());
        final String modelType = modelFile.getClassFqn();
        final String modelFactoryType = modelType.concat("Factory");
        final ResourceModelFile resourceFile =
                new ResourceModelFile(data.getModuleName(), data.getResourceModelName());

        final EntityCreatorContext context =
                (EntityCreatorContext) GenerationContextRegistry.getInstance().getContext();
        Objects.requireNonNull(context);
        final String dtoTypeFqn = context.getUserData(EntityCreatorContext.DTO_TYPE);
        Objects.requireNonNull(dtoTypeFqn);
        typesBuilder.append(
                "ENTITY_ID_REFERENCE",
                ClassPropertyFormatterUtil.formatNameToConstant(data.getEntityId(), dtoTypeFqn),
                false
        );
        typesBuilder.append("DTO_TYPE", dtoTypeFqn);

        typesBuilder
                .append("ENTITY_NAME", data.getEntityName(), false)
                .append("NAMESPACE", file.getNamespace(), false)
                .append("CLASS_NAME", DeleteEntityByIdCommandFile.CLASS_NAME, false)
                .append("Exception", "Exception")
                .append("COULD_NOT_DELETE", ExceptionType.COULD_NOT_DELETE.getType())
                .append("NO_SUCH_ENTITY_EXCEPTION",
                        ExceptionType.NO_SUCH_ENTITY_EXCEPTION.getType())
                .append("LOGGER", FrameworkLibraryType.LOGGER.getType())
                .append("MODEL", modelType)
                .append("MODEL_FACTORY", modelFactoryType)
                .append("RESOURCE", resourceFile.getClassFqn());
    }
}
