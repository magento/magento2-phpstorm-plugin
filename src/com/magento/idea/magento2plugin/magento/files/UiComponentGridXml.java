/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public class UiComponentGridXml implements ModuleFileInterface {
    public static final String FILE_EXTENSION = "xml";
    public static final String TEMPLATE = "Magento Module UI Component Grid Xml";
    private final String componentName;

    /**
     * UI component grid XML template constructor.
     *
     * @param componentName component name
     */
    public UiComponentGridXml(final String componentName) {
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
