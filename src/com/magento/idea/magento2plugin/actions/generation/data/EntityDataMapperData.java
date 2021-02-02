/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import org.jetbrains.annotations.NotNull;

public class EntityDataMapperData {
    private final String moduleName;
    private final String entityName;
    private final String namespace;
    private final String classFqn;
    private final String modelClassFqn;
    private final String dataModelClassFqn;

    /**
     * Magento entity data mapper data constructor.
     *
     * @param moduleName String
     * @param entityName String
     * @param namespace String
     * @param modelClassFqn String
     * @param dataModelClassFqn String
     */
    public EntityDataMapperData(
            final @NotNull String moduleName,
            final @NotNull String entityName,
            final @NotNull String namespace,
            final @NotNull String classFqn,
            final @NotNull String modelClassFqn,
            final @NotNull String dataModelClassFqn
    ) {
        this.moduleName = moduleName;
        this.entityName = entityName;
        this.namespace = namespace;
        this.classFqn = classFqn;
        this.modelClassFqn = modelClassFqn;
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
     * Get class fqn.
     *
     * @return String
     */
    public String getClassFqn() {
        return classFqn;
    }

    /**
     * Get model class fqn.
     *
     * @return String
     */
    public String getModelClassFqn() {
        return modelClassFqn;
    }

    /**
     * Get data model class fqn.
     *
     * @return String
     */
    public String getDataModelClassFqn() {
        return dataModelClassFqn;
    }
}
