package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.magento.idea.magento2plugin.util.RegExUtil;

public class MenuIdentifierRule implements ValidationRule {
    public static final String MESSAGE = "validator.menuIdentifierInvalid";
    private static final ValidationRule INSTANCE = new MenuIdentifierRule();

    @Override
    public boolean check(final String value) {
        return value.matches(RegExUtil.Magento.MENU_IDENTIFIER);
    }

    public static ValidationRule getInstance() {
        return INSTANCE;
    }
}