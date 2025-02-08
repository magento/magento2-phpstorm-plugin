/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.magento.idea.magento2plugin.util.RegExUtil;
import org.jetbrains.annotations.NotNull;

public class RouteIdRule implements ValidationRule {

    public static final String MESSAGE = "validator.magentoRouteIdInvalid";
    private static final ValidationRule INSTANCE = new RouteIdRule();

    @Override
    public boolean check(final @NotNull String value) {
        return value.matches(RegExUtil.Magento.ROUTE_ID);
    }

    public static ValidationRule getInstance() {
        return INSTANCE;
    }
}
