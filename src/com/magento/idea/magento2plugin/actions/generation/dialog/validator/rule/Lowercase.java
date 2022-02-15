/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import java.util.Locale;
import org.jetbrains.annotations.NotNull;

public class Lowercase implements ValidationRule {

    public static final String MESSAGE = "validator.lowercaseCharacters";
    private static final ValidationRule INSTANCE = new Lowercase();

    @Override
    public boolean check(final @NotNull String value) {
        return value.equals(value.toLowerCase(Locale.getDefault()));
    }

    public static ValidationRule getInstance() {
        return INSTANCE;
    }
}
