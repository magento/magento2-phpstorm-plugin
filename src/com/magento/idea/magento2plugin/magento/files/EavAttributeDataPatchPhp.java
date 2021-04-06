/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public class EavAttributeDataPatchPhp implements ModuleFileInterface {
    public static final String TEMPLATE = "Magento Eav Attribute Data Patch Class";
    public static final String DEFAULT_DIR = "Setup/Patch/Data";
    private String fileName;

    /**
     * Constructor.
     *
     * @param className String
     */
    public EavAttributeDataPatchPhp(final String className) {
        fileName = className.concat(".php");
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getFileName() {
        return fileName;
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
