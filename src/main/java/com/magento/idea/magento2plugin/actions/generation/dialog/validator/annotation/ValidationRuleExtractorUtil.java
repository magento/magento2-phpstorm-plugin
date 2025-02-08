/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation;

import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.ValidationRule;
import java.lang.reflect.InvocationTargetException;
import org.jetbrains.annotations.NotNull;

public final class ValidationRuleExtractorUtil {

    private ValidationRuleExtractorUtil() {}

    /**
     * Extract validation rule from validation annotation.
     *
     * @param annotation FieldValidation
     *
     * @return ValidationRule
     */
    public static ValidationRule extract(final @NotNull FieldValidation annotation) {
        ValidationRule rule;
        final Class<?> ruleType = annotation.rule().getRule();

        try {
            rule = (ValidationRule) ruleType.getConstructor().newInstance();
        } catch (NoSuchMethodException
                | IllegalAccessException
                | InvocationTargetException
                | InstantiationException exception
        ) {
            return null;
        }

        return rule;
    }
}
