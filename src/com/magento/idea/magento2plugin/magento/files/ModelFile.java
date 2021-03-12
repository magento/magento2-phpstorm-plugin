/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import org.jetbrains.annotations.NotNull;

public class ModelFile extends AbstractPhpClass {
    public static final String ABSTRACT_MODEL =
            "Magento\\Framework\\Model\\AbstractModel";
    public static final String MODEL_DIRECTORY = "Model";
    public static final String TEMPLATE = "Magento Model Class";

    public ModelFile(final String className) {
        super(className);
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
                getClassName(),
                MODEL_DIRECTORY
        );
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }
}
