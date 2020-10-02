package com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation;

import java.lang.annotation.*;

@Repeatable(FieldValidations.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldValidation {
    public RuleRegistry rule();
    public String[] message();
}
