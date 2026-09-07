/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public class ProductTypeXml implements ModuleFileInterface {
    public static final String FILE_NAME = "product_types.xml";
    public static final String TEMPLATE = "Magento Product Types";

    //attributes
    public static final String XML_TAG_TYPE = "type";
    public static final String XML_ATTRIBUTE_NAME = "name";

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
