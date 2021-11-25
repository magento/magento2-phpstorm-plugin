/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import org.jetbrains.annotations.NotNull;

public class ControllerFrontendPhp extends AbstractPhpFile {

    public static final String TEMPLATE = "Magento Frontend Controller Class";
    public static final String DEFAULT_DIR = "Controller";

    public ControllerFrontendPhp(
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
        return null;
    }

    /**
     * Get name of frontend controller template.
     *
     * @return Name of frontend controller template.
     */
    public String getTemplate() {
        return TEMPLATE;
    }
}
