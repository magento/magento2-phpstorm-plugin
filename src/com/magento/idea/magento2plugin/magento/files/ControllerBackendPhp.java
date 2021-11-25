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
public class ControllerBackendPhp implements ModuleFileInterface {
    public static final String TEMPLATE = "Magento Backend Controller Class";
    public static final String DEFAULT_DIR = "Controller/Adminhtml";
    private static ControllerBackendPhp INSTANCE = null;
    private String fileName;

    /**
     * Returns a new instance of the class.
     *
     * @param className Controller class name
     * @return ControllerBackendPhp
     */
    public static ControllerBackendPhp getInstance(final String className) {
        if (null == INSTANCE) {
            INSTANCE = new ControllerBackendPhp();
        }

        INSTANCE.setFileName(className.concat(".php"));

        return INSTANCE;
    }

    /**
     * Get name of backend controller file.
     *
     * @return Name of the file.
     */
    public String getFileName() {
        return this.fileName;
    }

    /**
     * Get name of backend controller template.
     *
     * @return Name of backend controller template.
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
     * @param filename Name of backend controller file.
     */
    private void setFileName(final String filename) {
        this.fileName = filename;
    }
}
