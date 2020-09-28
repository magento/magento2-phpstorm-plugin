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
    private final String formName;

    /**
     * Layout XML data.
     *
     * @param area String
     * @param route String
     * @param moduleName String
     * @param controllerName String
     * @param actionName String
     * @param formName String
     */
    public LayoutXmlData(
            final String area,
            final String route,
            final String moduleName,
            final String controllerName,
            final String actionName,
            final String formName
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
