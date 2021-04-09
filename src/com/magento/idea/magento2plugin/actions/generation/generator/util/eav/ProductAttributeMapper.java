/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util.eav;

import com.magento.idea.magento2plugin.actions.generation.data.EavEntityDataInterface;
import com.magento.idea.magento2plugin.actions.generation.data.ProductEntityData;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeSourceModel;
import com.magento.idea.magento2plugin.magento.packages.eav.EavAttribute;
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

    @SuppressWarnings({"PMD.NullAssignment"})
    private Map<String, String> getMappedAttributes(final ProductEntityData eavEntityData) {
        final Map<String, String> mappedAttributes = new HashMap<>();

        mappedAttributes.put(EavAttribute.GROUP.getAttribute(),
                wrapStringValueForTemplate(eavEntityData.getGroup()));
        mappedAttributes.put(EavAttribute.TYPE.getAttribute(),
                wrapStringValueForTemplate(eavEntityData.getType()));
        mappedAttributes.put(EavAttribute.LABEL.getAttribute(),
                wrapStringValueForTemplate(eavEntityData.getLabel()));
        mappedAttributes.put(EavAttribute.INPUT.getAttribute(),
                wrapStringValueForTemplate(eavEntityData.getInput()));

        final String eavSource = eavEntityData.getSource();
        mappedAttributes.put(EavAttribute.SOURCE.getAttribute(),
                eavSource == null || eavSource.equals(AttributeSourceModel.NULLABLE_SOURCE.getSource())
                        ? null : eavSource + "::class");

        mappedAttributes.put(EavAttribute.REQUIRED.getAttribute(),
                Boolean.toString(eavEntityData.isRequired()));
        mappedAttributes.put(EavAttribute.SORT_ORDER.getAttribute(),
                Integer.toString(eavEntityData.getSortOrder()));
        mappedAttributes.put(EavAttribute.GLOBAL.getAttribute(),
                eavEntityData.getScope());
        mappedAttributes.put(EavAttribute.IS_USED_IN_GRID.getAttribute(),
                Boolean.toString(eavEntityData.isUsedInGrid()));
        mappedAttributes.put(EavAttribute.IS_VISIBLE_IN_GRID.getAttribute(),
                Boolean.toString(eavEntityData.isVisibleInGrid()));
        mappedAttributes.put(EavAttribute.IS_FILTERABLE_IN_GRID.getAttribute(),
                Boolean.toString(eavEntityData.isFilterableInGrid()));
        mappedAttributes.put(EavAttribute.VISIBLE.getAttribute(),
                Boolean.toString(eavEntityData.isVisible()));
        mappedAttributes.put(EavAttribute.IS_HTML_ALLOWED_ON_FRONT.getAttribute(),
                Boolean.toString(eavEntityData.isHtmlAllowedOnFront()));
        mappedAttributes.put(EavAttribute.VISIBLE_ON_FRONT.getAttribute(),
                Boolean.toString(eavEntityData.isVisibleOnFront()));

        return mappedAttributes;
    }

    private String wrapStringValueForTemplate(final String value) {
        return "'" + value + "'";
    }
}
