/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation;

public enum RuleRegistry {

    NOT_EMPTY("NotEmptyRule"),
    BOX_NOT_EMPTY("BoxNotEmptyRule"),
    PHP_CLASS("PhpClassRule"),
    PHP_CLASS_FQN("PhpClassFqnRule"),
    ROUTE_ID("RouteIdRule"),
    ALPHANUMERIC("AlphanumericRule"),
    ALPHANUMERIC_WITH_UNDERSCORE("AlphanumericWithUnderscoreRule"),
    ALPHA_WITH_PERIOD("AlphaWithPeriodRule"),
    ALPHA_WITH_DASH("AlphaWithDashRule"),
    DIRECTORY("DirectoryRule"),
    PHP_DIRECTORY("PhpDirectoryRule"),
    IDENTIFIER("IdentifierRule"),
    IDENTIFIER_WITH_COLON("IdentifierWithColonRule"),
    IDENTIFIER_WITH_FORWARD_SLASH("IdentifierWithForwardSlash"),
    PHP_NAMESPACE_NAME("PhpNamespaceNameRule"),
    START_WITH_NUMBER_OR_CAPITAL_LETTER("StartWithNumberOrCapitalLetterRule"),
    ACL_RESOURCE_ID("AclResourceIdRule"),
    LOWERCASE("Lowercase"),
    CRON_SCHEDULE("CronScheduleRule"),
    CONFIG_PATH("ConfigPathRule"),
    CLI_COMMAND("CliCommandRule"),
    NUMERIC("NumericRule"),
    EXTENDED_NUMERIC("ExtendedNumericRule"),
    TABLE_NAME_LENGTH("TableNameLength"),
    MENU_IDENTIFIER("MenuIdentifierRule"),
    LAYOUT_NAME("LayoutNameRule"),
    COMMA_SEPARATED_STRING("CommaSeparatedStringRule");

    private static final String RULE_PACKAGE =
            "com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.";
    private final String ruleClassName;

    RuleRegistry(final String ruleClassName) {
        this.ruleClassName = RULE_PACKAGE + ruleClassName;
    }

    public String getRuleClassName() {
        return ruleClassName;
    }
}
