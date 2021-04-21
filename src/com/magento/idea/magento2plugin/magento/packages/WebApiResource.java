/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages;

import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public enum WebApiResource {

    SELF("self"),
    ANONYMOUS("anonymous");

    private final String resource;

    /**
     * Web API resource ENUM constructor.
     *
     * @param resource String
     */
    WebApiResource(final @NotNull String resource) {
        this.resource = resource;
    }

    /**
     * Get Web API resource name.
     *
     * @return String
     */
    public String getResource() {
        return resource;
    }

    /**
     * Get default Web API resources.
     *
     * @return List[String]
     */
    public static List<String> getDefaultResourcesList() {
        final List<String> resources = new LinkedList<>();

        for (final WebApiResource resource : WebApiResource.values()) {
            resources.add(resource.getResource());
        }

        return resources;
    }
}
