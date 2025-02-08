/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.data.DataModelInterfaceData;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.DataModelInterfaceFile;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class DataModelInterfaceGenerator extends PhpFileGenerator {

    private final DataModelInterfaceData data;

    /**
     * DTO interface generator constructor.
     *
     * @param data DataModelInterfaceData
     * @param project Project
     */
    public DataModelInterfaceGenerator(
            final @NotNull DataModelInterfaceData data,
            final @NotNull Project project
    ) {
        this(data, project, true);
    }

    /**
     * DTO interface generator constructor.
     *
     * @param data DataModelInterfaceData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public DataModelInterfaceGenerator(
            final @NotNull DataModelInterfaceData data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project, checkFileAlreadyExists);
        this.data = data;
    }

    @Override
    protected AbstractPhpFile initFile() {
        return new DataModelInterfaceFile(data.getModuleName(), data.getName());
    }

    /**
     * Fill DTO interface file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        attributes.setProperty("NAME", data.getName());
        attributes.setProperty("NAMESPACE", file.getNamespace());
        attributes.setProperty("PROPERTIES", data.getProperties());
    }
}
