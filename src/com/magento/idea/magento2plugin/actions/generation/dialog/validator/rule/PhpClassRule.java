package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.jetbrains.php.refactoring.PhpNameUtil;

public class PhpClassRule implements ValidationRule {
    public static final String MESSAGE = "validator.class.isNotValid";
    private static final ValidationRule instance = new PhpClassRule();

    @Override
    public boolean check(String value) {
        return PhpNameUtil.isValidClassName(value);
    }

    public static ValidationRule getInstance() {
        return instance;
    }
}
