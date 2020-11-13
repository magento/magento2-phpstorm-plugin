/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public abstract class AbstractPhpClass implements ModuleFileInterface {
    private final String fileName;

    public AbstractPhpClass(final String className) {
        fileName = className.concat(".php");
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }
}
