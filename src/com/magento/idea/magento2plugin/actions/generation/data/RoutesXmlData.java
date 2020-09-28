/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class RoutesXmlData {

    private final String area;
    private final String route;
    private final String moduleName;

    /**
     * Routes XML Data.
     *
     * @param area String
     * @param route String
     * @param moduleName String
     */
    public RoutesXmlData(
            final String area,
            final String route,
            final String moduleName
    ) {
        this.area = area;
        this.route = route;
        this.moduleName = moduleName;
    }

    public String getArea() {
        return area;
    }

    public String getRoute() {
        return route;
    }

    public String getModuleName() {
        return moduleName;
    }
}
