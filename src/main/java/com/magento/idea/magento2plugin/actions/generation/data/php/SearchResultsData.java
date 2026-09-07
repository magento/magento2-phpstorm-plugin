/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.php;

import org.jetbrains.annotations.NotNull;

public class SearchResultsData {

    private final String moduleName;
    private final String entityName;
    private final String dtoType;

    /**
     * Entity search results data transfer object constructor.
     *
     * @param moduleName String
     * @param entityName String
     * @param dtoType String
     */
    public SearchResultsData(
            final @NotNull String moduleName,
            final @NotNull String entityName,
            final @NotNull String dtoType
    ) {
        this.moduleName = moduleName;
        this.entityName = entityName;
        this.dtoType = dtoType;
    }

    /**
     * Get module name.
     *
     * @return String
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Get entity name.
     *
     * @return String
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Get entity DTO type.
     *
     * @return String
     */
    public String getDtoType() {
        return dtoType;
    }
}
