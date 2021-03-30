/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import org.jetbrains.annotations.NotNull;

public class ResourceModelFile extends AbstractPhpClass {
    public static final String RESOURCE_MODEL_DIRECTORY = "Model/ResourceModel";
    public static final String TEMPLATE = "Magento Resource Model Class";
    public static final String ABSTRACT_DB
            = "Magento\\Framework\\Model\\ResourceModel\\Db\\AbstractDb";
    public static final String ALIAS = "ResourceModel";
    private NamespaceBuilder namespaceBuilder;

    public ResourceModelFile(final @NotNull String className) {
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
        if (namespaceBuilder == null) {
            namespaceBuilder = new NamespaceBuilder(
                    moduleName,
                    getClassName(),
                    RESOURCE_MODEL_DIRECTORY
            );
        }

        return namespaceBuilder;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }
}
