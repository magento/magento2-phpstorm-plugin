/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import org.jetbrains.annotations.NotNull;

public class DeleteEntityControllerFileData {

    private final String entityName;
    private final String moduleName;
    private final String namespace;
    private final String deleteCommandFqn;
    private final String acl;
    private final String entityId;

    /**
     * Controller Delete file constructor.
     *
     * @param entityName String
     * @param moduleName String
     * @param namespace String
     * @param acl String
     * @param entityId String
     */
    public DeleteEntityControllerFileData(
            final @NotNull String entityName,
            final @NotNull String moduleName,
            final @NotNull String namespace,
            final @NotNull String deleteCommandFqn,
            final @NotNull String acl,
            final @NotNull String entityId
    ) {
        this.entityName = entityName;
        this.moduleName = moduleName;
        this.namespace = namespace;
        this.deleteCommandFqn = deleteCommandFqn;
        this.acl = acl;
        this.entityId = entityId;
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
     * Get namespace.
     *
     * @return String
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Get delete command Fqn.
     *
     * @return String
     */
    public String getDeleteCommandFqn() {
        return deleteCommandFqn;
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
}
