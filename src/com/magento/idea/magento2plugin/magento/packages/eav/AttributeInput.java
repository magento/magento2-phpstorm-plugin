/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages.eav;

public enum AttributeInput {
    TEXT("text"),
    BOOLEAN("boolean"),
    MULTISELECT("multiselect"),
    SELECT("select"),
    TEXTAREA("textarea"),
    PRICE("price"),
    DATE("date"),
    GALLERY("gallery"),
    HIDDEN("hidden"),
    IMAGE("image"),
    MEDIA_IMAGE("media_image"),
    MULTILINE("multiline"),
    WEIGHT("weight");

    private String input;

    AttributeInput(final String input) {
        this.input = input;
    }

    public String getInput() {
        return this.input;
    }
}
