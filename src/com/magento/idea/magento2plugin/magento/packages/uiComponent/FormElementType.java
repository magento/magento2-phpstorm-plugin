/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages.uiComponent;
import com.intellij.openapi.externalSystem.service.execution.NotSupportedException;
import com.magento.idea.magento2plugin.magento.packages.PropertiesTypes;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public enum FormElementType {
    HIDDEN("hidden"),
    FILE("file"),
    INPUT("input"),
    DATE("date"),
    BOOLEAN("boolean"),
    CHECKBOX("checkbox"),
    CHECKBOX_SET("checkboxset"),
    EMAIL("email"),
    SELECT("select"),
    MULTISELECT("multiselect"),
    TEXT("text"),
    TEXTAREA("textarea"),
    PRICE("price"),
    RADIO_SET("radioset"),
    WYSIWYG("wysiwyg");

    /**
     * UI Component form element type.
     */
    private final String type;

    /**
     * Form element type constructor.
     *
     * @param type String
     */
    FormElementType(final @NotNull String type) {
        this.type = type;
    }

    /**
     * Get form element type.
     *
     * @return String
     */
    public @NotNull String getType() {
        return type;
    }

    /**
     * Get list of available types.
     *
     * @return List[String] of available types.
     */
    public static List<String> getTypeList() {
        final List<String> availableTypes = new LinkedList<>();

        for (final FormElementType type : FormElementType.values()) {
            availableTypes.add(type.getType());
        }

        return availableTypes;
    }

    /**
     * Get default form element for specified property type.
     *
     * @param property PropertiesTypes
     *
     * @return FormElementType
     */
    public static FormElementType getDefaultForProperty(final @NotNull PropertiesTypes property) {
        switch (property) {
            case INT:
            case STRING:
                return FormElementType.INPUT;
            case BOOL:
                return FormElementType.CHECKBOX;
            case FLOAT:
                return FormElementType.PRICE;
            default:
                throw new NotSupportedException(
                        "ENUMs " + FormElementType.class + " property is not supported."
                );
        }
    }
}
