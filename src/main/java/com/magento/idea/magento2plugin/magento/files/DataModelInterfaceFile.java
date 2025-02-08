/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import org.jetbrains.annotations.NotNull;

public class DataModelInterfaceFile extends AbstractPhpFile {

    public static final String TEMPLATE = "Magento Data Model Interface";
    public static final String HUMAN_READABLE_NAME = "Data transfer object interface";
    public static final String DIRECTORY = "Api/Data";

    /**
     * DTO interface file constructor.
     *
     * @param moduleName String
     * @param className String
     */
    public DataModelInterfaceFile(
            final @NotNull String moduleName,
            final @NotNull String className
    ) {
        super(moduleName, className);
    }

    @Override
    public String getHumanReadableName() {
        return HUMAN_READABLE_NAME;
    }

    @Override
    public String getDirectory() {
        return DIRECTORY;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }
}
