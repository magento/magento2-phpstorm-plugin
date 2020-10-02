package com.magento.idea.magento2plugin.actions.generation.dialog.validator.uiComponent;

import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormButtonData;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewUiComponentFormDialog;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.*;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FormButtonsValidator {
    private final ValidatorBundle validatorBundle;
    private final NewUiComponentFormDialog dialog;
    private final String errorTitle;

    public FormButtonsValidator(final NewUiComponentFormDialog dialog) {
        validatorBundle = new ValidatorBundle();
        errorTitle = (new CommonBundle()).message("common.error");
        this.dialog = dialog;
    }

    public boolean validate() {
        boolean valid = true;
        final Iterator<UiComponentFormButtonData> buttonsIterator = getButtonsIterator();
        ArrayList<String> buttonFqns = new ArrayList<>();

        if (buttonsIterator != null) {
            while (buttonsIterator.hasNext()) {
                UiComponentFormButtonData button = buttonsIterator.next();

                String buttonClassName = button.getButtonClassName();
                if (!NotEmptyRule.getInstance().check(buttonClassName)) {
                    showErrorMessage(validatorBundle.message(NotEmptyRule.MESSAGE, "Button Class"));
                    valid = false;
                    break;
                }
                if (!PhpClassRule.getInstance().check(buttonClassName)) {
                    showErrorMessage(validatorBundle.message(PhpClassRule.MESSAGE, "Button Class"));
                    valid = false;
                    break;
                }
                if (!AlphanumericRule.getInstance().check(buttonClassName)) {
                    showErrorMessage(validatorBundle.message(AlphanumericRule.MESSAGE, "Button Class"));
                    valid = false;
                    break;
                }
                if (!StartWithNumberOrCapitalLetterRule.getInstance().check(buttonClassName)) {
                    showErrorMessage(
                            validatorBundle.message(StartWithNumberOrCapitalLetterRule.MESSAGE, "Button Class")
                    );
                    valid = false;
                    break;
                }

                String buttonDirectory = button.getButtonDirectory();
                if (!NotEmptyRule.getInstance().check(buttonDirectory)) {
                    showErrorMessage(validatorBundle.message(NotEmptyRule.MESSAGE, "Button Directory"));
                    valid = false;
                    break;
                }
                if (!DirectoryRule.getInstance().check(buttonDirectory)) {
                    showErrorMessage(validatorBundle.message(DirectoryRule.MESSAGE, "Button Directory"));
                    valid = false;
                    break;
                }

                String buttonLabel = button.getButtonLabel();
                if (!NotEmptyRule.getInstance().check(buttonLabel)) {
                    showErrorMessage(validatorBundle.message(NotEmptyRule.MESSAGE, "Button Label"));
                    valid = false;
                    break;
                }

                String buttonSortOrder = button.getButtonSortOrder();
                if (!NotEmptyRule.getInstance().check(buttonSortOrder)) {
                    showErrorMessage(validatorBundle.message(NotEmptyRule.MESSAGE, "Button Sort Order"));
                    valid = false;
                    break;
                }
                if (!NumericRule.getInstance().check(buttonSortOrder)) {
                    showErrorMessage(validatorBundle.message(NumericRule.MESSAGE, "Button Sort Order"));
                    valid = false;
                    break;
                }

                String buttonFqn = button.getFqn();
                if (buttonFqns.contains(buttonFqn)) {
                    showErrorMessage(validatorBundle.message("validator.class.shouldBeUnique", buttonFqn));
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
        if (buttons.size() == 0) {
            return null;
        }
        return buttons.iterator();
    }

    private void showErrorMessage(String errorMessage) {
        JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);
    }
}
