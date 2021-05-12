/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.data.DeleteEntityControllerFileData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.actions.DeleteActionFile;
import com.magento.idea.magento2plugin.magento.files.commands.DeleteEntityByIdCommandFile;
import com.magento.idea.magento2plugin.magento.packages.HttpMethod;
import com.magento.idea.magento2plugin.magento.packages.code.BackendModuleType;
import com.magento.idea.magento2plugin.magento.packages.code.ExceptionType;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
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
        final PhpClassTypesBuilder phpClassTypesBuilder = new PhpClassTypesBuilder();

        phpClassTypesBuilder
                .appendProperty("NAMESPACE", file.getNamespace())
                .appendProperty("ENTITY_NAME", data.getEntityName())
                .appendProperty("CLASS_NAME", DeleteActionFile.CLASS_NAME)
                .appendProperty("ADMIN_RESOURCE", data.getAcl())
                .appendProperty("ENTITY_ID", data.getEntityId())
                .append("DELETE_COMMAND",
                        new DeleteEntityByIdCommandFile(
                                data.getModuleName(),
                                data.getEntityName(),
                                data.isDeleteCommandHasInterface()
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
                .append("COULD_NOT_DELETE", ExceptionType.COULD_NOT_DELETE.getType())
                .mergeProperties(attributes);

        attributes.setProperty("USES",
                PhpClassGeneratorUtil.formatUses(phpClassTypesBuilder.getUses()));
    }
}
