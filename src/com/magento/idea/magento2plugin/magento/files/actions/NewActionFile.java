/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files.actions;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;
import com.magento.idea.magento2plugin.magento.files.ModuleFileInterface;
import org.jetbrains.annotations.NotNull;

public final class NewActionFile implements ModuleFileInterface {
    public static final String CLASS_NAME = "NewAction";
    public static final String FILE_EXTENSION = "php";
    public static final String TEMPLATE = "Magento Entity New Action Controller Class";
    private static final String DIRECTORY = "Controller/Adminhtml";
    private static final NewActionFile INSTANCE = new NewActionFile();

    /**
     * Get singleton instance of the class.
     *
     * @return NewAction
     */
    public static NewActionFile getInstance() {
        return INSTANCE;
    }

    /**
     * Get Directory path from the module root.
     *
     * @param entityName String
     *
     * @return String
     */
    public static String getDirectory(final @NotNull String entityName) {
        return DIRECTORY.concat("/" + entityName);
    }

    @Override
    public String getFileName() {
        return CLASS_NAME.concat("." + FILE_EXTENSION);
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
