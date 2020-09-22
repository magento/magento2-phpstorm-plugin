/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class UiComponentFormFieldData {

    private String name;
    private String label;
    private String sortOrder;
    private String fieldset;
    private String formElementType;
    private String dataType;
    private String source;

    public UiComponentFormFieldData(
            String name,
            String label,
            String sortOrder,
            String fieldset,
            String formElementType,
            String dataType,
            String source
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
