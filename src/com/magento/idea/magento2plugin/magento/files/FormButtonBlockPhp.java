/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public class FormButtonBlockPhp implements ModuleFileInterface {
    public static String TEMPLATE = "Magento Php Form Button Block Class";

    public static String SAVE_METHOD_TEMPLATE = "Magento Php Form Button Block Type Save";
    public static String DELETE_METHOD_TEMPLATE = "Magento Php Form Button Block Type Delete";
    public static String BACK_METHOD_TEMPLATE = "Magento Php Form Button Block Type Back";

    public static final String TYPE_SAVE = "Save";
    public static final String TYPE_DELETE = "Delete";
    public static final String TYPE_BACK = "Back";
    public static final String TYPE_CUSTOM = "Custom";

    public static final String DEFAULT_METHOD = "getButtonData";

    private static FormButtonBlockPhp INSTANCE = null;
    private String fileName;

    public static FormButtonBlockPhp getInstance(String className) {
        if (null == INSTANCE) {
            INSTANCE = new FormButtonBlockPhp();
        }
        INSTANCE.setFileName(className.concat(".php"));
        return INSTANCE;
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }

    private void setFileName(String filename) {
        this.fileName = filename;
    }
}
