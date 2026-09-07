/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files.actions;

import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import org.jetbrains.annotations.NotNull;

public final class IndexActionFile extends AbstractPhpFile {

    public static final String CLASS_NAME = "Index";
    public static final String HUMAN_READABLE_NAME = "Entity list controller class";
    public static final String TEMPLATE = "Magento Entity Index Adminhtml Controller Class";
    private static final String DIRECTORY = "Controller/Adminhtml";
    private final String entityName;

    /**
     * Index adminhtml controller for an entity.
     *
     * @param entityName String
     */
    public IndexActionFile(
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
