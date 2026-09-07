/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class LayoutXmlData {
    private final String area;
    private final String route;
    private final String moduleName;
    private final String controllerName;
    private final String actionName;
    private final String uiComponentName;

    /**
     * Layout XML data.
     *
     * @param area String
     * @param route String
     * @param moduleName String
     * @param controllerName String
     * @param actionName String
     * @param uiComponentName String
     */
    public LayoutXmlData(
            final String area,
            final String route,
            final String moduleName,
            final String controllerName,
            final String actionName,
            final String uiComponentName
    ) {
        this.area = area;
        this.route = route;
        this.moduleName = moduleName;
        this.controllerName = controllerName;
        this.actionName = actionName;
        this.uiComponentName = uiComponentName;
    }

    /**
     * Layout XML data.
     *
     * @param area String
     * @param route String
     * @param moduleName String
     * @param controllerName String
     * @param actionName String
     */
    public LayoutXmlData(
            final String area,
            final String route,
            final String moduleName,
            final String controllerName,
            final String actionName
    ) {
        this.area = area;
        this.route = route;
        this.moduleName = moduleName;
        this.controllerName = controllerName;
        this.actionName = actionName;
        this.uiComponentName = "";
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

    public String getControllerName() {
        return controllerName;
    }

    public String getActionName() {
        return actionName;
    }

    public String getUiComponentName() {
        return uiComponentName;
    }
}
