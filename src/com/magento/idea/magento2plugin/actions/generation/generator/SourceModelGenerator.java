/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.data.SourceModelData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.SourceModelFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class SourceModelGenerator extends PhpFileGenerator {
    private final SourceModelData data;

    /**
     * Constructor.
     *
     * @param project Project
     * @param data    SourceModelData
     */
    public SourceModelGenerator(
            @NotNull final SourceModelData data,
            @NotNull final Project project
    ) {
        this(data, project, true);
    }

    /**
     * Constructor.
     *
     * @param project                Project
     * @param data                   SourceModelData
     * @param checkFileAlreadyExists boolean
     */
    public SourceModelGenerator(
            @NotNull final SourceModelData data,
            @NotNull final Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project, checkFileAlreadyExists);
        this.data = data;
    }

    @Override
    protected AbstractPhpFile initFile() {
        return new SourceModelFile(
                data.getModuleName(),
                data.getClassName(),
                data.getDirectory()
        );
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        final String abstractSourceClass =
                "Magento\\Eav\\Model\\Entity\\Attribute\\Source\\AbstractSource";
        final List<String> uses = new ArrayList<>();
        uses.add(abstractSourceClass);

        attributes.setProperty("NAME", data.getClassName());
        attributes.setProperty("NAMESPACE", this.getFile().getNamespace());
        attributes.setProperty(
                "EXTENDS",
                PhpClassGeneratorUtil.getNameFromFqn(abstractSourceClass)
        );
        attributes.setProperty("USES", PhpClassGeneratorUtil.formatUses(uses));
    }
}
