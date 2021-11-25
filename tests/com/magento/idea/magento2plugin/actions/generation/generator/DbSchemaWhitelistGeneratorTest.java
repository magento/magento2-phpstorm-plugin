/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.magento.idea.magento2plugin.actions.generation.data.DbSchemaXmlData;
import com.magento.idea.magento2plugin.magento.files.ModuleDbSchemaWhitelistJson;
import com.magento.idea.magento2plugin.magento.files.ModuleDbSchemaXml;
import com.magento.idea.magento2plugin.magento.packages.database.TableColumnTypes;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DbSchemaWhitelistGeneratorTest extends BaseGeneratorTestCase {
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/etc";
    private static final String TABLE_NAME = "test_table";

    /**
     * Test whether db_schema_whitelist.json file generated correctly.
     */
    public void testGenerateDbSchemaWhitelistJsonFile() {
        final DbSchemaXmlData dbSchemaXmlData = new DbSchemaXmlData(
                TABLE_NAME,
                "",
                "",
                "",
                createColumnsForTest()
        );
        final DbSchemaWhitelistJsonGenerator dbSchemaWhitelistJsonGenerator =
                new DbSchemaWhitelistJsonGenerator(
                        myFixture.getProject(),
                        dbSchemaXmlData,
                        MODULE_NAME
                );

        final String filePath = this.getFixturePath(ModuleDbSchemaWhitelistJson.FILE_NAME);

        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(filePath),
                EXPECTED_DIRECTORY,
                dbSchemaWhitelistJsonGenerator.generate("test")
        );
    }

    /**
     * Generate columns for testcase.
     *
     * @return List
     */
    private List<Map<String, String>> createColumnsForTest() {
        final List<Map<String, String>> columns = new LinkedList<>();
        final Map<String, String> entityIdColumnData = new LinkedHashMap<>();
        entityIdColumnData.put(
                ModuleDbSchemaXml.XML_ATTR_COLUMN_TYPE,
                TableColumnTypes.INT.getColumnType()
        );
        entityIdColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_NAME, "entity_id");
        entityIdColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_IDENTITY, "true");
        columns.add(entityIdColumnData);
        final Map<String, String> nameColumnData = new LinkedHashMap<>();
        nameColumnData.put(
                ModuleDbSchemaXml.XML_ATTR_COLUMN_TYPE,
                TableColumnTypes.VARCHAR.getColumnType()
        );
        nameColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_NAME, "name");
        columns.add(nameColumnData);
        final Map<String, String> ageColumnData = new LinkedHashMap<>();
        ageColumnData.put(
                ModuleDbSchemaXml.XML_ATTR_COLUMN_TYPE,
                TableColumnTypes.INT.getColumnType()
        );
        ageColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_NAME, "age");
        columns.add(ageColumnData);
        final Map<String, String> salaryColumnData = new LinkedHashMap<>();
        salaryColumnData.put(
                ModuleDbSchemaXml.XML_ATTR_COLUMN_TYPE,
                TableColumnTypes.DECIMAL.getColumnType()
        );
        salaryColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_NAME, "salary");
        columns.add(salaryColumnData);
        final Map<String, String> dobColumnData = new LinkedHashMap<>();
        dobColumnData.put(
                ModuleDbSchemaXml.XML_ATTR_COLUMN_TYPE,
                TableColumnTypes.DATE.getColumnType()
        );
        dobColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_NAME, "dob");
        columns.add(dobColumnData);
        final Map<String, String> createdAtColumnData = new LinkedHashMap<>();
        createdAtColumnData.put(
                ModuleDbSchemaXml.XML_ATTR_COLUMN_TYPE,
                TableColumnTypes.TIMESTAMP.getColumnType()
        );
        createdAtColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_NAME, "created_at");
        columns.add(createdAtColumnData);
        final Map<String, String> updatedAtColumnData = new LinkedHashMap<>();
        updatedAtColumnData.put(
                ModuleDbSchemaXml.XML_ATTR_COLUMN_TYPE,
                TableColumnTypes.TIMESTAMP.getColumnType()
        );
        updatedAtColumnData.put(ModuleDbSchemaXml.XML_ATTR_COLUMN_NAME, "updated_at");
        columns.add(updatedAtColumnData);

        return columns;
    }
}
