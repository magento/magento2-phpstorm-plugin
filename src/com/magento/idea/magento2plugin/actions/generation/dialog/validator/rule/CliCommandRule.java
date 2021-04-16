/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.magento.idea.magento2plugin.util.RegExUtil;
import org.jetbrains.annotations.NotNull;

public class CliCommandRule implements ValidationRule {

    public static final String MESSAGE = "validator.command.isNotValid";
    private static final ValidationRule INSTANCE = new CliCommandRule();

    @Override
    public boolean check(final @NotNull String value) {
        return value.matches(RegExUtil.CLI_COMMAND_NAME);
    }

    public static ValidationRule getInstance() {
        return INSTANCE;
    }
}
