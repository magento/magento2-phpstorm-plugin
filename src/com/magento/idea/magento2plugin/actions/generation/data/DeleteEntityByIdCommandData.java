/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import org.jetbrains.annotations.NotNull;

public class DeleteEntityByIdCommandData {

    private final String moduleName;
    private final String entityName;
    private final String entityId;
    private final String modelName;
    private final String resourceModelName;

    /**
     * Delete Command DTO Constructor.
     *
     * @param moduleName String
     * @param entityName String
     * @param entityId String
     * @param modelName String
     * @param resourceModelName String
     */
    public DeleteEntityByIdCommandData(
            final @NotNull String moduleName,
            final @NotNull String entityName,
            final @NotNull String entityId,
            final @NotNull String modelName,
            final @NotNull String resourceModelName
    ) {
        this.moduleName = moduleName;
        this.entityName = entityName;
        this.entityId = entityId;
        this.modelName = modelName;
        this.resourceModelName = resourceModelName;
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
     * Get entity Id.
     *
     * @return String
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Get model name.
     *
     * @return String
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Get resource model name.
     *
     * @return String
     */
    public String getResourceModelName() {
        return resourceModelName;
    }
}
