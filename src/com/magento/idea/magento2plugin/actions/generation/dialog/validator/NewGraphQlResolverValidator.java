/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.magento.idea.magento2plugin.actions.generation.dialog.NewGraphQlResolverDialog;
import com.magento.idea.magento2plugin.util.Regex;

import javax.swing.*;

public class NewGraphQlResolverValidator {
    private static NewGraphQlResolverValidator INSTANCE = null;
    private NewGraphQlResolverDialog dialog;

    public static NewGraphQlResolverValidator getInstance(NewGraphQlResolverDialog dialog) {
        if (null == INSTANCE) {
            INSTANCE = new NewGraphQlResolverValidator();
        }
        INSTANCE.dialog = dialog;
        return INSTANCE;
    }

    public boolean validate()
    {
        String errorTitle = "Error";

        String moduleName = dialog.getGraphQlResolverClassName();
        if (moduleName.length() == 0) {
            JOptionPane.showMessageDialog(null, "GraphQL Resolver Name must not be empty.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!moduleName.matches(Regex.ALPHANUMERIC)) {
            JOptionPane.showMessageDialog(null, "GraphQL Resolver Name must contain letters and numbers only.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!Character.isUpperCase(moduleName.charAt(0)) && !Character.isDigit(moduleName.charAt(0))) {
            JOptionPane.showMessageDialog(null, "GraphQL Resolver Name must start from a number or a capital letter", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String pluginDirectory = dialog.getGraphQlResolverDirectory();
        if (pluginDirectory.length() == 0) {
            JOptionPane.showMessageDialog(null, "GraphQL Resolver Directory must not be empty.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!pluginDirectory.matches(Regex.DIRECTORY)) {
            JOptionPane.showMessageDialog(null, "GraphQL Resolver Directory is not valid.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }
}
