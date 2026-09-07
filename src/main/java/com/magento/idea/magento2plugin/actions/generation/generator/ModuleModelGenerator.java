/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.data.ModelData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.ModelFile;
import com.magento.idea.magento2plugin.magento.files.ResourceModelFile;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class ModuleModelGenerator extends PhpFileGenerator {

    private final ModelData data;

    /**
     * Generates new Model PHP Class based on provided data.
     *
     * @param data ModelData
     * @param project Project
     */
    public ModuleModelGenerator(
            final @NotNull ModelData data,
            final @NotNull Project project
    ) {
        this(data, project, true);
    }

    /**
     * Generates new Model PHP Class based on provided data.
     *
     * @param data ModelData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public ModuleModelGenerator(
            final @NotNull ModelData data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project, checkFileAlreadyExists);
        this.data = data;
    }

    @Override
    protected AbstractPhpFile initFile() {
        return new ModelFile(data.getModuleName(), data.getModelName());
    }

    /**
     * Fill model file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        final ResourceModelFile resourceModelFile =
                new ResourceModelFile(data.getModuleName(), data.getResourceName());

        typesBuilder
                .append("NAME", data.getModelName(), false)
                .append("NAMESPACE", file.getNamespace(), false)
                .append("DB_NAME", data.getDbTableName(), false)
                .append("RESOURCE_MODEL", resourceModelFile.getClassFqn(), ResourceModelFile.ALIAS)
                .append(
                        "EXTENDS",
                        PhpClassGeneratorUtil.getNameFromFqn(ModelFile.ABSTRACT_MODEL),
                        false
                )
                .append("ABSTRACT_MODEL", ModelFile.ABSTRACT_MODEL);
    }
}
