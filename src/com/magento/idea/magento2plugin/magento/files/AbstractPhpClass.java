/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractPhpClass implements ModuleFileInterface {
    private final String fileName;
    private final String className;

    public AbstractPhpClass(final @NotNull String className) {
        this.className = className;
        fileName = className.concat(".php");
    }

    /**
     * Get class name.
     *
     * @return String
     */
    public String getClassName() {
        return className;
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
