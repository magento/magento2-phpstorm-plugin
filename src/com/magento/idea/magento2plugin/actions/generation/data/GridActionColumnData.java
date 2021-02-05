/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import org.jetbrains.annotations.NotNull;

public class GridActionColumnData {
    private final String moduleName;
    private final String entityName;
    private final String entityIdColumn;
    private final String editUrlPath;
    private final String deleteUrlPath;

    /**
     * Grid UI Component action column data.
     *
     * @param moduleName String
     * @param entityName String
     * @param entityIdColumn String
     * @param editUrlPath String
     * @param deleteUrlPath String
     */
    public GridActionColumnData(
            final @NotNull String moduleName,
            final @NotNull String entityName,
            final @NotNull String entityIdColumn,
            final @NotNull String editUrlPath,
            final @NotNull String deleteUrlPath
    ) {
        this.moduleName = moduleName;
        this.entityName = entityName;
        this.entityIdColumn = entityIdColumn;
        this.editUrlPath = editUrlPath;
        this.deleteUrlPath = deleteUrlPath;
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
     * Get entity id column name.
     *
     * @return String
     */
    public String getEntityIdColumn() {
        return entityIdColumn;
    }

    /**
     * Get edit url path.
     *
     * @return String
     */
    public String getEditUrlPath() {
        return editUrlPath;
    }

    /**
     * Get delete url path.
     *
     * @return String
     */
    public String getDeleteUrlPath() {
        return deleteUrlPath;
    }
}
