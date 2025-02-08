/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.data.SaveEntityControllerFileData;
import com.magento.idea.magento2plugin.actions.generation.dialog.util.ClassPropertyFormatterUtil;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.DataModelFile;
import com.magento.idea.magento2plugin.magento.files.DataModelInterfaceFile;
import com.magento.idea.magento2plugin.magento.files.actions.SaveActionFile;
import com.magento.idea.magento2plugin.magento.files.commands.SaveEntityCommandFile;
import com.magento.idea.magento2plugin.magento.packages.HttpMethod;
import com.magento.idea.magento2plugin.magento.packages.code.BackendModuleType;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class SaveEntityControllerFileGenerator extends PhpFileGenerator {

    private final SaveEntityControllerFileData data;

    /**
     * Save Entity Controller File Generator.
     *
     * @param data SaveEntityControllerFileData
     * @param project Project
     */
    public SaveEntityControllerFileGenerator(
            final @NotNull SaveEntityControllerFileData data,
            final @NotNull Project project
    ) {
        this(data, project, true);
    }

    /**
     * Save Entity Controller File Generator.
     *
     * @param data SaveEntityControllerFileData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public SaveEntityControllerFileGenerator(
            final @NotNull SaveEntityControllerFileData data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project, checkFileAlreadyExists);
        this.data = data;
    }

    @Override
    protected AbstractPhpFile initFile() {
        return new SaveActionFile(data.getModuleName(), data.getEntityName());
    }

    /**
     * Fill save action file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        String dtoType;

        if (data.isHasDtoInterface()) {
            final DataModelInterfaceFile dataModelInterfaceFile =
                    new DataModelInterfaceFile(data.getModuleName(), data.getDtoInterfaceName());
            dtoType = dataModelInterfaceFile.getClassFqn();
        } else {
            final DataModelFile dataModelFile =
                    new DataModelFile(data.getModuleName(), data.getDtoName());
            dtoType = dataModelFile.getClassFqn();
        }
        typesBuilder.append(
                "ENTITY_ID_REFERENCE",
                ClassPropertyFormatterUtil.formatNameToConstant(data.getEntityId(), dtoType),
                false
        );

        typesBuilder
                .append("NAMESPACE", file.getNamespace(), false)
                .append("ENTITY_NAME", data.getEntityName(), false)
                .append("CLASS_NAME", SaveActionFile.CLASS_NAME, false)
                .append("ADMIN_RESOURCE", data.getAcl(), false)
                .append("IMPLEMENTS", HttpMethod.POST.getInterfaceFqn())
                .append("DATA_PERSISTOR", FrameworkLibraryType.DATA_PERSISTOR.getType())
                .append("EXTENDS", BackendModuleType.EXTENDS.getType())
                .append("ENTITY_DTO", dtoType)
                .append("ENTITY_DTO_FACTORY", dtoType.concat("Factory"))
                .append("SAVE_COMMAND",
                        new SaveEntityCommandFile(
                                data.getModuleName(),
                                data.getEntityName(),
                                data.isHasSaveCommandInterface()
                        ).getClassFqn()
                )
                .append("DATA_OBJECT", FrameworkLibraryType.DATA_OBJECT.getType())
                .append("COULD_NOT_SAVE", SaveActionFile.COULD_NOT_SAVE)
                .append("CONTEXT", BackendModuleType.CONTEXT.getType())
                .append("RESPONSE_INTERFACE", FrameworkLibraryType.RESPONSE_INTERFACE.getType())
                .append("RESULT_INTERFACE", FrameworkLibraryType.RESULT_INTERFACE.getType());
    }
}
