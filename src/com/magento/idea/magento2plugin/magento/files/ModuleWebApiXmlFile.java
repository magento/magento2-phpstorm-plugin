/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public final class ModuleWebApiXmlFile implements ModuleFileInterface {

    public static final String FILE_NAME = "webapi.xml";
    public static final String TEMPLATE = "Magento Web Api XML";
    public static final String DECLARATION_TEMPLATE = "Magento Web Api Declaration";

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
