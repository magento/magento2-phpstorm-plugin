/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import com.jetbrains.php.lang.psi.elements.PhpClass;

public class RoutesXmlData {
    private String area;
    private String routerId;
    private String route;
    private boolean isAdminhtml;

    /**
     * Routes XML Data.
     *
     * @param area String
     * @param routerId String
     * @param route String
     * @param isAdminhtml boolean
     */
    public RoutesXmlData(
            String area,
            String routerId,
            String route,
            boolean isAdminhtml
    ) {
        this.area = area;
        this.routerId = routerId;
        this.route = route;
        this.isAdminhtml = isAdminhtml;
    }

    public String getArea() {
        return area;
    }

    public String getRouterId() {
        return routerId;
    }

    public String getRoute() {
        return route;
    }

    public boolean isAdminhtml() {
        return isAdminhtml;
    }
}
