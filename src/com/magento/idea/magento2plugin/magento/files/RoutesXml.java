/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public class RoutesXml implements ModuleFileInterface {
    public static String fileName = "routes.xml";
    public static String template = "Magento Routes XML";
    public static String routerIdStandart = "standart";
    public static String routerIdAdmin = "admin";

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getTemplate() {
        return template;
    }

    @Override
    public Language getLanguage() {
        return XMLLanguage.INSTANCE;
    }
}
