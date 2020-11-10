/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public class ViewModelPhp implements ModuleFileInterface {
    public static String TEMPLATE = "Magento Regular Class";
    public static String DEFAULT_DIR = "ViewModel";
    public static String INTERFACE_FQN = "Magento\\Framework\\View\\Element\\Block\\ArgumentInterface";
    public static String INTERFACE_NAME = "ArgumentInterface";

    private static ViewModelPhp INSTANCE = null;
    private String fileName;

    public static ViewModelPhp getInstance(String className) {
        if (null == INSTANCE) {
            INSTANCE = new ViewModelPhp();
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
