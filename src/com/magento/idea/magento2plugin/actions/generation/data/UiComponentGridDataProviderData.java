/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

@SuppressWarnings({"PMD.DataClass"})
public class UiComponentGridDataProviderData {
    private final String type;
    private final String name;
    private final String namespace;
    private final String path;
    private final String collectionFqn;

    /**
     * UiComponentGridDataProviderData constructor.
     *
     * @param type String
     * @param name String
     * @param namespace String
     * @param path String
     * @param collectionFqn String
     */
    public UiComponentGridDataProviderData(
            final String type,
            final String name,
            final String namespace,
            final String path,
            final String collectionFqn
    ) {
        this.type = type;
        this.name = name;
        this.namespace = namespace;
        this.path = path;
        this.collectionFqn = collectionFqn;
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
     * Get collection FQN.
     *
     * @return String
     */
    public String getCollectionFqn() {
        return collectionFqn;
    }

    /**
     * Get type of data provider.
     *
     * @return String
     */
    public String getType() {
        return type;
    }

    /**
     * Get path.
     *
     * @return String
     */
    public String getPath() {
        return path;
    }
}
