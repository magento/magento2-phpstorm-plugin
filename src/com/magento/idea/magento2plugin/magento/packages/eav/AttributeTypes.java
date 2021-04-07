/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages.eav;

public enum AttributeTypes {
    STATIC("static"),
    VARCHAR("varchar"),
    INT("int"),
    TEXT("text"),
    DATETIME("datetime"),
    DECIMAL("decimal");

    private String type;

    AttributeTypes(final String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
