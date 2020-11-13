/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

@SuppressWarnings({
        "PMD.FieldNamingConventions",
        "PMD.NonThreadSafeSingleton",
        "PMD.RedundantFieldInitializer"
})
public class UiComponentDataProviderPhp implements ModuleFileInterface {
    public static final String CUSTOM_TEMPLATE = "Magento UI Component Custom Data Provider Class";
    public static final String FILE_EXTENSION = "php";
    public static final String CUSTOM_TYPE = "custom";
    public static final String COLLECTION_TYPE = "collection";
    private static UiComponentDataProviderPhp INSTANCE = null;
    private String className;
    public static final String DEFAULT_DATA_PROVIDER =
            "Magento\\Framework\\View\\Element\\UiComponent\\DataProvider\\DataProvider";

    /**
     * Returns a new instance of the class.
     *
     * @param className DataProvider class name
     * @return UiComponentGridDataProviderPhp
     */
    public static UiComponentDataProviderPhp getInstance(
            final String className
    ) {
        if (null == INSTANCE) {
            INSTANCE = new UiComponentDataProviderPhp();
        }

        INSTANCE.setClassName(className);

        return INSTANCE;
    }

    /**
     * Set class name.
     *
     * @param className String
     */
    public void setClassName(final String className) {
        this.className = className;
    }

    @Override
    public String getFileName() {
        return String.format("%s.%s", className, FILE_EXTENSION);
    }

    @Override
    public String getTemplate() {
        return CUSTOM_TEMPLATE;
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }
}
