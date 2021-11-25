/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.magento.idea.magento2plugin.util.RegExUtil;

public class AlphaWithDashRule implements ValidationRule {
    public static final String MESSAGE = "validator.alphaAndDashCharacters";
    private static final ValidationRule INSTANCE = new AlphaWithDashRule();

    @Override
    public boolean check(final String value) {
        return value.matches(RegExUtil.ALPHA_WITH_DASH);
    }

    public static ValidationRule getInstance() {
        return INSTANCE;
    }
}
