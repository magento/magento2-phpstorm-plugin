/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public class ModuleDiXml implements ModuleFileInterface {
    public static String FILE_NAME = "di.xml";
    public static String TEMPLATE = "Magento Module DI Xml";
    public static String TEMPLATE_PLUGIN = "Magento Module DI Xml Plugin";
    public static String PLUGIN_TYPE_TAG = "type";
    public static String PLUGIN_TYPE_ATTRIBUTE = "type";
    public static String PLUGIN_TAG_NAME = "plugin";
    public static String PLUGIN_TYPE_ATTR_NAME = "name";
    private static ModuleDiXml INSTANCE = null;

    public static ModuleDiXml getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new ModuleDiXml();
        }
        return INSTANCE;
    }

    @Override
    public String getFileName() {
        return FILE_NAME;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public Language getLanguage() {
        return XMLLanguage.INSTANCE;
    }
}
