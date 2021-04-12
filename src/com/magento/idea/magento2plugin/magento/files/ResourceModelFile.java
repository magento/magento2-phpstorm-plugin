/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import org.jetbrains.annotations.NotNull;

public class ResourceModelFile extends AbstractPhpFile {

    public static final String RESOURCE_MODEL_DIRECTORY = "Model/ResourceModel";
    public static final String TEMPLATE = "Magento Resource Model Class";
    public static final String HUMAN_READABLE_NAME = "Resource model class";
    public static final String ABSTRACT_DB
            = "Magento\\Framework\\Model\\ResourceModel\\Db\\AbstractDb";
    public static final String ALIAS = "ResourceModel";

    /**
     * Resource model file constructor.
     *
     * @param moduleName String
     * @param className String
     */
    public ResourceModelFile(
            final @NotNull String moduleName,
            final @NotNull String className
    ) {
        super(moduleName, className);
    }

    @Override
    public String getDirectory() {
        return RESOURCE_MODEL_DIRECTORY;
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
