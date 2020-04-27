/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.magento.packages;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class HttpRequest {
    public static enum HttpMethod {
        GET("Magento\\Framework\\App\\Action\\HttpGetActionInterface"),
        POST("Magento\\Framework\\App\\Action\\HttpPostActionInterface"),
        DELETE("Magento\\Framework\\App\\Action\\HttpDeleteActionInterface"),
        PUT("Magento\\Framework\\App\\Action\\HttpPutActionInterface");

        private String interfaceFqn;

        HttpMethod(String interfaceFqn) {
            this.interfaceFqn = interfaceFqn;
        }

        public String getInterfaceFqn() {
            return interfaceFqn;
        }
    }

    public static String getRequestInterfaceFqnByMethodName(String methodName)
    {
        return HttpRequest.HttpMethod.valueOf(methodName).getInterfaceFqn();
    }

    @NotNull
    public static ArrayList<String> getHttpMethodList()
    {
        ArrayList<String> methodNameList = new ArrayList<String>();

        for (HttpMethod httpMethod: HttpMethod.values()) {
            methodNameList.add(httpMethod.name());
        }

        return methodNameList;
    }
}
