/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import org.jetbrains.annotations.NotNull;

public class BoxNotEmptyRule implements ValidationRule {

    public static final String MESSAGE = "validator.box.notEmpty";
    private static final ValidationRule INSTANCE = new BoxNotEmptyRule();

    @Override
    public boolean check(final @NotNull String value) {
        return !value.isEmpty();
    }

    public static ValidationRule getInstance() {
        return INSTANCE;
    }
}
