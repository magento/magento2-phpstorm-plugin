package com.magento.idea.magento2plugin.magento.packages.eav;

public enum AttributeInputs {
    BOOLEAN("boolean"),
    DATE("date"),
    GALLERY("gallery"),
    HIDDEN("hidden"),
    IMAGE("image"),
    MEDIA_IMAGE("media_image"),
    MULTILINE("multiline"),
    MULTISELECT("multiselect"),
    PRICE("price"),
    SELECT("select"),
    TEXT("text"),
    TEXTAREA("textarea"),
    WEIGHT("weight");

    private String input;

    AttributeInputs(final String input) {
        this.input = input;
    }

    public String getInput() {
        return this.input;
    }
}
