/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.magento.idea.magento2plugin.actions.generation.data.DbSchemaXmlData;
import com.magento.idea.magento2plugin.magento.files.ModuleDbSchemaXml;
import com.magento.idea.magento2plugin.magento.packages.database.TableColumnTypes;
import com.magento.idea.magento2plugin.magento.packages.database.TableEngines;
import com.magento.idea.magento2plugin.magento.packages.database.TableResources;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DbSchemaXmlGeneratorTest extends BaseGeneratorTestCase {
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/etc";
    private static final String TABLE_NAME = "test_table";
    private static final String TABLE_COMMENT = "Test Table";
    private static final String BOOLEAN_VALUE_TRUE = "true";
    private static final String BOOLEAN_VALUE_FALSE = "false";
    private static final String CURRENT_TIMESTAMP_DEFAULT_VALUE = "CURRENT_TIMESTAMP";
    private static final String TABLE_RESOURCE = TableResources.DEFAULT.getResource();
    private static final String TABLE_ENGINE = TableEngines.INNODB.getEngine();

    /**
     * Test whether db_schema.xml file generated correctly.
     */
    public void testGenerateDbSchemaXmlFile() {
        final DbSchemaXmlData dbSchemaXmlData = new DbSchemaXmlData(
                TABLE_NAME,
                TABLE_RESOURCE,
                TABLE_ENGINE,
                TABLE_COMMENT,
                createColumnsForTest()
        );
        final DbSchemaXmlGenerator dbSchemaXmlGenerator = new DbSchemaXmlGenerator(
                dbSchemaXmlData,
                myFixture.getProject(),
                MODULE_NAME
        );

        final String filePath = this.getFixturePath(ModuleDbSchemaXml.FILE_NAME);

        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(filePath),
                EXPECTED_DIRECTORY,
                dbSchemaXmlGenerator.generate("test")
        );
    }

    /**
     * Generate columns for testcase.
     *
     * @return List
     */
    @SuppressWarnings("PMD")
    private List<Map<String, String>> createColumnsForTest() {
        final List<Map<String, String>> columns = new LinkedList<>();
        final Map<String, String> entityIdColumnData = new LinkedHashMap<>();
        entityIdColumnData.put(
                ModuleDbSchemaXml.XML_ATTR_COLUMN_TYPE,
                TableColumnTypes.INT.getColumnType()
        );
        entityIdColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_NAME, "entity_id");
        entityIdColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_PADDING, "11");
        entityIdColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_UNSIGNED, BOOLEAN_VALUE_TRUE);
        entityIdColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_NULLABLE, BOOLEAN_VALUE_FALSE);
        entityIdColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_IDENTITY, BOOLEAN_VALUE_TRUE);
        entityIdColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_COMMENT, "Entity Id Column");
        columns.add(entityIdColumnData);
        final Map<String, String> nameColumnData = new LinkedHashMap<>();
        nameColumnData.put(
                ModuleDbSchemaXml.XML_ATTR_COLUMN_TYPE,
                TableColumnTypes.VARCHAR.getColumnType()
        );
        nameColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_NAME, "name");
        nameColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_NULLABLE, BOOLEAN_VALUE_FALSE);
        nameColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_LENGTH, "255");
        nameColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_DEFAULT, "John Smith");
        nameColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_COMMENT, "Name Column");
        columns.add(nameColumnData);
        final Map<String, String> ageColumnData = new LinkedHashMap<>();
        ageColumnData.put(
                ModuleDbSchemaXml.XML_ATTR_COLUMN_TYPE,
                TableColumnTypes.INT.getColumnType()
        );
        ageColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_NAME, "age");
        ageColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_PADDING, "5");
        ageColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_UNSIGNED, BOOLEAN_VALUE_TRUE);
        ageColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_NULLABLE, BOOLEAN_VALUE_TRUE);
        ageColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_IDENTITY, BOOLEAN_VALUE_FALSE);
        ageColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_COMMENT, "Age Column");
        columns.add(ageColumnData);
        final Map<String, String> salaryColumnData = new LinkedHashMap<>();
        salaryColumnData.put(
                ModuleDbSchemaXml.XML_ATTR_COLUMN_TYPE,
                TableColumnTypes.DECIMAL.getColumnType()
        );
        salaryColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_NAME, "salary");
        salaryColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_PADDING, "5");
        salaryColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_UNSIGNED, BOOLEAN_VALUE_TRUE);
        salaryColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_NULLABLE, BOOLEAN_VALUE_FALSE);
        salaryColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_PRECISION, "10");
        salaryColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_SCALE, "2");
        salaryColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_DEFAULT, "0.0");
        salaryColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_COMMENT, "Salary Column");
        columns.add(salaryColumnData);
        final Map<String, String> dobColumnData = new LinkedHashMap<>();
        dobColumnData.put(
                ModuleDbSchemaXml.XML_ATTR_COLUMN_TYPE,
                TableColumnTypes.DATE.getColumnType()
        );
        dobColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_NAME, "dob");
        dobColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_NULLABLE, BOOLEAN_VALUE_TRUE);
        dobColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_COMMENT, "Date Of The Birth Column");
        columns.add(dobColumnData);
        final Map<String, String> createdAtColumnData = new LinkedHashMap<>();
        createdAtColumnData.put(
                ModuleDbSchemaXml.XML_ATTR_COLUMN_TYPE,
                TableColumnTypes.TIMESTAMP.getColumnType()
        );
        createdAtColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_NAME, "created_at");
        createdAtColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_NULLABLE, BOOLEAN_VALUE_FALSE);
        createdAtColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_ON_UPDATE, BOOLEAN_VALUE_FALSE);
        createdAtColumnData.put(
                ModuleDbSchemaXml.XML_ATTR_COLUMN_DEFAULT,
                CURRENT_TIMESTAMP_DEFAULT_VALUE
        );
        createdAtColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_COMMENT, "Created At Column");
        columns.add(createdAtColumnData);
        final Map<String, String> updatedAtColumnData = new LinkedHashMap<>();
        updatedAtColumnData.put(
                ModuleDbSchemaXml.XML_ATTR_COLUMN_TYPE,
                TableColumnTypes.TIMESTAMP.getColumnType()
        );
        updatedAtColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_NAME, "updated_at");
        updatedAtColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_NULLABLE, BOOLEAN_VALUE_FALSE);
        updatedAtColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_ON_UPDATE, BOOLEAN_VALUE_TRUE);
        updatedAtColumnData.put(
                ModuleDbSchemaXml.XML_ATTR_COLUMN_DEFAULT,
                CURRENT_TIMESTAMP_DEFAULT_VALUE
        );
        updatedAtColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_COMMENT, "Updated At Column");
        columns.add(updatedAtColumnData);

        return columns;
    }
}
