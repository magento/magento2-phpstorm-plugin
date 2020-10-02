package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.magento.idea.magento2plugin.util.RegExUtil;

public class IdentifierRule implements ValidationRule {
    public static final String MESSAGE = "validator.identifier";
    private static final ValidationRule instance = new IdentifierRule();

    @Override
    public boolean check(String value) {
        return value.matches(RegExUtil.IDENTIFIER);
    }

    public static ValidationRule getInstance() {
        return instance;
    }
}
