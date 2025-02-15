/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.context.EntityCreatorContext;
import com.magento.idea.magento2plugin.actions.generation.data.DeleteEntityControllerFileData;
import com.magento.idea.magento2plugin.actions.generation.dialog.util.ClassPropertyFormatterUtil;
import com.magento.idea.magento2plugin.actions.generation.util.GenerationContextRegistry;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.actions.DeleteActionFile;
import com.magento.idea.magento2plugin.magento.files.commands.DeleteEntityByIdCommandFile;
import com.magento.idea.magento2plugin.magento.packages.HttpMethod;
import com.magento.idea.magento2plugin.magento.packages.code.BackendModuleType;
import com.magento.idea.magento2plugin.magento.packages.code.ExceptionType;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import java.util.Objects;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class DeleteEntityControllerFileGenerator extends PhpFileGenerator {

    private final DeleteEntityControllerFileData data;

    /**
     * Delete Entity Controller File Generator.
     *
     * @param data DeleteEntityControllerFileData
     * @param project Project
     */
    public DeleteEntityControllerFileGenerator(
            final @NotNull DeleteEntityControllerFileData data,
            final @NotNull Project project
    ) {
        this(data, project, true);
    }

    /**
     * Delete Entity Controller File Generator.
     *
     * @param data DeleteEntityControllerFileData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public DeleteEntityControllerFileGenerator(
            final @NotNull DeleteEntityControllerFileData data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project, checkFileAlreadyExists);
        this.data = data;
    }

    @Override
    protected AbstractPhpFile initFile() {
        return new DeleteActionFile(data.getModuleName(), data.getEntityName());
    }

    /**
     * Fill delete action file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        typesBuilder
                .append("NAMESPACE", file.getNamespace(), false)
                .append("ENTITY_NAME", data.getEntityName(), false)
                .append("CLASS_NAME", DeleteActionFile.CLASS_NAME, false)
                .append("ADMIN_RESOURCE", data.getAcl(), false)
                .append("DELETE_COMMAND",
                        new DeleteEntityByIdCommandFile(
                                data.getModuleName(),
                                data.getEntityName(),
                                data.isHasDeleteCommandInterface()
                        ).getClassFqn()
                )
                .append("CONTEXT", BackendModuleType.CONTEXT.getType())
                .append("RESULT_FACTORY", FrameworkLibraryType.RESULT_FACTORY.getType())
                .append("RESULT_INTERFACE", FrameworkLibraryType.RESULT_INTERFACE.getType())
                .append("EXTENDS", BackendModuleType.EXTENDS.getType())
                .append("IMPLEMENTS_POST", HttpMethod.POST.getInterfaceFqn())
                .append("IMPLEMENTS_GET", HttpMethod.GET.getInterfaceFqn())
                .append("NO_SUCH_ENTITY_EXCEPTION",
                        ExceptionType.NO_SUCH_ENTITY_EXCEPTION.getType())
                .append("COULD_NOT_DELETE", ExceptionType.COULD_NOT_DELETE.getType());

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
    }
}
