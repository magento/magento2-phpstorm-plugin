/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

@SuppressWarnings({"PMD.DataClass"})
public class ControllerFileData {
    private final String actionDirectory;
    private final String actionClassName;
    private final String controllerModule;
    private final String controllerArea;
    private final String httpMethodName;
    private final String acl;
    private final Boolean isInheritClass;
    private final String namespace;

    /**
     * Controller data file constructor.
     *
     * @param actionDirectory String
     * @param controllerClassName String
     * @param controllerModule String
     * @param controllerArea String
     * @param httpMethodName String
     * @param acl String
     * @param isInheritClass Boolean
     * @param namespace String
     */
    public ControllerFileData(
            final String actionDirectory,
            final String controllerClassName,
            final String controllerModule,
            final String controllerArea,
            final String httpMethodName,
            final String acl,
            final Boolean isInheritClass,
            final String namespace
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

    /**
     * Get action directory.
     *
     * @return String
     */
    public String getActionDirectory() {
        return actionDirectory;
    }

    /**
     * Get action class name.
     *
     * @return String
     */
    public String getActionClassName() {
        return actionClassName;
    }

    /**
     * Get controller module.
     *
     * @return String
     */
    public String getControllerModule() {
        return controllerModule;
    }

    /**
     * Get namespace.
     *
     * @return String
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Get controller area.
     *
     * @return String
     */
    public String getControllerArea() {
        return controllerArea;
    }

    /**
     * Get HTTP method name.
     *
     * @return String
     */
    public String getHttpMethodName() {
        return httpMethodName;
    }

    /**
     * Get ACL.
     *
     * @return String
     */
    public String getAcl() {
        return acl;
    }

    /**
     * Get is inherit class.
     *
     * @return String
     */
    public Boolean getIsInheritClass() {
        return isInheritClass;
    }
}
