/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.data.ResourceModelData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.ResourceModelFile;
import java.util.List;
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
        final PhpClassTypesBuilder phpClassTypesBuilder = new PhpClassTypesBuilder();

        phpClassTypesBuilder
                .appendProperty("NAME", data.getResourceModelName())
                .appendProperty(
                        "NAMESPACE",
                        file.getNamespace()
                )
                .appendProperty("DB_NAME", data.getDbTableName())
                .appendProperty("ENTITY_ID_COLUMN", data.getEntityIdColumn())
                .append("EXTENDS", ResourceModelFile.ABSTRACT_DB)
                .mergeProperties(attributes);

        final List<String> uses = phpClassTypesBuilder.getUses();
        attributes.setProperty("USES", PhpClassGeneratorUtil.formatUses(uses));
    }
}
