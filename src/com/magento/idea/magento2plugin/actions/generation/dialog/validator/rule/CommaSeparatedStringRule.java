/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.magento.idea.magento2plugin.util.RegExUtil;

public class CommaSeparatedStringRule implements ValidationRule {
    public static final String MESSAGE = "validator.commaSeparatedString.isNotValid";
    private static final ValidationRule INSTANCE = new CommaSeparatedStringRule();

    @Override
    public boolean check(final String value) {
        return value.isEmpty() || value.matches(RegExUtil.Magento.COMMA_SEPARATED_STRING);
    }

    public static ValidationRule getInstance() {
        return INSTANCE;
    }
}
