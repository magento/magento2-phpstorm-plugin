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
        this.name = name;
        this.namespace = namespace;
        this.path = path;
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
}
