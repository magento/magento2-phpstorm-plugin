package com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation;

import java.lang.annotation.*;

@Repeatable(FieldValidations.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldValidation {
    public RuleRegister rule();
    public String[] properties() default "";
    public String[] message();
}
