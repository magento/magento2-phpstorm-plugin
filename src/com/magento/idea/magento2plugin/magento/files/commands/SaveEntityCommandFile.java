/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files.commands;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.magento.files.ModuleFileInterface;
import org.jetbrains.annotations.NotNull;

public class SaveEntityCommandFile implements ModuleFileInterface {

    public static final String CLASS_NAME = "SaveCommand";
    public static final String FILE_EXTENSION = "php";
    public static final String TEMPLATE = "Magento Save Entity Command Model";
    private static final String DIRECTORY = "Command";
    private final String entityName;
    private NamespaceBuilder namespaceBuilder;

    /**
     * Save entity command file constructor.
     *
     * @param entityName String
     */
    public SaveEntityCommandFile(final @NotNull String entityName) {
        this.entityName = entityName;
    }

    /**
     * Get namespace.
     *
     * @param moduleName String
     *
     * @return String
     */
    public @NotNull String getNamespace(
            final @NotNull String moduleName
    ) {
        return getNamespaceBuilder(moduleName).getNamespace();
    }

    /**
     * Get class FQN.
     *
     * @param moduleName String
     *
     * @return String
     */
    public String getClassFqn(
            final @NotNull String moduleName
    ) {
        return getNamespaceBuilder(moduleName).getClassFqn();
    }

    /**
     * Get namespace builder for file.
     *
     * @param moduleName String
     *
     * @return String
     */
    public @NotNull NamespaceBuilder getNamespaceBuilder(
            final @NotNull String moduleName
    ) {
        if (namespaceBuilder == null) {
            namespaceBuilder = new NamespaceBuilder(
                    moduleName,
                    CLASS_NAME,
                    getDirectory()
            );
        }

        return namespaceBuilder;
    }

    /**
     * Get directory for save command file.
     *
     * @return String
     */
    public String getDirectory() {
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
