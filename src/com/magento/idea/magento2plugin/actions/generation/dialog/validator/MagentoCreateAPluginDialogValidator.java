/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.magento.idea.magento2plugin.actions.generation.dialog.MagentoCreateAPluginDialog;
import com.magento.idea.magento2plugin.util.Regex;

import javax.swing.*;

public class MagentoCreateAPluginDialogValidator {
    private static MagentoCreateAPluginDialogValidator INSTANCE = null;
    private MagentoCreateAPluginDialog dialog;

    public static MagentoCreateAPluginDialogValidator getInstance(MagentoCreateAPluginDialog dialog) {
        if (null == INSTANCE) {
            INSTANCE = new MagentoCreateAPluginDialogValidator();
        }
        INSTANCE.dialog = dialog;
        return INSTANCE;
    }

    public boolean validate()
    {
        String errorTitle = "Error";
        String packageName = dialog.getPluginClassName();
        if (packageName.length() == 0) {
            JOptionPane.showMessageDialog(null, "Package Name must not be empty.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!packageName.matches(Regex.ALPHANUMERIC)) {
            JOptionPane.showMessageDialog(null, "Package Name must contain letters and numbers only.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!Character.isUpperCase(packageName.charAt(0)) && !Character.isDigit(packageName.charAt(0))) {
            JOptionPane.showMessageDialog(null, "Package Name must start from a number or a capital letter", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String moduleName = dialog.getPluginDirectory();
        if (moduleName.length() == 0) {
            JOptionPane.showMessageDialog(null, "Module Name must not be empty.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!moduleName.matches(Regex.ALPHANUMERIC)) {
            JOptionPane.showMessageDialog(null, "Module Name must contain letters and numbers only.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!Character.isUpperCase(moduleName.charAt(0)) && !Character.isDigit(moduleName.charAt(0))) {
            JOptionPane.showMessageDialog(null, "Module Name must start from a number or a capital letter", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (dialog.getModuleVersion().length() == 0) {
            JOptionPane.showMessageDialog(null, "Module Version must not be empty.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (dialog.getModuleDescription().length() == 0) {
            JOptionPane.showMessageDialog(null, "Module Version must not be empty.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }
}
