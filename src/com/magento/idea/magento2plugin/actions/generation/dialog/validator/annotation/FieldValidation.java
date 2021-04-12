/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Repeatable(FieldValidations.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldValidation {

    /**
     * Get specified in the annotation RuleRegistry enum value to access ValidationRule class.
     *
     * @return RuleRegistry
     */
    RuleRegistry rule();

    /**
     * Get specified in the annotation message array.
     * The first element of array is a bundle message identifier,
     * next elements are the arguments for the bundle message.
     *
     * @return String[]
     */
    String[] message();
}
