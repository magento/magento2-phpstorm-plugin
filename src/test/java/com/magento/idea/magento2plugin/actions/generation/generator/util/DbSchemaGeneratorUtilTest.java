/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util;

import com.magento.idea.magento2plugin.magento.packages.PropertiesTypes;
import com.magento.idea.magento2plugin.magento.packages.database.ColumnAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

public class DbSchemaGeneratorUtilTest extends TestCase {

    private static final String PROPERTY_NAME = "Name";
    private static final String PROPERTY_TYPE = "Type";

    public void testComplementShortPropertiesSkipsBlankRowsAndHandlesEmptyNamePartsInComment() {
        final List<Map<String, String>> columnsData =
                DbSchemaGeneratorUtil.complementShortPropertiesByDefaults(List.of(
                        createProperty(""),
                        createProperty("_"),
                        createProperty("_product__entity_"),
                        createProperty(" product_name ", " string ")
                ));

        assertEquals(3, columnsData.size());
        assertEquals("Column", columnsData.get(0).get(ColumnAttributes.COMMENT.getName()));
        assertEquals(
                "Product Entity Column",
                columnsData.get(1).get(ColumnAttributes.COMMENT.getName())
        );
        assertEquals("product_name", columnsData.get(2).get(ColumnAttributes.NAME.getName()));
        assertEquals(
                "Product Name Column",
                columnsData.get(2).get(ColumnAttributes.COMMENT.getName())
        );
    }

    public void testTableIdentityColumnHandlesEmptyNamePartsInComment() {
        assertEquals(
                "Column",
                DbSchemaGeneratorUtil.getTableIdentityColumnData("_")
                        .get(ColumnAttributes.COMMENT.getName())
        );
    }

    private static Map<String, String> createProperty(final String name) {
        return createProperty(name, PropertiesTypes.STRING.getPropertyType());
    }

    private static Map<String, String> createProperty(final String name, final String type) {
        final Map<String, String> property = new HashMap<>();
        property.put(PROPERTY_NAME, name);
        property.put(PROPERTY_TYPE, type);

        return property;
    }
}
