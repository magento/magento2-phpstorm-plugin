/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util.eav;

import com.magento.idea.magento2plugin.actions.generation.data.EavEntityDataInterface;
import com.magento.idea.magento2plugin.actions.generation.data.ProductEntityData;
import com.magento.idea.magento2plugin.magento.packages.eav.EavAttributes;
import com.sun.istack.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductAttributeMapper implements AttributeMapperInterface {
    @Override
    public List<String> mapAttributesByEntityData(
            @NotNull final EavEntityDataInterface entityData
    ) {
        final ProductEntityData productEntityData = (ProductEntityData) entityData;

        final List<String> attributesWithValues = new ArrayList<>();

        final Map<String, String> mappedAttributes = getMappedAttributes(productEntityData);

        for (final Map.Entry<String, String> attributePair : mappedAttributes.entrySet()) {
            final String attributeKey = "'" + attributePair.getKey() + "'";
            final String attributeValue = attributePair.getValue();

            attributesWithValues.add(
                    String.join("=>", attributeKey, attributeValue)
            );
        }

        return attributesWithValues;
    }

    private Map<String, String> getMappedAttributes(final ProductEntityData eavEntityData) {
        final Map<String, String> mappedAttributes = new HashMap<>();

        mappedAttributes.put(EavAttributes.GROUP.getAttribute(),
                wrapStringValueForTemplate(eavEntityData.getGroup()));
        mappedAttributes.put(EavAttributes.TYPE.getAttribute(),
                wrapStringValueForTemplate(eavEntityData.getType()));
        mappedAttributes.put(EavAttributes.LABEL.getAttribute(),
                wrapStringValueForTemplate(eavEntityData.getLabel()));
        mappedAttributes.put(EavAttributes.INPUT.getAttribute(),
                wrapStringValueForTemplate(eavEntityData.getInput()));

        mappedAttributes.put(EavAttributes.SOURCE.getAttribute(),
                eavEntityData.getSource());
        mappedAttributes.put(EavAttributes.REQUIRED.getAttribute(),
                Boolean.toString(eavEntityData.isRequired()));
        mappedAttributes.put(EavAttributes.SORT_ORDER.getAttribute(),
                Integer.toString(eavEntityData.getSortOrder()));
        mappedAttributes.put(EavAttributes.GLOBAL.getAttribute(),
                eavEntityData.getScope());
        mappedAttributes.put(EavAttributes.IS_USED_IN_GRID.getAttribute(),
                Boolean.toString(eavEntityData.isUsedInGrid()));
        mappedAttributes.put(EavAttributes.IS_VISIBLE_IN_GRID.getAttribute(),
                Boolean.toString(eavEntityData.isVisibleInGrid()));
        mappedAttributes.put(EavAttributes.IS_FILTERABLE_IN_GRID.getAttribute(),
                Boolean.toString(eavEntityData.isFilterableInGrid()));
        mappedAttributes.put(EavAttributes.VISIBLE.getAttribute(),
                Boolean.toString(eavEntityData.isVisible()));
        mappedAttributes.put(EavAttributes.IS_HTML_ALLOWED_ON_FRONT.getAttribute(),
                Boolean.toString(eavEntityData.isHtmlAllowedOnFront()));
        mappedAttributes.put(EavAttributes.VISIBLE_ON_FRONT.getAttribute(),
                Boolean.toString(eavEntityData.isVisibleOnFront()));

        return mappedAttributes;
    }

    private String wrapStringValueForTemplate(final String value) {
        return "'" + value + "'";
    }
}
