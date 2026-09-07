/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.magento.idea.magento2plugin.util.RegExUtil;
import org.jetbrains.annotations.NotNull;

public class ExtendedNumericRule implements ValidationRule {

    public static final String MESSAGE = "validator.onlyIntegerOrFloatNumbers";
    private static final ValidationRule INSTANCE = new ExtendedNumericRule();

    @Override
    public boolean check(final @NotNull String value) {
        return value.matches(RegExUtil.EXTENDED_NUMERIC);
    }

    public static ValidationRule getInstance() {
        return INSTANCE;
    }
}
