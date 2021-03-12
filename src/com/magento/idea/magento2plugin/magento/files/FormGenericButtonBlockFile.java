/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import org.jetbrains.annotations.NotNull;

public class FormGenericButtonBlockFile implements ModuleFileInterface {

    public static final String CLASS_NAME = "GenericButton";
    public static final String FILE_EXTENSION = "php";
    public static final String TEMPLATE = "Magento PHP Form Generic Button Block Class";
    public static final String DIRECTORY = "Block/Form";
    public static final String CONTEXT = "Magento\\Backend\\Block\\Widget\\Context";

    /**
     * Get namespace builder for file.
     *
     * @param moduleName String
     *
     * @return String
     */
    public static @NotNull NamespaceBuilder getNamespaceBuilder(
            final @NotNull String moduleName
    ) {
        return new NamespaceBuilder(
                moduleName,
                CLASS_NAME,
                DIRECTORY
        );
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
