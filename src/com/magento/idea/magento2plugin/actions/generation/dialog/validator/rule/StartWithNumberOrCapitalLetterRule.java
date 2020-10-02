package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.magento.idea.magento2plugin.util.RegExUtil;

public class StartWithNumberOrCapitalLetterRule implements ValidationRule {
    public static final String MESSAGE = "validator.startWithNumberOrCapitalLetter";
    private static final ValidationRule instance = new StartWithNumberOrCapitalLetterRule();

    @Override
    public boolean check(String value) {
        return Character.isUpperCase(value.charAt(0)) || Character.isDigit(value.charAt(0));
    }

    public static ValidationRule getInstance() {
        return instance;
    }
}
