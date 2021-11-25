/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.magento.idea.magento2plugin.util.RegExUtil;

public class CronScheduleRule implements ValidationRule {
    public static final String MESSAGE = "validator.cronSchedule.invalidExpression";
    private static final ValidationRule INSTANCE = new CronScheduleRule();

    @Override
    public boolean check(final String value) {
        return value.matches(RegExUtil.Magento.CRON_SCHEDULE);
    }

    public static ValidationRule getInstance() {
        return INSTANCE;
    }
}
