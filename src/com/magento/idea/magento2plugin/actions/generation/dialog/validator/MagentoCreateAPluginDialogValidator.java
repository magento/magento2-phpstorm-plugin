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
        String pluginClassName = dialog.getPluginClassName();
        if (pluginClassName.length() == 0) {
            JOptionPane.showMessageDialog(null, "Plugin Class Name must not be empty.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!pluginClassName.matches(Regex.ALPHANUMERIC)) {
            JOptionPane.showMessageDialog(null, "Plugin Class Name must contain letters and numbers only.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!Character.isUpperCase(pluginClassName.charAt(0)) && !Character.isDigit(pluginClassName.charAt(0))) {
            JOptionPane.showMessageDialog(null, "Plugin Class Name must start from a number or a capital letter", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String pluginDirectory = dialog.getPluginDirectory();
        if (pluginDirectory.length() == 0) {
            JOptionPane.showMessageDialog(null, "Plugin Directory must not be empty.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }
}
