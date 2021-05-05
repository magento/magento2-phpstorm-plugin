/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import org.jetbrains.annotations.NotNull;

public class WebApiInterfaceFile extends AbstractPhpFile {

    public static final String TEMPLATE = "Web API Interface";
    public static final String HUMAN_READABLE_NAME = "Web API Interface for service %serviceName";
    public static final String DEFAULT_METHOD_DESCRIPTION = "TODO: need to describe this method.";
    public static final String METHODS_DELIMITER = "METHODS_DELIMITER";
    private static final String DIRECTORY = "Api";

    /**
     * Web API service interface file constructor.
     *
     * @param moduleName String
     * @param className String
     */
    public WebApiInterfaceFile(
            final @NotNull String moduleName,
            final @NotNull String className
    ) {
        super(moduleName, className);
    }

    @Override
    public String getDirectory() {
        return DIRECTORY;
    }

    @Override
    public String getHumanReadableName() {
        return HUMAN_READABLE_NAME.replace("%serviceName", getClassName());
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }
}
