/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.jetbrains.php.refactoring.PhpNameUtil;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewControllerDialog;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.util.RegExUtil;
import javax.swing.JOptionPane;

@SuppressWarnings({"PMD.OnlyOneReturn"})
public class NewControllerValidator {
    private static NewControllerValidator INSTANCE = null;
    private ValidatorBundle validatorBundle;
    private NewControllerDialog dialog;
    private CommonBundle commonBundle;

    /**
     * Get instance of a class.
     *
     * @param dialog New controller dialog
     *
     * @return NewControllerValidator
     */
    public static NewControllerValidator getInstance(NewControllerDialog dialog) {
        if (null == INSTANCE) {
            INSTANCE = new NewControllerValidator();
        }

        INSTANCE.dialog = dialog;
        return INSTANCE;
    }

    public NewControllerValidator()
    {
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    /**
     * Validate whenever new controller dialog data is ready for generation.
     *
     * @return Boolean
     */
    public boolean validate()
    {
        String errorTitle = commonBundle.message("common.error");
        String actionName = dialog.getActionName();

        if (!PhpNameUtil.isValidClassName(actionName)) {
            String errorMessage = this.validatorBundle.message(
                    "validator.class.isNotValid",
                    "Action Name"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (actionName.length() == 0) {
            String errorMessage = this.validatorBundle.message(
                    "validator.notEmpty",
                    "Action Name"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!actionName.matches(RegExUtil.ALPHANUMERIC)) {
            String errorMessage = this.validatorBundle.message(
                    "validator.alphaNumericCharacters",
                    "Action Name"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!Character.isUpperCase(actionName.charAt(0))
                && !Character.isDigit(actionName.charAt(0))
        ) {
            String errorMessage = this.validatorBundle.message(
                    "validator.startWithNumberOrCapitalLetter",
                    "Action Name"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        String controllerName = dialog.getControllerName();

        if (!PhpNameUtil.isValidNamespaceName(controllerName)) {
            String errorMessage = this.validatorBundle.message(
                    "validator.namespace.isNotValid",
                    "Controller Name"
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
