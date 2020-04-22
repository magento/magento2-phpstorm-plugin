/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

import java.util.HashMap;
import java.util.Map;

public class ControllerPhp implements ModuleFileInterface {
    public static String TEMPLATE = "Magento Module Controller Class";
    public static String ADMINHTML_CONTROLLER_FQN = "Magento\\Backend\\App\\Action";
    public static String FRONTEND_CONTROLLER_FQN = "Magento\\Framework\\App\\Action\\Action";
    public static String DEFAULT_ADMINHTML_DIR = "Controller/Adminhtml";
    public static String DEFAULT_FRONTEND_DIR = "Controller";
    public static final String CONTROLLER_ACTION_CONTENTS_TEMPLATE_NAME = "Magento Controller Class Body";

    private static ControllerPhp INSTANCE = null;
    private static Map<String, String> httpMethodInterfaces = null;
    private String fileName;

    public static ControllerPhp getInstance(String className) {
        if (null == INSTANCE) {
            INSTANCE = new ControllerPhp();
        }

        INSTANCE.setFileName(className.concat(".php"));

        return INSTANCE;
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }

    private void setFileName(String filename) {
        this.fileName = filename;
    }

    public static String getHttpMethodInterfaceByMethod(String method)
    {
        Map<String, String> httpMethodInterfaces = getHttpMethodInterfaces();

        return httpMethodInterfaces.get(method);
    }

    private static Map<String, String> getHttpMethodInterfaces()
    {
        if (httpMethodInterfaces == null) {
            httpMethodInterfaces = new HashMap<String, String>();

            httpMethodInterfaces.put("GET", "Magento\\Framework\\App\\Action\\HttpGetActionInterface");
            httpMethodInterfaces.put("POST", "Magento\\Framework\\App\\Action\\HttpPostActionInterface");
            httpMethodInterfaces.put("PUT", "Magento\\Framework\\App\\Action\\HttpPutActionInterface");
            httpMethodInterfaces.put("DELETE", "Magento\\Framework\\App\\Action\\HttpDeleteActionInterface");
        }

        return httpMethodInterfaces;
    }
}
