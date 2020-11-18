/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public class DataModelInterface implements ModuleFileInterface {
    public static final String DIRECTORY = "Api/Data";
    private final String className;

    public DataModelInterface(final String className) {
        this.className = className.concat(".php");
    }

    @Override
    public String getFileName() {
        return className;
    }

    @Override
    public String getTemplate() {
        return "Magento Data Model Interface";
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }
}
