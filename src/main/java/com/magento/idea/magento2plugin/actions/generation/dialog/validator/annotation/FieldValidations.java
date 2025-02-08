/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldValidations {

    /**
     * Get the array of FieldValidation annotations.
     * Used to add possibility to read multiple FieldValidation annotations from the class fields.
     *
     * @return FieldValidation[]
     */
    FieldValidation[] value();
}
