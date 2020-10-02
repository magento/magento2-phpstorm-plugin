/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.jetbrains.php.refactoring.PhpNameUtil;

public class PhpClassRule implements ValidationRule {
    public static final String MESSAGE = "validator.class.isNotValid";
    private static final ValidationRule INSTANCE = new PhpClassRule();

    @Override
    public boolean check(final String value) {
        return PhpNameUtil.isValidClassName(value);
    }

    public static ValidationRule getInstance() {
        return INSTANCE;
    }
}
