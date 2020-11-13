/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

@SuppressWarnings({"PMD.ExcessiveParameterList"})
public class CollectionData {
    private final String moduleName;
    private final String dbTableName;
    private final String modelName;
    private final String collectionName;
    private final String collectionFqn;
    private final String collectionDirectory;
    private final String collectionNamespace;
    private final String resourceModelName;
    private final String resourceModelFqn;
    private final String modelFqn;

    /**
     * Models Data.
     *
     * @param moduleName String
     * @param dbTableName String
     * @param modelName String
     * @param collectionName String
     * @param collectionFqn String
     * @param collectionDirectory String
     * @param resourceModelName String
     * @param resourceModelFqn String
     * @param modelFqn String
     */
    public CollectionData(
            final String moduleName,
            final String dbTableName,
            final String modelName,
            final String collectionName,
            final String collectionFqn,
            final String collectionDirectory,
            final String collectionNamespace,
            final String resourceModelName,
            final String resourceModelFqn,
            final String modelFqn
    ) {
        this.moduleName = moduleName;
        this.dbTableName = dbTableName;
        this.modelName = modelName;
        this.collectionName = collectionName;
        this.collectionFqn = collectionFqn;
        this.collectionDirectory = collectionDirectory;
        this.collectionNamespace = collectionNamespace;
        this.resourceModelName = resourceModelName;
        this.resourceModelFqn = resourceModelFqn;
        this.modelFqn = modelFqn;
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
     * Collection Name.
     *
     * @return String
     */
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * Collection FQN.
     *
     * @return String
     */
    public String getCollectionFqn() {
        return collectionFqn;
    }

    /**
     * Collection Directory.
     *
     * @return String
     */
    public String getCollectionDirectory() {
        return collectionDirectory;
    }

    /**
     * Collection Namespace.
     *
     * @return String
     */
    public String getCollectionNamespace() {
        return collectionNamespace;
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
     * Resource Model FQN.
     *
     * @return String
     */
    public String getResourceModelFqn() {
        return resourceModelFqn;
    }

    /**
     * Model FQN.
     *
     * @return String
     */
    public String getModelFqn() {
        return modelFqn;
    }
}
