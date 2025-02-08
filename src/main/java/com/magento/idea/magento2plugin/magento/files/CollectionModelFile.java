/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.magento.idea.magento2plugin.magento.packages.File;
import org.jetbrains.annotations.NotNull;

public class CollectionModelFile extends AbstractPhpFile {

    public static final String TEMPLATE = "Magento Collection Class";
    public static final String HUMAN_READABLE_NAME = "Collection class";
    public static final String ABSTRACT_COLLECTION
            = "Magento\\Framework\\Model\\ResourceModel\\Db\\Collection\\AbstractCollection";
    private final String directoryName;

    /**
     * Collection model file constructor.
     *
     * @param moduleName String
     * @param className String
     * @param directoryName String
     */
    public CollectionModelFile(
            final @NotNull String moduleName,
            final @NotNull String className,
            final @NotNull String directoryName
    ) {
        super(moduleName, className);
        this.directoryName = directoryName;
    }

    @Override
    public @NotNull String getDirectory() {
        return ResourceModelFile.RESOURCE_MODEL_DIRECTORY + File.separator + directoryName;
    }

    @Override
    public String getHumanReadableName() {
        return HUMAN_READABLE_NAME;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }
}
