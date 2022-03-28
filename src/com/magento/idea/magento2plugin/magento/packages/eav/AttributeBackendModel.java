/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 *
 */

package com.magento.idea.magento2plugin.magento.packages.eav;

public enum AttributeBackendModel {
    ARRAY("\\Magento\\Eav\\Model\\Entity\\Attribute\\Backend\\ArrayBackend");

    private String model;

    AttributeBackendModel(final String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }
}
