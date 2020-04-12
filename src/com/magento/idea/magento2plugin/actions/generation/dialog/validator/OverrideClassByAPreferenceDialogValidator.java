/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.dialog.OverrideClassByAPreferenceDialog;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.util.Regex;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import javax.swing.*;
import java.util.List;

public class OverrideClassByAPreferenceDialogValidator {
    private static OverrideClassByAPreferenceDialogValidator INSTANCE = null;
    private ValidatorBundle validatorBundle;
    private OverrideClassByAPreferenceDialog dialog;

    public static OverrideClassByAPreferenceDialogValidator getInstance(OverrideClassByAPreferenceDialog dialog) {
        if (null == INSTANCE) {
            INSTANCE = new OverrideClassByAPreferenceDialogValidator();
        }
        INSTANCE.dialog = dialog;
        return INSTANCE;
    }

    public OverrideClassByAPreferenceDialogValidator() {
        validatorBundle = new ValidatorBundle();
    }

    public boolean validate(Project project)
    {
        String errorTitle = "Error";
        String preferenceClassName = dialog.getPreferenceClassName();
        if (preferenceClassName.length() == 0) {
            String errorMessage = validatorBundle.message("validator.notEmpty", "Preference Class Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!preferenceClassName.matches(Regex.ALPHANUMERIC)) {
            String errorMessage = validatorBundle.message("validator.alphaNumericCharacters", "Preference Class");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!Character.isUpperCase(preferenceClassName.charAt(0)) && !Character.isDigit(preferenceClassName.charAt(0))) {
            String errorMessage = validatorBundle.message("validator.startWithNumberOrCapitalLetter", "Preference Class");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        String preferenceDirectory = dialog.getPreferenceDirectory();
        if (preferenceDirectory.length() == 0) {
            String errorMessage = validatorBundle.message("validator.notEmpty", "Preference Directory");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!preferenceDirectory.matches(Regex.DIRECTORY)) {
            String errorMessage = validatorBundle.message("validator.directory.isNotValid", "Preference Directory");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String preferenceModule = dialog.getPreferenceModule();
        if (preferenceModule.length() == 0) {
            String errorMessage = validatorBundle.message("validator.notEmpty", "Preference Module");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        List<String> allModulesList = ModuleIndex.getInstance(project).getEditableModuleNames();
        if (!allModulesList.contains(preferenceModule)) {
            String errorMessage = validatorBundle.message("validator.module.noSuchModule", preferenceModule);
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        return true;
    }
}
