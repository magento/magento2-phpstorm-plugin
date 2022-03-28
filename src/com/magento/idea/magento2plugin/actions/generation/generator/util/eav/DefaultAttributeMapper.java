/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util.eav;

import com.magento.idea.magento2plugin.actions.generation.data.EavEntityDataInterface;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeBackendModel;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeInput;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeProperty;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeSourceModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultAttributeMapper implements AttributeMapperInterface {
    private static final String PHP_DOUBLE_ARROW_OPERATOR = " => ";

    @Override
    public List<String> mapAttributesByEntityData(final EavEntityDataInterface entityData) {
        final List<String> attributesWithValues = new ArrayList<>();

        final Map<String, String> mappedAttributes = getMappedAttributes(entityData);

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
    protected Map<String, String> getMappedAttributes(final EavEntityDataInterface eavEntityData) {
        final Map<String, String> mappedAttributes = new HashMap<>();
        mappedAttributes.put(
                AttributeProperty.TYPE.getProperty(),
                wrapStringValueForTemplate(eavEntityData.getType())
        );
        mappedAttributes.put(
                AttributeProperty.LABEL.getProperty(),
                wrapStringValueForTemplate(eavEntityData.getLabel())
        );
        mappedAttributes.put(
                AttributeProperty.INPUT.getProperty(),
                wrapStringValueForTemplate(eavEntityData.getInput())
        );
        mappedAttributes.put(
                AttributeProperty.SOURCE.getProperty(),
                getEntitySource(eavEntityData)
        );
        mappedAttributes.put(
                AttributeProperty.REQUIRED.getProperty(),
                Boolean.toString(eavEntityData.isRequired())
        );
        mappedAttributes.put(
                AttributeProperty.VISIBLE.getProperty(),
                Boolean.toString(eavEntityData.isVisible())
        );

        final String attributeOptions = getMappedOptions(
                eavEntityData.getOptions(),
                eavEntityData.getOptionsSortOrder()
        );

        if (!attributeOptions.isEmpty()) {
            mappedAttributes.put(
                    AttributeProperty.OPTION.getProperty(),
                    getMappedOptions(
                            eavEntityData.getOptions(),
                            eavEntityData.getOptionsSortOrder()
                    )
            );
        }

        if (eavEntityData.getInput().equals(AttributeInput.MULTISELECT.getInput())) {
            mappedAttributes.put(
                    AttributeProperty.BACKEND_MODEL.getProperty(),
                    wrapClassValueForTemplate(AttributeBackendModel.ARRAY.getModel())
            );
        }

        return mappedAttributes;
    }

    protected String wrapStringValueForTemplate(final String value) {
        return "'" + value + "'";
    }

    protected String wrapClassValueForTemplate(final String value) {
        return value + "::class";
    }

    protected String getEntitySource(final EavEntityDataInterface eavEntityData) {
        final String eavSource = eavEntityData.getSource();

        return eavSource == null
                || eavSource.equals(AttributeSourceModel.NULLABLE_SOURCE.getSource())
                ? null : wrapClassValueForTemplate(eavSource);
    }

    protected String getMappedOptions(
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

    protected String getParsedOptions(final Map<Integer, String> optionValues) {
        final String valueNode = "->" + wrapStringValueForTemplate("value")
                + PHP_DOUBLE_ARROW_OPERATOR;
        final StringBuilder optionsContent = new StringBuilder();

        for (final Integer optionKey : optionValues.keySet()) {
            final String optionValue = optionValues.get(optionKey);

            if (optionValue.isEmpty()) {
                continue;
            }

            optionsContent
                    .append("->")
                    .append(wrapStringValueForTemplate("option_" + optionKey))
                    .append(PHP_DOUBLE_ARROW_OPERATOR)
                    .append('[')
                    .append(wrapStringValueForTemplate(optionValue))
                    .append("], ");
        }

        return valueNode + "[" + optionsContent + "->]";
    }

    protected String getParsedOptionSortOrders(final Map<Integer, String> optionSortOrders) {
        final String orderNode = "->" + wrapStringValueForTemplate("order")
                + PHP_DOUBLE_ARROW_OPERATOR;
        final StringBuilder ordersContent = new StringBuilder();

        for (final Integer optionKey : optionSortOrders.keySet()) {
            final String orderValue = optionSortOrders.get(optionKey);

            if (orderValue.isEmpty()) {
                continue;
            }

            ordersContent
                    .append("->")
                    .append(wrapStringValueForTemplate("option_" + optionKey))
                    .append(PHP_DOUBLE_ARROW_OPERATOR)
                    .append(orderValue)
                    .append(',');
        }

        return orderNode + "[" + ordersContent + "->]->";
    }
}
