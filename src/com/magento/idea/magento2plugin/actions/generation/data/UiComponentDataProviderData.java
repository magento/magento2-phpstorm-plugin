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
        this(name, path, null, null);
    }

    /**
     * UiComponentGridDataProviderData constructor.
     *
     * @param name String
     * @param path String
     * @param entityName String
     * @param entityIdFieldName String
     */
    public UiComponentDataProviderData(
            final String name,
            final String path,
            final String entityName,
            final String entityIdFieldName
    ) {
        this.name = name;
        this.path = path;
        this.entityName = entityName;
        this.entityIdFieldName = entityIdFieldName;
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
}
