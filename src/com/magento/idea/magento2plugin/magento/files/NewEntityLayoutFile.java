/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;
import org.jetbrains.annotations.NotNull;

public class NewEntityLayoutFile implements ModuleFileInterface {

    public static final String TEMPLATE = "Magento New Entity Layout XML";
    private static final String PARENT_DIRECTORY = "layout";
    private static final String DIRECTORY = "view/adminhtml/layout";
    private final String filename;

    /**
     * New entity layout file constructor.
     *
     * @param filename String
     */
    public NewEntityLayoutFile(final @NotNull String filename) {
        this.filename = filename;
    }

    /**
     * Get parent directory for file.
     *
     * @return String
     */
    public String getParentDirectory() {
        return PARENT_DIRECTORY;
    }

    /**
     * Get directory for file.
     *
     * @return String
     */
    public String getDirectory() {
        return DIRECTORY;
    }

    @Override
    public String getFileName() {
        return filename.concat(".xml");
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public Language getLanguage() {
        return XMLLanguage.INSTANCE;
    }
}
