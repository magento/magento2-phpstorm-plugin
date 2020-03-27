/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public class Plugin implements ModuleFileInterface {
    public static String TEMPLATE = "Magento Plugin Class";

    //plugin prefixes
    public static final String aroundPluginPrefix = "around";
    public static final String beforePluginPrefix = "before";
    public static final String afterPluginPrefix = "after";

    //forbidden target methods
    public static final String constructMethodName = "__construct";

    //allowed methods access types
    public static final String publicAccess = "public";

    private static Plugin INSTANCE = null;
    private String fileName;

    public static Plugin getInstance(String className) {
        if (null == INSTANCE) {
            INSTANCE = new Plugin();
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
    };
}
