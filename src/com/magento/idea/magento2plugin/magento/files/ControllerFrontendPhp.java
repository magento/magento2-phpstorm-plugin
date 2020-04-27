/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public class ControllerFrontendPhp implements ModuleFileInterface {
    public static String TEMPLATE = "Magento Module Controller Frontend Class";
    public static String DEFAULT_DIR = "Controller";

    private static ControllerFrontendPhp INSTANCE = null;
    private String fileName;

    public static ControllerFrontendPhp getInstance(String className) {
        if (null == INSTANCE) {
            INSTANCE = new ControllerFrontendPhp();
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
