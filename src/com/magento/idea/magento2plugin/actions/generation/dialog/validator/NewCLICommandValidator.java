/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewCLICommandDialog;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.util.RegExUtil;

import javax.swing.*;

public class NewCLICommandValidator {
    private static NewCLICommandValidator INSTANCE = null;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;

    public static NewCLICommandValidator getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new NewCLICommandValidator();
        }

        return INSTANCE;
    }

    public NewCLICommandValidator() {
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    public boolean validate(Project project, NewCLICommandDialog dialog) {
        String errorTitle = "Validation Error";
        String cliCommandClassName = dialog.getCLICommandClassName();
        String cliCommandParentDirectory = dialog.getCLICommandParentDirectory();
        String cliCommandName = dialog.getCLICommandName();
        String cliCommandDescription = dialog.getCLICommandDescription();

        if (cliCommandClassName.length() == 0) {
            String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    this.commonBundle.message("common.cli.class.name")
            );
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }
        if (!cliCommandClassName.matches(RegExUtil.ALPHANUMERIC)) {
            String errorMessage = validatorBundle.message(
                    "validator.alphaNumericCharacters",
                    this.commonBundle.message("common.cli.class.name")
            );
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }
        if (!Character.isUpperCase(cliCommandClassName.charAt(0)) && !Character.isDigit(cliCommandClassName.charAt(0))) {
            String errorMessage = validatorBundle.message(
                    "validator.startWithNumberOrCapitalLetter",
                    this.commonBundle.message("common.cli.class.name")
            );
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (cliCommandParentDirectory.length() == 0) {
            String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    this.commonBundle.message("common.cli.parent.directory")
            );
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }
        if (!cliCommandParentDirectory.matches(RegExUtil.DIRECTORY)) {
            String errorMessage = validatorBundle.message(
                    "validator.directory.isNotValid",
                    this.commonBundle.message("common.cli.parent.directory")
            );
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (cliCommandName.length() == 0) {
            String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    this.commonBundle.message("common.cli.cli.name")
            );
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }
        if (!cliCommandName.matches(RegExUtil.CLI_COMMAND_NAME)) {
            String errorMessage = validatorBundle.message(
                    "validator.identifier",
                    this.commonBundle.message("common.cli.cli.name")
            );
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (cliCommandDescription.length() == 0) {
            String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    this.commonBundle.message("common.cli.cli.description")
            );
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        return true;
    }
}
