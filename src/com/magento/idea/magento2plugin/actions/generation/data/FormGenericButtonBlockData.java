/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import org.jetbrains.annotations.NotNull;

public class FormGenericButtonBlockData {

    private final String moduleName;
    private final String entityName;
    private final String entityId;

    /**
     * Generic button DTO constructor.
     *
     * @param moduleName String
     * @param entityName String
     * @param entityId String
     */
    public FormGenericButtonBlockData(
            final @NotNull String moduleName,
            final @NotNull String entityName,
            final @NotNull String entityId
    ) {
        this.moduleName = moduleName;
        this.entityName = entityName;
        this.entityId = entityId;
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
     * Get entity id.
     *
     * @return String
     */
    public String getEntityId() {
        return entityId;
    }
}
