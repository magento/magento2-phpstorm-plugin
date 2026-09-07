/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import org.jetbrains.annotations.NotNull;

public class EntityDataMapperFile extends AbstractPhpFile {

    public static final String CLASS_NAME_SUFFIX = "DataMapper";
    public static final String HUMAN_READABLE_NAME = "Entity data mapper class";
    public static final String TEMPLATE = "Magento Entity Data Mapper";
    private static final String DIRECTORY = "Mapper";

    /**
     * Entity data mapper file constructor.
     *
     * @param moduleName String
     * @param entityName String
     */
    public EntityDataMapperFile(
            final @NotNull String moduleName,
            final @NotNull String entityName
    ) {
        super(moduleName, entityName.concat(CLASS_NAME_SUFFIX));
    }

    @Override
    public String getHumanReadableName() {
        return HUMAN_READABLE_NAME;
    }

    @Override
    public @NotNull String getDirectory() {
        return DIRECTORY;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }
}
