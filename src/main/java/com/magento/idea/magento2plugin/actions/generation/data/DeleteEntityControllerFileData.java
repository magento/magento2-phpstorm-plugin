/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import org.jetbrains.annotations.NotNull;

public class DeleteEntityControllerFileData {

    private final String entityName;
    private final String moduleName;
    private final String acl;
    private final String entityId;
    private final boolean hasDeleteCommandInterface;

    /**
     * Controller Delete file constructor.
     *
     * @param entityName String
     * @param moduleName String
     * @param acl String
     * @param entityId String
     * @param hasDeleteCommandInterface boolean
     */
    public DeleteEntityControllerFileData(
            final @NotNull String entityName,
            final @NotNull String moduleName,
            final @NotNull String acl,
            final @NotNull String entityId,
            final boolean hasDeleteCommandInterface
    ) {
        this.entityName = entityName;
        this.moduleName = moduleName;
        this.acl = acl;
        this.entityId = entityId;
        this.hasDeleteCommandInterface = hasDeleteCommandInterface;
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
     * Get acl.
     *
     * @return String
     */
    public String getAcl() {
        return acl;
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
     * Check if delete command has Web API interface.
     *
     * @return boolean
     */
    public boolean isHasDeleteCommandInterface() {
        return hasDeleteCommandInterface;
    }
}
