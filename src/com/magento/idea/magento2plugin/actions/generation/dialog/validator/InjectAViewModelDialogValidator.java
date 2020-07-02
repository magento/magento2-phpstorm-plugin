/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.refactoring.PhpNameUtil;
import com.magento.idea.magento2plugin.actions.generation.dialog.InjectAViewModelDialog;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.util.RegExUtil;
import javax.swing.JOptionPane;

@SuppressWarnings({"PMD.NPathComplexity"})
public class InjectAViewModelDialogValidator {
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final InjectAViewModelDialog dialog;

    /**
     * Get instance of a class.
     *
     * @param dialog Inject a View Model dialog
     */
    public InjectAViewModelDialogValidator(final InjectAViewModelDialog dialog) {
        validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
        this.dialog = dialog;
    }

    /**
     * Validate whenever override class by viewModel dialog data is ready for generation.
     *
     * @param project Project
     *
     * @return boolean
     */
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength"})
    public boolean validate(final Project project) {
        final String errorTitle = commonBundle.message("common.error");
        final String viewModelClassName = dialog.getViewModelClassName();

        if (!PhpNameUtil.isValidClassName(viewModelClassName)) {
            final String errorMessage = this.validatorBundle.message(
                    "validator.class.isNotValid",
                    "ViewModel Class Name"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (viewModelClassName.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    "ViewModel Class Name"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!viewModelClassName.matches(RegExUtil.ALPHANUMERIC)) {
            final String errorMessage = validatorBundle.message(
                    "validator.alphaNumericCharacters",
                    "ViewModel Class"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!Character.isUpperCase(viewModelClassName.charAt(0))
                && !Character.isDigit(viewModelClassName.charAt(0))
        ) {
            final String errorMessage = validatorBundle.message(
                    "validator.startWithNumberOrCapitalLetter",
                    "ViewModel Class"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        final String viewModelDirectory = dialog.getViewModelDirectory();
        if (viewModelDirectory.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    "ViewModel Directory"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!viewModelDirectory.matches(RegExUtil.DIRECTORY)) {
            final String errorMessage = validatorBundle.message(
                    "validator.directory.isNotValid",
                    "ViewModel Directory"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        final String viewModelArgumentName = dialog.getViewModelArgumentName();
        if (viewModelArgumentName.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    "ViewModel Argument Name"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!viewModelArgumentName.matches(RegExUtil.ALPHANUMERIC)) {
            final String errorMessage = validatorBundle.message(
                    "validator.alphaNumericCharacters",
                    "ViewModel Argument Name"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        return true;
    }
}
