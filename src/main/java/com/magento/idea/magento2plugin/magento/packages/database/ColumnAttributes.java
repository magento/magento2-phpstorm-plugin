/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages.database;

import java.util.InputMismatchException;
import org.jetbrains.annotations.NotNull;

public enum ColumnAttributes {
    NAME("name", null),
    TYPE("xsi:type", null),
    PADDING("padding", "10"),
    UNSIGNED("unsigned", "true"),
    NULLABLE("nullable", "false"),
    IDENTITY("identity", "false"),
    LENGTH("length", "255"),
    PRECISION("precision", "20"),
    SCALE("scale", "2"),
    ON_UPDATE("on_update", "false"),
    DEFAULT("default", null),
    COMMENT("comment", null);

    private final String name;
    private final String defaultValue;

    /**
     * Column Attribute ENUM Constructor.
     *
     * @param name String
     * @param defaultValue String
     */
    ColumnAttributes(final @NotNull String name, final String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    /**
     * Get column attribute name.
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Get column attribute default value.
     *
     * @return String
     */
    public String getDefault() {
        return defaultValue;
    }

    /**
     * Check if column attribute has default value.
     *
     * @return boolean
     */
    public boolean hasDefault() {
        return defaultValue != null;
    }

    /**
     * Get column attribute ENUM by its name string representation.
     *
     * @param name String
     *
     * @return ColumnAttributes
     */
    public static @NotNull ColumnAttributes getByName(final @NotNull String name) {
        for (final ColumnAttributes attribute : ColumnAttributes.values()) {
            if (attribute.getName().equals(name)) {
                return attribute;
            }
        }

        throw new InputMismatchException(
                "Invalid column attribute name provided. Should be compatible with "
                        + ColumnAttributes.class
        );
    }
}
