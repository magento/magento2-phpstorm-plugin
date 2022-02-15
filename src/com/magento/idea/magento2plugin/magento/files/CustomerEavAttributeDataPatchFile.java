/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import org.jetbrains.annotations.NotNull;

public class CustomerEavAttributeDataPatchFile extends AbstractPhpFile {
    public static final String HUMAN_READABLE_NAME = "Customer Eav Attribute Data Patch Class";
    public static final String TEMPLATE = "Magento Customer Eav Attribute Data Patch Class";
    public static final String DEFAULT_DIR = "Setup/Patch/Data";

    /**
     * Abstract php file constructor.
     *
     * @param moduleName String
     * @param className  String
     */
    public CustomerEavAttributeDataPatchFile(
            final @NotNull String moduleName,
            final @NotNull String className
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
