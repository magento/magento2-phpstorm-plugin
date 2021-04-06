package com.magento.idea.magento2plugin.actions.generation.generator.util.eav;

import com.magento.idea.magento2plugin.magento.packages.eav.EavEntities;
import com.sun.istack.NotNull;

public class AttributeMapperFactory {
    public AttributeMapperInterface createByEntityClass(@NotNull String entityClass) {
        if (entityClass.equals(EavEntities.PRODUCT.getEntityClass())) {
            return new ProductAttributeMapper();
        }

        return null;
    }
}
