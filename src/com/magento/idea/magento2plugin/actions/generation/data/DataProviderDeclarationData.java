/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class DataProviderDeclarationData {

    private final String moduleName;
    private final String virtualTypeName;
    private final String collectionFqn;
    private final String dataSource;
    private final String tableName;

    /**
     * Data Provider XML declaration data.
     *
     * @param moduleName String
     * @param virtualTypeName String
     * @param collectionFqn String
     * @param dataSource String
     */
    public DataProviderDeclarationData(
            final String moduleName,
            final String virtualTypeName,
            final String collectionFqn,
            final String dataSource,
            final String tableName
    ) {
        this.moduleName = moduleName;
        this.virtualTypeName = virtualTypeName;
        this.collectionFqn = collectionFqn;
        this.dataSource = dataSource;
        this.tableName = tableName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getVirtualTypeName() {
        return virtualTypeName;
    }

    public String getCollectionFqn() {
        return collectionFqn;
    }

    public String getDataSource() {
        return dataSource;
    }

    public String getTableName() {
        return tableName;
    }
}
