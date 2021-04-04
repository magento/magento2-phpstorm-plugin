/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.data.GridActionColumnData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.GridActionColumnFile;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class GridActionColumnFileGenerator extends PhpFileGenerator {

    private final GridActionColumnData data;

    /**
     * Constructor for grid ui component action column class generator.
     *
     * @param data GridActionColumnData
     * @param project Project
     */
    public GridActionColumnFileGenerator(
            final @NotNull GridActionColumnData data,
            final @NotNull Project project
    ) {
        this(data, project, true);
    }

    /**
     * Constructor for grid ui component action column class generator.
     *
     * @param data GridActionColumnData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public GridActionColumnFileGenerator(
            final @NotNull GridActionColumnData data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project, checkFileAlreadyExists);
        this.data = data;
    }

    @Override
    protected AbstractPhpFile initFile() {
        return new GridActionColumnFile(data.getModuleName());
    }

    /**
     * Fill index controller file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        final PhpClassTypesBuilder phpClassTypesBuilder = new PhpClassTypesBuilder();

        phpClassTypesBuilder
                .appendProperty("ENTITY_NAME", data.getEntityName())
                .appendProperty("ENTITY_ID", data.getEntityIdColumn())
                .appendProperty("NAMESPACE", file.getNamespace())
                .appendProperty("CLASS_NAME", file.getClassName())
                .appendProperty("EDIT_URL_PATH", data.getEditUrlPath())
                .appendProperty("DELETE_URL_PATH", data.getDeleteUrlPath())
                .append("PARENT_CLASS", GridActionColumnFile.PARENT_CLASS)
                .append("URL", FrameworkLibraryType.URL.getType())
                .append("CONTEXT", GridActionColumnFile.CONTEXT)
                .append("UI_COMPONENT_FACTORY", GridActionColumnFile.UI_COMPONENT_FACTORY)
                .mergeProperties(attributes);

        attributes.setProperty("USES",
                PhpClassGeneratorUtil.formatUses(phpClassTypesBuilder.getUses()));
    }
}
