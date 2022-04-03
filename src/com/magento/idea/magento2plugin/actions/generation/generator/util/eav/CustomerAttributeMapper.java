/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util.eav;

import com.magento.idea.magento2plugin.actions.generation.data.CustomerEntityData;
import com.magento.idea.magento2plugin.actions.generation.data.EavEntityDataInterface;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeProperty;
import java.util.Map;

public class CustomerAttributeMapper extends DefaultAttributeMapper {

    @Override
    protected Map<String, String> getMappedAttributes(final EavEntityDataInterface eavEntityData) {
        final Map<String, String> mappedAttributes =  super.getMappedAttributes(eavEntityData);
        final CustomerEntityData customerEntityData = (CustomerEntityData) eavEntityData;

        mappedAttributes.put(
                AttributeProperty.POSITION.getProperty(),
                Integer.toString(customerEntityData.getSortOrder())
        );
        mappedAttributes.put(
                AttributeProperty.USER_DEFINED.getProperty(),
                Boolean.toString(customerEntityData.isUserDefined())
        );
        mappedAttributes.put(
                AttributeProperty.IS_USED_IN_GRID.getProperty(),
                Boolean.toString(customerEntityData.isUsedInGrid())
        );
        mappedAttributes.put(
                AttributeProperty.IS_VISIBLE_IN_GRID.getProperty(),
                Boolean.toString(customerEntityData.isVisibleInGrid())
        );
        mappedAttributes.put(
                AttributeProperty.IS_FILTERABLE_IN_GRID.getProperty(),
                Boolean.toString(customerEntityData.isFilterableInGrid())
        );
        mappedAttributes.put(
                AttributeProperty.SYSTEM.getProperty(),
                Boolean.toString(customerEntityData.isSystem())
        );

        return mappedAttributes;
    }
}
