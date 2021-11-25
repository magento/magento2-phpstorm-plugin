/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.magento.idea.magento2plugin.util.RegExUtil;

public class AlphaWithPeriodRule implements ValidationRule {
    public static final String MESSAGE = "validator.alphaAndPeriodCharacters";
    private static final ValidationRule INSTANCE = new AlphaWithPeriodRule();

    @Override
    public boolean check(final String value) {
        return value.matches(RegExUtil.ALPHA_WITH_PERIOD);
    }

    public static ValidationRule getInstance() {
        return INSTANCE;
    }
}
