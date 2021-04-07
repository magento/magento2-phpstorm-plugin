/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files.actions;

import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import org.jetbrains.annotations.NotNull;

public final class SaveActionFile extends AbstractPhpFile {

    public static final String CLASS_NAME = "Save";
    public static final String HUMAN_READABLE_NAME = "Entity save controller class";
    public static final String TEMPLATE = "Magento Entity Save Controller Class";
    private static final String DIRECTORY = "Controller/Adminhtml";
    public static final String COULD_NOT_SAVE =
            "Magento\\Framework\\Exception\\CouldNotSaveException";
    private final String entityName;

    /**
     * Save action file controller.
     *
     * @param moduleName String
     * @param entityName String
     */
    public SaveActionFile(
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
