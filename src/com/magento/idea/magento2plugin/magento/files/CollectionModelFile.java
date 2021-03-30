/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.magento.packages.File;
import org.jetbrains.annotations.NotNull;

public class CollectionModelFile extends AbstractPhpClass {
    public static final String TEMPLATE = "Magento Collection Class";
    public static final String ABSTRACT_COLLECTION
            = "Magento\\Framework\\Model\\ResourceModel\\Db\\Collection\\AbstractCollection";
    private NamespaceBuilder namespaceBuilder;

    public CollectionModelFile(final @NotNull String className) {
        super(className);
    }

    /**
     * Get namespace builder for file.
     *
     * @param moduleName String
     * @param directoryName String
     *
     * @return String
     */
    public @NotNull NamespaceBuilder getNamespaceBuilder(
            final @NotNull String moduleName,
            final @NotNull String directoryName
    ) {
        if (namespaceBuilder == null) {
            namespaceBuilder = new NamespaceBuilder(
                    moduleName,
                    getClassName(),
                    getDirectory(directoryName)
            );
        }

        return namespaceBuilder;
    }

    /**
     * Get collection file directory.
     *
     * @param directoryName String
     *
     * @return String
     */
    public @NotNull String getDirectory(final @NotNull String directoryName) {
        return ResourceModelFile.RESOURCE_MODEL_DIRECTORY + File.separator + directoryName;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }
}
