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
public class UiComponentGridDataProviderPhp implements ModuleFileInterface {
    public static final String COLLECTION_TEMPLATE =
            "Magento Module Ui Grid Collection Data Provider Php";
    public static final String CUSTOM_TEMPLATE = "Magento Module Ui Grid Custom Data Provider Php";
    public static final String FILE_EXTENSION = "php";
    public static final String CUSTOM_TYPE = "custom";
    public static final String COLLECTION_TYPE = "collection";
    private static UiComponentGridDataProviderPhp INSTANCE = null;
    private String className;
    private String providerType;

    /**
     * Returns a new instance of the class.
     *
     * @param className DataProvider class name
     * @param providerType DataProvider type
     * @return UiComponentGridDataProviderPhp
     */
    public static UiComponentGridDataProviderPhp getInstance(
            final String className,
            final String providerType
    ) {
        if (null == INSTANCE) {
            INSTANCE = new UiComponentGridDataProviderPhp();
        }

        INSTANCE.setClassName(className);
        INSTANCE.setProviderType(providerType);

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

    /**
     * Set provided type.
     *
     * @param providerType String
     */
    public void setProviderType(final String providerType) {
        this.providerType = providerType;
    }

    @Override
    public String getFileName() {
        return String.format("%s.%s", className, FILE_EXTENSION);
    }

    @Override
    public String getTemplate() {
        if (providerType.equals(COLLECTION_TYPE)) {
            return COLLECTION_TEMPLATE;
        }

        if (providerType.equals(CUSTOM_TYPE)) {
            return CUSTOM_TEMPLATE;
        }

        return null;
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }
}
