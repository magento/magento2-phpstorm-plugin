/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import org.jetbrains.annotations.NotNull;

public class SaveEntityControllerFileData {

    private final String entityName;
    private final String moduleName;
    private final String namespace;
    private final String saveCommandFqn;
    private final String dtoType;
    private final String acl;
    private final String entityId;

    /**
     * Controller save file constructor.
     *
     * @param entityName String
     * @param moduleName String
     * @param namespace String
     * @param saveCommandFqn String
     * @param acl String
     * @param dtoType String
     * @param entityId String
     */
    public SaveEntityControllerFileData(
            final @NotNull String entityName,
            final @NotNull String moduleName,
            final @NotNull String namespace,
            final @NotNull String saveCommandFqn,
            final @NotNull String dtoType,
            final @NotNull String acl,
            final @NotNull String entityId
    ) {
        this.entityName = entityName;
        this.moduleName = moduleName;
        this.namespace = namespace;
        this.saveCommandFqn = saveCommandFqn;
        this.dtoType = dtoType;
        this.acl = acl;
        this.entityId = entityId;
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
     * Get dto type.
     *
     * @return String
     */
    public  String getDtoType() {
        return dtoType;
    }

    /**
     * Get save command Fqn.
     *
     * @return String
     */
    public String getSaveCommandFqn() {
        return saveCommandFqn;
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
}
