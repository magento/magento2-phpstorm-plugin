/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public class UiComponentGridDataProviderPhp implements ModuleFileInterface {
    public final static String COLLECTION_TEMPLATE = "Magento Module Ui Grid Collection Data Provider Php";
    public final static String CUSTOM_TEMPLATE = "Magento Module Ui Grid Custom Data Provider Php";
    public final static String FILE_EXTENSION = "php";
    public final static String CUSTOM_TYPE = "custom";
    public final static String COLLECTION_TYPE = "collection";
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
     * @return void
     */
    public void setClassName(final String className) {
        this.className = className;
    }

    /**
     * Set provided type.
     *
     * @param providerType String
     * @return void
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
        if (providerType.equals("collection")) {
            return COLLECTION_TEMPLATE;
        }

        if (providerType.equals("custom")) {
            return CUSTOM_TEMPLATE;
        }

        return null;
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }
}
