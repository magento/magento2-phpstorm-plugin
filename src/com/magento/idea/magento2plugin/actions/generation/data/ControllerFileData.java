/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.data;

public class ControllerFileData {
    private String actionDirectory;
    private String actionClassName;
    private String controllerModule;
    private String controllerArea;
    private String httpMethodName;
    private String acl;
    private Boolean isInheritClass;
    private String namespace;

    public ControllerFileData(
            String actionDirectory,
            String controllerClassName,
            String controllerModule,
            String controllerArea,
            String httpMethodName,
            String acl,
            Boolean isInheritClass,
            String namespace
    ) {
        this.actionDirectory = actionDirectory;
        this.actionClassName = controllerClassName;
        this.controllerModule = controllerModule;
        this.controllerArea = controllerArea;
        this.httpMethodName = httpMethodName;
        this.acl = acl;
        this.isInheritClass = isInheritClass;
        this.namespace = namespace;
    }

    public String getActionDirectory() {
        return actionDirectory;
    }

    public String getActionClassName() {
        return actionClassName;
    }

    public String getControllerModule() {
        return controllerModule;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getControllerArea() {
        return controllerArea;
    }

    public String getHttpMethodName() {
        return httpMethodName;
    }

    public String getAcl() {
        return acl;
    }

    public Boolean getIsInheritClass() {
        return isInheritClass;
    }
}
