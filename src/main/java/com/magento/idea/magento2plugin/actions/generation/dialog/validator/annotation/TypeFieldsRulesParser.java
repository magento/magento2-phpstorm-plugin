/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation;

import com.intellij.openapi.util.Pair;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.data.FieldValidationData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.ValidationRule;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class TypeFieldsRulesParser {

    private final Class<?> type;
    private List<FieldValidationData> fieldValidationList;

    /**
     * Type fields rules parser.
     *
     * @param object Object
     */
    public TypeFieldsRulesParser(final @NotNull Object object) {
        type = object.getClass();
    }

    /**
     * Parse validation rules for type.
     *
     * @return List[FieldValidationData]
     */
    public List<FieldValidationData> parseValidationRules() {
        if (fieldValidationList == null) {
            fieldValidationList = new LinkedList<>();
            final List<FieldValidation> annotations = new LinkedList<>();
            final List<Pair<ValidationRule, String>> rulePairList = new LinkedList<>();

            for (final Field field : type.getDeclaredFields()) {
                annotations.clear();
                field.setAccessible(true);

                if (field.isAnnotationPresent(FieldValidation.class)) {
                    annotations.add(field.getAnnotation(FieldValidation.class));
                }

                if (field.isAnnotationPresent(FieldValidations.class)) {
                    annotations.addAll(
                            Arrays.asList(field.getAnnotation(FieldValidations.class).value())
                    );
                }
                field.setAccessible(false);
                rulePairList.clear();

                for (final FieldValidation annotation : annotations) {
                    final ValidationRule rule = ValidationRuleExtractorUtil.extract(annotation);
                    final String message = ValidationMessageExtractorUtil.extract(annotation);

                    if (rule != null) {
                        rulePairList.add(new Pair<>(rule, message)); //NOPMD
                    }
                }

                if (!rulePairList.isEmpty()) {
                    fieldValidationList.add(new FieldValidationData(field, rulePairList)); //NOPMD
                }
            }
        }

        return fieldValidationList;
    }
}
