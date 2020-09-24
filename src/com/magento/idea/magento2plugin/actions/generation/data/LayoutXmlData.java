/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class LayoutXmlData {
    private String area;
    private String route;
    private String moduleName;
    private String controllerName;
    private String actionName;
    private String formName;

    /**
     * Routes XML Data.
     *
     * @param area String
     * @param route String
     */
    public LayoutXmlData(
            String area,
            String route,
            String moduleName,
            String controllerName,
            String actionName,
            String formName
    ) {
        this.area = area;
        this.route = route;
        this.moduleName = moduleName;
        this.controllerName = controllerName;
        this.actionName = actionName;
        this.formName = formName;
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

    public String getFormName() {
        return formName;
    }
}
