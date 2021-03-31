/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.google.common.base.CaseFormat;
import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.data.SaveEntityCommandData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
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
        final PhpClassTypesBuilder phpClassTypesBuilder = new PhpClassTypesBuilder();

        phpClassTypesBuilder
                .appendProperty("NAMESPACE", file.getNamespace())
                .appendProperty("ENTITY_NAME", data.getEntityName())
                .appendProperty("CLASS_NAME", SaveEntityCommandFile.CLASS_NAME)
                .append("EXCEPTION", "Exception")
                .append("DATA_OBJECT", FrameworkLibraryType.DATA_OBJECT.getType())
                .append("COULD_NOT_SAVE", ExceptionType.COULD_NOT_SAVE.getType())
                .append("LOGGER", FrameworkLibraryType.LOGGER.getType());

        final ModelFile modelFile = new ModelFile(data.getModuleName(), data.getModelName());
        final ResourceModelFile resourceModelFile =
                new ResourceModelFile(data.getModuleName(), data.getResourceModelName());

        final String modelType = modelFile.getClassFqn();
        final String modelFactoryType = modelType.concat("Factory");
        final String resourceType = resourceModelFile.getClassFqn();

        if (data.isDtoWithInterface()) {
            final DataModelInterfaceFile dataModelInterfaceFile =
                    new DataModelInterfaceFile(data.getDtoInterfaceName());
            final String dtoType = dataModelInterfaceFile
                    .getNamespaceBuilder(data.getModuleName()).getClassFqn();
            phpClassTypesBuilder.append("DTO", dtoType);
        } else {
            final DataModelFile dataModelFile = new DataModelFile(data.getDtoName());
            final String dtoType = dataModelFile
                    .getNamespaceBuilder(data.getModuleName()).getClassFqn();
            phpClassTypesBuilder.append("DTO", dtoType);
        }

        final String dtoProperty = CaseFormat.UPPER_CAMEL.to(
                CaseFormat.LOWER_CAMEL, data.getEntityName()
        );

        phpClassTypesBuilder
                .appendProperty("DTO_PROPERTY", dtoProperty)
                .append("MODEL", modelType)
                .append("MODEL_FACTORY", modelFactoryType)
                .append("RESOURCE", resourceType);

        phpClassTypesBuilder.mergeProperties(attributes);

        attributes.setProperty(
                "USES",
                PhpClassGeneratorUtil.formatUses(phpClassTypesBuilder.getUses())
        );
    }
}
