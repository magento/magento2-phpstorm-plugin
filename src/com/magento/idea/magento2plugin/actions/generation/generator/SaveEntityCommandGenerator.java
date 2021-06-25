/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.google.common.base.CaseFormat;
import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.data.SaveEntityCommandData;
import com.magento.idea.magento2plugin.actions.generation.dialog.util.ClassPropertyFormatterUtil;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.DataModelFile;
import com.magento.idea.magento2plugin.magento.files.DataModelInterfaceFile;
import com.magento.idea.magento2plugin.magento.files.ModelFile;
import com.magento.idea.magento2plugin.magento.files.ResourceModelFile;
import com.magento.idea.magento2plugin.magento.files.commands.SaveEntityCommandFile;
import com.magento.idea.magento2plugin.magento.packages.code.ExceptionType;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class SaveEntityCommandGenerator extends PhpFileGenerator {

    private final SaveEntityCommandData data;

    /**
     * Save entity command generator constructor.
     *
     * @param data SaveEntityCommandData
     * @param project Project
     */
    public SaveEntityCommandGenerator(
            final @NotNull SaveEntityCommandData data,
            final @NotNull Project project
    ) {
        this(data, project, true);
    }

    /**
     * Save entity command generator constructor.
     *
     * @param data SaveEntityCommandData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public SaveEntityCommandGenerator(
            final @NotNull SaveEntityCommandData data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project, checkFileAlreadyExists);
        this.data = data;
    }

    @Override
    protected AbstractPhpFile initFile() {
        return new SaveEntityCommandFile(data.getModuleName(), data.getEntityName());
    }

    /**
     * Fill save command file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        typesBuilder
                .append("NAMESPACE", file.getNamespace(), false)
                .append("ENTITY_NAME", data.getEntityName(), false)
                .append("CLASS_NAME", SaveEntityCommandFile.CLASS_NAME, false)
                .append("EXCEPTION", "Exception")
                .append("COULD_NOT_SAVE", ExceptionType.COULD_NOT_SAVE.getType())
                .append("LOGGER", FrameworkLibraryType.LOGGER.getType());

        final ModelFile modelFile = new ModelFile(data.getModuleName(), data.getModelName());
        final ResourceModelFile resourceModelFile =
                new ResourceModelFile(data.getModuleName(), data.getResourceModelName());

        final String modelType = modelFile.getClassFqn();
        final String modelFactoryType = modelType.concat("Factory");
        final String resourceType = resourceModelFile.getClassFqn();
        String dtoType;

        if (data.isDtoWithInterface()) {
            final DataModelInterfaceFile dataModelInterfaceFile =
                    new DataModelInterfaceFile(data.getModuleName(), data.getDtoInterfaceName());
            dtoType = dataModelInterfaceFile.getClassFqn();
        } else {
            final DataModelFile dataModelFile =
                    new DataModelFile(data.getModuleName(), data.getDtoName());
            dtoType = dataModelFile.getClassFqn();
        }
        typesBuilder.append("DTO", dtoType);
        typesBuilder.append(
                "ENTITY_ID_CONST",
                ClassPropertyFormatterUtil.formatNameToConstant(data.getEntityId(), dtoType),
                false
        );

        final String dtoProperty = CaseFormat.UPPER_CAMEL.to(
                CaseFormat.LOWER_CAMEL, data.getEntityName()
        );

        typesBuilder
                .append("DTO_PROPERTY", dtoProperty, false)
                .append("MODEL", modelType)
                .append("MODEL_FACTORY", modelFactoryType)
                .append("RESOURCE", resourceType);
    }
}
