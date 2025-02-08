/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.jetbrains.php.refactoring.PhpNameUtil;
import org.jetbrains.annotations.NotNull;

public class PhpNamespaceNameRule implements ValidationRule {

    public static final String MESSAGE = "validator.namespace.isNotValid";
    private static final ValidationRule INSTANCE = new PhpNamespaceNameRule();

    @Override
    public boolean check(final @NotNull String value) {
        return PhpNameUtil.isValidNamespaceName(value);
    }

    public static ValidationRule getInstance() {
        return INSTANCE;
    }
}
