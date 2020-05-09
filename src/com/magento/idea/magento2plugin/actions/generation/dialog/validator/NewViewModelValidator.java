/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.jetbrains.php.refactoring.PhpNameUtil;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewViewModelDialog;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.util.RegExUtil;
import javax.swing.JOptionPane;

@SuppressWarnings({
        "PMD.OnlyOneReturn",
        "PMD.FieldNamingConventions",
        "PMD.DataflowAnomalyAnalysis",
        "PMD.NonThreadSafeSingleton",
        "PMD.NPathComplexity"
})
public class NewViewModelValidator {
    private static NewViewModelValidator INSTANCE;
    private static final String VIEW_MODEL_NAME = "View Model Name";
    private static final String VIEW_MODEL_DIR = "View Model Directory";
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private NewViewModelDialog dialog;

    /**
     * Get instance of a class.
     *
     * @param dialog New view model dialog
     *
     * @return NewViewModelValidator
     */
    public static NewViewModelValidator getInstance(final NewViewModelDialog dialog) {
        if (null == INSTANCE) {
            INSTANCE = new NewViewModelValidator();
        }
        INSTANCE.dialog = dialog;
        return INSTANCE;
    }

    /**
     * New view model validator constructor.
     */
    public NewViewModelValidator() {
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    /**
     * Validate whenever new view model dialog data is ready for generation.
     *
     * @return Boolean
     */
    public boolean validate() {
        final String errorTitle = commonBundle.message("common.error");
        final String moduleName = dialog.getViewModelName();

        if (!PhpNameUtil.isValidClassName(moduleName)) {
            final String errorMessage = this.validatorBundle.message(
                    "validator.class.isNotValid",
                    VIEW_MODEL_NAME
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (moduleName.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    VIEW_MODEL_NAME
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!moduleName.matches(RegExUtil.ALPHANUMERIC)) {
            final String errorMessage = validatorBundle.message(
                    "validator.alphaNumericCharacters",
                    VIEW_MODEL_NAME
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!Character.isUpperCase(moduleName.charAt(0))
                && !Character.isDigit(moduleName.charAt(0))
        ) {
            final String errorMessage = validatorBundle.message(
                    "validator.startWithNumberOrCapitalLetter",
                    VIEW_MODEL_NAME
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        final String pluginDirectory = dialog.getViewModelDirectory();
        if (pluginDirectory.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    VIEW_MODEL_DIR
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!pluginDirectory.matches(RegExUtil.DIRECTORY)) {
            final String errorMessage = validatorBundle.message(
                    "validator.directory.isNotValid",
                    VIEW_MODEL_DIR
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
