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

@SuppressWarnings({"PMD.OnlyOneReturn"})
public class NewBlockValidator {
    private static NewBlockValidator INSTANCE = null;
    private ValidatorBundle validatorBundle;
    private CommonBundle commonBundle;
    private NewBlockDialog dialog;

    /**
     * Get instance of a class.
     *
     * @param dialog New block dialog
     *
     * @return NewBlockValidator
     */
    public static NewBlockValidator getInstance(NewBlockDialog dialog) {
        if (null == INSTANCE) {
            INSTANCE = new NewBlockValidator();
        }
        INSTANCE.dialog = dialog;
        return INSTANCE;
    }

    /**
     * New block validator constructor.
     */
    public NewBlockValidator() {
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    /**
     * Validate whenever new block dialog data is ready for generation.
     *
     * @return Boolean
     */
    public boolean validate() {
        String errorTitle = commonBundle.message("common.error");
        String moduleName = dialog.getBlockName();

        if (!PhpNameUtil.isValidClassName(moduleName)) {
            String errorMessage = this.validatorBundle.message(
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
            String errorMessage = validatorBundle.message(
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
            String errorMessage = validatorBundle.message(
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
            String errorMessage = validatorBundle.message(
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

        String pluginDirectory = dialog.getBlockDirectory();
        if (pluginDirectory.length() == 0) {
            String errorMessage = validatorBundle.message(
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
            String errorMessage = validatorBundle.message(
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
