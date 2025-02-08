/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.data.NewActionEntityControllerFileData;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.actions.NewActionFile;
import com.magento.idea.magento2plugin.magento.packages.HttpMethod;
import com.magento.idea.magento2plugin.magento.packages.code.BackendModuleType;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class NewActionEntityControllerFileGenerator extends PhpFileGenerator {

    private final NewActionEntityControllerFileData data;

    /**
     * NewAction Entity Controller File Generator.
     *
     * @param data NewActionEntityControllerFileData
     * @param project Project
     */
    public NewActionEntityControllerFileGenerator(
            final NewActionEntityControllerFileData data,
            final @NotNull Project project
    ) {
        super(project, true);
        this.data = data;
    }

    /**
     * NewAction Entity Controller File Generator.
     *
     * @param data NewActionEntityControllerFileData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public NewActionEntityControllerFileGenerator(
            final NewActionEntityControllerFileData data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project, checkFileAlreadyExists);
        this.data = data;
    }

    @Override
    protected AbstractPhpFile initFile() {
        return new NewActionFile(data.getModuleName(), data.getEntityName());
    }

    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        typesBuilder
                .append("NAMESPACE", data.getNamespace(), false)
                .append("ENTITY_NAME", data.getEntityName(), false)
                .append("CLASS_NAME", file.getClassName(), false)
                .append("ADMIN_RESOURCE", data.getAcl(), false)
                .append("MENU_IDENTIFIER", data.getMenu(), false)
                .append("EXTENDS", BackendModuleType.EXTENDS.getType())
                .append("IMPLEMENTS", HttpMethod.GET.getInterfaceFqn())
                .append("RESULT_INTERFACE", FrameworkLibraryType.RESULT_INTERFACE.getType())
                .append("RESULT_FACTORY", FrameworkLibraryType.RESULT_FACTORY.getType())
                .append("RESULT_PAGE", BackendModuleType.RESULT_PAGE.getType());
    }
}
