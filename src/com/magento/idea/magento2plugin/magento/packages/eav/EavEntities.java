/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages.eav;

public enum EavEntities {
    PRODUCT("\\Magento\\Catalog\\Model\\Product");

    private String entityClass;

    EavEntities(final String entityClass) {
        this.entityClass = entityClass;
    }

    public String getEntityClass() {
        return entityClass;
    }
}
