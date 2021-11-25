/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"PMD.ExcessiveParameterList", "PMD.TooManyFields"})
public class EntityDataMapperData {

    private final String moduleName;
    private final String entityName;
    private final String modelName;
    private final String dtoName;
    private final String dtoInterfaceName;
    private final boolean hasDtoInterface;

    /**
     * Magento entity data mapper data constructor.
     *
     * @param moduleName String
     * @param entityName String
     * @param modelName String
     * @param dtoName String
     * @param dtoInterfaceName String
     * @param hasDtoInterface boolean
     */
    public EntityDataMapperData(
            final @NotNull String moduleName,
            final @NotNull String entityName,
            final @NotNull String modelName,
            final @NotNull String dtoName,
            final @NotNull String dtoInterfaceName,
            final boolean hasDtoInterface
    ) {
        this.moduleName = moduleName;
        this.entityName = entityName;
        this.modelName = modelName;
        this.dtoName = dtoName;
        this.dtoInterfaceName = dtoInterfaceName;
        this.hasDtoInterface = hasDtoInterface;
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
     * Get model name.
     *
     * @return String
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Get DTO name.
     *
     * @return String
     */
    public String getDtoName() {
        return dtoName;
    }

    /**
     * Get DTO interface name.
     *
     * @return String
     */
    public String getDtoInterfaceName() {
        return dtoInterfaceName;
    }

    /**
     * Check if DTO has interface.
     *
     * @return boolean
     */
    public boolean isHasDtoInterface() {
        return hasDtoInterface;
    }
}
