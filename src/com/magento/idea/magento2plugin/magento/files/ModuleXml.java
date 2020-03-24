/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public class ModuleXml implements ModuleFileInterface {
    public static String FILE_NAME = "module.xml";
    public static String TEMPLATE = "Magento Module Xml";
    private static ModuleXml INSTANCE = null;

    public static ModuleXml getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new ModuleXml();
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
