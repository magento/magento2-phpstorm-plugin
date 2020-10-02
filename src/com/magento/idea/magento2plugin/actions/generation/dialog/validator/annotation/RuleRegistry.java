package com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation;

import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.*;

public enum RuleRegistry {
    NOT_EMPTY(NotEmptyRule.class),
    PHP_CLASS(PhpClassRule.class),
    ROUTE_ID(RouteIdRule.class),
    ALPHANUMERIC(AlphanumericRule.class),
    ALPHANUMERIC_WITH_UNDERSCORE(AlphanumericWithUnderscoreRule.class),
    DIRECTORY(DirectoryRule.class),
    IDENTIFIER(IdentifierRule.class),
    PHP_NAMESPACE_NAME(PhpNamespaceNameRule.class),
    START_WITH_NUMBER_OR_CAPITAL_LETTER(StartWithNumberOrCapitalLetterRule.class),
    ACL_RESOURCE_ID(AclResourceIdRule.class),
    LOWERCASE(Lowercase.class),
    NUMERIC(NumericRule.class);

    private Class<?> rule;

    RuleRegistry(final Class<?> rule) {
        this.rule = rule;
    }

    public Class<?> getRule() {
        return rule;
    }
}
