/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.dialog;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"PMD.ExcessiveParameterList", "PMD.TooManyFields"})
public class NewEntityDialogData implements DialogData {

    // General tab data.
    private final String entityName;
    private final String tableName;
    private final String idFieldName;
    private final String tableEngine;
    private final String tableResource;
    private final boolean adminUiComponents;
    private final boolean dtoInterface;
    private final boolean webApi;

    // Admin UI Components tab data.
    private final String route;
    private final String formLabel;
    private final String formName;
    private final String gridName;
    private final boolean toolbar;
    private final boolean toolbarBookmarks;
    private final boolean toolbarColumnsControl;
    private final boolean toolbarListingFilters;
    private final boolean toolbarListingPaging;
    private final boolean toolbarFullTextSearch;

    // Acl tab data.
    private final String parentAclId;
    private final String aclId;
    private final String aclTitle;

    // Menu tab data.
    private final String parentMenuId;
    private final int menuSortOrder;
    private final String menuId;
    private final String menuTitle;

    // Properties tab data.
    private final String properties;

    /**
     * New entity dialog data.
     *
     * @param entityName String
     * @param tableName String
     * @param idFieldName String
     * @param tableEngine String
     * @param tableResource String
     * @param hasAdminUiComponents boolean
     * @param hasDtoInterface boolean
     * @param hasWebApi boolean
     * @param route String
     * @param formLabel String
     * @param formName String
     * @param gridName String
     * @param hasToolbar boolean
     * @param hasToolbarBookmarks boolean
     * @param hasToolbarColumnsControl boolean
     * @param hasToolbarListingFilters boolean
     * @param hasToolbarListingPaging boolean
     * @param hasToolbarFullTextSearch boolean
     * @param parentAclId String
     * @param aclId String
     * @param aclTitle String
     * @param parentMenuId String
     * @param menuSortOrder int
     * @param menuId String
     * @param menuTitle String
     * @param properties String
     */
    public NewEntityDialogData(
            final @NotNull String entityName,
            final @NotNull String tableName,
            final @NotNull String idFieldName,
            final @NotNull String tableEngine,
            final @NotNull String tableResource,
            final boolean hasAdminUiComponents,
            final boolean hasDtoInterface,
            final boolean hasWebApi,
            final @NotNull String route,
            final @NotNull String formLabel,
            final @NotNull String formName,
            final @NotNull String gridName,
            final boolean hasToolbar,
            final boolean hasToolbarBookmarks,
            final boolean hasToolbarColumnsControl,
            final boolean hasToolbarListingFilters,
            final boolean hasToolbarListingPaging,
            final boolean hasToolbarFullTextSearch,
            final @NotNull String parentAclId,
            final @NotNull String aclId,
            final @NotNull String aclTitle,
            final @NotNull String parentMenuId,
            final int menuSortOrder,
            final @NotNull String menuId,
            final @NotNull String menuTitle,
            final @NotNull String properties
    ) {
        this.entityName = entityName;
        this.tableName = tableName;
        this.idFieldName = idFieldName;
        this.tableEngine = tableEngine;
        this.tableResource = tableResource;
        this.adminUiComponents = hasAdminUiComponents;
        this.dtoInterface = hasDtoInterface;
        this.webApi = hasWebApi;
        this.route = route;
        this.formLabel = formLabel;
        this.formName = formName;
        this.gridName = gridName;
        this.toolbar = hasToolbar;
        this.toolbarBookmarks = hasToolbarBookmarks;
        this.toolbarColumnsControl = hasToolbarColumnsControl;
        this.toolbarListingFilters = hasToolbarListingFilters;
        this.toolbarListingPaging = hasToolbarListingPaging;
        this.toolbarFullTextSearch = hasToolbarFullTextSearch;
        this.parentAclId = parentAclId;
        this.aclId = aclId;
        this.aclTitle = aclTitle;
        this.parentMenuId = parentMenuId;
        this.menuSortOrder = menuSortOrder;
        this.menuId = menuId;
        this.menuTitle = menuTitle;
        this.properties = properties;
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
     * Get table name.
     *
     * @return String
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Get id field name.
     *
     * @return String
     */
    public String getIdFieldName() {
        return idFieldName;
    }

    /**
     * Get table engine.
     *
     * @return String
     */
    public String getTableEngine() {
        return tableEngine;
    }

    /**
     * Get table resource.
     *
     * @return String
     */
    public String getTableResource() {
        return tableResource;
    }

    /**
     * Check if has admin ui components.
     *
     * @return boolean
     */
    public boolean hasAdminUiComponents() {
        return adminUiComponents;
    }

    /**
     * Check if has dto interface.
     *
     * @return boolean
     */
    public boolean hasDtoInterface() {
        return dtoInterface;
    }

    /**
     * Check if has web api services.
     *
     * @return boolean
     */
    public boolean hasWebApi() {
        return webApi;
    }

    /**
     * Get route.
     *
     * @return String
     */
    public String getRoute() {
        return route;
    }

    /**
     * Get form label.
     *
     * @return String
     */
    public String getFormLabel() {
        return formLabel;
    }

    /**
     * Get form name.
     *
     * @return String
     */
    public String getFormName() {
        return formName;
    }

    /**
     * Get grid name.
     *
     * @return String
     */
    public String getGridName() {
        return gridName;
    }

    /**
     * Check if has toolbar.
     *
     * @return boolean
     */
    public boolean hasToolbar() {
        return toolbar;
    }

    /**
     * Check if has toolbar bookmarks.
     *
     * @return boolean
     */
    public boolean hasToolbarBookmarks() {
        return toolbarBookmarks;
    }

    /**
     * Check if has toolbar columns control.
     *
     * @return boolean
     */
    public boolean hasToolbarColumnsControl() {
        return toolbarColumnsControl;
    }

    /**
     * Check if has toolbar listing filters.
     *
     * @return boolean
     */
    public boolean hasToolbarListingFilters() {
        return toolbarListingFilters;
    }

    /**
     * Check if has toolbar listing paging.
     *
     * @return boolean
     */
    public boolean hasToolbarListingPaging() {
        return toolbarListingPaging;
    }

    /**
     * Check if has toolbar fulltext search.
     *
     * @return boolean
     */
    public boolean hasToolbarFullTextSearch() {
        return toolbarFullTextSearch;
    }

    /**
     * Get parent acl id.
     *
     * @return String
     */
    public String getParentAclId() {
        return parentAclId;
    }

    /**
     * Get acl id.
     *
     * @return String
     */
    public String getAclId() {
        return aclId;
    }

    /**
     * Get acl title.
     *
     * @return String
     */
    public String getAclTitle() {
        return aclTitle;
    }

    /**
     * Get parent menu id.
     *
     * @return String
     */
    public String getParentMenuId() {
        return parentMenuId;
    }

    /**
     * Get menu sort order.
     *
     * @return int
     */
    public int getMenuSortOrder() {
        return menuSortOrder;
    }

    /**
     * Get menu id.
     *
     * @return String
     */
    public String getMenuId() {
        return menuId;
    }

    /**
     * Get menu title.
     *
     * @return String
     */
    public String getMenuTitle() {
        return menuTitle;
    }

    /**
     * Get properties.
     *
     * @return String
     */
    public String getProperties() {
        return properties;
    }
}
