package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.magento.idea.magento2plugin.util.RegExUtil;

public class IdentifierWithColonRule implements ValidationRule {
    public static final String MESSAGE = "validator.identifier.colon";
    private static final ValidationRule INSTANCE = new IdentifierWithColonRule();

    @Override
    public boolean check(final String value) {
        return value.matches(RegExUtil.IDENTIFIER_WITH_COLON);
    }

    public static ValidationRule getInstance() {
        return INSTANCE;
    }
}
