/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public class RegistrationPhp implements ModuleFileInterface {
    public static String FILE_NAME = "registration.php";
    public static String TEMPLATE = "Magento Module Registration Php";
    public static String REGISTER_METHOD_NAME = "register";
    private static RegistrationPhp INSTANCE = null;

    public static RegistrationPhp getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new RegistrationPhp();
        }
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
