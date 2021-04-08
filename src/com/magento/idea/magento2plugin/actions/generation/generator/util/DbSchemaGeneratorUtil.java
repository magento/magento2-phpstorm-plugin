/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util;

import com.magento.idea.magento2plugin.magento.files.ModuleDbSchemaXml;
import com.magento.idea.magento2plugin.magento.packages.PropertiesTypes;
import com.magento.idea.magento2plugin.magento.packages.database.ColumnAttributes;
import com.magento.idea.magento2plugin.magento.packages.database.PropertyToDefaultTypeMapperUtil;
import com.magento.idea.magento2plugin.magento.packages.database.TableColumnTypes;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class DbSchemaGeneratorUtil {
    private static final String PROPERTY_NAME = "Name";
    private static final String PROPERTY_TYPE = "Type";

    private DbSchemaGeneratorUtil() {}

    /**
     * Complement short entity properties to expected in the db_schema.xml generator.
     *
     * @param shortProperties List
     *
     * @return List of expected in the DbSchemaXmlGenerator properties.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public static List<Map<String, String>> complementShortPropertiesByDefaults(
            final List<Map<String, String>> shortProperties
    ) {
        final List<Map<String, String>> complemented = new LinkedList<>();

        for (final Map<String, String> property : shortProperties) {
            final String name = property.get(PROPERTY_NAME);
            final String type = property.get(PROPERTY_TYPE);

            final PropertiesTypes propType = PropertiesTypes.getByValue(type);
            final TableColumnTypes tableColumnType = PropertyToDefaultTypeMapperUtil.map(propType);

            final List<String> allowedAttributes = ModuleDbSchemaXml.getAllowedAttributes(
                    tableColumnType
            );

            final Map<String, String> columnData = new LinkedHashMap<>();
            columnData.put(ColumnAttributes.TYPE.getName(), tableColumnType.getColumnType());
            columnData.put(ColumnAttributes.NAME.getName(), name);

            for (final String columnAttributeName : allowedAttributes) {
                final ColumnAttributes attribute = ColumnAttributes.getByName(columnAttributeName);

                if (columnData.containsKey(columnAttributeName) || !attribute.hasDefault()) {
                    continue;
                }
                columnData.put(columnAttributeName, attribute.getDefault());
            }
            columnData.put(ColumnAttributes.COMMENT.getName(), getColumnCommentByName(name));

            complemented.add(columnData);
        }

        return complemented;
    }

    /**
     * Get primary key default data for specified name.
     *
     * @param name String
     *
     * @return String
     */
    public static Map<String, String> getTableIdentityColumnData(final @NotNull String name) {
        final Map<String, String> columnData = new LinkedHashMap<>();

        columnData.put(ColumnAttributes.TYPE.getName(), TableColumnTypes.INT.getColumnType());
        columnData.put(ColumnAttributes.NAME.getName(), name);
        columnData.put(ColumnAttributes.PADDING.getName(), ColumnAttributes.PADDING.getDefault());
        columnData.put(ColumnAttributes.UNSIGNED.getName(), ColumnAttributes.UNSIGNED.getDefault());
        columnData.put(ColumnAttributes.NULLABLE.getName(), ColumnAttributes.NULLABLE.getDefault());
        columnData.put(ColumnAttributes.IDENTITY.getName(), "true");
        columnData.put(ColumnAttributes.COMMENT.getName(), getColumnCommentByName(name));

        return columnData;
    }

    /**
     * Generate column comment by its name.
     *
     * @param name String
     *
     * @return String
     */
    @SuppressWarnings("PMD.UseLocaleWithCaseConversions")
    private static String getColumnCommentByName(final @NotNull String name) {
        final StringBuilder commentStringBuilder = new StringBuilder();
        final String[] nameParts = name.split("_");

        for (final String namePart : nameParts) {
            commentStringBuilder
                    .append(namePart.substring(0, 1).toUpperCase())
                    .append(namePart.substring(1))
                    .append(' ');
        }

        return commentStringBuilder.append("Column").toString();
    }
}
