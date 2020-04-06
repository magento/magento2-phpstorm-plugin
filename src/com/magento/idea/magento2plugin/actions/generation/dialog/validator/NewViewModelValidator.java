/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.magento.idea.magento2plugin.actions.generation.dialog.NewViewModelDialog;
import com.magento.idea.magento2plugin.util.Regex;

import javax.swing.*;

public class NewViewModelValidator {
    private static NewViewModelValidator INSTANCE = null;
    private NewViewModelDialog dialog;

    public static NewViewModelValidator getInstance(NewViewModelDialog dialog) {
        if (null == INSTANCE) {
            INSTANCE = new NewViewModelValidator();
        }
        INSTANCE.dialog = dialog;
        return INSTANCE;
    }

    public boolean validate()
    {
        String errorTitle = "Error";

        String moduleName = dialog.getViewModelName();
        if (moduleName.length() == 0) {
            JOptionPane.showMessageDialog(null, "View Model Name must not be empty.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!moduleName.matches(Regex.ALPHANUMERIC)) {
            JOptionPane.showMessageDialog(null, "View Model Name must contain letters and numbers only.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!Character.isUpperCase(moduleName.charAt(0)) && !Character.isDigit(moduleName.charAt(0))) {
            JOptionPane.showMessageDialog(null, "View Model Name must start from a number or a capital letter", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String pluginDirectory = dialog.getViewModelDirectory();
        if (pluginDirectory.length() == 0) {
            JOptionPane.showMessageDialog(null, "View Model Directory must not be empty.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!pluginDirectory.matches(Regex.DIRECTORY)) {
            JOptionPane.showMessageDialog(null, "View Model Directory is not valid.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }
}
