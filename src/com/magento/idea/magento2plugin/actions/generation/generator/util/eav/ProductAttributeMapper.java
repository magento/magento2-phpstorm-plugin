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
    public List<String> mapAttributesByEntityData(@NotNull EavEntityDataInterface entityData) {
        ProductEntityData productEntityData = (ProductEntityData) entityData;

        List<String> attributesWithValues = new ArrayList<>();

        final Map<String, String> mappedAttributes = getMappedAttributes(productEntityData);

        for (Map.Entry<String, String> attributePair : mappedAttributes.entrySet()) {
            final String attributeKey = "'" + attributePair.getKey() + "'";
            final String attributeValue = attributePair.getValue();

            attributesWithValues.add(
                    String.join("=>", attributeKey, attributeValue)
            );
        }

        return attributesWithValues;
    }

    private Map<String, String> getMappedAttributes(ProductEntityData eavEntityData) {
        Map<String, String> mappedAttributes = new HashMap<>();

        mappedAttributes.put(EavAttributes.group.name(), wrapStringValueForTemplate(eavEntityData.getGroup()));
        mappedAttributes.put(EavAttributes.type.name(), wrapStringValueForTemplate(eavEntityData.getType()));
        mappedAttributes.put(EavAttributes.label.name(), wrapStringValueForTemplate(eavEntityData.getLabel()));
        mappedAttributes.put(EavAttributes.input.name(), wrapStringValueForTemplate(eavEntityData.getInput()));

        mappedAttributes.put(EavAttributes.source.name(), wrapStringValueForTemplate(eavEntityData.getSource()));
        mappedAttributes.put(EavAttributes.required.name(), Boolean.toString(eavEntityData.isRequired()));
        mappedAttributes.put(EavAttributes.sort_order.name(), Integer.toString(eavEntityData.getSortOrder()));
        mappedAttributes.put(EavAttributes.global.name(), eavEntityData.getScope());
        mappedAttributes.put(EavAttributes.is_used_in_grid.name(), Boolean.toString(eavEntityData.isUsedInGrid()));
        mappedAttributes.put(EavAttributes.is_visible_in_grid.name(), Boolean.toString(eavEntityData.isVisibleInGrid()));
        mappedAttributes.put(EavAttributes.is_filterable_in_grid.name(), Boolean.toString(eavEntityData.isFilterableInGrid()));
        mappedAttributes.put(EavAttributes.visible.name(), Boolean.toString(eavEntityData.isVisible()));
        mappedAttributes.put(EavAttributes.is_html_allowed_on_front.name(), Boolean.toString(eavEntityData.isHtmlAllowedOnFront()));
        mappedAttributes.put(EavAttributes.visible_on_front.name(), Boolean.toString(eavEntityData.isVisibleOnFront()));

        return mappedAttributes;
    }

    private String wrapStringValueForTemplate(final String value) {
        return "'" + value + "'";
    }
}
