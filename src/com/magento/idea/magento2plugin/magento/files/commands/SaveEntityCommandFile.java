/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files.commands;

import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.ModuleFileInterface;
import org.jetbrains.annotations.NotNull;

public class SaveEntityCommandFile extends AbstractPhpFile implements ModuleFileInterface {

    public static final String CLASS_NAME = "SaveCommand";
    public static final String TEMPLATE = "Magento Save Entity Command Model";
    public static final String HUMAN_READABLE_NAME = "Save entity command model";
    private static final String DIRECTORY = "Command";
    private final String entityName;

    /**
     * Save entity command file constructor.
     *
     * @param moduleName String
     * @param entityName String
     */
    public SaveEntityCommandFile(
            final @NotNull String moduleName,
            final @NotNull String entityName
    ) {
        super(moduleName, CLASS_NAME);
        this.entityName = entityName;
    }

    /**
     * Get directory for save command file.
     *
     * @return String
     */
    @Override
    public String getDirectory() {
        return DIRECTORY.concat("/" + entityName);
    }

    @Override
    public String getHumanReadableName() {
        return HUMAN_READABLE_NAME;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }
}
