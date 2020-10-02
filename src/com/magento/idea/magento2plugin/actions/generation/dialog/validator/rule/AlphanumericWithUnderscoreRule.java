package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.magento.idea.magento2plugin.util.RegExUtil;

public class AlphanumericWithUnderscoreRule implements ValidationRule {
    public static final String MESSAGE = "validator.alphaNumericAndUnderscoreCharacters";
    private static final ValidationRule instance = new AlphanumericWithUnderscoreRule();

    @Override
    public boolean check(String value) {
        return value.matches(RegExUtil.ALPHANUMERIC_WITH_UNDERSCORE);
    }

    public static ValidationRule getInstance() {
        return instance;
    }
}
