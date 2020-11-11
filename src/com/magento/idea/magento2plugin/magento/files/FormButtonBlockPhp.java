/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public class FormButtonBlockPhp implements ModuleFileInterface {
    public static String template = "Magento Form Button Block Class";

    public static String saveMethodTemplate = "Magento Php Form Button Block Type Save";
    public static String deleteMethodTemplate = "Magento Php Form Button Block Type Delete";
    public static String backMethodTemplate = "Magento Php Form Button Block Type Back";

    public static final String TYPE_SAVE = "Save";
    public static final String TYPE_DELETE = "Delete";
    public static final String TYPE_BACK = "Back";
    public static final String TYPE_CUSTOM = "Custom";

    public static final String DEFAULT_METHOD = "getButtonData";
    private String fileName;

    public FormButtonBlockPhp(final String className) {
        this.setFileName(className.concat(".php"));
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public String getTemplate() {
        return template;
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }

    private void setFileName(final String filename) {
        this.fileName = filename;
    }
}
