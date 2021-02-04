/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UiComponentGridData {
    private final String moduleName;
    private final String area;
    private final String name;
    private final String providerClassName;
    private final String idFieldName;
    private final String acl;
    private final UiComponentGridToolbarData gridToolbarData;
    private final List<Map<String, String>> columns;

    /**
     * Ui component grid data constructor.
     *
     * @param area Area
     * @param name Name
     * @param idFieldName Id field name
     * @param acl ACL
     * @param gridToolbarData Toolbar data
     */
    public UiComponentGridData(
            final String moduleName,
            final String area,
            final String name,
            final String providerClassName,
            final String idFieldName,
            final String acl,
            final UiComponentGridToolbarData gridToolbarData
    ) {
        this(
                moduleName,
                area,
                name,
                providerClassName,
                idFieldName,
                acl,
                gridToolbarData,
                new ArrayList<>()
        );
    }

    /**
     * Ui component grid data constructor.
     *
     * @param area Area
     * @param name Name
     * @param idFieldName Id field name
     * @param acl ACL
     * @param gridToolbarData Toolbar data
     * @param columns List
     */
    public UiComponentGridData(
            final String moduleName,
            final String area,
            final String name,
            final String providerClassName,
            final String idFieldName,
            final String acl,
            final UiComponentGridToolbarData gridToolbarData,
            final List<Map<String, String>> columns
    ) {
        this.moduleName = moduleName;
        this.area = area;
        this.name = name;
        this.providerClassName = providerClassName;
        this.idFieldName = idFieldName;
        this.acl = acl;
        this.gridToolbarData = gridToolbarData;
        this.columns = columns;
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
     * Get area.
     *
     * @return String
     */
    public String getArea() {
        return area;
    }

    /**
     * Get name.
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Get grid toolbar data.
     *
     * @return UiComponentGridToolbarData
     */
    public UiComponentGridToolbarData getGridToolbarData() {
        return gridToolbarData;
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
     * Get ID field name.
     *
     * @return String
     */
    public String getIdFieldName() {
        return idFieldName;
    }

    /**
     * Get data provider class name.
     *
     * @return String
     */
    public String getProviderClassName() {
        return providerClassName;
    }

    /**
     * Get entity columns data.
     *
     * @return List of columns properties.
     */
    public List<Map<String, String>> getColumns() {
        return columns;
    }
}
