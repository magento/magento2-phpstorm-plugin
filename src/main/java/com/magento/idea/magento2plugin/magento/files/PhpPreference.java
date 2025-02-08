/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public class PhpPreference implements ModuleFileInterface {
    public static final String TEMPLATE = "Magento Preference Class";
    private String fileName;
    private static final PhpPreference INSTANCE = new PhpPreference();

    /**
     * Getter for singleton instance of class.
     */
    public static PhpPreference getInstance(final String className) {
        INSTANCE.fileName = className.concat(".php");

        return INSTANCE;
    }

    @Override
    public String getFileName() {
        return this.fileName;
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
