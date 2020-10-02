package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.magento.idea.magento2plugin.util.RegExUtil;

public class RouteIdRule implements ValidationRule {
    public static final String MESSAGE = "validator.magentoRouteIdInvalid";
    private static final ValidationRule instance = new RouteIdRule();

    @Override
    public boolean check(String value) {
        return value.matches(RegExUtil.Magento.ROUTE_ID);
    }

    public static ValidationRule getInstance() {
        return instance;
    }
}
