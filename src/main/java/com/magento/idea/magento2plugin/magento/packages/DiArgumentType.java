/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages;

import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.ExtendedNumericRule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public enum DiArgumentType {

    OBJECT("object"),
    STRING("string"),
    BOOLEAN("boolean"),
    NUMBER("number"),
    INIT_PARAMETER("init_parameter"),
    CONST("const"),
    NULL("null"),
    ARRAY("array");

    private final String argumentType;

    /**
     * Dependency Injection argument types ENUM constructor.
     *
     * @param argumentType String
     */
    DiArgumentType(final String argumentType) {
        this.argumentType = argumentType;
    }

    /**
     * Get argument type.
     *
     * @return String
     */
    public String getArgumentType() {
        return argumentType;
    }

    /**
     * Check if provided value is valid for the current type.
     *
     * @param value String
     *
     * @return boolean
     */
    public boolean isValid(final @NotNull String value) {
        if (getArgumentType().equals(DiArgumentType.STRING.getArgumentType())) {
            return validateString(value);
        } else if (getArgumentType().equals(DiArgumentType.BOOLEAN.getArgumentType())) {
            return validateBoolean(value);
        } else if (getArgumentType().equals(DiArgumentType.NUMBER.getArgumentType())) {
            return validateNumber(value);
        } else if (getArgumentType().equals(DiArgumentType.NULL.getArgumentType())) {
            return validateNull(value);
        } else if (getArgumentType().equals(DiArgumentType.ARRAY.getArgumentType())) {
            return validateArray(value);
        }

        return true;
    }

    /**
     * Get ENUM by its string representation.
     *
     * @param value String
     *
     * @return PropertiesTypes
     */
    public static DiArgumentType getByValue(final @NotNull String value) {
        for (final DiArgumentType type : DiArgumentType.values()) {
            if (type.getArgumentType().equals(value)) {
                return type;
            }
        }

        throw new InputMismatchException(
                "Invalid argument type value provided. Should be compatible with "
                        + DiArgumentType.class
        );
    }

    /**
     * Get argument value list.
     *
     * @return List[String]
     */
    public static List<String> getValueList() {
        final List<String> valueList = new ArrayList<>();
        final List<DiArgumentType> simpleTypes = new ArrayList<>();

        simpleTypes.add(DiArgumentType.STRING);
        simpleTypes.add(DiArgumentType.BOOLEAN);
        simpleTypes.add(DiArgumentType.NUMBER);
        simpleTypes.add(DiArgumentType.NULL);
        simpleTypes.add(DiArgumentType.ARRAY);

        for (final DiArgumentType type : DiArgumentType.values()) {
            if (!simpleTypes.contains(type)) {
                continue;
            }
            valueList.add(type.getArgumentType());
        }

        return valueList;
    }

    private boolean validateString(final @NotNull String value) {
        return true;
    }

    private boolean validateBoolean(final @NotNull String value) {
        return Arrays.asList("true", "false", "1", "0").contains(value);
    }

    private boolean validateNumber(final @NotNull String value) {
        return ExtendedNumericRule.getInstance().check(value);
    }

    private boolean validateNull(final @NotNull String value) {
        return true;
    }

    private boolean validateArray(final @NotNull String value) {
        return true;
    }
}
