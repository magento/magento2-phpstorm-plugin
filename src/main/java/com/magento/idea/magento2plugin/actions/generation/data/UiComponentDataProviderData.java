/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class UiComponentDataProviderData {

    private final String name;
    private final String path;
    private final String entityName;
    private final String entityIdFieldName;
    private final boolean hasQueryInterface;

    /**
     * UiComponentGridDataProviderData constructor.
     *
     * @param name String
     * @param path String
     */
    public UiComponentDataProviderData(
            final String name,
            final String path
    ) {
        this(name, path, null, null, false);
    }

    /**
     * UiComponentGridDataProviderData constructor.
     *
     * @param name String
     * @param path String
     * @param entityName String
     * @param entityIdFieldName String
     * @param hasQueryInterface boolean
     */
    public UiComponentDataProviderData(
            final String name,
            final String path,
            final String entityName,
            final String entityIdFieldName,
            final boolean hasQueryInterface
    ) {
        this.name = name;
        this.path = path;
        this.entityName = entityName;
        this.entityIdFieldName = entityIdFieldName;
        this.hasQueryInterface = hasQueryInterface;
    }

    /**
     * Get data provider class name.
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Get path.
     *
     * @return String
     */
    public String getPath() {
        return path;
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
     * Get entity id field name.
     *
     * @return String
     */
    public String getEntityIdFieldName() {
        return entityIdFieldName;
    }

    /**
     * Check if query service has Web API interface.
     *
     * @return boolean
     */
    public boolean isHasQueryInterface() {
        return hasQueryInterface;
    }
}
