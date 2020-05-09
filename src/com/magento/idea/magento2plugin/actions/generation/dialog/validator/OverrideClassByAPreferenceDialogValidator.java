/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.refactoring.PhpNameUtil;
import com.magento.idea.magento2plugin.actions.generation.dialog.OverrideClassByAPreferenceDialog;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.util.RegExUtil;
import java.util.List;
import javax.swing.JOptionPane;

@SuppressWarnings({
        "PMD.OnlyOneReturn",
        "PMD.FieldNamingConventions",
        "PMD.DataflowAnomalyAnalysis",
        "PMD.NPathComplexity"
})
public class OverrideClassByAPreferenceDialogValidator {
    private static OverrideClassByAPreferenceDialogValidator INSTANCE = null;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private OverrideClassByAPreferenceDialog dialog;

    /**
     * Get instance of a class.
     *
     * @param dialog New override class by a preference dialog
     *
     * @return OverrideClassByAPreferenceDialogValidator
     */
    public static OverrideClassByAPreferenceDialogValidator getInstance(
            final OverrideClassByAPreferenceDialog dialog
    ) {
        if (null == INSTANCE) {
            INSTANCE = new OverrideClassByAPreferenceDialogValidator();
        }
        INSTANCE.dialog = dialog;
        return INSTANCE;
    }

    private OverrideClassByAPreferenceDialogValidator() {
        validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    /**
     * Validate whenever override class by preference dialog data is ready for generation.
     *
     * @param project Project
     *
     * @return boolean
     */
    public boolean validate(Project project) {
        final String errorTitle = commonBundle.message("common.error");
        final String preferenceClassName = dialog.getPreferenceClassName();

        if (!PhpNameUtil.isValidClassName(preferenceClassName)) {
            final String errorMessage = this.validatorBundle.message(
                    "validator.class.isNotValid",
                    "Preference Class Name"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (preferenceClassName.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    "Preference Class Name"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!preferenceClassName.matches(RegExUtil.ALPHANUMERIC)) {
            final String errorMessage = validatorBundle.message(
                    "validator.alphaNumericCharacters",
                    "Preference Class"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!Character.isUpperCase(preferenceClassName.charAt(0))
                && !Character.isDigit(preferenceClassName.charAt(0))
        ) {
            final String errorMessage = validatorBundle.message(
                    "validator.startWithNumberOrCapitalLetter",
                    "Preference Class"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        final String preferenceDirectory = dialog.getPreferenceDirectory();
        if (preferenceDirectory.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    "Preference Directory"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!preferenceDirectory.matches(RegExUtil.DIRECTORY)) {
            final String errorMessage = validatorBundle.message(
                    "validator.directory.isNotValid",
                    "Preference Directory"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        final String preferenceModule = dialog.getPreferenceModule();
        if (preferenceModule.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    "Preference Module"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        final List<String> allModulesList = ModuleIndex.getInstance(project).getEditableModuleNames();
        if (!allModulesList.contains(preferenceModule)) {
            final String errorMessage = validatorBundle.message(
                    "validator.module.noSuchModule",
                    preferenceModule
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
