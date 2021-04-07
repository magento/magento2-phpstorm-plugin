/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import org.jetbrains.annotations.NotNull;

public class DataModelFile extends AbstractPhpFile {

    public static final String TEMPLATE = "Magento Data Model";
    public static final String HUMAN_READABLE_NAME = "Data transfer object model";
    public static final String DIRECTORY = "Model/Data";
    public static final String DATA_OBJECT = "Magento\\Framework\\DataObject";

    /**
     * Data model generator file constructor.
     *
     * @param moduleName String
     * @param className String
     */
    public DataModelFile(
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
