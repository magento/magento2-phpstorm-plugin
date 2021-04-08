/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages;

import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public enum PropertiesTypes {
    INT("int"),
    FLOAT("float"),
    STRING("string"),
    BOOL("bool");

    private final String propertyType;

    /**
     * Entity property types ENUM constructor.
     *
     * @param propertyType String
     */
    PropertiesTypes(final String propertyType) {
        this.propertyType = propertyType;
    }

    /**
     * Get property type.
     *
     * @return String
     */
    public String getPropertyType() {
        return propertyType;
    }

    /**
     * Get ENUM by its string representation.
     *
     * @param value String
     *
     * @return PropertiesTypes
     */
    public static PropertiesTypes getByValue(final @NotNull String value) {
        for (final PropertiesTypes type : PropertiesTypes.values()) {
            if (type.getPropertyType().equals(value)) {
                return type;
            }
        }

        throw new InputMismatchException(
                "Invalid property type value provided. Should be compatible with "
                        + PropertiesTypes.class
        );
    }

    /**
     * Get property types.
     *
     * @return String[] array of property types.
     */
    public static String[] getPropertyTypes() {
        return new String[]{
            valueOf(INT.toString()).getPropertyType(),
            valueOf(FLOAT.toString()).getPropertyType(),
            valueOf(STRING.toString()).getPropertyType(),
            valueOf(BOOL.toString()).getPropertyType()
        };
    }

    /**
     * Get property types list.
     *
     * @return List of entity property types.
     */
    public static List<String> getPropertyTypesList() {
        final List<String> propertyList = new LinkedList<>();

        for (final PropertiesTypes property : PropertiesTypes.values()) {
            propertyList.add(property.getPropertyType());
        }

        return propertyList;
    }
}
