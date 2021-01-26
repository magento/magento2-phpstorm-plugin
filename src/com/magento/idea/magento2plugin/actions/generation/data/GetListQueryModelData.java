/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import org.jetbrains.annotations.NotNull;

public class GetListQueryModelData {
    private final String moduleName;
    private final String entityName;
    private final String collectionType;
    private final String collectionTypeFactory;
    private final String entityDataMapperType;

    /**
     * Query Model DTO Constructor.
     *
     * @param moduleName String
     * @param entityName String
     * @param collectionType String
     * @param entityDataMapperType String
     */
    public GetListQueryModelData(
            final @NotNull String moduleName,
            final @NotNull String entityName,
            final @NotNull String collectionType,
            final @NotNull String entityDataMapperType
    ) {
        this.moduleName = moduleName;
        this.entityName = entityName;
        this.collectionType = collectionType;
        this.collectionTypeFactory = collectionType.concat("Factory");
        this.entityDataMapperType = entityDataMapperType;
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
     * Get entity collection type.
     *
     * @return String
     */
    public String getCollectionType() {
        return collectionType;
    }

    /**
     * Get entity collection type factory.
     *
     * @return String
     */
    public String getCollectionTypeFactory() {
        return collectionTypeFactory;
    }

    /**
     * Get entity data mapper type.
     *
     * @return String
     */
    public String getEntityDataMapperType() {
        return entityDataMapperType;
    }
}
