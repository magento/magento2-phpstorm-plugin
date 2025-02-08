/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public class RoutesXml implements ModuleFileInterface {

    public static final String FILE_NAME = "routes.xml";
    public static final String TEMPLATE = "Magento Routes XML";
    public static final String ROUTER_ID_STANDARD = "standard";
    public static final String ROUTER_ID_ADMIN = "admin";

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
