/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleDbSchemaXml;

/**
 * Test table and columns name references in the db_schema.xml file.
 */
public class TableNameAndColumnReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {

    /**
     * The `name` attribute of the `table` tag in a `db_schema.xml` must
     * have reference to the `name` attribute of the another `table` tag.
     */
    public void testTableTagMustHaveReference() {
        final String filePath = this.getFixturePath(ModuleDbSchemaXml.FILE_NAME);
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlTag(ModuleDbSchemaXml.XML_TAG_TABLE);
    }

    /**
     * The `table` attribute of the `constraint` tag in a `db_schema.xml` must
     * have reference to the `name` attribute of the another `table` tag.
     */
    public void testConstraintTableTagMustHaveReference() {
        final String filePath = this.getFixturePath(ModuleDbSchemaXml.FILE_NAME);
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlTag(ModuleDbSchemaXml.XML_TAG_TABLE);
    }

    /**
     * The `referenceTable` attribute of the `constraint` tag in a `db_schema.xml` must
     * have reference to the `name` attribute of the another `table` tag.
     */
    public void testConstraintReferenceTableTagMustHaveReference() {
        final String filePath = this.getFixturePath(ModuleDbSchemaXml.FILE_NAME);
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlTag(ModuleDbSchemaXml.XML_TAG_TABLE);
    }

    /**
     * The `column` attribute of the `constraint` tag in a `db_schema.xml` must
     * have reference to the `name` attribute of the `column` tag.
     */
    public void testConstraintColumnTagMustHaveReference() {
        final String filePath = this.getFixturePath(ModuleDbSchemaXml.FILE_NAME);
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlTag(ModuleDbSchemaXml.XML_TAG_COLUMN);
    }

    /**
     * The `referenceColumn` attribute of the `constraint` tag in a `db_schema.xml` must
     * have reference to the `name` attribute of the `column` tag.
     */
    public void testConstraintReferenceColumnTagMustHaveReference() {
        final String filePath = this.getFixturePath(ModuleDbSchemaXml.FILE_NAME);
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlTag(ModuleDbSchemaXml.XML_TAG_COLUMN);
    }
}
