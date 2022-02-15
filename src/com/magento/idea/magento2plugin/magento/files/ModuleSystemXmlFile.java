/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public final class ModuleSystemXmlFile implements ModuleFileInterface {

    public static final String FILE_NAME = "system.xml";
    public static final String TEMPLATE = "Magento System XML";

    public static final String FIELD_ELEMENT_NAME = "field";
    public static final String XML_TAG_SOURCE_MODEL = "source_model";
    public static final String XML_TAG_FRONTEND_MODEL = "frontend_model";
    public static final String XML_TAG_BACKEND_MODEL = "backend_model";

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
