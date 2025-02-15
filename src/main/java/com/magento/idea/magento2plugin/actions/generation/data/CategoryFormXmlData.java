/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 *
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import org.jetbrains.annotations.NotNull;

public class CategoryFormXmlData {
    private final String fieldSetName;
    private final String fieldName;
    private final String attributeInput;
    private final int sortOrder;

    /**
     * Category Form data class.
     *
     * @param fieldSetName name of the fieldset
     * @param fieldName field name
     * @param attributeInput attribute input
     * @param sortOrder sort order
     */
    public CategoryFormXmlData(
            @NotNull final String fieldSetName,
            @NotNull final String fieldName,
            @NotNull final String attributeInput,
            @NotNull final int sortOrder
    ) {
        this.fieldSetName = convertGroupNameToFieldSet(fieldSetName);
        this.fieldName = fieldName;
        this.attributeInput = attributeInput;
        this.sortOrder = sortOrder;
    }

    private String convertGroupNameToFieldSet(final String groupName) {
        final String[] nameParts = groupName.toLowerCase().split(" ");//NOPMD

        return String.join("_", nameParts);
    }

    public String getFieldSetName() {
        return fieldSetName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public String getAttributeInput() {
        return attributeInput;
    }
}
