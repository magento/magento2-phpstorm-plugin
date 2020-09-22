/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class UiComponentFormFieldsetData {

    private String label;
    private String sortOrder;

    public UiComponentFormFieldsetData(
            String label,
            String sortOrder
    ) {
        this.label = label;
        this.sortOrder = sortOrder;
    }

    public String getLabel() {
        return label;
    }

    public String getSortOrder() {
        return sortOrder;
    }
}
