/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.data.DataModelData;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.DataModelFile;
import com.magento.idea.magento2plugin.magento.files.DataModelInterfaceFile;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class DataModelGenerator extends PhpFileGenerator {

    private final DataModelData data;

    /**
     * Data model generator constructor.
     *
     * @param project Project
     * @param data DataModelData
     */
    public DataModelGenerator(
            final @NotNull Project project,
            final @NotNull DataModelData data
    ) {
        this(project, data, true);
    }

    /**
     * Data model generator constructor.
     *
     * @param project Project
     * @param data DataModelData
     * @param checkFileAlreadyExists boolean
     */
    public DataModelGenerator(
            final @NotNull Project project,
            final @NotNull DataModelData data,
            final boolean checkFileAlreadyExists
    ) {
        super(project, checkFileAlreadyExists);
        this.data = data;
    }

    @Override
    protected AbstractPhpFile initFile() {
        return new DataModelFile(data.getModuleName(), data.getName());
    }

    /**
     * Fill data model file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        typesBuilder
                .append("NAMESPACE", file.getNamespace(), false)
                .append("NAME", data.getName(), false)
                .append("PROPERTIES", data.getProperties(), false)
                .append("HASINTERFACE", Boolean.toString(data.hasInterface()), false)
                .append("EXTENDS", DataModelFile.DATA_OBJECT);

        if (data.hasInterface()) {
            typesBuilder.append(
                    "IMPLEMENTS",
                    new DataModelInterfaceFile(
                            data.getModuleName(),
                            data.getInterfaceName()
                    ).getClassFqn());
        }
    }
}
