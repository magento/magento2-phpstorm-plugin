/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.magento.idea.magento2plugin.actions.generation.data.DbSchemaXmlData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DbSchemaGeneratorDataProviderUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DbSchemaGeneratorUtil;
import com.magento.idea.magento2plugin.magento.files.ModuleDbSchemaXml;
import com.magento.idea.magento2plugin.magento.packages.database.ColumnAttributes;
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
     * Test db_schema.xml file generation when columns provided as short entity properties.
     */
    public void testGenerateDbSchemaXmlFileForShortProperties() {
        final List<Map<String, String>> properties =
                DbSchemaGeneratorDataProviderUtil.generateEntityPropertiesForTest();

        final List<Map<String, String>> columnsData =
                DbSchemaGeneratorUtil.complementShortPropertiesByDefaults(properties);
        columnsData.add(0, DbSchemaGeneratorUtil.getTableIdentityColumnData("entity_id"));

        final DbSchemaXmlData dbSchemaXmlData = new DbSchemaXmlData(
                TABLE_NAME,
                TABLE_RESOURCE,
                TABLE_ENGINE,
                TABLE_COMMENT,
                columnsData
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
                ColumnAttributes.TYPE.getName(),
                TableColumnTypes.INT.getColumnType()
        );
        entityIdColumnData.put(ColumnAttributes.NAME.getName(), "entity_id");
        entityIdColumnData.put(ColumnAttributes.PADDING.getName(), "11");
        entityIdColumnData.put(ColumnAttributes.UNSIGNED.getName(), BOOLEAN_VALUE_TRUE);
        entityIdColumnData.put(ColumnAttributes.NULLABLE.getName(), BOOLEAN_VALUE_FALSE);
        entityIdColumnData.put(ColumnAttributes.IDENTITY.getName(), BOOLEAN_VALUE_TRUE);
        entityIdColumnData.put(ColumnAttributes.COMMENT.getName(), "Entity Id Column");
        columns.add(entityIdColumnData);
        final Map<String, String> nameColumnData = new LinkedHashMap<>();
        nameColumnData.put(
                ColumnAttributes.TYPE.getName(),
                TableColumnTypes.VARCHAR.getColumnType()
        );
        nameColumnData.put(ColumnAttributes.NAME.getName(), "name");
        nameColumnData.put(ColumnAttributes.NULLABLE.getName(), BOOLEAN_VALUE_FALSE);
        nameColumnData.put(ColumnAttributes.LENGTH.getName(), "255");
        nameColumnData.put(ColumnAttributes.DEFAULT.getName(), "John Smith");
        nameColumnData.put(ColumnAttributes.COMMENT.getName(), "Name Column");
        columns.add(nameColumnData);
        final Map<String, String> ageColumnData = new LinkedHashMap<>();
        ageColumnData.put(
                ColumnAttributes.TYPE.getName(),
                TableColumnTypes.INT.getColumnType()
        );
        ageColumnData.put(ColumnAttributes.NAME.getName(), "age");
        ageColumnData.put(ColumnAttributes.PADDING.getName(), "5");
        ageColumnData.put(ColumnAttributes.UNSIGNED.getName(), BOOLEAN_VALUE_TRUE);
        ageColumnData.put(ColumnAttributes.NULLABLE.getName(), BOOLEAN_VALUE_TRUE);
        ageColumnData.put(ColumnAttributes.IDENTITY.getName(), BOOLEAN_VALUE_FALSE);
        ageColumnData.put(ColumnAttributes.COMMENT.getName(), "Age Column");
        columns.add(ageColumnData);
        final Map<String, String> salaryColumnData = new LinkedHashMap<>();
        salaryColumnData.put(
                ColumnAttributes.TYPE.getName(),
                TableColumnTypes.DECIMAL.getColumnType()
        );
        salaryColumnData.put(ColumnAttributes.NAME.getName(), "salary");
        salaryColumnData.put(ColumnAttributes.PADDING.getName(), "5");
        salaryColumnData.put(ColumnAttributes.UNSIGNED.getName(), BOOLEAN_VALUE_TRUE);
        salaryColumnData.put(ColumnAttributes.NULLABLE.getName(), BOOLEAN_VALUE_FALSE);
        salaryColumnData.put(ColumnAttributes.PRECISION.getName(), "10");
        salaryColumnData.put(ColumnAttributes.SCALE.getName(), "2");
        salaryColumnData.put(ColumnAttributes.DEFAULT.getName(), "0.0");
        salaryColumnData.put(ColumnAttributes.COMMENT.getName(), "Salary Column");
        columns.add(salaryColumnData);
        final Map<String, String> dobColumnData = new LinkedHashMap<>();
        dobColumnData.put(
                ColumnAttributes.TYPE.getName(),
                TableColumnTypes.DATE.getColumnType()
        );
        dobColumnData.put(ColumnAttributes.NAME.getName(), "dob");
        dobColumnData.put(ColumnAttributes.NULLABLE.getName(), BOOLEAN_VALUE_TRUE);
        dobColumnData.put(ColumnAttributes.COMMENT.getName(), "Date Of The Birth Column");
        columns.add(dobColumnData);
        final Map<String, String> createdAtColumnData = new LinkedHashMap<>();
        createdAtColumnData.put(
                ColumnAttributes.TYPE.getName(),
                TableColumnTypes.TIMESTAMP.getColumnType()
        );
        createdAtColumnData.put(ColumnAttributes.NAME.getName(), "created_at");
        createdAtColumnData.put(ColumnAttributes.NULLABLE.getName(), BOOLEAN_VALUE_FALSE);
        createdAtColumnData.put(ColumnAttributes.ON_UPDATE.getName(), BOOLEAN_VALUE_FALSE);
        createdAtColumnData.put(
                ColumnAttributes.DEFAULT.getName(),
                CURRENT_TIMESTAMP_DEFAULT_VALUE
        );
        createdAtColumnData.put(ColumnAttributes.COMMENT.getName(), "Created At Column");
        columns.add(createdAtColumnData);
        final Map<String, String> updatedAtColumnData = new LinkedHashMap<>();
        updatedAtColumnData.put(
                ColumnAttributes.TYPE.getName(),
                TableColumnTypes.TIMESTAMP.getColumnType()
        );
        updatedAtColumnData.put(ColumnAttributes.NAME.getName(), "updated_at");
        updatedAtColumnData.put(ColumnAttributes.NULLABLE.getName(), BOOLEAN_VALUE_FALSE);
        updatedAtColumnData.put(ColumnAttributes.ON_UPDATE.getName(), BOOLEAN_VALUE_TRUE);
        updatedAtColumnData.put(
                ColumnAttributes.DEFAULT.getName(),
                CURRENT_TIMESTAMP_DEFAULT_VALUE
        );
        updatedAtColumnData.put(ColumnAttributes.COMMENT.getName(), "Updated At Column");
        columns.add(updatedAtColumnData);

        return columns;
    }
}
