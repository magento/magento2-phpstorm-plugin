/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.xml;

import org.jetbrains.annotations.NotNull;

public class WebApiXmlRouteData {

    private final String moduleName;
    private final String url;
    private final String httpMethod;
    private final String serviceClass;
    private final String serviceMethod;
    private final String aclResource;

    /**
     * Web API XML declaration DTO constructor.
     *
     * @param moduleName String
     * @param url String
     * @param httpMethod String
     * @param serviceClass String
     * @param serviceMethod String
     * @param aclResource String
     */
    public WebApiXmlRouteData(
            final @NotNull String moduleName,
            final @NotNull String url,
            final @NotNull String httpMethod,
            final @NotNull String serviceClass,
            final @NotNull String serviceMethod,
            final @NotNull String aclResource
    ) {
        this.moduleName = moduleName;
        this.url = url;
        this.httpMethod = httpMethod;
        this.serviceClass = serviceClass;
        this.serviceMethod = serviceMethod;
        this.aclResource = aclResource;
    }

    /**
     * Get module name.
     *
     * @return String
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Get service url.
     *
     * @return String
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get HTTP method.
     *
     * @return String
     */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * Get service class.
     *
     * @return String
     */
    public String getServiceClass() {
        return serviceClass;
    }

    /**
     * Get service method.
     *
     * @return String
     */
    public String getServiceMethod() {
        return serviceMethod;
    }

    /**
     * Get ACL resource id.
     *
     * @return String
     */
    public String getAclResource() {
        return aclResource;
    }
}
