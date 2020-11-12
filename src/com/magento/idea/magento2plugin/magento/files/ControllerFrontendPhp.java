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
public class ControllerFrontendPhp implements ModuleFileInterface {
    public static final String TEMPLATE = "Magento Frontend Controller Class";
    public static final String DEFAULT_DIR = "Controller";
    private static ControllerFrontendPhp INSTANCE = null;
    private String fileName;

    /**
     * Returns a new instance of the class.
     *
     * @param className Controller class name
     * @return ControllerFrontendPhp
     */
    public static ControllerFrontendPhp getInstance(final String className) {
        if (null == INSTANCE) {
            INSTANCE = new ControllerFrontendPhp();
        }

        INSTANCE.setFileName(className.concat(".php"));

        return INSTANCE;
    }

    /**
     * Get name of frontend controller file.
     *
     * @return Name of the file.
     */
    public String getFileName() {
        return this.fileName;
    }

    /**
     * Get name of frontend controller template.
     *
     * @return Name of frontend controller template.
     */
    public String getTemplate() {
        return TEMPLATE;
    }

    /**
     * Get return language.
     *
     * @return PHP language
     */
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }

    /**
     * Get name of controller file.
     *
     * @param filename Name of frontend controller file.
     */
    private void setFileName(final String filename) {
        this.fileName = filename;
    }
}
