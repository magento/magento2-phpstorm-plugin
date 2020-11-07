/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.magento.idea.magento2plugin.util.RegExUtil;

public class ConfigPathRule implements ValidationRule {
    public static final String MESSAGE = "validator.configPath.invalidFormat";
    private static final ValidationRule INSTANCE = new ConfigPathRule();

    @Override
    public boolean check(final String value) {
        return value.matches(RegExUtil.Magento.CONFIG_PATH);
    }

    public static ValidationRule getInstance() {
        return INSTANCE;
    }
}
