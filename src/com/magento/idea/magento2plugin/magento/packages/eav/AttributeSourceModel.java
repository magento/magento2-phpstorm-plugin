/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages.eav;

public enum AttributeSourceModel {
    BOOLEAN("\\Magento\\Eav\\Model\\Entity\\Attribute\\Source\\Boolean"),
    TABLE("\\Magento\\Eav\\Model\\Entity\\Attribute\\Source\\Table"),
    CONFIG("\\Magento\\Eav\\Model\\Entity\\Attribute\\Source\\Config"),
    STORE("\\Magento\\Eav\\Model\\Entity\\Attribute\\Source\\Store"),
    GENERATE_SOURCE("Custom Source"),
    NULLABLE_SOURCE("Without Source Model");

    private String source;

    AttributeSourceModel(final String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }
}
