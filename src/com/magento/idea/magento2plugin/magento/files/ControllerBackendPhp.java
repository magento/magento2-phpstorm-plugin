/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import org.jetbrains.annotations.NotNull;

public class ControllerBackendPhp extends AbstractPhpFile {

    public static final String TEMPLATE = "Magento Backend Controller Class";
    public static final String DEFAULT_DIR = "Controller/Adminhtml";

    public ControllerBackendPhp(
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
     * Get name of backend controller template.
     *
     * @return Name of backend controller template.
     */
    public String getTemplate() {
        return TEMPLATE;
    }
}
