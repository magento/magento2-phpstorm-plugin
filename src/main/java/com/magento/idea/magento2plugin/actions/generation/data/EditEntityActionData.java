/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import org.jetbrains.annotations.NotNull;

public class EditEntityActionData {

    private final String entityName;
    private final String moduleName;
    private final String acl;
    private final String menuIdentifier;

    /**
     * Edit action data constructor.
     *
     * @param entityName String
     * @param moduleName String
     * @param acl String
     * @param menuIdentifier String
     */
    public EditEntityActionData(
            final @NotNull String entityName,
            final @NotNull String moduleName,
            final @NotNull String acl,
            final @NotNull String menuIdentifier
    ) {
        this.entityName = entityName;
        this.moduleName = moduleName;
        this.acl = acl;
        this.menuIdentifier = menuIdentifier;
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
     * Get menu.
     *
     * @return String
     */
    public String getMenu() {
        return menuIdentifier;
    }
}
