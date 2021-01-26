/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import org.jetbrains.annotations.NotNull;

public class SaveEntityCommandData {
    private final String moduleName;
    private final String entityName;
    private final String namespace;
    private final String classFqn;
    private final String modelClassFqn;
    private final String resourceModelClassFqn;
    private final String dataModelClassFqn;

    /**
     * Save Command DTO Constructor.
     *
     * @param moduleName String
     * @param entityName String
     * @param namespace String
     * @param classFqn String
     * @param modelClassFqn String
     * @param resourceModelClassFqn String
     * @param dataModelClassFqn String
     */
    public SaveEntityCommandData(
            final @NotNull String moduleName,
            final @NotNull String entityName,
            final @NotNull String namespace,
            final @NotNull String classFqn,
            final @NotNull String modelClassFqn,
            final @NotNull String resourceModelClassFqn,
            final @NotNull String dataModelClassFqn
    ) {
        this.moduleName = moduleName;
        this.entityName = entityName;
        this.namespace = namespace;
        this.classFqn = classFqn;
        this.modelClassFqn = modelClassFqn;
        this.resourceModelClassFqn = resourceModelClassFqn;
        this.dataModelClassFqn = dataModelClassFqn;
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
     * Get namespace.
     *
     * @return String
     */
    public String getNamespace() {
        return namespace;
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
     * Get entity model class FQN.
     *
     * @return String
     */
    public String getModelClassFqn() {
        return modelClassFqn;
    }

    /**
     * Get entity resource model class FQN.
     *
     * @return String
     */
    public String getResourceModelClassFqn() {
        return resourceModelClassFqn;
    }

    /**
     * Get entity DTO class FQN.
     *
     * @return String
     */
    public String getDataModelClassFqn() {
        return dataModelClassFqn;
    }
}
