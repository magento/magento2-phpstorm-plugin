/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class DataModelData {

    private final String name;
    private final String interfaceName;
    private final String moduleName;
    private final String properties;
    private final boolean withInterface;

    /**
     * Constructor.
     *
     * @param name String
     * @param interfaceName String
     * @param moduleName String
     * @param properties String
     * @param hasInterface boolean
     */
    public DataModelData(
            final String name,
            final String interfaceName,
            final String moduleName,
            final String properties,
            final boolean hasInterface
    ) {
        this.name = name;
        this.interfaceName = interfaceName;
        this.moduleName = moduleName;
        this.properties = properties;
        this.withInterface = hasInterface;
    }

    /**
     * Get Name.
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Get interface name.
     *
     * @return String
     */
    public String getInterfaceName() {
        return interfaceName;
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
     * Get Properties.
     *
     * @return String
     */
    public String getProperties() {
        return properties;
    }

    /**
     * Check if model has interface.
     *
     * @return boolean
     */
    public boolean hasInterface() {
        return withInterface;
    }
}
