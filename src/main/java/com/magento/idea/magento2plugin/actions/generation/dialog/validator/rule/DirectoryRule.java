/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.magento.idea.magento2plugin.util.RegExUtil;
import org.jetbrains.annotations.NotNull;

public class DirectoryRule implements ValidationRule {

    public static final String MESSAGE = "validator.directory.isNotValid";
    private static final ValidationRule INSTANCE = new DirectoryRule();

    @Override
    public boolean check(final @NotNull String value) {
        return value.matches(RegExUtil.DIRECTORY);
    }

    public static ValidationRule getInstance() {
        return INSTANCE;
    }
}
