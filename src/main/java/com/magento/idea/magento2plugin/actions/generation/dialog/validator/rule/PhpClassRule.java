/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.magento.idea.magento2plugin.util.RegExUtil;
import com.magento.idea.magento2plugin.util.php.PhpNameValidator;
import org.jetbrains.annotations.NotNull;

public class PhpClassRule implements ValidationRule {

    public static final String MESSAGE = "validator.class.isNotValid";
    private static final ValidationRule INSTANCE = new PhpClassRule();

    @Override
    public boolean check(final @NotNull String value) {
        return value.matches(RegExUtil.Magento.PHP_CLASS) && PhpNameValidator.isValidClassName(value);
    }

    public static ValidationRule getInstance() {
        return INSTANCE;
    }
}
