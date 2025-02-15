/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public class UiComponentGridXmlFile implements ModuleFileInterface {

    public static final String FILE_EXTENSION = "xml";
    public static final String TEMPLATE = "Magento UI Component Grid XML";
    public static final String COLUMN_TEMPLATE = "Magento Grid UI Component Column";
    private final String componentName;

    /**
     * UI component grid XML template constructor.
     *
     * @param componentName component name
     */
    public UiComponentGridXmlFile(final String componentName) {
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
