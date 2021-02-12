/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages.database;

import java.util.LinkedList;
import java.util.List;

public enum TableResources {
    DEFAULT("default"),
    CHECKOUT("checkout"),
    SALES("sales");

    private final String resource;

    /**
     * Table Resources ENUM constructor.
     *
     * @param tableResource String
     */
    TableResources(final String tableResource) {
        resource = tableResource;
    }

    /**
     * Get table resource name.
     *
     * @return String
     */
    public String getResource() {
        return resource;
    }

    /**
     * Get table available/supported resources list.
     *
     * @return List of available resources.
     */
    public static List<String> getTableResourcesList() {
        final List<String> availableResources = new LinkedList<>();

        for (final TableResources resource : TableResources.values()) {
            availableResources.add(resource.getResource());
        }

        return availableResources;
    }
}
