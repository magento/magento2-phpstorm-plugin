/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages.eav;

public enum AttributeInput {
    TEXT("text"),
    TEXTAREA("textarea"),
    BOOLEAN("boolean"),
    SELECT("select"),
    MULTISELECT("multiselect"),
    DATE("date"),
    PRICE("price"),
    HIDDEN("hidden");

    private String input;

    AttributeInput(final String input) {
        this.input = input;
    }

    /**
     * Return attribute input.
     *
     * @return String
     */
    public String getInput() {
        return this.input;
    }

    /**
     * Return attribute input item by input code.
     *
     * @param code String
     * @return AttributeInput
     */
    public static AttributeInput getAttributeInputByCode(final String code) {
        for (final AttributeInput attributeInput: values()) {
            if (attributeInput.getInput().equals(code)) {
                return attributeInput;
            }
        }

        return null;
    }
}
