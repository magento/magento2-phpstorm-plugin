package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.magento.idea.magento2plugin.util.RegExUtil;

public class AlphanumericRule implements ValidationRule {
    public static final String MESSAGE = "validator.alphaNumericCharacters";
    private static final ValidationRule instance = new AlphanumericRule();

    @Override
    public boolean check(String value) {
        return value.matches(RegExUtil.ALPHANUMERIC);
    }

    public static ValidationRule getInstance() {
        return instance;
    }
}
