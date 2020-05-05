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

@SuppressWarnings({
        "PMD.FieldNamingConventions",
        "PMD.RedundantFieldInitializer",
        "PMD.OnlyOneReturn",
        "PMD.AvoidDuplicateLiterals",
})
public class NewCLICommandValidator {
    private static NewCLICommandValidator INSTANCE = null;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;

    /**
     * Returns a new instance of a class.
     *
     * @return NewCLICommandValidator
     */
    public static NewCLICommandValidator getInstance() {
        if (null != INSTANCE) {
            return INSTANCE;
        }
        INSTANCE = new NewCLICommandValidator();

        return INSTANCE;
    }

    public NewCLICommandValidator() {
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
        return this.isClassNameValid(dialog)
                && this.isParentDirectoryValid(dialog)
                && this.isCommandNameValid(dialog)
                && this.isCommandDescriptionValid(dialog)
                && this.isPHPClassValid(project, dialog);
    }

    private Boolean isClassNameValid(final NewCLICommandDialog dialog) {
        final String className = dialog.getCLICommandClassName();
        if (className.length() == 0) {
            this.showOptionPane(
                    "validator.notEmpty",
                    "common.className"
            );
            return false;
        }
        if (!className.matches(RegExUtil.ALPHANUMERIC)) {
            this.showOptionPane(
                    "validator.alphaNumericCharacters",
                    "common.className"
            );
            return false;
        }
        if (!Character.isUpperCase(className.charAt(0))
                && !Character.isDigit(className.charAt(0))
        ) {
            this.showOptionPane(
                    "validator.startWithNumberOrCapitalLetter",
                    "common.className"
            );
            return false;
        }

        return true;
    }

    private Boolean isParentDirectoryValid(final NewCLICommandDialog dialog) {
        final String directory = dialog.getCLICommandParentDirectory();
        if (directory.length() == 0) {
            this.showOptionPane(
                    "validator.notEmpty",
                    "common.parentDirectory"
            );
            return false;
        }
        if (!directory.matches(RegExUtil.DIRECTORY)) {
            this.showOptionPane(
                    "validator.directory.isNotValid",
                    "common.parentDirectory"
            );
            return false;
        }

        return true;
    }

    private Boolean isCommandNameValid(final NewCLICommandDialog dialog) {
        final String cliCommandName = dialog.getCLICommandName();
        if (cliCommandName.length() == 0) {
            this.showOptionPane(
                    "validator.notEmpty",
                    "common.cliCommandName"
            );
            return false;
        }
        if (!cliCommandName.matches(RegExUtil.CLI_COMMAND_NAME)) {
            this.showOptionPane(
                    "validator.directory.isNotValid",
                    "common.cliCommandName"
            );
            return false;
        }

        return true;
    }

    private Boolean isCommandDescriptionValid(final NewCLICommandDialog dialog) {
        final String description = dialog.getCLICommandDescription();
        if (description.length() == 0) {
            this.showOptionPane(
                    "validator.notEmpty",//NOPMD
                    "common.description"
            );
            return false;
        }

        return true;
    }

    private Boolean isPHPClassValid(final Project project, final NewCLICommandDialog dialog) {
        final String moduleName = dialog.getCLICommandModule();
        final NamespaceBuilder namespaceBuilder = new NamespaceBuilder(
                moduleName,
                dialog.getCLICommandClassName(),
                dialog.getCLICommandParentDirectory()
        );
        final String namespace = namespaceBuilder.getClassFqn();
        final PhpClass phpClass = GetPhpClassByFQN.getInstance(project).execute(namespace);
        if (phpClass != null) {
            this.showOptionPane(
                    "validator.file.alreadyExists",
                    "common.cli.class.title"
            );
            return false;
        }

        return true;
    }

    private void showOptionPane(final String key, final String resourceKey) {
        final String errorMessage = validatorBundle.message(
                key,
                this.commonBundle.message(resourceKey)
        );
        JOptionPane.showMessageDialog(
                null,
                errorMessage,
                this.commonBundle.message("common.validationErrorTitle"),
                JOptionPane.ERROR_MESSAGE
        );
    }
}
