/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util.eav;

import com.magento.idea.magento2plugin.magento.packages.eav.EavEntity;
import com.sun.istack.NotNull;

public class AttributeMapperFactory {
    /**
     * Create entity mapper by entity class.
     *
     * @param entityClass String
     */
    public AttributeMapperInterface createByEntityClass(@NotNull final String entityClass) {
        if (entityClass.equals(EavEntity.PRODUCT.getEntityClass())) {
            return new ProductAttributeMapper();
        } else if (entityClass.equals(EavEntity.CATEGORY.getEntityClass())) {
            return new CategoryAttributeMapper();
        } else if (entityClass.equals(EavEntity.CUSTOMER.getEntityClass())) {
            return new CustomerAttributeMapper();
        }

        return null;
    }
}
