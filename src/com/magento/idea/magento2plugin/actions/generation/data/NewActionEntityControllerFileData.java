/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import org.jetbrains.annotations.NotNull;

public class NewActionEntityControllerFileData {

    private final String entityName;
    private final String moduleName;
    private final String namespace;
    private final String acl;

    /**
     * Controller NewAction file constructor.
     *
     * @param entityName String
     * @param moduleName String
     * @param namespace String
     */
    public NewActionEntityControllerFileData(
            final @NotNull String entityName,
            final @NotNull String moduleName,
            final @NotNull String namespace,
            final @NotNull String acl
    ) {
        this.entityName = entityName;
        this.moduleName = moduleName;
        this.namespace = namespace;
        this.acl = acl;
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
     * Get acl.
     *
     * @return String
     */
    public String getAcl() {
        return acl;
    }
}
