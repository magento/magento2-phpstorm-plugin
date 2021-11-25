/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class ModelData {
    private final String moduleName;
    private final String dbTableName;
    private final String modelName;
    private final String resourceName;
    private final String fqn;
    private final String namespace;
    private final String resourceModelFqn;

    /**
     * Model Data.
     *
     * @param moduleName String
     * @param dbTableName String
     * @param modelName String
     * @param resourceName String
     * @param fqn String
     * @param namespace String
     * @param resourceModelFqn String
     */
    public ModelData(
            final String moduleName,
            final String dbTableName,
            final String modelName,
            final String resourceName,
            final String fqn,
            final String namespace,
            final String resourceModelFqn
    ) {
        this.moduleName = moduleName;
        this.dbTableName = dbTableName;
        this.modelName = modelName;
        this.resourceName = resourceName;
        this.fqn = fqn;
        this.namespace = namespace;
        this.resourceModelFqn = resourceModelFqn;
    }

    /**
     * Module Name.
     *
     * @return String
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * DB table Name.
     *
     * @return String
     */
    public String getDbTableName() {
        return dbTableName;
    }

    /**
     * Model Name.
     *
     * @return String
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Resource Name.
     *
     * @return String
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Class FQN.
     *
     * @return String
     */
    public String getFqn() {
        return fqn;
    }

    /**
     * Class Namespace.
     *
     * @return String
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Resource model FQN.
     *
     * @return String
     */
    public String getResourceModelFqn() {
        return resourceModelFqn;
    }
}
