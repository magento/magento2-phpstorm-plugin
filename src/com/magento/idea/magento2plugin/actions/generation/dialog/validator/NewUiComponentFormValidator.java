/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.jetbrains.php.refactoring.PhpNameUtil;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormButtonData;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewUiComponentFormDialog;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.util.RegExUtil;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JOptionPane;

public class NewUiComponentFormValidator {
    private static final String NOT_EMPTY = "validator.notEmpty";
    private static final String BUTTON_CLASS_NAME = "Button ClassName";
    private static final String BUTTON_DIRECTORY = "Button Directory";
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final NewUiComponentFormDialog dialog;

    /**
     * New UI form validator constructor.
     */
    public NewUiComponentFormValidator(final NewUiComponentFormDialog dialog) {
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
        this.dialog = dialog;
    }

    /**
     * Validate whenever UI form dialog data is ready for generation.
     *
     * @return Boolean
     */
    public boolean validate() {
        final String errorTitle = commonBundle.message("common.error");

        final String name = dialog.getFormName();
        if (name.length() == 0) {
            final String errorMessage = this.validatorBundle.message(
                    NOT_EMPTY,
                    "Name"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!name.matches(RegExUtil.IDENTIFIER)) {
            final String errorMessage = validatorBundle.message(
                    "validator.identifier",
                    "Name"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        final String area = dialog.getArea();
        if (area.length() == 0) {
            final String errorMessage = this.validatorBundle.message(
                    NOT_EMPTY,
                    "Area"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        final String label = dialog.getFormLabel();
        if (label.length() == 0) {
            final String errorMessage = this.validatorBundle.message(
                    NOT_EMPTY,
                    "Label"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        return validateButtons();
    }

    protected boolean validateButtons() {
        final String errorTitle = commonBundle.message("common.error");

        final ArrayList<UiComponentFormButtonData> buttons = dialog.getButtons();
        if (buttons.size() == 0) {
            return true;
        }

        boolean result = true;
        ArrayList<String> buttonFqns = new ArrayList<>();
        for (Iterator iterator = buttons.iterator(); iterator.hasNext(); ) {
            UiComponentFormButtonData button = (UiComponentFormButtonData) iterator.next();
            String className = button.getButtonClassName();

            if (className.length() == 0) {
                final String errorMessage = validatorBundle.message(
                        NOT_EMPTY,
                        BUTTON_CLASS_NAME
                );
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        errorTitle,
                        JOptionPane.ERROR_MESSAGE
                );

                result = false;
                break;
            }

            if (!PhpNameUtil.isValidClassName(className)) {
                final String errorMessage = this.validatorBundle.message(
                        "validator.class.isNotValid",
                        BUTTON_CLASS_NAME
                );
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        errorTitle,
                        JOptionPane.ERROR_MESSAGE
                );

                result = false;
                break;
            }

            if (!className.matches(RegExUtil.ALPHANUMERIC)) {
                final String errorMessage = validatorBundle.message(
                        "validator.alphaNumericCharacters",
                        BUTTON_CLASS_NAME
                );
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        errorTitle,
                        JOptionPane.ERROR_MESSAGE
                );

                result = false;
                break;
            }

            if (!Character.isUpperCase(className.charAt(0))
                    && !Character.isDigit(className.charAt(0))
            ) {
                final String errorMessage = validatorBundle.message(
                        "validator.startWithNumberOrCapitalLetter",
                        BUTTON_CLASS_NAME
                );

                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        errorTitle,
                        JOptionPane.ERROR_MESSAGE
                );

                result = false;
                break;
            }

            String directory = button.getButtonDirectory();

            if (directory.length() == 0) {
                final String errorMessage = validatorBundle.message(
                        "validator.notEmpty",
                        BUTTON_DIRECTORY
                );
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        errorTitle,
                        JOptionPane.ERROR_MESSAGE
                );

                result = false;
                break;
            }

            if (!directory.matches(RegExUtil.DIRECTORY)) {
                final String errorMessage = validatorBundle.message(
                        "validator.directory.isNotValid",
                        BUTTON_DIRECTORY
                );
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        errorTitle,
                        JOptionPane.ERROR_MESSAGE
                );

                result = false;
                break;
            }

            if (buttonFqns.contains(button.getFqn())) {
                final String errorMessage = validatorBundle.message(
                        "validator.class.shouldBeUnique",
                        button.getFqn()
                );
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        errorTitle,
                        JOptionPane.ERROR_MESSAGE
                );

                result = false;
                break;
            }

            buttonFqns.add(button.getFqn());
        }

        return result;
    }
}
