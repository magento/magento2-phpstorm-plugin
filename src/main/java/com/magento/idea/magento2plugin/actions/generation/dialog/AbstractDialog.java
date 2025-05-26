/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * All code generate dialog should extend this class.
 */
@SuppressWarnings({
        "PMD.TooManyMethods"
})
public abstract class AbstractDialog extends DialogWrapper {

    protected transient CommonBundle bundle;
    protected final transient ValidatorBundle validatorBundle = new ValidatorBundle();
    protected final List<FieldValidationData> fieldsValidationsList;
    private final String errorTitle;
    private JTabbedPane tabbedPane;
    private boolean isValidationErrorShown;

    /**
     * Abstract Dialog Constructor.
     *
     * @param project Project
     */
    public AbstractDialog(final @Nullable Project project) {
        super(project, true);
        bundle = new CommonBundle();
        errorTitle = bundle.message("common.error");
        fieldsValidationsList = new TypeFieldsRulesParser(this).parseValidationRules();
        init();
    }

    /**
     * Abstract Dialog Constructor without project.
     */
    public AbstractDialog() {
        this(null);
    }

    /**
     * Center the dialog on the screen.
     * Note: This is handled automatically by DialogWrapper, 
     * so this method is kept for compatibility.
     *
     * @param dialog AbstractDialog
     * @deprecated This method is no longer needed as DialogWrapper handles centering automatically.
     *             It is kept for backward compatibility with existing code.
     */
    @Deprecated
    @SuppressWarnings({
            "PMD.UncommentedEmptyMethod",
            "PMD.EmptyMethodInAbstractClassShouldBeAbstract"
    })
    protected void centerDialog(final AbstractDialog dialog) {
        // DialogWrapper handles centering automatically
        // This method is intentionally left with minimal implementation
        // as it's deprecated and only kept for backward compatibility
    }

    /**
     * Create the center panel for the dialog.
     * This method must be implemented by subclasses to provide the content panel.
     *
     * @return JComponent
     */
    @Nullable
    @Override
    protected abstract JComponent createCenterPanel();

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
        close(CANCEL_EXIT_CODE);
    }

    /**
     * Executes onOK within a WriteAction context.
     */
    protected final void executeOnOk() {
        WriteAction.run(this::onWriteActionOK);
    }

    /**
     * This method should contain the core logic for onOk.
     * Subclasses can override to provide their implementation.
     * Must be invoked via executeOnOk().
     */
    protected abstract void onWriteActionOK();

    /**
     * Hook executed when the OK button is pressed.
     */
    protected final void onOK() {
        if (validateFormFields()) {
            executeOnOk();
        }
    }

    /**
     * Called when the OK button is pressed.
     * This method is called by DialogWrapper.
     */
    @Override
    public void doOKAction() {
        onOK();
    }

    /**
     * Called when the Cancel button is pressed.
     * This method is called by DialogWrapper.
     */
    @Override
    public void doCancelAction() {
        onCancel();
    }

    /**
     * Validate all form fields.
     *
     * @return boolean
     */
    @SuppressWarnings({
            "PMD.CyclomaticComplexity",
            "PMD.AvoidDeeplyNestedIfStmts",
            "PMD.CognitiveComplexity"
    })
    protected boolean validateFormFields() {
        boolean dialogHasErrors = false;
        isValidationErrorShown = false;
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
        Messages.showErrorDialog(
                getContentPanel(),
                errorMessage,
                errorTitle
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

    /**
     * Show the dialog.
     * This method should be used instead of setVisible(true).
     */
    public void showDialog() {
        new PlaceholderInitializerUtil(this).initialize();
        show();
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
     *             sampleField.requestFocus();
     *     }
     * </pre>
     *
     * <p>2) call in the constructor method:</p><br/>
     * <pre>
     *     addComponentListener(
     *             new FocusOnAFieldListener(this::focusOnTheSampleField)
     *     )
     * </pre>
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
