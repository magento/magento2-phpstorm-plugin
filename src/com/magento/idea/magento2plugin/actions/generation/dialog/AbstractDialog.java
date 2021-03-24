/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.magento.idea.magento2plugin.actions.generation.dialog.util.DialogFieldErrorUtil;
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
import org.jetbrains.annotations.NotNull;

/**
 * All code generate dialog should extend this class.
 */
@SuppressWarnings({"PMD.ShortVariable", "PMD.MissingSerialVersionUID"})
public abstract class AbstractDialog extends JDialog {

    protected CommonBundle bundle;
    protected final ValidatorBundle validatorBundle = new ValidatorBundle();
    private final String errorTitle;
    private final Map<Field, List<ValidationRule>> textFieldValidationRuleMap;
    private final Map<Field, Map<ValidationRule, String>> errorMessageFieldValidationRuleMap;
    private boolean isValidationErrorShown;
    private boolean dialogHasErrors;

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

    /**
     * Validate all form fields.
     *
     * @return boolean
     */
    protected boolean validateFormFields() {
        addValidationRulesFromAnnotations();
        isValidationErrorShown = dialogHasErrors = false;

        for (final Map.Entry<Field, List<ValidationRule>> entry
                : textFieldValidationRuleMap.entrySet()) {
            final Field field = entry.getKey();
            final List<ValidationRule> rules = entry.getValue();

            for (final ValidationRule rule : rules) {
                final String value = resolveFieldValueByComponentType(field);

                if (value != null && !rule.check(value)) {
                    if (errorMessageFieldValidationRuleMap.containsKey(field)
                            && errorMessageFieldValidationRuleMap.get(field).containsKey(rule)) {
                        dialogHasErrors = true;
                        showErrorMessage(
                                field,
                                errorMessageFieldValidationRuleMap.get(field).get(rule)
                        );
                    }
                    break;
                }
            }
        }

        if (dialogHasErrors && !isValidationErrorShown) {
            showErrorMessage(
                    validatorBundle.message("validator.someFieldsHaveErrors")
            );
        }

        return !dialogHasErrors;
    }

    /**
     * Show error message for field.
     *
     * @param field Field
     * @param errorMessage String
     */
    protected void showErrorMessage(
            final @NotNull Field field,
            final @NotNull String errorMessage
    ) {
        final boolean isMessageShown =
                DialogFieldErrorUtil.showErrorMessageForField(this, field, errorMessage);

        if (!isMessageShown) {
            showErrorMessage(errorMessage);
            DialogFieldErrorUtil.highlightField(this, field);
        }
    }

    /**
     * Show error message in dialog.
     *
     * @param errorMessage String
     */
    protected void showErrorMessage(final String errorMessage) {
        if (isValidationErrorShown) {
            return;
        }
        JOptionPane.showMessageDialog(
                null,
                errorMessage,
                errorTitle,
                JOptionPane.ERROR_MESSAGE
        );
        isValidationErrorShown = true;
    }

    /**
     * Process validation rules from annotations.
     */
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
                            field,
                            getRuleFromAnnotation(validation),
                            getMessageFromAnnotation(validation)
                    );
                } catch (NoSuchMethodException | IllegalAccessException
                        | InvocationTargetException | InstantiationException exception) {
                    return;
                } finally {
                    field.setAccessible(false);
                }
            }
            field.setAccessible(false);
        }
    }

    /**
     * Get error message from annotation.
     *
     * @param validation FieldValidation
     *
     * @return String
     */
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

    /**
     * Get validation rule from annotation.
     *
     * @param validation FieldValidation
     *
     * @return ValidationRule
     */
    private ValidationRule getRuleFromAnnotation(final FieldValidation validation)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException {
        final Class<?> ruleType = validation.rule().getRule();

        return (ValidationRule) ruleType.getConstructor().newInstance();
    }

    /**
     * Add validation rule for field.
     *
     * @param field Field
     * @param rule ValidationRule
     * @param message String
     */
    protected void addValidationRuleToField(
            final Field field,
            final ValidationRule rule,
            final String message
    ) {
        if (getComponentForField(field) == null) {
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

    /**
     * Associate validation rule with field.
     *
     * @param field Field
     * @param rule ValidationRule
     * @param message String
     */
    private void addFieldValidationRuleMessageAssociation(
            final Field field,
            final ValidationRule rule,
            final String message
    ) {
        Map<ValidationRule, String> validationRuleErrorMessageMap;
        if (errorMessageFieldValidationRuleMap.containsKey(field)) {
            validationRuleErrorMessageMap = errorMessageFieldValidationRuleMap.get(field);
        } else {
            validationRuleErrorMessageMap = new HashMap<>();
        }
        validationRuleErrorMessageMap.put(rule, message);
        errorMessageFieldValidationRuleMap.put(field, validationRuleErrorMessageMap);
    }

    /**
     * Resolve value of stored component by field.
     *
     * @param field Field
     *
     * @return String
     */
    private String resolveFieldValueByComponentType(final Field field) {
        final JComponent component = getComponentForField(field);

        if (component instanceof JTextField) {
            return ((JTextField) component).isEditable()
                    ? ((JTextField) component).getText() : null;
        } else if (component instanceof JComboBox) {
            if (((JComboBox<?>) component).getSelectedIndex() == -1) {
                return "";
            } else {
                return ((JComboBox) component).getSelectedItem().toString();
            }
        } else if (component instanceof JTextArea) {
            return ((JTextArea) component).getText();
        }

        return null;
    }

    /**
     * Get JComponent for field.
     *
     * @param field Field
     *
     * @return JComponent
     */
    private JComponent getComponentForField(final @NotNull Field field) {
        try {
            field.setAccessible(true);
            final Object component = field.get(this);

            if (component instanceof JComponent) {
                return (JComponent) component;
            }
        } catch (IllegalAccessException exception) {
            return null;
        } finally {
            field.setAccessible(false);
        }

        return null;
    }
}
