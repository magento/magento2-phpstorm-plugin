/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.dialog.CreateAPluginDialog;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.util.Regex;
import com.magento.idea.magento2plugin.validators.ValidatorBundle;
import javax.swing.*;
import java.util.List;

public class CreateAPluginDialogValidator {
    private static CreateAPluginDialogValidator INSTANCE = null;
    private CreateAPluginDialog dialog;

    public static CreateAPluginDialogValidator getInstance(CreateAPluginDialog dialog) {
        if (null == INSTANCE) {
            INSTANCE = new CreateAPluginDialogValidator();
        }

        INSTANCE.dialog = dialog;
        return INSTANCE;
    }

    public boolean validate(Project project)
    {
        String errorTitle = "Error";
        String pluginClassName = dialog.getPluginClassName();

        if (pluginClassName.length() == 0) {
            String errorMessage = ValidatorBundle.message("validator.notEmpty", "Plugin Class Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!pluginClassName.matches(Regex.ALPHANUMERIC)) {
            String errorMessage = ValidatorBundle.message("validator.alphaNumericCharacters", "Plugin Class Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!Character.isUpperCase(pluginClassName.charAt(0)) && !Character.isDigit(pluginClassName.charAt(0))) {
            String errorMessage = ValidatorBundle.message("validator.startWithNumberOrCapitalLetter", "Plugin Class Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        String pluginDirectory = dialog.getPluginDirectory();
        if (pluginDirectory.length() == 0) {
            String errorMessage = ValidatorBundle.message("validator.notEmpty", "Plugin Directory");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!pluginDirectory.matches(Regex.DIRECTORY)) {
            String errorMessage = ValidatorBundle.message("validator.directory.isNotValid", "Plugin Directory");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        String pluginName = dialog.getPluginName();
        if (pluginName.length() == 0) {
            String errorMessage = ValidatorBundle.message("validator.notEmpty", "Plugin Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!pluginName.matches(Regex.IDENTIFIER)) {
            String errorMessage = ValidatorBundle.message("validator.identifier", "Plugin Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        String sortOrder = dialog.getPluginSortOrder();
        if (sortOrder.length() == 0) {
            String errorMessage = ValidatorBundle.message("validator.notEmpty", "Sort Order");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!sortOrder.matches(Regex.NUMERIC)) {
            String errorMessage = ValidatorBundle.message("validator.onlyNumbers", "Sort Order");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        String pluginModule = dialog.getPluginModule();
        if (pluginModule.length() == 0) {
            String errorMessage = ValidatorBundle.message("validator.notEmpty", "Plugin Module");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        List<String> allModulesList = ModuleIndex.getInstance(project).getEditableModuleNames();
        if (!allModulesList.contains(pluginModule)) {
            String errorMessage = ValidatorBundle.message("validator.module.noSuchModule", pluginModule);
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        return true;
    }
}
