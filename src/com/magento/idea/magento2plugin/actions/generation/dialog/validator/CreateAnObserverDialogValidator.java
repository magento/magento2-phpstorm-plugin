/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.refactoring.PhpNameUtil;
import com.magento.idea.magento2plugin.actions.generation.dialog.CreateAnObserverDialog;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.util.RegExUtil;
import java.util.List;
import javax.swing.JOptionPane;

@SuppressWarnings({
        "PMD.OnlyOneReturn",
        "PMD.FieldNamingConventions",
        "PMD.CyclomaticComplexity",
        "PMD.NonThreadSafeSingleton",
        "PMD.ExcessiveMethodLength",
        "PMD.DataflowAnomalyAnalysis",
        "PMD.NPathComplexity"
})
public class CreateAnObserverDialogValidator {
    private static final String OBSERVER_CLASS_NAME = "Observer Class Name";
    private static CreateAnObserverDialogValidator INSTANCE;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private CreateAnObserverDialog dialog;

    /**
     * Get instance of a class.
     *
     * @param dialog Create observer dialog
     *
     * @return CreateAnObserverDialogValidator
     */
    public static CreateAnObserverDialogValidator getInstance(final CreateAnObserverDialog dialog) {
        if (null == INSTANCE) {
            INSTANCE = new CreateAnObserverDialogValidator();
        }
        INSTANCE.dialog = dialog;
        return INSTANCE;
    }

    /**
     * Create an observer dialog validator.
     */
    public CreateAnObserverDialogValidator() {
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    /**
     * Validate whenever new create observer dialog data is ready for generation.
     *
     * @param project Project
     *
     * @return Boolean
     */
    public boolean validate(final Project project) {
        final String errorTitle = commonBundle.message("common.error");
        final String observerClassName = dialog.getObserverClassName();

        if (!PhpNameUtil.isValidClassName(observerClassName)) {
            final String errorMessage = this.validatorBundle.message(
                    "validator.class.isNotValid",
                    OBSERVER_CLASS_NAME
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (observerClassName.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    OBSERVER_CLASS_NAME
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!observerClassName.matches(RegExUtil.ALPHANUMERIC)) {
            final String errorMessage = validatorBundle.message(
                    "validator.alphaNumericCharacters",
                    OBSERVER_CLASS_NAME
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!Character.isUpperCase(observerClassName.charAt(0))
                && !Character.isDigit(observerClassName.charAt(0))
        ) {
            final String errorMessage = validatorBundle.message(
                    "validator.startWithNumberOrCapitalLetter",
                    OBSERVER_CLASS_NAME
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        final String observerDirectory = dialog.getObserverDirectory();

        if (observerDirectory.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    "Observer Directory"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!observerDirectory.matches(RegExUtil.DIRECTORY)) {
            final String errorMessage = validatorBundle.message(
                    "validator.directory.isNotValid",
                    "Observer Directory"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }


        final String observerModule = dialog.getObserverModule();
        if (observerModule.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    "Observer Module"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        final List<String> allModulesList = ModuleIndex.getInstance(project)
                .getEditableModuleNames();
        if (!allModulesList.contains(observerModule)) {
            final String errorMessage = validatorBundle.message(
                    "validator.module.noSuchModule",
                    observerModule
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
