/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.util.Pair;
import com.magento.idea.magento2plugin.actions.generation.dialog.reflection.ExtractComponentFromFieldUtil;
import com.magento.idea.magento2plugin.actions.generation.dialog.util.DialogFieldErrorUtil;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.TypeFieldsRulesParser;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.data.FieldValidationData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.ValidationRule;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.jetbrains.annotations.NotNull;

/**
 * All code generate dialog should extend this class.
 */
public abstract class AbstractDialog extends JDialog {

    protected CommonBundle bundle;
    protected final ValidatorBundle validatorBundle = new ValidatorBundle();
    protected final List<FieldValidationData> fieldsValidationsList;
    private final String errorTitle;
    private JTabbedPane tabbedPane;
    private boolean isValidationErrorShown;

    /**
     * Abstract Dialog Constructor.
     */
    public AbstractDialog() {
        super();
        bundle = new CommonBundle();
        errorTitle = bundle.message("common.error");
        fieldsValidationsList = new TypeFieldsRulesParser(this).parseValidationRules();
    }

    protected void centerDialog(final AbstractDialog dialog) {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final int coordinateX = screenSize.width / 2  - dialog.getSize().width / 2;
        final int coordinateY = screenSize.height / 2 - dialog.getSize().height / 2;
        dialog.setLocation(coordinateX, coordinateY);
    }

    protected void onCancel() {
        this.setVisible(false);
    }

    /**
     * Validate all form fields.
     *
     * @return boolean
     */
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.AvoidDeeplyNestedIfStmts"})
    protected boolean validateFormFields() {
        boolean dialogHasErrors;
        isValidationErrorShown = dialogHasErrors = false;
        clearValidationHighlighting();

        for (final FieldValidationData fieldValidationData : getFieldsToValidate()) {
            final Field field = fieldValidationData.getField();
            final List<Pair<ValidationRule, String>> rules = fieldValidationData.getRules();

            for (final Pair<ValidationRule, String> rulePair : rules) {
                final ValidationRule rule = rulePair.getFirst();
                final String message = rulePair.getSecond();
                final String value = resolveFieldValueByComponentType(field);

                if (value != null && !rule.check(value)) {
                    if (!dialogHasErrors) {
                        final JComponent component =
                                ExtractComponentFromFieldUtil.extract(field, this);

                        if (component != null && tabbedPane != null) {
                            navigateToTabWithComponent(component);
                        }
                    }
                    dialogHasErrors = true;
                    showErrorMessage(field, message);
                    break;
                }
            }
        }

        if (dialogHasErrors && !isValidationErrorShown) {
            showErrorMessage(validatorBundle.message("validator.someFieldsHaveErrors"));
        }

        return !dialogHasErrors;
    }

    /**
     * Reset highlighting for fields.
     */
    protected void clearValidationHighlighting() {
        for (final FieldValidationData fieldValidationData : fieldsValidationsList) {
            DialogFieldErrorUtil.resetFieldHighlighting(fieldValidationData.getField(), this);
        }
    }

    /**
     * Override this method to change which fields should or shouldn't be validated.
     *
     * @return List[FieldValidationData]
     */
    protected List<FieldValidationData> getFieldsToValidate() {
        return new LinkedList<>(fieldsValidationsList);
    }

    /**
     * Tabbed pane should be registered to be possible navigate to the tab in which error occurred.
     *
     * @param tabbedPane JTabbedPane
     */
    protected void registerTabbedPane(final @NotNull JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
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
     * Resolve value of stored component by field.
     *
     * @param field Field
     *
     * @return String
     */
    private String resolveFieldValueByComponentType(final Field field) {
        final JComponent component = ExtractComponentFromFieldUtil.extract(field, this);

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
     * Navigate to tab with specified component.
     *
     * @param component JComponent
     */
    private void navigateToTabWithComponent(final @NotNull JComponent component) {
        if (tabbedPane == null) {
            return;
        }

        final int index = getParentTabPaneForComponent(component);

        if (index != -1) {
            tabbedPane.setSelectedIndex(index);
        }
    }

    /**
     * Get parent tab index for component.
     *
     * @param component Container
     *
     * @return int
     */
    private int getParentTabPaneForComponent(final @NotNull Container component) {
        if (tabbedPane == null) {
            return -1;
        }
        final int parentTabIndex = tabbedPane.indexOfComponent(component);

        if (parentTabIndex != -1) {
            return parentTabIndex;
        }
        final Container parent = component.getParent();

        if (parent == null) {
            return -1;
        }

        return getParentTabPaneForComponent(parent);
    }
}
