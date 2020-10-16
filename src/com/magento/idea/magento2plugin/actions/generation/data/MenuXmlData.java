/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class MenuXmlData {

    private final String parentMenuItem;
    private final String sortOrder;
    private final String moduleName;
    private final String menuIdentifier;
    private final String title;
    private final String acl;
    private final String action;

    /**
     * Menu XML Data.
     *
     * @param parentMenuItem String
     * @param sortOrder String
     * @param moduleName String
     * @param menuIdentifier String
     * @param title String
     */
    public MenuXmlData(
            final String parentMenuItem,
            final String sortOrder,
            final String moduleName,
            final String menuIdentifier,
            final String title,
            final String acl,
            final String action
    ) {
        this.parentMenuItem = parentMenuItem;
        this.sortOrder = sortOrder;
        this.moduleName = moduleName;
        this.menuIdentifier = menuIdentifier;
        this.title = title;
        this.acl = acl;
        this.action = action;
    }

    public String getParentMenuItem() {
        return parentMenuItem;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getMenuIdentifier() {
        return menuIdentifier;
    }

    public String getTitle() {
        return title;
    }

    public String getAcl() {
        return acl;
    }

    public String getAction() {
        return action;
    }
}
