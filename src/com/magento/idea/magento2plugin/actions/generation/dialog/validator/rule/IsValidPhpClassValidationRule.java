package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.jetbrains.php.refactoring.PhpNameUtil;

public class IsValidPhpClassValidationRule implements ValidationRule {
    public static final String DEFAULT_BUNDLE_MESSAGE_KEY = "validator.class.isNotValid";
    private static final ValidationRule instance = new IsValidPhpClassValidationRule();

    @Override
    public boolean check(String value) {
        return PhpNameUtil.isValidClassName(value);
    }

    public static ValidationRule getInstance() {
        return instance;
    }
}
