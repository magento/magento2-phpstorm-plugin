/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class ResourceModelData {
    private final String moduleName;
    private final String dbTableName;
    private final String resourceModelName;
    private final String entityIdColumn;
    private final String namespace;
    private final String fqn;

    /**
     * Resource Model Data.
     *
     * @param moduleName String
     * @param dbTableName String
     * @param resourceModelName String
     * @param entityIdColumn String
     * @param namespace String
     * @param fqn String
     */
    public ResourceModelData(
            final String moduleName,
            final String dbTableName,
            final String resourceModelName,
            final String entityIdColumn,
            final String namespace,
            final String fqn
    ) {
        this.moduleName = moduleName;
        this.dbTableName = dbTableName;
        this.resourceModelName = resourceModelName;
        this.entityIdColumn = entityIdColumn;
        this.namespace = namespace;
        this.fqn = fqn;
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
     * Entity ID column.
     *
     * @return String
     */
    public String getEntityIdColumn() {
        return entityIdColumn;
    }

    /**
     * Resource Model name.
     *
     * @return String
     */
    public String getResourceModelName() {
        return resourceModelName;
    }

    /**
     * Namespace getter.
     *
     * @return String.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * FQN getter.
     *
     * @return String.
     */
    public String getFqn() {
        return fqn;
    }
}
