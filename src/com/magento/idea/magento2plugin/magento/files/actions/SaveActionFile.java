/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files.actions;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;
import com.magento.idea.magento2plugin.magento.files.ModuleFileInterface;
import org.jetbrains.annotations.NotNull;

public final class SaveActionFile implements ModuleFileInterface {
    public static final String CLASS_NAME = "Save";
    public static final String FILE_EXTENSION = "php";
    public static final String TEMPLATE = "Magento Entity Save Controller Class";
    private static final String DIRECTORY = "Controller/Adminhtml";
    private static final SaveActionFile INSTANCE = new SaveActionFile();
    public static final String COULD_NOT_SAVE =
            "Magento\\Framework\\Exception\\CouldNotSaveException";

    /**
     * Get singleton instance of the class.
     *
     * @return SaveAction
     */
    public static SaveActionFile getInstance() {
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

    /**
     * Get admin resource.
     *
     * @param moduleName String
     * @param acl String
     *
     * @return String
     */
    public static String getAdminResource(
            final @NotNull String moduleName,
            final  @NotNull String acl
    ) {
        return moduleName + "::" + acl;
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
