/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import org.jetbrains.annotations.NotNull;

public class ModelFile extends AbstractPhpFile {

    public static final String ABSTRACT_MODEL =
            "Magento\\Framework\\Model\\AbstractModel";
    public static final String MODEL_DIRECTORY = "Model";
    public static final String TEMPLATE = "Magento Model Class";
    public static final String HUMAN_READABLE_NAME = "Model class";
    public static final String ALIAS = "Model";

    /**
     * Model file constructor.
     *
     * @param moduleName String
     * @param className String
     */
    public ModelFile(
            final @NotNull String moduleName,
            final @NotNull String className
    ) {
        super(moduleName, className);
    }

    @Override
    public String getDirectory() {
        return MODEL_DIRECTORY;
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
