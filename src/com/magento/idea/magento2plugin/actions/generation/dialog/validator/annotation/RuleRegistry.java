/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation;

import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AclResourceIdRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphaWithDashRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphaWithPeriodRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphanumericRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphanumericWithUnderscoreRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.BoxNotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.CliCommandRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.ConfigPathRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.CronScheduleRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.DirectoryRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.IdentifierRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.IdentifierWithColonRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.Lowercase;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NumericRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassFqnRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpDirectoryRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpNamespaceNameRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.RouteIdRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.StartWithNumberOrCapitalLetterRule;

public enum RuleRegistry {
    NOT_EMPTY(NotEmptyRule.class),
    BOX_NOT_EMPTY(BoxNotEmptyRule.class),
    PHP_CLASS(PhpClassRule.class),
    PHP_CLASS_FQN(PhpClassFqnRule.class),
    ROUTE_ID(RouteIdRule.class),
    ALPHANUMERIC(AlphanumericRule.class),
    ALPHANUMERIC_WITH_UNDERSCORE(AlphanumericWithUnderscoreRule.class),
    ALPHA_WITH_PERIOD(AlphaWithPeriodRule.class),
    ALPHA_WITH_DASH(AlphaWithDashRule.class),
    DIRECTORY(DirectoryRule.class),
    PHP_DIRECTORY(PhpDirectoryRule.class),
    IDENTIFIER(IdentifierRule.class),
    IDENTIFIER_WITH_COLON(IdentifierWithColonRule.class),
    PHP_NAMESPACE_NAME(PhpNamespaceNameRule.class),
    START_WITH_NUMBER_OR_CAPITAL_LETTER(StartWithNumberOrCapitalLetterRule.class),
    ACL_RESOURCE_ID(AclResourceIdRule.class),
    LOWERCASE(Lowercase.class),
    CRON_SCHEDULE(CronScheduleRule.class),
    CONFIG_PATH(ConfigPathRule.class),
    CLI_COMMAND(CliCommandRule.class),
    NUMERIC(NumericRule.class);

    private Class<?> rule;

    RuleRegistry(final Class<?> rule) {
        this.rule = rule;
    }

    public Class<?> getRule() {
        return rule;
    }
}
