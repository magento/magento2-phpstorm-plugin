/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.magento.idea.magento2plugin.util.php.PhpNameValidator;
import org.jetbrains.annotations.NotNull;

public class PhpNamespaceNameRule implements ValidationRule {

    public static final String MESSAGE = "validator.namespace.isNotValid";
    private static final ValidationRule INSTANCE = new PhpNamespaceNameRule();

    @Override
    public boolean check(final @NotNull String value) {
        return PhpNameValidator.isValidNamespace(value);
    }

    public static ValidationRule getInstance() {
        return INSTANCE;
    }
}
