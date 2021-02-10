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
    private final String classFqn;
    private final String namespace;

    /**
     * Generic button DTO constructor.
     *
     * @param moduleName String
     * @param entityName String
     * @param entityId String
     * @param classFqn String
     * @param namespace String
     */
    public FormGenericButtonBlockData(
            final @NotNull String moduleName,
            final @NotNull String entityName,
            final @NotNull String entityId,
            final @NotNull String classFqn,
            final @NotNull String namespace
    ) {
        this.moduleName = moduleName;
        this.entityName = entityName;
        this.entityId = entityId;
        this.classFqn = classFqn;
        this.namespace = namespace;
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

    /**
     * Get class FQN.
     *
     * @return String
     */
    public String getClassFqn() {
        return classFqn;
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
