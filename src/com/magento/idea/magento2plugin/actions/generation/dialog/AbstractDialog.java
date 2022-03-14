/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.util.Pair;
import com.magento.idea.magento2plugin.actions.generation.data.ui.ComboBoxItemData;
import com.magento.idea.magento2plugin.actions.generation.dialog.prompt.PlaceholderInitializerUtil;
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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
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

    /**
     * Default on cancel action.
     */
    protected void onCancel() {
        this.exit();
    }

    /**
     * Right way to hide dialog window.
     */
    protected void exit() {
        dispose();
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
        final List<FieldValidationData> result = new LinkedList<>();

        for (final FieldValidationData fieldValidationData : fieldsValidationsList) {
            final JComponent component = ExtractComponentFromFieldUtil.extract(
                    fieldValidationData.getField(),
                    this
            );

            if (component != null && component.isVisible() && component.getParent().isVisible()) {
                result.add(fieldValidationData);
            }
        }

        return result;
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
            return ((JTextField) component).getText();
        } else if (component instanceof JComboBox) {
            if (((JComboBox<?>) component).getSelectedIndex() == -1) {
                return "";
            }
            final Object selectedItem = ((JComboBox<?>) component).getSelectedItem();

            if (selectedItem == null) {
                return "";
            }

            if (selectedItem instanceof ComboBoxItemData) {
                return ((ComboBoxItemData) selectedItem).getKey();
            } else {
                return selectedItem.toString();
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

    @Override
    public void setVisible(final boolean status) {
        new PlaceholderInitializerUtil(this).initialize();
        super.setVisible(status);
    }

    /**
     * Listener that helps focus on a field for dialogues after it is opened.
     *
     * <p><b>This inner class designed to simplify focusing on a field for the
     * implementations of this abstract class.</b></p>
     * <p><b>To use this listener:</b></p>
     * <p>1) specify method in which desired field is focused:</p><br/>
     * <pre>
     *     public void focusOnTheSampleField() {
     *             sampleField.requestFocusInWindow();
     *     }
     * </pre>
     *
     * <p>2) call in the constructor method:</p><br/>
     * <pre>
     *     addComponentListener(
     *             new FocusOnAFieldListener(this::focusOnTheSampleField)
     *     )
     * </pre>
     *
     * @see #requestFocusInWindow()
     */
    public static final class FocusOnAFieldListener implements ComponentListener {

        private final @NotNull Runnable makeAFieldFocusedAction;

        /**
         * Focus on a field listener constructor.
         *
         * @param makeAFieldFocused Runnable method in which desired field is focused.
         */
        public FocusOnAFieldListener(final @NotNull Runnable makeAFieldFocused) {
            makeAFieldFocusedAction = makeAFieldFocused;
        }

        @Override
        @SuppressWarnings("PMD.UncommentedEmptyMethodBody")
        public void componentResized(final ComponentEvent event) {
        }

        @Override
        @SuppressWarnings("PMD.UncommentedEmptyMethodBody")
        public void componentMoved(final ComponentEvent event) {
        }

        @Override
        public void componentShown(final ComponentEvent event) {
            makeAFieldFocusedAction.run();
        }

        @Override
        @SuppressWarnings("PMD.UncommentedEmptyMethodBody")
        public void componentHidden(final ComponentEvent event) {
        }
    }
}
