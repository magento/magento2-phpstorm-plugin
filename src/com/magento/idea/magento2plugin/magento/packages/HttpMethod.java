/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages;

import java.util.ArrayList;
import java.util.List;

public enum HttpMethod {
    GET("Magento\\Framework\\App\\Action\\HttpGetActionInterface"),
    POST("Magento\\Framework\\App\\Action\\HttpPostActionInterface"),
    DELETE("Magento\\Framework\\App\\Action\\HttpDeleteActionInterface"),
    PUT("Magento\\Framework\\App\\Action\\HttpPutActionInterface");

    private final String interfaceFqn;

    /**
     * Http method constructor.
     *
     * @param interfaceFqn String
     */
    HttpMethod(String interfaceFqn) {
        this.interfaceFqn = interfaceFqn;
    }

    /**
     * Get interface FQN.
     *
     * @return String
     */
    public String getInterfaceFqn() {
        return interfaceFqn;
    }

    /**
     * Get HTTP request interfaced FQN by method name.
     *
     * @param methodName HTTP Method name
     * @return Request Interface
     */
    public static String getRequestInterfaceFqnByMethodName(final String methodName) {
        return HttpMethod.valueOf(methodName).getInterfaceFqn();
    }

    /**
     * Get list of HTTP methods.
     *
     * @return List of HTTP methods.
     */
    public static List<String> getHttpMethodList() {
        final List<String> methodNameList = new ArrayList<>();

        for (final HttpMethod httpMethod: HttpMethod.values()) {
            methodNameList.add(httpMethod.name());
        }

        return methodNameList;
    }
}
