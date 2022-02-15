/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import org.jetbrains.annotations.NotNull;

public class TableNameLength implements ValidationRule {

    public static final String MESSAGE = "validator.db.invalidTableNameLength";
    private static final int MAX_TABLE_NAME_LENGTH = 64;

    private static final ValidationRule INSTANCE = new TableNameLength();

    @Override
    public boolean check(final @NotNull String value) {
        return value.length() <= MAX_TABLE_NAME_LENGTH;
    }

    public static ValidationRule getInstance() {
        return INSTANCE;
    }
}
