/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages.database;

import java.util.LinkedList;
import java.util.List;

public enum TableColumnTypes {
    // binaries
    BLOB("blob"),
    MEDIUMBLOB("mediumblob"),
    LONGBLOB("longblob"),
    VARBINARY("varbinary"),
    // integers
    TINYINT("tinyint"),
    SMALLINT("smallint"),
    INT("int"),
    BIGINT("bigint"),
    // reals
    DECIMAL("decimal"),
    DOUBLE("double"),
    FLOAT("float"),
    // text
    VARCHAR("varchar"),
    TEXT("text"),
    MEDIUMTEXT("mediumtext"),
    LONGTEXT("longtext"),
    // boolean
    BOOLEAN("boolean"),
    // date
    DATETIME("datetime"),
    DATE("date"),
    TIMESTAMP("timestamp");

    private final String columnType;

    /**
     * Table Column Types ENUM constructor.
     *
     * @param tableColumnType String
     */
    TableColumnTypes(final String tableColumnType) {
        columnType = tableColumnType;
    }

    /**
     * Get table column type name.
     *
     * @return String
     */
    public String getColumnType() {
        return columnType;
    }

    /**
     * Get ENUM by its string representation.
     *
     * @param value String
     *
     * @return TableColumnTypes
     */
    public static TableColumnTypes getByValue(final String value) {
        for (final TableColumnTypes columnType : TableColumnTypes.values()) {
            if (columnType.getColumnType().equals(value)) {
                return columnType;
            }
        }
        return null;
    }

    /**
     * Get table available/supported column types list.
     *
     * @return List of available column types.
     */
    public static List<String> getTableColumnTypesList() {
        final List<String> availableColumnTypes = new LinkedList<>();

        for (final TableColumnTypes columnType : TableColumnTypes.values()) {
            availableColumnTypes.add(columnType.getColumnType());
        }

        return availableColumnTypes;
    }
}
