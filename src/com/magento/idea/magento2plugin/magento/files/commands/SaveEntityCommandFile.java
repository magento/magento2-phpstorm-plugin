/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files.commands;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.magento.files.ModuleFileInterface;
import com.magento.idea.magento2plugin.magento.packages.Package;
import org.jetbrains.annotations.NotNull;

public class SaveEntityCommandFile implements ModuleFileInterface {
    public static final String CLASS_NAME = "SaveCommand";
    public static final String FILE_EXTENSION = "php";
    public static final String TEMPLATE = "Magento Save Entity Command Model";
    private static final String DIRECTORY = "Command";
    private static final SaveEntityCommandFile INSTANCE = new SaveEntityCommandFile();

    /**
     * Get singleton object of save command file.
     *
     * @return SaveEntityCommandFile
     */
    public static SaveEntityCommandFile getInstance() {
        return INSTANCE;
    }

    /**
     * Get namespace.
     *
     * @param moduleName String
     * @param entityName String
     *
     * @return String
     */
    public static @NotNull String getNamespace(
            final @NotNull String moduleName,
            final @NotNull String entityName
    ) {
        final NamespaceBuilder namespaceBuilder = new NamespaceBuilder(
                moduleName,
                SaveEntityCommandFile.CLASS_NAME,
                SaveEntityCommandFile.getDirectory(entityName)
        );

        return namespaceBuilder.getNamespace();
    }

    /**
     * Get class FQN.
     *
     * @param moduleName String
     * @param entityName String
     *
     * @return String
     */
    public static String getClassFqn(
            final @NotNull String moduleName,
            final @NotNull String entityName
    ) {
        return getNamespace(moduleName, entityName)
                .concat(Package.fqnSeparator)
                .concat(CLASS_NAME);
    }

    /**
     * Get directory for save command file.
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
