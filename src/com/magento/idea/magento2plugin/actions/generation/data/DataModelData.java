/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class DataModelData {
    private final String namespace;
    private final String name;
    private final String moduleName;
    private final String fqn;
    private final String interfaceFQN;
    private final String properties;
    private final boolean withInterface;

    /**
     * Constructor.
     *
     * @param namespace String
     * @param name String
     * @param moduleName String
     * @param fqn String
     * @param interfaceFQN String
     * @param properties String
     * @param hasInterface boolean
     */
    public DataModelData(
            final String namespace,
            final String name,
            final String moduleName,
            final String fqn,
            final String interfaceFQN,
            final String properties,
            final boolean hasInterface
    ) {
        this.namespace = namespace;
        this.name = name;
        this.moduleName = moduleName;
        this.fqn = fqn;
        this.interfaceFQN = interfaceFQN;
        this.properties = properties;
        this.withInterface = hasInterface;
    }

    /**
     * Get Namespace.
     *
     * @return String
     */
    public String getNamespace() {
        return namespace;
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
     * Get module name.
     *
     * @return String
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Get FQN.
     *
     * @return String
     */
    public String getFQN() {
        return fqn;
    }

    /**
     * Get Interface FQN.
     *
     * @return String
     */
    public String getInterfaceFQN() {
        return interfaceFQN;
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
