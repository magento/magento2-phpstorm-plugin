/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public class UiComponentFormXml implements ModuleFileInterface {
    public static final String FILE_EXTENSION = "xml";
    public static final String TEMPLATE = "Magento UI Component Form XML";
    public static final String FIELD_TEMPLATE = "Magento Module UI Component Form Field Xml";
    private final String componentName;

    /**
     * UI component Form XML template constructor.
     *
     * @param componentName component name
     */
    public UiComponentFormXml(final String componentName) {
        this.componentName = componentName;
    }

    @Override
    public String getFileName() {
        return String.format("%s.%s", componentName, FILE_EXTENSION);
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
