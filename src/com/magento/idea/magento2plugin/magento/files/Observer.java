/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public class Observer implements ModuleFileInterface {

    private String fileName;
    public static final String INTERFACE = "Magento\\Framework\\Event\\ManagerInterface";
    public static final String DISPATCH_METHOD = "dispatch";

    public Observer(String className) {
        fileName = className.concat(".php");
    }

    public static final String OBSERVER_EXECUTE_TEMPLATE_NAME = "Magento Observer Execute Method";

    public static Observer getInstance(String className) {
        return new Observer(className);
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public String getTemplate() {
        return "Magento Observer Class";
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }
}
