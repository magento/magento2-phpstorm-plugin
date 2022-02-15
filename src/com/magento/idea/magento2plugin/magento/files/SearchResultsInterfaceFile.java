/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import org.jetbrains.annotations.NotNull;

public class SearchResultsInterfaceFile extends AbstractPhpFile {

    public static final String HUMAN_READABLE_NAME
            = "Magento Entity Search Results Interface class";
    public static final String TEMPLATE = "Magento Entity Search Results Interface";
    private static final String CLASS_NAME_PATTERN = "%entityNameSearchResultsInterface";
    private static final String DIRECTORY = "Api/Data";

    /**
     * Search results interface file constructor.
     *
     * @param moduleName String
     * @param entityName String
     */
    public SearchResultsInterfaceFile(
            final @NotNull String moduleName,
            final @NotNull String entityName
    ) {
        super(moduleName, CLASS_NAME_PATTERN.replace("%entityName", entityName));
    }

    @Override
    public String getDirectory() {
        return DIRECTORY;
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
