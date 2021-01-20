/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages;

public enum PropertiesTypes {
    INT("int"),
    FLOAT("float"),
    STRING("string"),
    BOOL("bool");

    private final String propertyType;

    PropertiesTypes(final String propertyType) {
        this.propertyType = propertyType;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public static String[] getPropertyTypes() {
        return new String[]{
            valueOf(INT.toString()).getPropertyType(),
            valueOf(FLOAT.toString()).getPropertyType(),
            valueOf(STRING.toString()).getPropertyType(),
            valueOf(BOOL.toString()).getPropertyType()
        };
    }
}
