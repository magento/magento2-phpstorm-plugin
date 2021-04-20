/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.ui.component;

import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormButtonData;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewUiComponentFormDialog;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphanumericRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.DirectoryRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NumericRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.StartWithNumberOrCapitalLetterRule;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;

public class FormButtonsValidator {

    private static final String BUTTON_CLASS_TEXT = "Button Class";
    private static final String BUTTON_DIRECTORY_TEXT = "Button Directory";
    private static final String BUTTON_LABEL_TEXT = "Button Label";
    private static final String BUTTON_SORT_ORDER_TEXT = "Sort Order";

    private final ValidatorBundle validatorBundle;
    private final NewUiComponentFormDialog dialog;
    private final String errorTitle;

    /**
     * Validator Constructor.
     *
     * @param dialog NewUiComponentFormDialog
     */
    public FormButtonsValidator(final NewUiComponentFormDialog dialog) {
        validatorBundle = new ValidatorBundle();
        errorTitle = new CommonBundle().message("common.error");
        this.dialog = dialog;
    }

    /**
     * Validate button table form fields in the NewUiComponentFormDialog dialog.
     *
     * @return boolean
     */
    @SuppressWarnings({"PMD.NPathComplexity", "PMD.CyclomaticComplexity"})
    public boolean validate() {
        boolean valid = true;
        final Iterator<UiComponentFormButtonData> buttonsIterator = getButtonsIterator();
        final List<String> buttonFqns = new ArrayList<>();

        if (buttonsIterator != null) {
            while (buttonsIterator.hasNext()) {
                final UiComponentFormButtonData button = buttonsIterator.next();

                final String buttonClassName = button.getButtonClassName();
                if (!NotEmptyRule.getInstance().check(buttonClassName)) {
                    showErrorMessage(
                            validatorBundle.message(NotEmptyRule.MESSAGE, BUTTON_CLASS_TEXT)
                    );
                    valid = false;
                    break;
                }
                if (!PhpClassRule.getInstance().check(buttonClassName)) {
                    showErrorMessage(
                            validatorBundle.message(PhpClassRule.MESSAGE, BUTTON_CLASS_TEXT)
                    );
                    valid = false;
                    break;
                }
                if (!AlphanumericRule.getInstance().check(buttonClassName)) {
                    showErrorMessage(
                            validatorBundle.message(AlphanumericRule.MESSAGE, BUTTON_CLASS_TEXT)
                    );
                    valid = false;
                    break;
                }
                if (!StartWithNumberOrCapitalLetterRule.getInstance().check(buttonClassName)) {
                    showErrorMessage(
                            validatorBundle.message(
                                    StartWithNumberOrCapitalLetterRule.MESSAGE,
                                    BUTTON_CLASS_TEXT
                            )
                    );
                    valid = false;
                    break;
                }

                final String buttonDirectory = button.getButtonDirectory();
                if (!NotEmptyRule.getInstance().check(buttonDirectory)) {
                    showErrorMessage(
                            validatorBundle.message(NotEmptyRule.MESSAGE, BUTTON_DIRECTORY_TEXT)
                    );
                    valid = false;
                    break;
                }
                if (!DirectoryRule.getInstance().check(buttonDirectory)) {
                    showErrorMessage(
                            validatorBundle.message(DirectoryRule.MESSAGE, BUTTON_DIRECTORY_TEXT)
                    );
                    valid = false;
                    break;
                }

                final String buttonLabel = button.getButtonLabel();
                if (!NotEmptyRule.getInstance().check(buttonLabel)) {
                    showErrorMessage(
                            validatorBundle.message(NotEmptyRule.MESSAGE, BUTTON_LABEL_TEXT)
                    );
                    valid = false;
                    break;
                }

                final String buttonSortOrder = button.getButtonSortOrder();
                if (!NotEmptyRule.getInstance().check(buttonSortOrder)) {
                    showErrorMessage(
                            validatorBundle.message(NotEmptyRule.MESSAGE, BUTTON_SORT_ORDER_TEXT)
                    );
                    valid = false;
                    break;
                }
                if (!NumericRule.getInstance().check(buttonSortOrder)) {
                    showErrorMessage(
                            validatorBundle.message(NumericRule.MESSAGE, BUTTON_SORT_ORDER_TEXT)
                    );
                    valid = false;
                    break;
                }

                final String buttonFqn = button.getFqn();
                if (buttonFqns.contains(buttonFqn)) {
                    showErrorMessage(
                            validatorBundle.message("validator.class.shouldBeUnique", buttonFqn)
                    );
                    valid = false;
                    break;
                }
                buttonFqns.add(buttonFqn);
            }
        }
        return valid;
    }

    private Iterator<UiComponentFormButtonData> getButtonsIterator() {
        final List<UiComponentFormButtonData> buttons = dialog.getButtons();
        if (buttons.isEmpty()) {
            return null;
        }
        return buttons.iterator();
    }

    private void showErrorMessage(final String errorMessage) {
        JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);
    }
}
