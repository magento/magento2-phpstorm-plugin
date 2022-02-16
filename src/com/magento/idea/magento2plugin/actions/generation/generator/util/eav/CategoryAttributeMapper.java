/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 *
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util.eav;

import com.magento.idea.magento2plugin.actions.generation.data.CategoryEntityData;
import com.magento.idea.magento2plugin.actions.generation.data.EavEntityDataInterface;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeProperty;
import java.util.Map;

public class CategoryAttributeMapper extends DefaultAttributeMapper {
    @Override
    protected Map<String, String> getMappedAttributes(final EavEntityDataInterface eavEntityData) {
        final Map<String, String> mappedAttributes = super.getMappedAttributes(eavEntityData);
        final CategoryEntityData categoryEavEntityData = (CategoryEntityData) eavEntityData;

        mappedAttributes.put(
                AttributeProperty.SORT_ORDER.getProperty(),
                Integer.toString(categoryEavEntityData.getSortOrder())
        );
        mappedAttributes.put(
                AttributeProperty.GLOBAL.getProperty(),
                categoryEavEntityData.getScope()
        );
        mappedAttributes.put(
                AttributeProperty.GROUP.getProperty(),
                wrapStringValueForTemplate(categoryEavEntityData.getGroup())
        );

        return mappedAttributes;
    }
}
