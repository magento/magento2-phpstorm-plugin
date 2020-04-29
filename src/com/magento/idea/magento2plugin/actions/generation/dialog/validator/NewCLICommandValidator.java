/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewCLICommandDialog;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import com.magento.idea.magento2plugin.util.RegExUtil;

import javax.swing.*;

public class NewCLICommandValidator {
    private static NewCLICommandValidator INSTANCE = null;
    private ValidatorBundle validatorBundle;

    public static NewCLICommandValidator getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new NewCLICommandValidator();
        }

        return INSTANCE;
    }

    public NewCLICommandValidator() {
        this.validatorBundle = new ValidatorBundle();
    }

    public boolean validate(Project project, NewCLICommandDialog dialog) {
        String errorTitle = "Validation Error";
        String cliCommandClassName = dialog.getCLICommandClassName();
        String cliCommandParentDirectory = dialog.getCLICommandParentDirectory();
        String cliCommandName = dialog.getCLICommandName();
        String cliCommandDescription = dialog.getCLICommandDescription();

        if (cliCommandClassName.length() == 0) {
            String errorMessage = validatorBundle.message("validator.notEmpty", "CLI Command Class Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }
        if (!cliCommandClassName.matches(RegExUtil.ALPHANUMERIC)) {
            String errorMessage = validatorBundle.message(
                "validator.alphaNumericCharacters",
                "CLI Command Class Name"
            );
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }
        if (!Character.isUpperCase(cliCommandClassName.charAt(0)) && !Character.isDigit(cliCommandClassName.charAt(0))) {
            String errorMessage = validatorBundle.message(
                "validator.startWithNumberOrCapitalLetter",
                "CLI Command Class Name"
            );
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (cliCommandParentDirectory.length() == 0) {
            String errorMessage = validatorBundle.message("validator.notEmpty", "CLI Command Parent Directory");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }
        if (!cliCommandParentDirectory.matches(RegExUtil.DIRECTORY)) {
            String errorMessage = validatorBundle.message(
                    "validator.directory.isNotValid",
                    "CLI Command Parent Directory"
            );
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (cliCommandName.length() == 0) {
            String errorMessage = validatorBundle.message("validator.notEmpty", "CLI Command Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }
        if (!cliCommandName.matches(RegExUtil.CLI_COMMAND_NAME)) {
            String errorMessage = validatorBundle.message("validator.identifier", "CLI Command Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (cliCommandDescription.length() == 0) {
            String errorMessage = validatorBundle.message("validator.notEmpty", "CLI Command Description");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        return true;
    }
}
