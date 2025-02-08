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

    /**
     * Resource Model Data.
     *
     * @param moduleName String
     * @param dbTableName String
     * @param resourceModelName String
     * @param entityIdColumn String
     */
    public ResourceModelData(
            final String moduleName,
            final String dbTableName,
            final String resourceModelName,
            final String entityIdColumn
    ) {
        this.moduleName = moduleName;
        this.dbTableName = dbTableName;
        this.resourceModelName = resourceModelName;
        this.entityIdColumn = entityIdColumn;
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
}
