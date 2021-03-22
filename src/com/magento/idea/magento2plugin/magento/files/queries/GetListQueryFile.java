/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files.queries;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.magento.files.ModuleFileInterface;
import java.io.File;
import org.jetbrains.annotations.NotNull;

public final class GetListQueryFile implements ModuleFileInterface {

    public static final String CLASS_NAME = "GetListQuery";
    public static final String FILE_EXTENSION = "php";
    public static final String TEMPLATE = "Magento Get List Query Model";
    private static final String DIRECTORY = "Query";
    private final String entityName;
    private NamespaceBuilder namespaceBuilder;

    /**
     * Get list query file constructor.
     *
     * @param entityName String
     */
    public GetListQueryFile(final @NotNull String entityName) {
        this.entityName = entityName;
    }

    /**
     * Get namespace builder for file.
     *
     * @param moduleName String
     *
     * @return NamespaceBuilder
     */
    public NamespaceBuilder getNamespaceBuilder(final @NotNull String moduleName) {
        if (namespaceBuilder == null) {
            namespaceBuilder = new NamespaceBuilder(
                    moduleName,
                    GetListQueryFile.CLASS_NAME,
                    getDirectory()
            );
        }

        return namespaceBuilder;
    }

    /**
     * Get get list query file directory.
     *
     * @return String
     */
    public String getDirectory() {
        return DIRECTORY.concat(File.separator).concat(entityName);
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
