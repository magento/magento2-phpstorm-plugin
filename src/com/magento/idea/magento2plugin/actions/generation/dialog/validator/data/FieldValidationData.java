/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.data;

import com.intellij.openapi.util.Pair;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.ValidationRule;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class FieldValidationData {

    private final Field field;
    private final List<Pair<ValidationRule, String>> rules;

    public FieldValidationData(
            final @NotNull Field field,
            final @NotNull List<Pair<ValidationRule, String>> rules
    ) {
        this.field = field;
        this.rules = new LinkedList<>(rules);
    }

    /**
     * Get field.
     *
     * @return Field
     */
    public Field getField() {
        return field;
    }

    /**
     * Get validation rules.
     *
     * @return List
     */
    public List<Pair<ValidationRule, String>> getRules() {
        return new LinkedList<>(rules);
    }
}
