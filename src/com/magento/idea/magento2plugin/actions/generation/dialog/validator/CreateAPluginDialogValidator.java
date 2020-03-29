/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.dialog.CreateAPluginDialog;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.util.Regex;
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
            JOptionPane.showMessageDialog(null, "Plugin Class Name must not be empty.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!pluginClassName.matches(Regex.ALPHANUMERIC)) {
            JOptionPane.showMessageDialog(null, "Plugin Class Name must contain letters and numbers only.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!Character.isUpperCase(pluginClassName.charAt(0)) && !Character.isDigit(pluginClassName.charAt(0))) {
            JOptionPane.showMessageDialog(null, "Plugin Class Name must start from a number or a capital letter", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String pluginDirectory = dialog.getPluginDirectory();
        if (pluginDirectory.length() == 0) {
            JOptionPane.showMessageDialog(null, "Plugin Directory must not be empty.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!pluginDirectory.matches(Regex.DIRECTORY)) {
            JOptionPane.showMessageDialog(null, "Plugin Directory is not valid.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String pluginName = dialog.getPluginName();
        if (pluginName.length() == 0) {
            JOptionPane.showMessageDialog(null, "Plugin Name must not be empty.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!pluginName.matches(Regex.IDENTIFIER)) {
            JOptionPane.showMessageDialog(null, "Plugin Name must contain letters, numbers, dashes, and underscores only.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String sortOrder = dialog.getPluginSortOrder();
        if (sortOrder.length() == 0) {
            JOptionPane.showMessageDialog(null, "Sort Order must not be empty.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!sortOrder.matches(Regex.NUMERIC)) {
            JOptionPane.showMessageDialog(null, "Sort Order must contain numbers only.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String pluginModule = dialog.getPluginModule();
        if (pluginModule.length() == 0) {
            JOptionPane.showMessageDialog(null, "Plugin Module must not be empty.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        List<String> allModulesList = ModuleIndex.getInstance(project).getEditableModuleNames();
        if (!allModulesList.contains(pluginModule)) {
            JOptionPane.showMessageDialog(null, "No such module '".concat(pluginModule).concat("'."), errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }
}
