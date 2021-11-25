/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.magento.idea.magento2plugin.util.RegExUtil;

public class PhpClassFqnRule implements ValidationRule {
    public static final String MESSAGE = "validator.fqn.isNotValid";
    private static final ValidationRule INSTANCE = new PhpClassFqnRule();

    @Override
    public boolean check(final String value) {
        return value.matches(RegExUtil.Magento.PHP_CLASS_FQN);
    }

    public static ValidationRule getInstance() {
        return INSTANCE;
    }
}
