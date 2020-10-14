/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;
import com.magento.idea.magento2plugin.actions.generation.data.DbSchemaXmlSourceData;

import java.util.ArrayList;
import java.util.List;

public class ModuleDbSchemaXml implements ModuleFileInterface {
    private static final ModuleDbSchemaXml INSTANCE = new ModuleDbSchemaXml();
    public static final String FILE_NAME = "db_schema.xml";
    public static final String TEMPLATE = "Magento Module Declarative Schema XML";

    //attributes
    public static final String XML_ATTR_TABLE_NAME = "name";
    public static final String XML_ATTR_TABLE_RESOURCE = "resource";
    public static final String XML_ATTR_TABLE_ENGINE = "engine";
    public static final String XML_ATTR_TABLE_COMMENT = "comment";
    public static final String XML_ATTR_CONSTRAINT_TABLE_NAME = "table";
    public static final String XML_ATTR_CONSTRAINT_REFERENCE_TABLE_NAME = "referenceTable";
    public static final String XML_ATTR_CONSTRAINT_COLUMN_NAME = "column";
    public static final String XML_ATTR_CONSTRAINT_REFERENCE_COLUMN_NAME = "referenceColumn";
    public static final String XML_ATTR_COLUMN_NAME = "name";
    public static final String XML_ATTR_COLUMN_TYPE = "xsi:type";
    public static final String XML_ATTR_COLUMN_PADDING = "padding";
    public static final String XML_ATTR_COLUMN_UNSIGNED = "unsigned";
    public static final String XML_ATTR_COLUMN_NULLABLE = "nullable";
    public static final String XML_ATTR_COLUMN_IDENTITY = "identity";
    public static final String XML_ATTR_COLUMN_COMMENT = "comment";
    public static final String XML_ATTR_COLUMN_DEFAULT = "default";
    public static final String XML_ATTR_COLUMN_LENGTH = "length";
    public static final String XML_ATTR_COLUMN_SCALE = "scale";
    public static final String XML_ATTR_COLUMN_PRECISION = "precision";
    public static final String XML_ATTR_COLUMN_ON_UPDATE = "on_update";

    //tags
    public static final String XML_TAG_SCHEMA = "schema";
    public static final String XML_TAG_TABLE = "table";
    public static final String XML_TAG_COLUMN = "column";
    public static final String XML_TAG_CONSTRAINT = "constraint";

    public static List<String> getAllowedAttributes(final String columnType) {
        List<String> allowedAttributes = new ArrayList<>();

        switch (columnType) {
            case DbSchemaXmlSourceData.COLUMN_TYPE_BLOB:
            case DbSchemaXmlSourceData.COLUMN_TYPE_MEDIUMBLOB:
            case DbSchemaXmlSourceData.COLUMN_TYPE_LONGBLOB:
                allowedAttributes.add(XML_ATTR_COLUMN_NAME);
                allowedAttributes.add(XML_ATTR_COLUMN_NULLABLE);
                allowedAttributes.add(XML_ATTR_COLUMN_COMMENT);
                break;
            case DbSchemaXmlSourceData.COLUMN_TYPE_VARBINARY:
                allowedAttributes.add(XML_ATTR_COLUMN_NAME);
                allowedAttributes.add(XML_ATTR_COLUMN_DEFAULT);
                allowedAttributes.add(XML_ATTR_COLUMN_NULLABLE);
                allowedAttributes.add(XML_ATTR_COLUMN_LENGTH);
                allowedAttributes.add(XML_ATTR_COLUMN_COMMENT);
                break;
            case DbSchemaXmlSourceData.COLUMN_TYPE_TINYINT:
            case DbSchemaXmlSourceData.COLUMN_TYPE_SMALLINT:
            case DbSchemaXmlSourceData.COLUMN_TYPE_INT:
            case DbSchemaXmlSourceData.COLUMN_TYPE_BIGINT:
                allowedAttributes.add(XML_ATTR_COLUMN_NAME);
                allowedAttributes.add(XML_ATTR_COLUMN_PADDING);
                allowedAttributes.add(XML_ATTR_COLUMN_UNSIGNED);
                allowedAttributes.add(XML_ATTR_COLUMN_NULLABLE);
                allowedAttributes.add(XML_ATTR_COLUMN_IDENTITY);
                allowedAttributes.add(XML_ATTR_COLUMN_DEFAULT);
                allowedAttributes.add(XML_ATTR_COLUMN_COMMENT);
                break;
            case DbSchemaXmlSourceData.COLUMN_TYPE_DECIMAL:
            case DbSchemaXmlSourceData.COLUMN_TYPE_DOUBLE:
            case DbSchemaXmlSourceData.COLUMN_TYPE_FLOAT:
                allowedAttributes.add(XML_ATTR_COLUMN_NAME);
                allowedAttributes.add(XML_ATTR_COLUMN_DEFAULT);
                allowedAttributes.add(XML_ATTR_COLUMN_SCALE);
                allowedAttributes.add(XML_ATTR_COLUMN_PRECISION);
                allowedAttributes.add(XML_ATTR_COLUMN_UNSIGNED);
                allowedAttributes.add(XML_ATTR_COLUMN_NULLABLE);
                allowedAttributes.add(XML_ATTR_COLUMN_COMMENT);
                break;
            case DbSchemaXmlSourceData.COLUMN_TYPE_VARCHAR:
            case DbSchemaXmlSourceData.COLUMN_TYPE_TEXT:
            case DbSchemaXmlSourceData.COLUMN_TYPE_MEDIUMTEXT:
            case DbSchemaXmlSourceData.COLUMN_TYPE_LONGTEXT:
                allowedAttributes.add(XML_ATTR_COLUMN_NAME);
                allowedAttributes.add(XML_ATTR_COLUMN_NULLABLE);
                allowedAttributes.add(XML_ATTR_COLUMN_LENGTH);
                allowedAttributes.add(XML_ATTR_COLUMN_DEFAULT);
                allowedAttributes.add(XML_ATTR_COLUMN_COMMENT);
                break;
            case DbSchemaXmlSourceData.COLUMN_TYPE_BOOLEAN:
                allowedAttributes.add(XML_ATTR_COLUMN_NAME);
                allowedAttributes.add(XML_ATTR_COLUMN_DEFAULT);
                allowedAttributes.add(XML_ATTR_COLUMN_NULLABLE);
                allowedAttributes.add(XML_ATTR_COLUMN_COMMENT);
                break;
            default:
                break;
        }
        return allowedAttributes;
    }

    public static ModuleDbSchemaXml getInstance() {
        return INSTANCE;
    }

    @Override
    public String getFileName() {
        return FILE_NAME;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public Language getLanguage() {
        return XMLLanguage.INSTANCE;
    }
}
