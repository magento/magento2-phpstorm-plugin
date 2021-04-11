/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import org.jetbrains.annotations.NotNull;

public class SourceModelFile extends AbstractPhpFile {
    public static final String HUMAN_READABLE_NAME = "Source model class";
    public static final String TEMPLATE = "Magento Source Model Class";
    public static final String DEFAULT_DIR = "Model/Source";

    /**
     * Constructor.
     *
     * @param className String
     */
    public SourceModelFile(
            @NotNull final String moduleName,
            @NotNull final String className
    ) {
        super(moduleName, className);
    }

    @Override
    public String getDirectory() {
        return DEFAULT_DIR;
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
