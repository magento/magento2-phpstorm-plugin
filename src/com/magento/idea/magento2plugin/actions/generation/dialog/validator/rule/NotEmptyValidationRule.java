package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

public class NotEmptyValidationRule implements ValidationRule {
    public static final String DEFAULT_BUNDLE_MESSAGE_KEY = "validator.notEmpty";
    private static final ValidationRule instance = new NotEmptyValidationRule();

    @Override
    public boolean check(String value) {
        return !(value.length() == 0);
    }

    public static ValidationRule getInstance() {
        return instance;
    }
}
