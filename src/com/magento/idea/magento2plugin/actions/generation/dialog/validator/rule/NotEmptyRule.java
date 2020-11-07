/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

public class NotEmptyRule implements ValidationRule {
    public static final String MESSAGE = "validator.notEmpty";
    private static final ValidationRule INSTANCE = new NotEmptyRule();

    @Override
    public boolean check(final String value) {
        return !value.isEmpty();
    }

    public static ValidationRule getInstance() {
        return INSTANCE;
    }
}
