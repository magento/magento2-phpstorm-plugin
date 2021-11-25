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

    /**
     * Model Data.
     *
     * @param moduleName String
     * @param dbTableName String
     * @param modelName String
     * @param resourceName String
     */
    public ModelData(
            final String moduleName,
            final String dbTableName,
            final String modelName,
            final String resourceName
    ) {
        this.moduleName = moduleName;
        this.dbTableName = dbTableName;
        this.modelName = modelName;
        this.resourceName = resourceName;
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
}
