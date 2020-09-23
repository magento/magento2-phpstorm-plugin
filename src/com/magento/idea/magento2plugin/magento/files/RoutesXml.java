/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

@SuppressWarnings({
        "PMD.FieldNamingConventions",
        "PMD.RedundantFieldInitializer",
        "PMD.NonThreadSafeSingleton",
        "PMD.TooManyFields"
})
public class RoutesXml implements ModuleFileInterface {
    public static String FILE_NAME = "routes.xml";
    public static String TEMPLATE = "Magento Routes Xml";

    private static RoutesXml INSTANCE = null;

    /**
     * Get instance of the class.
     *
     * @return ModuleDiXml
     */
    public static RoutesXml getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new RoutesXml();
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
