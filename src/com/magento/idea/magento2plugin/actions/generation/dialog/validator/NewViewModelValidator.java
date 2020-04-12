/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.magento.idea.magento2plugin.actions.generation.dialog.NewViewModelDialog;
import com.magento.idea.magento2plugin.util.Regex;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;

import javax.swing.*;

public class NewViewModelValidator {
    private static NewViewModelValidator INSTANCE = null;
    private ValidatorBundle validatorBundle;
    private NewViewModelDialog dialog;

    public static NewViewModelValidator getInstance(NewViewModelDialog dialog) {
        if (null == INSTANCE) {
            INSTANCE = new NewViewModelValidator();
        }
        INSTANCE.dialog = dialog;
        return INSTANCE;
    }

    public NewViewModelValidator() {
        this.validatorBundle = new ValidatorBundle();
    }

    public boolean validate()
    {
        String errorTitle = "Error";

        String moduleName = dialog.getViewModelName();
        if (moduleName.length() == 0) {
            String errorMessage = validatorBundle.message("validator.notEmpty", "View Model Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!moduleName.matches(Regex.ALPHANUMERIC)) {
            String errorMessage = validatorBundle.message("validator.alphaNumericCharacters", "View Model Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!Character.isUpperCase(moduleName.charAt(0)) && !Character.isDigit(moduleName.charAt(0))) {
            String errorMessage = validatorBundle.message("validator.startWithNumberOrCapitalLetter", "View Model Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        String pluginDirectory = dialog.getViewModelDirectory();
        if (pluginDirectory.length() == 0) {
            String errorMessage = validatorBundle.message("validator.notEmpty", "View Model Directory");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!pluginDirectory.matches(Regex.DIRECTORY)) {
            String errorMessage = validatorBundle.message("validator.directory.isNotValid", "View Model Directory");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        return true;
    }
}
