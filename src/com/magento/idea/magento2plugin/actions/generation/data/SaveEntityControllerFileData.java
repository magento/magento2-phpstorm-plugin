/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import org.jetbrains.annotations.NotNull;

public class SaveEntityControllerFileData {

    private final String entityName;
    private final String moduleName;
    private final String acl;
    private final String entityId;
    private final String dtoName;
    private final String dtoInterfaceName;
    private final boolean hasDtoInterface;
    private final boolean hasSaveCommandInterface;

    /**
     * Controller save file constructor.
     *
     * @param entityName String
     * @param moduleName String
     * @param acl String
     * @param entityId String
     * @param dtoName String
     * @param dtoInterfaceName String
     * @param hasDtoInterface boolean
     * @param hasSaveCommandInterface boolean
     */
    public SaveEntityControllerFileData(
            final @NotNull String entityName,
            final @NotNull String moduleName,
            final @NotNull String acl,
            final @NotNull String entityId,
            final @NotNull String dtoName,
            final @NotNull String dtoInterfaceName,
            final boolean hasDtoInterface,
            final boolean hasSaveCommandInterface
    ) {
        this.entityName = entityName;
        this.moduleName = moduleName;
        this.acl = acl;
        this.entityId = entityId;
        this.dtoName = dtoName;
        this.dtoInterfaceName = dtoInterfaceName;
        this.hasDtoInterface = hasDtoInterface;
        this.hasSaveCommandInterface = hasSaveCommandInterface;
    }

    /**
     * Get entity id.
     *
     * @return String
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Get acl.
     *
     * @return String
     */
    public String getAcl() {
        return acl;
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
     * Get module name.
     *
     * @return String
     */
    public String getModuleName() {
        return moduleName;
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

    /**
     * Check if save command has interface.
     *
     * @return boolean
     */
    public boolean isHasSaveCommandInterface() {
        return hasSaveCommandInterface;
    }
}
