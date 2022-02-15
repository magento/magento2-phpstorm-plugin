/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.context.EntityCreatorContext;
import com.magento.idea.magento2plugin.actions.generation.data.FormGenericButtonBlockData;
import com.magento.idea.magento2plugin.actions.generation.dialog.util.ClassPropertyFormatterUtil;
import com.magento.idea.magento2plugin.actions.generation.util.GenerationContextRegistry;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.FormGenericButtonBlockFile;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class FormGenericButtonBlockGenerator extends PhpFileGenerator {

    private final FormGenericButtonBlockData data;

    /**
     * Generic Button Block generator constructor.
     *
     * @param data FormGenericButtonBlockData
     * @param project Project
     */
    public FormGenericButtonBlockGenerator(
            final @NotNull FormGenericButtonBlockData data,
            final @NotNull Project project
    ) {
        this(data, project, true);
    }

    /**
     * Generic Button Block generator constructor.
     *
     * @param data FormGenericButtonBlockData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public FormGenericButtonBlockGenerator(
            final @NotNull FormGenericButtonBlockData data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project, checkFileAlreadyExists);
        this.data = data;
    }

    @Override
    protected AbstractPhpFile initFile() {
        return new FormGenericButtonBlockFile(data.getModuleName(), data.getEntityName());
    }

    /**
     * Fill file property attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        final String entityIdGetter = "get" + Arrays.stream(data.getEntityId().split("_"))
                .map(s -> s.substring(0, 1).toUpperCase(Locale.getDefault()) + s.substring(1))
                .collect(Collectors.joining());

        final EntityCreatorContext context =
                (EntityCreatorContext) GenerationContextRegistry.getInstance().getContext();

        if (context != null) {
            final String dtoTypeFqn = context.getUserData(EntityCreatorContext.DTO_TYPE);
            Objects.requireNonNull(dtoTypeFqn);
            typesBuilder.append(
                    "ENTITY_ID_REFERENCE",
                    ClassPropertyFormatterUtil.formatNameToConstant(data.getEntityId(), dtoTypeFqn),
                    false
            );
            typesBuilder.append("DTO_TYPE", dtoTypeFqn);
        }

        typesBuilder
                .append("NAMESPACE", file.getNamespace(), false)
                .append("ENTITY_NAME", data.getEntityName(), false)
                .append("CLASS_NAME", FormGenericButtonBlockFile.CLASS_NAME, false)
                .append("ENTITY_ID_GETTER", entityIdGetter, false)
                .append("CONTEXT", FormGenericButtonBlockFile.CONTEXT)
                .append("URL", FrameworkLibraryType.URL.getType());
    }
}
