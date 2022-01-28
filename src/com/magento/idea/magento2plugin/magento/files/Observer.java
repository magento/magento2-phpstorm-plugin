/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public class Observer implements ModuleFileInterface {

    private final String fileName;
    public static final String INTERFACE = "Magento\\Framework\\Event\\ManagerInterface";
    public static final String IMPLEMENTATION = "Magento\\Framework\\Event\\Manager";
    public static final String ENTITY_IMPL = "Magento\\Framework\\EntityManager\\EventManager";
    public static final String STAGING_IMPL = "Magento\\Staging\\Model\\Event\\Manager";
    public static final String DISPATCH_METHOD = "dispatch";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String OBSERVER_EXECUTE_TEMPLATE_NAME = "Magento Observer Execute Method";

    public Observer(final String className) {
        fileName = className.concat(".php");
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
