/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.fileTypes.LanguageFileType;

public class BlockPhp implements ModuleFileInterface {
    public static final String TEMPLATE = "Magento Regular Class";
    public static final String DEFAULT_DIR = "Block";
    public static final String STOREFRONT_BLOCK_FQN = "Magento\\Framework\\View\\Element\\Template";
    public static final String STOREFRONT_BLOCK_NAME = "Template";

    private static final BlockPhp INSTANCE = new BlockPhp();
    private String fileName;

    /**
     * Getter for singleton instance of class.
     */
    public static BlockPhp getInstance(final String className) {
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
        final var fileType = FileTypeRegistry.getInstance().findFileTypeByName("PHP");
        if (!(fileType instanceof LanguageFileType languageFileType)) {
            throw new IllegalStateException("PHP support is required");
        }
        return languageFileType.getLanguage();
    }
}
