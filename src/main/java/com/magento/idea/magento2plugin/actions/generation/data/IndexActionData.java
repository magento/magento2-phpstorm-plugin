/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import org.jetbrains.annotations.NotNull;

public class IndexActionData {

    private final String moduleName;
    private final String entityName;
    private final String acl;
    private final String menu;

    /**
     * Magento entity index adminhtml controller data constructor.
     *
     * @param moduleName String
     * @param entityName String
     * @param acl String
     * @param menu String
     */
    public IndexActionData(
            final @NotNull String moduleName,
            final @NotNull String entityName,
            final @NotNull String acl,
            final @NotNull String menu
    ) {
        this.moduleName = moduleName;
        this.entityName = entityName;
        this.acl = acl;
        this.menu = menu;
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
     * Get ACL resource id.
     *
     * @return String
     */
    public String getAcl() {
        return acl;
    }

    /**
     * Get menu resource id.
     *
     * @return String
     */
    public String getMenu() {
        return menu;
    }
}
