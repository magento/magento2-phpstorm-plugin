/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public class ModuleDbSchemaXml implements ModuleFileInterface {
    private static final ModuleDbSchemaXml INSTANCE = new ModuleDbSchemaXml();
    public static final String FILE_NAME = "db_schema.xml";
    public static final String TEMPLATE = "Magento Module Declarative Schema XML";

    //attributes
    public static final String XML_ATTR_TABLE_NAME = "name";
    public static final String XML_ATTR_COLUMN_NAME = "name";
    public static final String XML_ATTR_CONSTRAINT_TABLE_NAME = "table";
    public static final String XML_ATTR_CONSTRAINT_REFERENCE_TABLE_NAME = "referenceTable";
    public static final String XML_ATTR_CONSTRAINT_COLUMN_NAME = "column";
    public static final String XML_ATTR_CONSTRAINT_REFERENCE_COLUMN_NAME = "referenceColumn";
    //tags
    public static final String XML_TAG_SCHEMA = "schema";
    public static final String XML_TAG_TABLE = "table";
    public static final String XML_TAG_COLUMN = "column";
    public static final String XML_TAG_CONSTRAINT = "constraint";

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
