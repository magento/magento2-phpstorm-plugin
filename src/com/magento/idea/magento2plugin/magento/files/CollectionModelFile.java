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

    public CollectionModelFile(final @NotNull String className) {
        super(className);
    }

    /**
     * Get namespace builder for file.
     *
     * @param moduleName String
     * @param entityName String
     *
     * @return String
     */
    public @NotNull NamespaceBuilder getNamespaceBuilder(
            final @NotNull String moduleName,
            final @NotNull String entityName
    ) {
        return new NamespaceBuilder(
                moduleName,
                getClassName(),
                getDirectory(entityName)
        );
    }

    /**
     * Get collection file directory.
     *
     * @param entityName String
     *
     * @return String
     */
    public @NotNull String getDirectory(final @NotNull String entityName) {
        return ResourceModelFile.RESOURCE_MODEL_DIRECTORY + File.separator + entityName;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }
}
