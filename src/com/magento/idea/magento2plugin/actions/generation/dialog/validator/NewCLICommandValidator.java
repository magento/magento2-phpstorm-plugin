/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewCLICommandDialog;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import com.magento.idea.magento2plugin.util.RegExUtil;
import javax.swing.JOptionPane;

public class CLICommandValidator {
    private static CLICommandValidator INSTANCE = null;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;

    /**
     * Returns a new instance of a class.
     *
     * @return NewCLICommandValidator
     */
    public static CLICommandValidator getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new CLICommandValidator();
        }

        return INSTANCE;
    }

    public CLICommandValidator() {
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    /**
     * Validate new CLI command form data.
     *
     * @param project Project
     * @param dialog Dialog
     * @return boolen
     */
    public boolean validate(final Project project, final NewCLICommandDialog dialog) {
        this.validateClassName(dialog);
        this.validateParentDirectory(dialog);
        this.validateCommandName(dialog);
        this.validateCommandDescription(dialog);
        this.validatePHPClassName(project, dialog);

        return true;
    }

    private void validatePHPClassName(final Project project, final NewCLICommandDialog dialog) {
        final String moduleName = dialog.getCLICommandModule();
        final NamespaceBuilder namespaceBuilder = new NamespaceBuilder(
                moduleName,
                dialog.getCLICommandClassName(),
                dialog.getCLICommandParentDirectory()
        );
        final String namespace = namespaceBuilder.getClassFqn();
        final PhpClass phpClass = GetPhpClassByFQN.getInstance(project).execute(namespace);
        if (phpClass != null) {
            final String errorMessage = validatorBundle.message(
                    "validator.file.alreadyExists",
                    this.commonBundle.message("common.cli.class.title")
            );
            this.showOptionPane(errorMessage);
        }
    }

    private void validateCommandDescription(final NewCLICommandDialog dialog) {
        final String description = dialog.getCLICommandDescription();
        if (description.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    this.commonBundle.message("common.description")
            );
            this.showOptionPane(errorMessage);
        }
    }

    private void validateCommandName(final NewCLICommandDialog dialog) {
        final String cliCommandName = dialog.getCLICommandName();
        if (cliCommandName.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    this.commonBundle.message("common.cliCommandName")
            );
            this.showOptionPane(errorMessage);
        }
        if (!cliCommandName.matches(RegExUtil.CLI_COMMAND_NAME)) {
            final String errorMessage = validatorBundle.message(
                    "validator.directory.isNotValid",
                    this.commonBundle.message("common.cliCommandName")
            );
            this.showOptionPane(errorMessage);
        }
    }

    private void validateParentDirectory(final NewCLICommandDialog dialog) {
        final String directory = dialog.getCLICommandParentDirectory();
        if (directory.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    this.commonBundle.message("common.parentDirectory")
            );
            this.showOptionPane(errorMessage);
        }
        if (!directory.matches(RegExUtil.DIRECTORY)) {
            final String errorMessage = validatorBundle.message(
                    "validator.directory.isNotValid",
                    this.commonBundle.message("common.parentDirectory")
            );
            this.showOptionPane(errorMessage);
        }
    }

    private void validateClassName(final NewCLICommandDialog dialog) {
        final String className = dialog.getCLICommandClassName();
        if (className.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    this.commonBundle.message("common.className")
            );
            this.showOptionPane(errorMessage);
        }
        if (!className.matches(RegExUtil.ALPHANUMERIC)) {
            final String errorMessage = validatorBundle.message(
                    "validator.alphaNumericCharacters",
                    this.commonBundle.message("common.className")
            );
            this.showOptionPane(errorMessage);
        }
        if (!Character.isUpperCase(className.charAt(0))
                && !Character.isDigit(className.charAt(0))
        ) {
            final String errorMessage = validatorBundle.message(
                    "validator.startWithNumberOrCapitalLetter",
                    this.commonBundle.message("common.className")
            );
            this.showOptionPane(errorMessage);
        }
    }

    private void showOptionPane(final String errorMessage) {
        JOptionPane.showMessageDialog(
                null,
                errorMessage,
                this.commonBundle.message("common.validationErrorTitle"),
                JOptionPane.ERROR_MESSAGE
        );
    }
}
