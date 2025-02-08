/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util;

import com.magento.idea.magento2plugin.magento.packages.PropertiesTypes;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class DbSchemaGeneratorDataProviderUtil {

    private static final String PROPERTY_NAME = "Name";
    private static final String PROPERTY_TYPE = "Type";

    private DbSchemaGeneratorDataProviderUtil() {}

    /**
     * Generate properties for testcases.
     *
     * @return List of prepared properties.
     */
    public static List<Map<String, String>> generateEntityPropertiesForTest() {
        final List<Map<String, String>> propertyList = new LinkedList<>();

        final Map<String, String> nameProperty = new HashMap<>();
        nameProperty.put(PROPERTY_NAME, "name");
        nameProperty.put(PROPERTY_TYPE, PropertiesTypes.STRING.getPropertyType());
        propertyList.add(nameProperty);

        final Map<String, String> ageProperty = new HashMap<>();
        ageProperty.put(PROPERTY_NAME, "age");
        ageProperty.put(PROPERTY_TYPE, PropertiesTypes.INT.getPropertyType());
        propertyList.add(ageProperty);

        final Map<String, String> salaryProperty = new HashMap<>();
        salaryProperty.put(PROPERTY_NAME, "salary");
        salaryProperty.put(PROPERTY_TYPE, PropertiesTypes.FLOAT.getPropertyType());
        propertyList.add(salaryProperty);

        final Map<String, String> singleProperty = new HashMap<>();
        singleProperty.put(PROPERTY_NAME, "is_single");
        singleProperty.put(PROPERTY_TYPE, PropertiesTypes.BOOL.getPropertyType());
        propertyList.add(singleProperty);

        return propertyList;
    }
}
