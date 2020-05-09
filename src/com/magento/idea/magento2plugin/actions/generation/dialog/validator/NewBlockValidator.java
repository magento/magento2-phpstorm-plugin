/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.jetbrains.php.refactoring.PhpNameUtil;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewBlockDialog;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.util.RegExUtil;
import javax.swing.JOptionPane;

@SuppressWarnings({
        "PMD.OnlyOneReturn",
        "PMD.FieldNamingConventions",
        "PMD.DataflowAnomalyAnalysis",
        "PMD.NPathComplexity"
})
public class NewBlockValidator {
    private static NewBlockValidator INSTANCE = null;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private NewBlockDialog dialog;

    /**
     * Get instance of a class.
     *
     * @param dialog New block dialog
     *
     * @return NewBlockValidator
     */
    public static NewBlockValidator getInstance(final NewBlockDialog dialog) {
        if (null == INSTANCE) {
            INSTANCE = new NewBlockValidator();
        }
        INSTANCE.dialog = dialog;
        return INSTANCE;
    }

    /**
     * New block validator constructor.
     */
    private NewBlockValidator() {
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    /**
     * Validate whenever new block dialog data is ready for generation.
     *
     * @return Boolean
     */
    public boolean validate() {
        final String errorTitle = commonBundle.message("common.error");
        final String moduleName = dialog.getBlockName();

        if (!PhpNameUtil.isValidClassName(moduleName)) {
            final String errorMessage = this.validatorBundle.message(
                    "validator.class.isNotValid",
                    "Block Name"
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
                    "Block Name"
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
                    "Block Name"
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
                    "Block Name"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        final String pluginDirectory = dialog.getBlockDirectory();
        if (pluginDirectory.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    "Block Directory"
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
                    "Block Directory"
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
