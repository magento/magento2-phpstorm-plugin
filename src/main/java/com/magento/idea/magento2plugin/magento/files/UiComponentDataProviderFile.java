/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import org.jetbrains.annotations.NotNull;

public class UiComponentDataProviderFile extends AbstractPhpFile {

    public static final String TEMPLATE = "Magento UI Component Custom Data Provider Class";
    public static final String HUMAN_READABLE_NAME = "Data Provider Class";
    public static final String DIRECTORY = "Ui/DataProvider";
    public static final String CUSTOM_TYPE = "custom";
    public static final String COLLECTION_TYPE = "collection";
    public static final String DEFAULT_DATA_PROVIDER =
            "Magento\\Framework\\View\\Element\\UiComponent\\DataProvider\\DataProvider";
    public static final String SEARCH_RESULT_FACTORY =
            "Magento\\Ui\\DataProvider\\SearchResultFactory";
    private final String directory;

    /**
     * Ui Component data provider file constructor.
     *
     * @param moduleName String
     * @param className String
     * @param directory String
     */
    public UiComponentDataProviderFile(
            final @NotNull String moduleName,
            final @NotNull String className,
            final String directory
    ) {
        super(moduleName, className);
        this.directory = directory;
    }

    @Override
    public String getHumanReadableName() {
        return HUMAN_READABLE_NAME;
    }

    @Override
    public String getDirectory() {
        return directory == null ? DIRECTORY : directory;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }
}
