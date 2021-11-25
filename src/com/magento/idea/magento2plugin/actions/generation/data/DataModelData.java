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

    /**
     * Constructor.
     */
    public DataModelData(
            final String namespace,
            final String name,
            final String moduleName,
            final String fqn,
            final String interfaceFQN,
            final String properties
    ) {
        this.namespace = namespace;
        this.name = name;
        this.moduleName = moduleName;
        this.fqn = fqn;
        this.interfaceFQN = interfaceFQN;
        this.properties = properties;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getFQN() {
        return fqn;
    }

    public String getInterfaceFQN() {
        return interfaceFQN;
    }

    public String getProperties() {
        return properties;
    }
}
