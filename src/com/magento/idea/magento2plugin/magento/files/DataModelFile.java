/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import org.jetbrains.annotations.NotNull;

public class DataModelFile implements ModuleFileInterface {
    public static final String TEMPLATE = "Magento Data Model";
    public static final String DIRECTORY = "Model/Data";
    public static final String DATA_OBJECT = "Magento\\Framework\\DataObject";
    private final String className;
    private final String fileName;

    public DataModelFile(final @NotNull String className) {
        this.className = className;
        fileName = className.concat(".php");
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
        return fileName;
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
