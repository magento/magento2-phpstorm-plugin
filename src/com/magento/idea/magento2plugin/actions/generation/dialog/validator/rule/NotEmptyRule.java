package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

public class NotEmptyRule implements ValidationRule {
    public static final String MESSAGE = "validator.notEmpty";
    private static final ValidationRule instance = new NotEmptyRule();

    @Override
    public boolean check(String value) {
        return !(value.length() == 0);
    }

    public static ValidationRule getInstance() {
        return instance;
    }
}
