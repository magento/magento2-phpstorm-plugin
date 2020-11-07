/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleDbSchemaXml;

/**
 * Test table and column names completion in the db_schema.xml file.
 */
public class TableAndColumnNameCompletionRegistrarTest extends CompletionXmlFixtureTestCase {
    private static final String CATALOG_PRODUCT_ENTITY_TABLE_NAME = "catalog_product_entity";

    /**
     * The `name` attribute of the `table` tag in `db_schema.xml` file must
     * have completion based on table and column names index.
     */
    public void testTableNameMustHaveCompletion() {
        final String filePath = this.getFixturePath(ModuleDbSchemaXml.FILE_NAME);
        assertCompletionContains(filePath, "catalog_category_entity");
    }

    /**
     * The `table` attribute of the `constraint` tag in `db_schema.xml` file must
     * have completion based on table and column names index.
     */
    public void testConstraintTagTableMustHaveCompletion() {
        final String filePath = this.getFixturePath(ModuleDbSchemaXml.FILE_NAME);
        assertCompletionContains(filePath, CATALOG_PRODUCT_ENTITY_TABLE_NAME);
    }

    /**
     * The `referenceTable` attribute of the `constraint` tag in `db_schema.xml` file must
     * have completion based on table and column names index.
     */
    public void testConstraintTagReferenceTableMustHaveCompletion() {
        final String filePath = this.getFixturePath(ModuleDbSchemaXml.FILE_NAME);
        assertCompletionContains(filePath, CATALOG_PRODUCT_ENTITY_TABLE_NAME);
    }

    /**
     * The `column` attribute of the `constraint` tag in `db_schema.xml` file must
     * have completion based on table and column names index.
     */
    public void testConstraintColumnNameMustHaveCompletion() {
        final String filePath = this.getFixturePath(ModuleDbSchemaXml.FILE_NAME);
        assertCompletionContains(filePath, "children_count");
    }

    /**
     * The `referenceColumn` attribute of the `constraint` tag in `db_schema.xml` file must
     * have completion based on table and column names index.
     */
    public void testConstraintReferenceColumnNameMustHaveCompletion() {
        final String filePath = this.getFixturePath(ModuleDbSchemaXml.FILE_NAME);
        assertCompletionContains(filePath, "attribute_set_id");
    }
}
