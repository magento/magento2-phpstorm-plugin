package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

public class Lowercase implements ValidationRule {
    public static final String MESSAGE = "validator.lowercaseCharacters";
    private static final ValidationRule instance = new Lowercase();

    @Override
    public boolean check(String value) {
        return value.equals(value.toLowerCase());
    }

    public static ValidationRule getInstance() {
        return instance;
    }
}
