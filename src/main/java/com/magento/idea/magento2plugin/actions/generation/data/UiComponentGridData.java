/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"PMD.ExcessiveParameterList"})
public class UiComponentGridData {

    private final String moduleName;
    private final String area;
    private final String name;
    private final String idFieldName;
    private final String acl;
    private final String dataProviderName;
    private final String dataProviderPath;
    private final String entityName;
    private final UiComponentGridToolbarData gridToolbarData;
    private final List<Map<String, String>> columns;

    /**
     * Ui component grid data constructor.
     *
     * @param moduleName String
     * @param area String
     * @param name String
     * @param idFieldName String
     * @param acl String
     * @param dataProviderName String
     * @param dataProviderPath String
     * @param gridToolbarData UiComponentGridToolbarData
     */
    public UiComponentGridData(
            final String moduleName,
            final String area,
            final String name,
            final String idFieldName,
            final String acl,
            final String dataProviderName,
            final String dataProviderPath,
            final UiComponentGridToolbarData gridToolbarData
    ) {
        this(
                moduleName,
                area,
                name,
                idFieldName,
                acl,
                dataProviderName,
                dataProviderPath,
                null,
                gridToolbarData,
                new ArrayList<>()
        );
    }

    /**
     * Ui component grid data constructor.
     *
     * @param moduleName String
     * @param area String
     * @param name String
     * @param idFieldName String
     * @param acl String
     * @param dataProviderName String
     * @param dataProviderPath String
     * @param entityName String
     * @param gridToolbarData UiComponentGridToolbarData
     * @param columns List
     */
    public UiComponentGridData(
            final String moduleName,
            final String area,
            final String name,
            final String idFieldName,
            final String acl,
            final String dataProviderName,
            final String dataProviderPath,
            final String entityName,
            final UiComponentGridToolbarData gridToolbarData,
            final List<Map<String, String>> columns
    ) {
        this.moduleName = moduleName;
        this.area = area;
        this.name = name;
        this.idFieldName = idFieldName;
        this.acl = acl;
        this.dataProviderName = dataProviderName;
        this.dataProviderPath = dataProviderPath;
        this.entityName = entityName;
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
     * Get data provider name.
     *
     * @return String
     */
    public String getDataProviderName() {
        return dataProviderName;
    }

    /**
     * Get data provider path.
     *
     * @return String
     */
    public String getDataProviderPath() {
        return dataProviderPath;
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
     * Get ID field name.
     *
     * @return String
     */
    public String getIdFieldName() {
        return idFieldName;
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
