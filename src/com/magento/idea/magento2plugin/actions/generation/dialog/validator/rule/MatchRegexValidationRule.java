package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import java.util.HashMap;
import java.util.Map;

public class MatchRegexValidationRule implements ValidationRule {
    private static final Map<String, MatchRegexValidationRule> instances = new HashMap<>();
    private String regex;

    public MatchRegexValidationRule(String regex) {
        this.regex = regex;
    }

    @Override
    public boolean check(String value) {
        return value.matches(regex);
    }

    public static ValidationRule getInstance(String regex) {
        if (!instances.containsKey(regex)) {
            instances.put(regex, new MatchRegexValidationRule(regex));
        }
        return instances.get(regex);
    }
}
