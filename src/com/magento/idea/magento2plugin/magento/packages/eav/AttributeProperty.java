/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages.eav;

public enum AttributeProperty {
    GROUP("group"),
    TYPE("type"),
    LABEL("label"),
    INPUT("input"),
    SOURCE("source"),
    REQUIRED("required"),
    SORT_ORDER("sort_order"),
    GLOBAL("global"),
    IS_USED_IN_GRID("is_used_in_grid"),
    IS_VISIBLE_IN_GRID("is_visible_in_grid"),
    IS_FILTERABLE_IN_GRID("is_filterable_in_grid"),
    VISIBLE("visible"),
    IS_HTML_ALLOWED_ON_FRONT("is_html_allowed_on_front"),
    VISIBLE_ON_FRONT("visible_on_front"),
    APPLY_TO("apply_to"),
    OPTION("option"),
    BACKEND_MODEL("backend"),
    USER_DEFINED("user_defined"),
    POSITION("position"),
    SYSTEM("system");

    private String attribute;

    AttributeProperty(final String attribute) {
        this.attribute = attribute;
    }

    public String getProperty() {
        return attribute;
    }
}
