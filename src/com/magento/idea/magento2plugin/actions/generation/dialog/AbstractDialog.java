/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidations;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.swing.*;

import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.ValidationRule;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;

/**
 * All code generate dialog should extend this class.
 */
@SuppressWarnings({"PMD.ShortVariable", "PMD.MissingSerialVersionUID"})
public abstract class AbstractDialog extends JDialog {
    protected CommonBundle bundle;
    protected final ValidatorBundle validatorBundle = new ValidatorBundle();
    private final String errorTitle;
    private final Map<Object, List<ValidationRule>> textFieldValidationRuleMap = new LinkedHashMap<>();
    private final Map<Object, Map<ValidationRule, String>> errorMessageFieldValidationRuleMap = new HashMap<>();

    public AbstractDialog() {
        super();
        bundle = new CommonBundle();
        errorTitle = bundle.message("common.error");
    }

    protected void centerDialog(final AbstractDialog dialog) {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final int x = screenSize.width / 2  - dialog.getSize().width / 2;
        final int y = screenSize.height / 2 - dialog.getSize().height / 2;
        dialog.setLocation(x, y);
    }

    protected void onCancel() {
        this.setVisible(false);
    }

    protected boolean validateFormFields() {
        addValidationRulesFromAnnotations();
        for (Map.Entry<Object, List<ValidationRule>> entry : textFieldValidationRuleMap.entrySet()) {
            Object field = entry.getKey();
            List<ValidationRule> rules = entry.getValue();

            for (ValidationRule rule : rules) {
                String value = resolveFieldValueByComponentType(field);

                if (value != null && !rule.check(value)) {
                    if (errorMessageFieldValidationRuleMap.containsKey(field)
                            && errorMessageFieldValidationRuleMap.get(field).containsKey(rule)) {
                        showErrorMessage(errorMessageFieldValidationRuleMap.get(field).get(rule));
                    }
                    return false;
                }
            }
        }
        return true;
    }

    protected void showErrorMessage(String errorMessage) {
        JOptionPane.showMessageDialog(
                null,
                errorMessage,
                errorTitle,
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void addValidationRulesFromAnnotations() {
        Class<?> type = this.getClass();
        for (Field field : type.getDeclaredFields()) {
            field.setAccessible(true);
            List<FieldValidation> validations = new LinkedList<>();

            if (field.isAnnotationPresent(FieldValidation.class)) {
                validations.add(field.getAnnotation(FieldValidation.class));
            }
            if (field.isAnnotationPresent(FieldValidations.class)) {
                validations.addAll(Arrays.asList(field.getAnnotation(FieldValidations.class).value()));
            }

            for (FieldValidation validation : validations) {
                try {
                    addValidationRuleToField(
                            field.get(this),
                            getRuleFromAnnotation(validation),
                            getMessageFromAnnotation(validation)
                    );
                } catch (Exception exception) {
                    // Do nothing.
                }
            }
            field.setAccessible(false);
        }
    }

    private String getMessageFromAnnotation(FieldValidation validation) {
        String[] params;
        if (validation.message().length > 1) {
            params = Arrays.copyOfRange(validation.message(), 1, validation.message().length);
        } else {
            params = new String[]{};
        }
        return validatorBundle.message(validation.message()[0], params);
    }

    private ValidationRule getRuleFromAnnotation(FieldValidation validation) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        ValidationRule rule;
        Class<?> ruleType = validation.rule().getRule();
        String[] ruleParams = validation.properties();

        if (ruleParams.length >= 1 && !ruleParams[0].isEmpty()) {
            rule = (ValidationRule)ruleType.getConstructor(String.class).newInstance(ruleParams);
        } else {
            rule = (ValidationRule)ruleType.getConstructor().newInstance();
        }
        return rule;
    }

    protected void addValidationRuleToField(Object field, ValidationRule rule, String message) {
        if (!(field instanceof JComponent)) {
            return;
        }
        List<ValidationRule> rules;
        if (!textFieldValidationRuleMap.containsKey(field)) {
            rules = new ArrayList<>();
        } else {
            rules = textFieldValidationRuleMap.get(field);
        }

        if (!rules.contains(rule) && rule != null) {
            addFieldValidationRuleMessageAssociation(field, rule, message);
            rules.add(rule);
            textFieldValidationRuleMap.put(field, rules);
        }
    }

    private void addFieldValidationRuleMessageAssociation(Object field, ValidationRule rule, String message) {
        Map<ValidationRule, String> validationRuleErrorMessageMap;
        if (!errorMessageFieldValidationRuleMap.containsKey(field)) {
            validationRuleErrorMessageMap = new HashMap<>();
        } else {
            validationRuleErrorMessageMap = errorMessageFieldValidationRuleMap.get(field);
        }
        validationRuleErrorMessageMap.put(rule, message);
        errorMessageFieldValidationRuleMap.put(field, validationRuleErrorMessageMap);
    }

    private String resolveFieldValueByComponentType(Object field) {
        if (field instanceof JTextField) {
            return ((JTextField) field).getText();
        } else if (field instanceof JComboBox) {
            return ((JComboBox) field).getSelectedItem().toString();
        }
        return null;
    }
}
