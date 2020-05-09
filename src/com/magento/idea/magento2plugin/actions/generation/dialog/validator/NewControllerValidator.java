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

@SuppressWarnings({
        "PMD.OnlyOneReturn",
        "PMD.FieldNamingConventions",
        "PMD.DataflowAnomalyAnalysis",
        "PMD.NonThreadSafeSingleton",
        "PMD.NPathComplexity"
})
public class NewControllerValidator {
    private static final String ACTION_NAME = "Action Name";
    private static NewControllerValidator INSTANCE;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private NewControllerDialog dialog;

    /**
     * Get instance of a class.
     *
     * @param dialog New controller dialog
     *
     * @return NewControllerValidator
     */
    public static NewControllerValidator getInstance(final NewControllerDialog dialog) {
        if (null == INSTANCE) {
            INSTANCE = new NewControllerValidator();
        }

        INSTANCE.dialog = dialog;
        return INSTANCE;
    }

    /**
     * New controller validator constructor.
     */
    public NewControllerValidator() {
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    /**
     * Validate whenever new controller dialog data is ready for generation.
     *
     * @return Boolean
     */
    public boolean validate() {
        final String errorTitle = commonBundle.message("common.error");
        final String actionName = dialog.getActionName();

        if (!PhpNameUtil.isValidClassName(actionName)) {
            final String errorMessage = this.validatorBundle.message(
                    "validator.class.isNotValid",
                    ACTION_NAME
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
            final String errorMessage = this.validatorBundle.message(
                    "validator.notEmpty",
                    ACTION_NAME
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
            final String errorMessage = this.validatorBundle.message(
                    "validator.alphaNumericCharacters",
                    ACTION_NAME
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
            final String errorMessage = this.validatorBundle.message(
                    "validator.startWithNumberOrCapitalLetter",
                    ACTION_NAME
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        final String controllerName = dialog.getControllerName();

        if (!PhpNameUtil.isValidNamespaceName(controllerName)) {
            final String errorMessage = this.validatorBundle.message(
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
