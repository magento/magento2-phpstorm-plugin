package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.jetbrains.php.refactoring.PhpNameUtil;

public class PhpNamespaceNameRule implements ValidationRule {
    public static final String MESSAGE = "validator.namespace.isNotValid";
    private static final ValidationRule instance = new PhpNamespaceNameRule();

    @Override
    public boolean check(String value) {
        return PhpNameUtil.isValidNamespaceName(value);
    }

    public static ValidationRule getInstance() {
        return instance;
    }
}
