/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public final class ModuleConfigXmlFile implements ModuleFileInterface {

    public static final String FILE_NAME = "config.xml";
    public static final String TEMPLATE = "Magento Config XML";

    public static final String ROOT_TAG_NAME = "config";
    public static final String DEFAULT_SCOPE = "default";
    public static final String STORE_SCOPE = "stores";
    public static final String WEBSITE_SCOPE = "websites";

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
