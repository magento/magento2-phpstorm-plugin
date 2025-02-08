/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public class RegistrationPhp implements ModuleFileInterface {
    public static final String FILE_NAME = "registration.php";
    public static final String TEMPLATE = "Magento Registration PHP";
    public static final String REGISTER_METHOD_NAME = "register";
    private static final RegistrationPhp INSTANCE = new RegistrationPhp();

    /**
     * Getter for singleton instance of class.
     */
    public static RegistrationPhp getInstance() {
        return INSTANCE;
    }

    @Override
    public String getFileName() {
        return FILE_NAME;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }
}
