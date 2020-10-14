/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class DbSchemaXmlSourceData {
    // currently available engines
    public static final String TABLE_ENGINE_INNODB = "innodb";
    public static final String TABLE_ENGINE_MEMORY = "memory";
    // currently available resources
    public static final String TABLE_RESOURCE_DEFAULT = "default";
    public static final String TABLE_RESOURCE_CHECKOUT = "checkout";
    public static final String TABLE_RESOURCE_SALES = "sales";
    // available column types
    // binaries
    public static final String COLUMN_TYPE_BLOB = "blob";
    public static final String COLUMN_TYPE_MEDIUMBLOB = "mediumblob";
    public static final String COLUMN_TYPE_LONGBLOB = "longblob";
    public static final String COLUMN_TYPE_VARBINARY = "varbinary";
    // integers
    public static final String COLUMN_TYPE_TINYINT = "tinyint";
    public static final String COLUMN_TYPE_SMALLINT = "smallint";
    public static final String COLUMN_TYPE_INT = "int";
    public static final String COLUMN_TYPE_BIGINT = "bigint";
    // reals
    public static final String COLUMN_TYPE_DECIMAL = "decimal";
    public static final String COLUMN_TYPE_DOUBLE = "double";
    public static final String COLUMN_TYPE_FLOAT = "float";
    // text
    public static final String COLUMN_TYPE_VARCHAR = "varchar";
    public static final String COLUMN_TYPE_TEXT = "text";
    public static final String COLUMN_TYPE_MEDIUMTEXT = "mediumtext";
    public static final String COLUMN_TYPE_LONGTEXT = "longtext";
    // boolean
    public static final String COLUMN_TYPE_BOOLEAN = "boolean";

    /**
     * Denying the possibility to initialize this class.
     */
    private DbSchemaXmlSourceData() {}

    /**
     * Get source list for available table engines.
     *
     * @return List
     */
    public static List<String> getTableEngineSource() {
        return new LinkedList<>(Arrays.asList(
                DbSchemaXmlSourceData.TABLE_ENGINE_INNODB,
                DbSchemaXmlSourceData.TABLE_ENGINE_MEMORY)
        );
    }

    /**
     * Get source list for available table resources.
     *
     * @return List
     */
    public static List<String> getTableResourceSource() {
        return new LinkedList<>(Arrays.asList(
                TABLE_RESOURCE_DEFAULT,
                TABLE_RESOURCE_CHECKOUT,
                TABLE_RESOURCE_SALES
        ));
    }

    /**
     * Get source list for available column types.
     *
     * @return List
     */
    public static List<String> getColumnTypes() {
        return new LinkedList<>(Arrays.asList(
                "",
                COLUMN_TYPE_BLOB,
                COLUMN_TYPE_MEDIUMBLOB,
                COLUMN_TYPE_LONGBLOB,
                COLUMN_TYPE_VARBINARY,
                COLUMN_TYPE_TINYINT,
                COLUMN_TYPE_SMALLINT,
                COLUMN_TYPE_INT,
                COLUMN_TYPE_BIGINT,
                COLUMN_TYPE_DECIMAL,
                COLUMN_TYPE_DOUBLE,
                COLUMN_TYPE_FLOAT,
                COLUMN_TYPE_VARCHAR,
                COLUMN_TYPE_TEXT,
                COLUMN_TYPE_MEDIUMTEXT,
                COLUMN_TYPE_LONGTEXT,
                COLUMN_TYPE_BOOLEAN
        ));
    }
}
