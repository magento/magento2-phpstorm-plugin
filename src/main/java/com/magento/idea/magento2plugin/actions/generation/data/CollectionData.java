/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class CollectionData {

    private final String moduleName;
    private final String dbTableName;
    private final String modelName;
    private final String collectionName;
    private final String collectionDirectory;
    private final String resourceModelName;

    /**
     * Models Data.
     *
     * @param moduleName String
     * @param dbTableName String
     * @param modelName String
     * @param resourceModelName String
     * @param collectionName String
     * @param collectionDirectory String
     */
    public CollectionData(
            final String moduleName,
            final String dbTableName,
            final String modelName,
            final String resourceModelName,
            final String collectionName,
            final String collectionDirectory
    ) {
        this.moduleName = moduleName;
        this.dbTableName = dbTableName;
        this.modelName = modelName;
        this.collectionName = collectionName;
        this.collectionDirectory = collectionDirectory;
        this.resourceModelName = resourceModelName;
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
     * Resource Model Name.
     *
     * @return String
     */
    public String getResourceModelName() {
        return resourceModelName;
    }

    /**
     * Collection Name.
     *
     * @return String
     */
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * Collection Directory.
     *
     * @return String
     */
    public String getCollectionDirectory() {
        return collectionDirectory;
    }
}
