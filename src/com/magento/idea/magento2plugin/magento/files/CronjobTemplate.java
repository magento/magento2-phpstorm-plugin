/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public class CronjobTemplate implements ModuleFileInterface {
    private String fileName;// NOPMD

    public CronjobTemplate(final String className) {
        this.fileName = className.concat(".php");
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public String getTemplate() {
        return "Magento Cron Job Class";
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }
}
