/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files.commands;

import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import org.jetbrains.annotations.NotNull;

public class DeleteEntityByIdCommandFile extends AbstractPhpFile {

    public static final String CLASS_NAME = "DeleteByIdCommand";
    public static final String HUMAN_READABLE_NAME = "Delete entity by id command class";
    public static final String TEMPLATE = "Magento Delete Entity By Id Command";
    private static final String DIRECTORY = "Command";
    private final String entityName;

    /**
     * Delete entity by id command file constructor.
     *
     * @param moduleName String
     * @param entityName String
     */
    public DeleteEntityByIdCommandFile(
            final @NotNull String moduleName,
            final @NotNull String entityName
    ) {
        super(moduleName, CLASS_NAME);
        this.entityName = entityName;
    }

    @Override
    public String getHumanReadableName() {
        return HUMAN_READABLE_NAME;
    }

    @Override
    public String getDirectory() {
        return DIRECTORY.concat("/" + entityName);
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }
}
