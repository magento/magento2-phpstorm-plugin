/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.xml;

import com.magento.idea.magento2plugin.magento.packages.DiArgumentType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DiArrayValueData {

    private static final String ITEM_TEMPLATE =
            "<item name=\"%name%\" xsi:type=\"%type%\">%value%</item>";
    private static final String NULL_VALUE_ITEM_TEMPLATE =
            "<item name=\"%name%\" xsi:type=\"%type%\"/>";

    private final List<DiArrayItemData> items = new ArrayList<>();

    /**
     * Set items data.
     *
     * @param items List[DiArrayItemData]
     */
    public void setItems(final List<DiArrayItemData> items) {
        this.items.clear();
        this.items.addAll(items);
    }

    /**
     * Get items data.
     *
     * @return List[DiArrayItemData]
     */
    public List<DiArrayItemData> getItems() {
        return items;
    }

    /**
     * Get internal item by its path.
     *
     * @param path String divided by `:`.
     *
     * @return DiArrayItemData
     */
    public DiArrayItemData getItemByPath(final String path) {
        DiArrayItemData target = null;
        final String[] parts = path.split(":");
        final String[] left = Arrays.copyOfRange(parts, 1, parts.length);
        final String key = parts[0];

        for (final DiArrayItemData item : getItems()) {
            if (item.getName().equals(key)) {
                target = item;

                if (left.length > 0) {
                    final DiArrayValueData childrenHolder = item.getChildren();

                    if (childrenHolder == null) { // NOPMD
                        break;
                    }
                    target = childrenHolder.getItemByPath(String.join(":", left));
                }
                break;
            }
        }

        return target;
    }

    @Override
    public String toString() {
        return buildArrayTree(this, 0);
    }

    /**
     * Convert to XML.
     *
     * @param data DiArrayValueData
     *
     * @return String
     */
    public String convertToXml(final DiArrayValueData data) {
        final StringBuilder stringBuilder = new StringBuilder();

        for (final DiArrayItemData item : data.getItems()) {
            String value = item.getValue();
            String template = ITEM_TEMPLATE;

            if (item.getType().equals(DiArgumentType.ARRAY) && item.hasChildren()) {
                value = "\n" + convertToXml(item.getChildren()) + "\n";
            } else if (item.getType().equals(DiArgumentType.NULL)) {
                template = NULL_VALUE_ITEM_TEMPLATE;
            }
            final String name = item.getName();
            final String type = item.getType().getArgumentType();

            stringBuilder.append(
                    template
                            .replace("%name%", name)
                            .replace("%type%", type)
                            .replace("%value%", value)
            );
        }

        return stringBuilder.toString();
    }

    @SuppressWarnings({"PMD.CognitiveComplexity", "PMD.CyclomaticComplexity"})
    private String buildArrayTree(final DiArrayValueData data, final int indentSize) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(indent(0))
                .append("[\n");
        int currentItem = 0;
        final int itemsCount = data.getItems().size();

        for (final DiArrayItemData item : data.getItems()) {
            stringBuilder.append(indent(indentSize + 1));
            String value = item.getValue();

            if (item.getType().equals(DiArgumentType.STRING)) {
                value = "'" + value + "'";// NOPMD
            } else if (item.getType().equals(DiArgumentType.ARRAY) && item.hasChildren()) {
                value = buildArrayTree(item.getChildren(), indentSize + 1);
            } else if (item.getType().equals(DiArgumentType.ARRAY) && !item.hasChildren()) {
                value = "[]";
            } else if (item.getType().equals(DiArgumentType.BOOLEAN)) {
                value = Arrays.asList("1", "true").contains(item.getValue()) ? "true" : "false";
            } else if (item.getType().equals(DiArgumentType.NULL)) {
                value = "null";
            }

            stringBuilder
                    .append('\'')
                    .append(item.getName())
                    .append("' => ")
                    .append(value);

            if (currentItem != itemsCount - 1) {
                stringBuilder.append(',');
            }
            stringBuilder.append('\n');
            currentItem++;
        }
        stringBuilder
                .append(indent(indentSize))
                .append(']');

        return stringBuilder.toString();
    }

    private String indent(final int indentSize) {
        return "  ".repeat(Math.max(0, indentSize));
    }

    public static class DiArrayItemData {

        private DiArrayValueData children;
        private final String name;
        private final DiArgumentType type;
        private final String value;

        /**
         * DiArrayItemData DTO constructor.
         *
         * @param name String
         * @param type DiArgumentType
         * @param value DiArgumentType
         */
        public DiArrayItemData(
                final String name,
                final DiArgumentType type,
                final String value
        ) {
            this.name = name;
            this.type = type;
            this.value = value;
        }

        /**
         * Check if current item has children.
         *
         * @return boolean
         */
        public boolean hasChildren() {
            return children != null && !children.getItems().isEmpty();
        }

        /**
         * Get children elements holder.
         *
         * @return DiArrayValueData
         */
        public DiArrayValueData getChildren() {
            return children;
        }

        /**
         * Set children elements holder.
         *
         * @param children DiArrayValueData
         */
        public void setChildren(final DiArrayValueData children) {
            this.children = children;
        }

        /**
         * Get item name.
         *
         * @return String
         */
        public String getName() {
            return name;
        }

        /**
         * Get item type.
         *
         * @return DiArgumentType
         */
        public DiArgumentType getType() {
            return type;
        }

        /**
         * Get item value.
         *
         * @return String
         */
        public String getValue() {
            return value;
        }
    }
}
