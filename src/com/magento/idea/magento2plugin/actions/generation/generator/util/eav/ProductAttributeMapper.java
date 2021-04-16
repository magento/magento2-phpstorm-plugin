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
                    String.join("=>", attributeKey, attributeValue + ",")
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
        mappedAttributes.put(EavAttribute.SOURCE.getAttribute(),
                getEntitySource(eavEntityData));
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

        final String attributeOptions = getMappedOptions(
                eavEntityData.getOptions(),
                eavEntityData.getOptionsSortOrder()
        );

        if (!attributeOptions.isEmpty()) {
            mappedAttributes.put(EavAttribute.OPTION.getAttribute(),
                    getMappedOptions(eavEntityData.getOptions(), eavEntityData.getOptionsSortOrder()));
        }

        if (eavEntityData.getApplyTo() != null && !eavEntityData.getApplyTo().isEmpty()) {
            mappedAttributes.put(EavAttribute.APPLY_TO.getAttribute(),
                    wrapStringValueForTemplate(eavEntityData.getApplyTo()));
        }

        return mappedAttributes;
    }

    private String wrapStringValueForTemplate(final String value) {
        return "'" + value + "'";
    }


    private String getEntitySource(final ProductEntityData eavEntityData) {
        final String eavSource = eavEntityData.getSource();

        return eavSource == null
                || eavSource.equals(AttributeSourceModel.NULLABLE_SOURCE.getSource())
                ? null : eavSource + "::class";
    }

    private String getMappedOptions(
            final Map<Integer, String> optionValues,
            final Map<Integer, String> optionSortOrders
    ) {
        if (optionValues == null || optionValues.isEmpty()) {
            return "";
        }

        return "[" + getParsedOptions(optionValues)
                + (optionSortOrders == null || optionSortOrders.isEmpty()
                ? "->" : "," + getParsedOptionSortOrders(optionSortOrders)) + "]";
    }

    private String getParsedOptions(final Map<Integer, String> optionValues) {
        final String valueNode = "->" + wrapStringValueForTemplate("value") + " => ";
        final StringBuilder optionsContent = new StringBuilder();

        for (final Integer optionKey : optionValues.keySet()) {
            final String optionValue = optionValues.get(optionKey);

            if (optionValue.isEmpty()) {
                continue;
            }

            optionsContent
                    .append("->")
                    .append(wrapStringValueForTemplate("option_" + optionKey))
                    .append(" => ")
                    .append("[")
                    .append(wrapStringValueForTemplate(optionValue))
                    .append("], ");
        }

        return valueNode + "[" + optionsContent + "->]";
    }

    private String getParsedOptionSortOrders(final Map<Integer, String> optionSortOrders) {
        final String orderNode = "->" + wrapStringValueForTemplate("order") + " => ";
        final StringBuilder ordersContent = new StringBuilder();

        for (final Integer optionKey : optionSortOrders.keySet()) {
            final String orderValue = optionSortOrders.get(optionKey);

            if (orderValue.isEmpty()) {
                continue;
            }

            ordersContent
                    .append("->")
                    .append(wrapStringValueForTemplate("option_" + optionKey))
                    .append(" => ")
                    .append(orderValue)
                    .append(",");
        }

        return orderNode + "[" + ordersContent + "->]->";
    }
}
