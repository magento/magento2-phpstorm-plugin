/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public class DataModel implements ModuleFileInterface {
    public static final String DIRECTORY = "Model/Data";
    public static final String DATA_OBJECT = "Magento\\Framework\\DataObject";
    private final String className;

    public DataModel(final String className) {
        this.className = className.concat(".php");
    }

    @Override
    public String getFileName() {
        return className;
    }

    @Override
    public String getTemplate() {
        return "Magento Data Model";
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }
}
