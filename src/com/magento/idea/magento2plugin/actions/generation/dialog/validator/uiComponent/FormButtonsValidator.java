package com.magento.idea.magento2plugin.actions.generation.dialog.validator.uiComponent;

import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormButtonData;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewUiComponentFormDialog;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.IsValidPhpClassValidationRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.MatchRegexValidationRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyValidationRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.ValidationRule;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.util.RegExUtil;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;

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
                if (!NotEmptyValidationRule.getInstance().check(buttonClassName)) {
                    showErrorMessage(
                            validatorBundle.message(
                                    NotEmptyValidationRule.DEFAULT_BUNDLE_MESSAGE_KEY,
                                    "Button Class"
                            )
                    );
                    valid = false;
                    break;
                }
                if (!IsValidPhpClassValidationRule.getInstance().check(buttonClassName)) {
                    showErrorMessage(
                            validatorBundle.message(
                                    IsValidPhpClassValidationRule.DEFAULT_BUNDLE_MESSAGE_KEY,
                                    "Button Class"
                            )
                    );
                    valid = false;
                    break;
                }
                if (!MatchRegexValidationRule.getInstance(RegExUtil.ALPHANUMERIC).check(buttonClassName)) {
                    showErrorMessage(
                            validatorBundle.message("validator.alphaNumericCharacters", "Button Class")
                    );
                    valid = false;
                    break;
                }
                boolean startWithNumberOrCapitalLetterValidationResult = (
                        (ValidationRule) value -> Character.isUpperCase(value.charAt(0))
                                || Character.isDigit(value.charAt(0))
                ).check(buttonClassName);
                if (!startWithNumberOrCapitalLetterValidationResult) {
                    showErrorMessage(
                            validatorBundle.message("validator.startWithNumberOrCapitalLetter", "Button Class")
                    );
                    valid = false;
                    break;
                }

                String buttonDirectory = button.getButtonDirectory();
                if (!NotEmptyValidationRule.getInstance().check(buttonDirectory)) {
                    showErrorMessage(
                            validatorBundle.message(
                                    NotEmptyValidationRule.DEFAULT_BUNDLE_MESSAGE_KEY,
                                    "Button Directory"
                            )
                    );
                    valid = false;
                    break;
                }
                if (!MatchRegexValidationRule.getInstance(RegExUtil.DIRECTORY).check(buttonDirectory)) {
                    showErrorMessage(
                            validatorBundle.message("validator.directory.isNotValid", "Button Directory")
                    );
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
        final ArrayList<UiComponentFormButtonData> buttons = dialog.getButtons();
        if (buttons.size() == 0) {
            return null;
        }

        return buttons.iterator();
    }

    private void showErrorMessage(String errorMessage) {
        JOptionPane.showMessageDialog(
                null,
                errorMessage,
                errorTitle,
                JOptionPane.ERROR_MESSAGE
        );
    }
}
