/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.magento.idea.magento2plugin.actions.generation.data.DbSchemaXmlData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DbSchemaGeneratorDataProviderUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DbSchemaGeneratorUtil;
import com.magento.idea.magento2plugin.magento.files.ModuleDbSchemaWhitelistJson;
import com.magento.idea.magento2plugin.magento.packages.database.ColumnAttributes;
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
     * Test whether db_schema_whitelist.json file generated correctly
     * when columns provided as short entity properties.
     */
    public void testGenerateDbSchemaWhitelistJsonFileForShortProperties() {
        final List<Map<String, String>> properties =
                DbSchemaGeneratorDataProviderUtil.generateEntityPropertiesForTest();

        final List<Map<String, String>> columnsData =
                DbSchemaGeneratorUtil.complementShortPropertiesByDefaults(properties);
        columnsData.add(0, DbSchemaGeneratorUtil.getTableIdentityColumnData("entity_id"));

        final DbSchemaXmlData dbSchemaXmlData = new DbSchemaXmlData(
                TABLE_NAME,
                "",
                "",
                "",
                columnsData
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
                ColumnAttributes.TYPE.getName(),
                TableColumnTypes.INT.getColumnType()
        );
        entityIdColumnData.put(ColumnAttributes.NAME.getName(), "entity_id");
        entityIdColumnData.put(ColumnAttributes.IDENTITY.getName(), "true");
        columns.add(entityIdColumnData);
        final Map<String, String> nameColumnData = new LinkedHashMap<>();
        nameColumnData.put(
                ColumnAttributes.TYPE.getName(),
                TableColumnTypes.VARCHAR.getColumnType()
        );
        nameColumnData.put(ColumnAttributes.NAME.getName(), "name");
        columns.add(nameColumnData);
        final Map<String, String> ageColumnData = new LinkedHashMap<>();
        ageColumnData.put(
                ColumnAttributes.TYPE.getName(),
                TableColumnTypes.INT.getColumnType()
        );
        ageColumnData.put(ColumnAttributes.NAME.getName(), "age");
        columns.add(ageColumnData);
        final Map<String, String> salaryColumnData = new LinkedHashMap<>();
        salaryColumnData.put(
                ColumnAttributes.TYPE.getName(),
                TableColumnTypes.DECIMAL.getColumnType()
        );
        salaryColumnData.put(ColumnAttributes.NAME.getName(), "salary");
        columns.add(salaryColumnData);
        final Map<String, String> dobColumnData = new LinkedHashMap<>();
        dobColumnData.put(
                ColumnAttributes.TYPE.getName(),
                TableColumnTypes.DATE.getColumnType()
        );
        dobColumnData.put(ColumnAttributes.NAME.getName(), "dob");
        columns.add(dobColumnData);
        final Map<String, String> createdAtColumnData = new LinkedHashMap<>();
        createdAtColumnData.put(
                ColumnAttributes.TYPE.getName(),
                TableColumnTypes.TIMESTAMP.getColumnType()
        );
        createdAtColumnData.put(ColumnAttributes.NAME.getName(), "created_at");
        columns.add(createdAtColumnData);
        final Map<String, String> updatedAtColumnData = new LinkedHashMap<>();
        updatedAtColumnData.put(
                ColumnAttributes.TYPE.getName(),
                TableColumnTypes.TIMESTAMP.getColumnType()
        );
        updatedAtColumnData.put(ColumnAttributes.NAME.getName(), "updated_at");
        columns.add(updatedAtColumnData);

        return columns;
    }
}
