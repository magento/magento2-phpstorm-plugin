/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import org.jetbrains.annotations.NotNull;

public class EntityDataMapperFile implements ModuleFileInterface {

    public static final String CLASS_NAME_SUFFIX = "DataMapper";
    public static final String FILE_EXTENSION = "php";
    public static final String TEMPLATE = "Magento Entity Data Mapper";
    private static final String DIRECTORY = "Mapper";
    private final String className;
    private NamespaceBuilder namespaceBuilder;

    /**
     * Entity data mapper file constructor.
     *
     * @param entityName String
     */
    public EntityDataMapperFile(final @NotNull String entityName) {
        this.className = entityName.concat(CLASS_NAME_SUFFIX);
    }

    /**
     * Get namespace builder.
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
                    className,
                    DIRECTORY
            );
        }

        return namespaceBuilder;
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
    public @NotNull String getClassFqn(
            final @NotNull String moduleName
    ) {
        return getNamespaceBuilder(moduleName).getClassFqn();
    }

    /**
     * Get class name.
     *
     * @return String
     */
    public @NotNull String getClassName() {
        return className;
    }

    /**
     * Get directory for entity data mapper file.
     *
     * @return String
     */
    public @NotNull String getDirectory() {
        return DIRECTORY;
    }

    @Override
    public String getFileName() {
        return className.concat("." + FILE_EXTENSION);
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
