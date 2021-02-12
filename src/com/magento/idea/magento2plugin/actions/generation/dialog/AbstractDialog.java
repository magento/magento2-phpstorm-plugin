/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.magento.idea.magento2plugin.actions.generation.dialog.util.HighlightDialogFieldOnErrorUtil;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidations;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.ValidationRule;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * All code generate dialog should extend this class.
 */
@SuppressWarnings({"PMD.ShortVariable", "PMD.MissingSerialVersionUID"})
public abstract class AbstractDialog extends JDialog {
    protected CommonBundle bundle;
    protected final ValidatorBundle validatorBundle = new ValidatorBundle();
    private final String errorTitle;
    private final Map<Object, List<ValidationRule>> textFieldValidationRuleMap;
    private final Map<Object, Map<ValidationRule, String>> errorMessageFieldValidationRuleMap;

    /**
     * Abstract Dialog Constructor.
     */
    public AbstractDialog() {
        super();
        bundle = new CommonBundle();
        errorTitle = bundle.message("common.error");
        textFieldValidationRuleMap = new LinkedHashMap<>();
        errorMessageFieldValidationRuleMap = new HashMap<>();
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
        for (final Map.Entry<Object, List<ValidationRule>> entry
                : textFieldValidationRuleMap.entrySet()) {
            final Object field = entry.getKey();
            final List<ValidationRule> rules = entry.getValue();

            for (final ValidationRule rule : rules) {
                final String value = resolveFieldValueByComponentType(field);

                if (value != null && !rule.check(value)) {
                    if (errorMessageFieldValidationRuleMap.containsKey(field)
                            && errorMessageFieldValidationRuleMap.get(field).containsKey(rule)) {
                        showErrorMessage(errorMessageFieldValidationRuleMap.get(field).get(rule));
                        highlightFieldWithErrorStyle(field);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    protected void showErrorMessage(final String errorMessage) {
        JOptionPane.showMessageDialog(
                null,
                errorMessage,
                errorTitle,
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void addValidationRulesFromAnnotations() {
        final Class<?> type = this.getClass();
        final List<FieldValidation> validations = new LinkedList<>();

        for (final Field field : type.getDeclaredFields()) {
            field.setAccessible(true);
            validations.clear();

            if (field.isAnnotationPresent(FieldValidation.class)) {
                validations.add(field.getAnnotation(FieldValidation.class));
            }
            if (field.isAnnotationPresent(FieldValidations.class)) {
                validations.addAll(
                        Arrays.asList(field.getAnnotation(FieldValidations.class).value())
                );
            }

            for (final FieldValidation validation : validations) {
                try {
                    addValidationRuleToField(
                            field.get(this),
                            getRuleFromAnnotation(validation),
                            getMessageFromAnnotation(validation)
                    );
                } catch (Exception exception) { // NOPMD
                    // We don't need to cover this case.
                }
            }
            field.setAccessible(false);
        }
    }

    private String getMessageFromAnnotation(final FieldValidation validation) {
        String[] params;
        final int minMessageArrayLength = 1;

        if (validation.message().length > minMessageArrayLength) {
            params = Arrays.copyOfRange(validation.message(), 1, validation.message().length);
        } else {
            params = new String[]{};
        }
        return validatorBundle.message(validation.message()[0], (Object[]) params);
    }

    private ValidationRule getRuleFromAnnotation(final FieldValidation validation)
            throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        final Class<?> ruleType = validation.rule().getRule();

        return (ValidationRule) ruleType.getConstructor().newInstance();
    }

    protected void addValidationRuleToField(
            final Object field,
            final ValidationRule rule,
            final String message) {
        if (!(field instanceof JComponent)) {
            return;
        }
        List<ValidationRule> rules;
        if (textFieldValidationRuleMap.containsKey(field)) {
            rules = textFieldValidationRuleMap.get(field);
        } else {
            rules = new ArrayList<>();
        }

        if (!rules.contains(rule) && rule != null) {
            addFieldValidationRuleMessageAssociation(field, rule, message);
            rules.add(rule);
            textFieldValidationRuleMap.put(field, rules);
        }
    }

    private void addFieldValidationRuleMessageAssociation(
            final Object field,
            final ValidationRule rule,
            final String message) {
        Map<ValidationRule, String> validationRuleErrorMessageMap;
        if (errorMessageFieldValidationRuleMap.containsKey(field)) {
            validationRuleErrorMessageMap = errorMessageFieldValidationRuleMap.get(field);
        } else {
            validationRuleErrorMessageMap = new HashMap<>();
        }
        validationRuleErrorMessageMap.put(rule, message);
        errorMessageFieldValidationRuleMap.put(field, validationRuleErrorMessageMap);
    }

    private String resolveFieldValueByComponentType(final Object field) {
        if (field instanceof JTextField) {
            return ((JTextField) field).isEditable() ? ((JTextField) field).getText() : null;
        } else if (field instanceof JComboBox) {
            if (((JComboBox<?>) field).getSelectedIndex() == -1) {
                return "";
            } else {
                return ((JComboBox) field).getSelectedItem().toString();
            }
        } else if (field instanceof JTextArea) {
            return ((JTextArea) field).getText();
        }
        return null;
    }

    /**
     * Highlight field with error style.
     *
     * @param field Object
     */
    private void highlightFieldWithErrorStyle(final Object field) {
        if (field instanceof JTextField) {
            HighlightDialogFieldOnErrorUtil.execute((JTextField) field);
        } else if (field instanceof JComboBox) {
            HighlightDialogFieldOnErrorUtil.execute((JComboBox) field);
        }
    }
}
