/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import org.jetbrains.annotations.NotNull;

public class DataModelInterfaceFile implements ModuleFileInterface {
    public static final String TEMPLATE = "Magento Data Model Interface";
    public static final String DIRECTORY = "Api/Data";
    private final String className;
    private final String filename;

    public DataModelInterfaceFile(final @NotNull String className) {
        this.className = className;
        filename = className.concat(".php");
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
        return new NamespaceBuilder(
                moduleName,
                className,
                DIRECTORY
        );
    }

    @Override
    public String getFileName() {
        return filename;
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
