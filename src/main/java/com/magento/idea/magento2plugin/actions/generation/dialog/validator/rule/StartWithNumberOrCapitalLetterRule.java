/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import org.jetbrains.annotations.NotNull;

public class StartWithNumberOrCapitalLetterRule implements ValidationRule {

    public static final String MESSAGE = "validator.startWithNumberOrCapitalLetter";
    private static final ValidationRule INSTANCE = new StartWithNumberOrCapitalLetterRule();

    @Override
    public boolean check(final @NotNull String value) {
        return Character.isUpperCase(value.charAt(0)) || Character.isDigit(value.charAt(0));
    }

    public static ValidationRule getInstance() {
        return INSTANCE;
    }
}
