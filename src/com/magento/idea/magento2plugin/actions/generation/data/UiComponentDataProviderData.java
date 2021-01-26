/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

@SuppressWarnings({"PMD.DataClass"})
public class UiComponentDataProviderData {
    private final String name;
    private final String namespace;
    private final String path;
    private final String entityIdFieldName;

    /**
     * UiComponentGridDataProviderData constructor.
     *
     * @param name String
     * @param namespace String
     * @param path String
     */
    public UiComponentDataProviderData(
            final String name,
            final String namespace,
            final String path
    ) {
        this(name, namespace, path, null);
    }

    /**
     * UiComponentGridDataProviderData constructor.
     *
     * @param name String
     * @param namespace String
     * @param path String
     * @param entityIdFieldName String
     */
    public UiComponentDataProviderData(
            final String name,
            final String namespace,
            final String path,
            final String entityIdFieldName
    ) {
        this.name = name;
        this.namespace = namespace;
        this.path = path;
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
     * Get data provider class namespace.
     *
     * @return String
     */
    public String getNamespace() {
        return namespace;
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
     * Get entity id field name.
     *
     * @return String
     */
    public String getEntityIdFieldName() {
        return entityIdFieldName;
    }
}
