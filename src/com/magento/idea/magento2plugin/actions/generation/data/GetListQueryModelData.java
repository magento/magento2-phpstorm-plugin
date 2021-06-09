/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import org.jetbrains.annotations.NotNull;

public class GetListQueryModelData {

    private final String moduleName;
    private final String entityName;
    private final String modelName;
    private final String collectionName;
    private final String aclResource;

    /**
     * Query Model DTO Constructor.
     *
     * @param moduleName String
     * @param entityName String
     * @param modelName String
     * @param collectionName String
     * @param aclResource String
     */
    public GetListQueryModelData(
            final @NotNull String moduleName,
            final @NotNull String entityName,
            final @NotNull String modelName,
            final @NotNull String collectionName,
            final @NotNull String aclResource
    ) {
        this.moduleName = moduleName;
        this.entityName = entityName;
        this.modelName = modelName;
        this.collectionName = collectionName;
        this.aclResource = aclResource;
    }

    /**
     * Get Query model module name.
     *
     * @return String
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Get entity name.
     *
     * @return String
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Get model name.
     *
     * @return String
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Get entity collection name.
     *
     * @return String
     */
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * Get entity acl resource.
     *
     * @return String
     */
    public String getAclResource() {
        return aclResource;
    }
}
