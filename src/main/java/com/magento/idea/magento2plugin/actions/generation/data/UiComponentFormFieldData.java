/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class UiComponentFormFieldData {

    private final String name;
    private final String label;
    private final String sortOrder;
    private final String fieldset;
    private final String formElementType;
    private final String dataType;
    private final String source;

    /**
     * Form field data.
     *
     * @param name String
     * @param label String
     * @param sortOrder String
     * @param fieldset String
     * @param formElementType String
     * @param dataType String
     * @param source String
     */
    public UiComponentFormFieldData(
            final String name,
            final String label,
            final String sortOrder,
            final String fieldset,
            final String formElementType,
            final String dataType,
            final String source
    ) {
        this.name = name;
        this.label = label;
        this.sortOrder = sortOrder;
        this.fieldset = fieldset;
        this.formElementType = formElementType;
        this.dataType = dataType;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public String getFieldset() {
        return fieldset;
    }

    public String getFormElementType() {
        return formElementType;
    }

    public String getDataType() {
        return dataType;
    }

    public String getSource() {
        return source;
    }
}
