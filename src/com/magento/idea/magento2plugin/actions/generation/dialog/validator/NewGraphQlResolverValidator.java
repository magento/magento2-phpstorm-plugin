/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.magento.idea.magento2plugin.actions.generation.dialog.NewGraphQlResolverDialog;
import com.magento.idea.magento2plugin.util.RegExUtil;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;

import javax.swing.*;

public class NewGraphQlResolverValidator {
    private static NewGraphQlResolverValidator INSTANCE = null;
    private ValidatorBundle validatorBundle;
    private NewGraphQlResolverDialog dialog;

    public static NewGraphQlResolverValidator getInstance(NewGraphQlResolverDialog dialog) {
        if (null == INSTANCE) {
            INSTANCE = new NewGraphQlResolverValidator();
        }
        INSTANCE.dialog = dialog;
        return INSTANCE;
    }

    public NewGraphQlResolverValidator() {
        this.validatorBundle = new ValidatorBundle();
    }

    public boolean validate()
    {
        String errorTitle = "Error";

        String resolverClassName = dialog.getGraphQlResolverClassName();
        if (resolverClassName.length() == 0) {
            String errorMessage = validatorBundle.message("validator.notEmpty", "GraphQL Resolver Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!resolverClassName.matches(RegExUtil.ALPHANUMERIC)) {
            String errorMessage = validatorBundle.message("validator.alphaNumericCharacters", "GraphQL Resolver Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!Character.isUpperCase(resolverClassName.charAt(0)) && !Character.isDigit(resolverClassName.charAt(0))) {
            String errorMessage = validatorBundle.message("validator.startWithNumberOrCapitalLetter", "GraphQL Resolver Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        String graphQlResolverDirectory = dialog.getGraphQlResolverDirectory();
        if (graphQlResolverDirectory.length() == 0) {
            String errorMessage = validatorBundle.message("validator.notEmpty", "GraphQL Resolver Directory");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!graphQlResolverDirectory.matches(RegExUtil.DIRECTORY)) {
            String errorMessage = validatorBundle.message("validator.directory.isNotValid", "GraphQL Resolver Directory");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        return true;
    }
}
