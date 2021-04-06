/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages.eav;

public enum AttributeSourceModels {
    BOOLEAN("\\Magento\\Eav\\Model\\Entity\\Attribute\\Source\\Boolean"),
    TABLE("\\Magento\\Eav\\Model\\Entity\\Attribute\\Source\\Table"),
    CONFIG("\\Magento\\Eav\\Model\\Entity\\Attribute\\Source\\Config"),
    STORE("\\Magento\\Eav\\Model\\Entity\\Attribute\\Source\\Store");

    private String source;

    AttributeSourceModels(final String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }
}
