package com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation;

import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyValidationRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.MatchRegexValidationRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.IsValidPhpClassValidationRule;

public enum RuleRegister {
    NOT_EMPTY(NotEmptyValidationRule.class),
    MATCH_REGEX(MatchRegexValidationRule.class),
    IS_VALID_PHP_CLASS(IsValidPhpClassValidationRule.class);

    private Class<?> rule;

    RuleRegister(final Class<?> rule) {
        this.rule = rule;
    }

    public Class<?> getRule() {
        return rule;
    }
}
