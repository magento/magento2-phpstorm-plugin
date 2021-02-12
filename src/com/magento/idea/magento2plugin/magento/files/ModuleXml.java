/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public class ModuleXml implements ModuleFileInterface {
    public static final String FILE_NAME = "module.xml";
    public static final String MODULE_ATTR_NAME = "name";
    public static final String TEMPLATE = "Magento Module XML";
    public static final String NO_SEQUENCES_LABEL = "None";
    private static final ModuleXml INSTANCE = new ModuleXml();

    /**
     * Getter for singleton instance of class.
     */
    public static ModuleXml getInstance() {
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
