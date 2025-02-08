/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util.eav;

import com.magento.idea.magento2plugin.actions.generation.data.EavEntityDataInterface;
import com.magento.idea.magento2plugin.actions.generation.data.ProductEntityData;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeProperty;
import java.util.Map;

public class ProductAttributeMapper extends DefaultAttributeMapper {

    @SuppressWarnings({"PMD.NullAssignment"})
    @Override
    protected Map<String, String> getMappedAttributes(final EavEntityDataInterface eavEntityData) {
        final Map<String, String> mappedAttributes = super.getMappedAttributes(eavEntityData);

        final ProductEntityData productEavEntityData = (ProductEntityData) eavEntityData;
        mappedAttributes.put(
                AttributeProperty.SORT_ORDER.getProperty(),
                Integer.toString(productEavEntityData.getSortOrder())
        );
        mappedAttributes.put(
                AttributeProperty.GROUP.getProperty(),
                wrapStringValueForTemplate(productEavEntityData.getGroup())
        );
        mappedAttributes.put(
                AttributeProperty.GLOBAL.getProperty(),
                productEavEntityData.getScope()
        );
        mappedAttributes.put(
                AttributeProperty.IS_USED_IN_GRID.getProperty(),
                Boolean.toString(productEavEntityData.isUsedInGrid())
        );
        mappedAttributes.put(
                AttributeProperty.IS_VISIBLE_IN_GRID.getProperty(),
                Boolean.toString(productEavEntityData.isVisibleInGrid())
        );
        mappedAttributes.put(
                AttributeProperty.IS_FILTERABLE_IN_GRID.getProperty(),
                Boolean.toString(productEavEntityData.isFilterableInGrid())
        );
        mappedAttributes.put(
                AttributeProperty.IS_HTML_ALLOWED_ON_FRONT.getProperty(),
                Boolean.toString(productEavEntityData.isHtmlAllowedOnFront())
        );
        mappedAttributes.put(
                AttributeProperty.VISIBLE_ON_FRONT.getProperty(),
                Boolean.toString(productEavEntityData.isVisibleOnFront())
        );

        if (productEavEntityData.getApplyTo() != null
                && !productEavEntityData.getApplyTo().isEmpty()) {
            mappedAttributes.put(
                    AttributeProperty.APPLY_TO.getProperty(),
                    wrapStringValueForTemplate(productEavEntityData.getApplyTo())
            );
        }

        return mappedAttributes;
    }
}
