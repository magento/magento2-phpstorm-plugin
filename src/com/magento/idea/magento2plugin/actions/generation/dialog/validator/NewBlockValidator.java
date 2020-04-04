/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.magento.idea.magento2plugin.actions.generation.dialog.NewBlockDialog;
import com.magento.idea.magento2plugin.util.Regex;
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
            JOptionPane.showMessageDialog(null, "Block Name must not be empty.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!moduleName.matches(Regex.ALPHANUMERIC)) {
            JOptionPane.showMessageDialog(null, "Block Name must contain letters and numbers only.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!Character.isUpperCase(moduleName.charAt(0)) && !Character.isDigit(moduleName.charAt(0))) {
            JOptionPane.showMessageDialog(null, "Block Name must start from a number or a capital letter", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String pluginDirectory = dialog.getBlockDirectory();
        if (pluginDirectory.length() == 0) {
            JOptionPane.showMessageDialog(null, "Block Directory must not be empty.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!pluginDirectory.matches(Regex.DIRECTORY)) {
            JOptionPane.showMessageDialog(null, "Block Directory is not valid.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }
}
