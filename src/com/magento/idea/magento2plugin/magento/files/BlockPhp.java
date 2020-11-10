/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public class BlockPhp implements ModuleFileInterface {
    public static String TEMPLATE = "Magento Regular Class";
    public static String DEFAULT_DIR = "Block";
    public static String STOREFRONT_BLOCK_FQN = "Magento\\Framework\\View\\Element\\Template";
    public static String STOREFRONT_BLOCK_NAME = "Template";

    private static BlockPhp INSTANCE = null;
    private String fileName;

    /**
     * Getter for singleton instance of class.
     */
    public static BlockPhp getInstance(String className) {
        if (null == INSTANCE) {
            INSTANCE = new BlockPhp();
        }
        INSTANCE.setFileName(className.concat(".php"));
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

    private void setFileName(String filename) {
        this.fileName = filename;
    }
}
