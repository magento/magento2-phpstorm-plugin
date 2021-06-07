/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.data.ResourceModelData;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.ResourceModelFile;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class ModuleResourceModelGenerator extends PhpFileGenerator {

    private final ResourceModelData data;

    /**
     * Generates new Resource Model PHP Class based on provided data.
     *
     * @param data ResourceModelData
     * @param project Project
     */
    public ModuleResourceModelGenerator(
            final ResourceModelData data,
            final Project project
    ) {
        this(data, project, true);
    }

    /**
     * Generates new Resource Model PHP Class based on provided data.
     *
     * @param data ResourceModelData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public ModuleResourceModelGenerator(
            final ResourceModelData data,
            final Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project, checkFileAlreadyExists);
        this.data = data;
    }

    @Override
    protected AbstractPhpFile initFile() {
        return new ResourceModelFile(data.getModuleName(), data.getResourceModelName());
    }

    /**
     * Fill resource model file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        typesBuilder
                .append("NAME", data.getResourceModelName(), false)
                .append("NAMESPACE", file.getNamespace(), false)
                .append("DB_NAME", data.getDbTableName(), false)
                .append("ENTITY_ID_COLUMN", data.getEntityIdColumn(), false)
                .append("EXTENDS", ResourceModelFile.ABSTRACT_DB);
    }
}
