/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.dialog.OverrideClassByAPreferenceDialog;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.util.Regex;
import javax.swing.*;
import java.util.List;

public class OverrideClassByAPreferenceDialogValidator {
    private static OverrideClassByAPreferenceDialogValidator INSTANCE = null;
    private OverrideClassByAPreferenceDialog dialog;

    public static OverrideClassByAPreferenceDialogValidator getInstance(OverrideClassByAPreferenceDialog dialog) {
        if (null == INSTANCE) {
            INSTANCE = new OverrideClassByAPreferenceDialogValidator();
        }
        INSTANCE.dialog = dialog;
        return INSTANCE;
    }

    public boolean validate(Project project)
    {
        String errorTitle = "Error";
        String preferenceClassName = dialog.getPreferenceClassName();
        if (preferenceClassName.length() == 0) {
            JOptionPane.showMessageDialog(null, "Preference Class Name must not be empty.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!preferenceClassName.matches(Regex.ALPHANUMERIC)) {
            JOptionPane.showMessageDialog(null, "Preference Class Name must contain letters and numbers only.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!Character.isUpperCase(preferenceClassName.charAt(0)) && !Character.isDigit(preferenceClassName.charAt(0))) {
            JOptionPane.showMessageDialog(null, "Preference Class Name must start from a number or a capital letter", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String preferenceDirectory = dialog.getPreferenceDirectory();
        if (preferenceDirectory.length() == 0) {
            JOptionPane.showMessageDialog(null, "Preference Directory must not be empty.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!preferenceDirectory.matches(Regex.DIRECTORY)) {
            JOptionPane.showMessageDialog(null, "Preference Directory is not valid.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String preferenceModule = dialog.getPreferenceModule();
        if (preferenceModule.length() == 0) {
            JOptionPane.showMessageDialog(null, "Preference Module must not be empty.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        List<String> allModulesList = ModuleIndex.getInstance(project).getEditableModuleNames();
        if (!allModulesList.contains(preferenceModule)) {
            JOptionPane.showMessageDialog(null, "No such module '".concat(preferenceModule).concat("'."), errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }
}
