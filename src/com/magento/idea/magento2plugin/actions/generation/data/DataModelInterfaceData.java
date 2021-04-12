/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import org.jetbrains.annotations.NotNull;

public class DataModelInterfaceData {

    private final String name;
    private final String moduleName;
    private final String properties;

    /**
     * Data model interface constructor.
     *
     * @param name String
     * @param moduleName String
     * @param properties String
     */
    public DataModelInterfaceData(
            final @NotNull String name,
            final @NotNull String moduleName,
            final @NotNull String properties
    ) {
        this.name = name;
        this.moduleName = moduleName;
        this.properties = properties;
    }

    /**
     * Get data model interface name.
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Get module name.
     *
     * @return String
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Get properties.
     *
     * @return String
     */
    public String getProperties() {
        return properties;
    }
}
