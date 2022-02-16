/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages.eav;

public enum EavEntity {
    PRODUCT("Magento\\Catalog\\Model\\Product"),
    CATEGORY("Magento\\Catalog\\Model\\Category"),
    CUSTOMER("Magento\\Customer\\Model\\Customer");

    private String entityClass;

    EavEntity(final String entityClass) {
        this.entityClass = entityClass;
    }

    public String getEntityClass() {
        return entityClass;
    }
}
