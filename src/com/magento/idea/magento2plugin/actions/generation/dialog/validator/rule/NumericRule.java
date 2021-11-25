/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.magento.idea.magento2plugin.util.RegExUtil;

public class NumericRule implements ValidationRule {
    public static final String MESSAGE = "validator.onlyNumbers";
    private static final ValidationRule INSTANCE = new NumericRule();

    @Override
    public boolean check(final String value) {
        return value.matches(RegExUtil.NUMERIC);
    }

    public static ValidationRule getInstance() {
        return INSTANCE;
    }
}
