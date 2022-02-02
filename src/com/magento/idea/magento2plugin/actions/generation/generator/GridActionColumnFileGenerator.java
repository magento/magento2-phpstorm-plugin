/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.context.EntityCreatorContext;
import com.magento.idea.magento2plugin.actions.generation.data.GridActionColumnData;
import com.magento.idea.magento2plugin.actions.generation.dialog.util.ClassPropertyFormatterUtil;
import com.magento.idea.magento2plugin.actions.generation.util.GenerationContextRegistry;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.GridActionColumnFile;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import java.util.Objects;
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
        return new GridActionColumnFile(data.getModuleName(), data.getEntityName());
    }

    /**
     * Fill index controller file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        typesBuilder
                .append("ENTITY_NAME", data.getEntityName(), false)
                .append("NAMESPACE", file.getNamespace(), false)
                .append("CLASS_NAME", file.getClassName(), false)
                .append("EDIT_URL_PATH", data.getEditUrlPath(), false)
                .append("DELETE_URL_PATH", data.getDeleteUrlPath(), false)
                .append("PARENT_CLASS", GridActionColumnFile.PARENT_CLASS)
                .append("URL", FrameworkLibraryType.URL.getType())
                .append("CONTEXT", GridActionColumnFile.CONTEXT)
                .append("UI_COMPONENT_FACTORY", GridActionColumnFile.UI_COMPONENT_FACTORY);

        final EntityCreatorContext context =
                (EntityCreatorContext) GenerationContextRegistry.getInstance().getContext();
        Objects.requireNonNull(context);
        final String dtoTypeFqn = context.getUserData(EntityCreatorContext.DTO_TYPE);
        Objects.requireNonNull(dtoTypeFqn);
        typesBuilder.append(
                "ENTITY_ID_REFERENCE",
                ClassPropertyFormatterUtil.formatNameToConstant(
                        data.getEntityIdColumn(),
                        dtoTypeFqn
                ),
                false
        );
        typesBuilder.append("DTO_TYPE", dtoTypeFqn);
    }
}
