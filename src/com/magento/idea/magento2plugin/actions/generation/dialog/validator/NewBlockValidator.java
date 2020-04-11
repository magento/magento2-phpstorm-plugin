/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.magento.idea.magento2plugin.actions.generation.dialog.NewBlockDialog;
import com.magento.idea.magento2plugin.util.Regex;
import com.magento.idea.magento2plugin.validators.ValidatorBundle;
import javax.swing.*;

public class NewBlockValidator {
    private static NewBlockValidator INSTANCE = null;
    private NewBlockDialog dialog;

    public static NewBlockValidator getInstance(NewBlockDialog dialog) {
        if (null == INSTANCE) {
            INSTANCE = new NewBlockValidator();
        }
        INSTANCE.dialog = dialog;
        return INSTANCE;
    }

    public boolean validate()
    {
        String errorTitle = "Error";

        String moduleName = dialog.getBlockName();
        if (moduleName.length() == 0) {
            String errorMessage = ValidatorBundle.message("validator.notEmpty", "Block Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!moduleName.matches(Regex.ALPHANUMERIC)) {
            String errorMessage = ValidatorBundle.message("validator.alphaNumericCharacters", "Block Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!Character.isUpperCase(moduleName.charAt(0)) && !Character.isDigit(moduleName.charAt(0))) {
            String errorMessage = ValidatorBundle.message("validator.startWithNumberOrCapitalLetter", "Block Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        String pluginDirectory = dialog.getBlockDirectory();
        if (pluginDirectory.length() == 0) {
            String errorMessage = ValidatorBundle.message("validator.notEmpty", "Block Directory");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!pluginDirectory.matches(Regex.DIRECTORY)) {
            String errorMessage = ValidatorBundle.message("validator.directory.isNotValid", "Block Directory");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        return true;
    }
}
