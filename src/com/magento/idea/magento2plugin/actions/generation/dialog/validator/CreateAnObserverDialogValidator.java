/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.dialog.CreateAnObserverDialog;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.util.Regex;

import javax.swing.*;
import java.util.List;

public class CreateAnObserverDialogValidator {
    private static CreateAnObserverDialogValidator INSTANCE = null;
    private CreateAnObserverDialog dialog;

    public static CreateAnObserverDialogValidator getInstance(CreateAnObserverDialog dialog) {
        if (null == INSTANCE) {
            INSTANCE = new CreateAnObserverDialogValidator();
        }
        INSTANCE.dialog = dialog;
        return INSTANCE;
    }

    public boolean validate(Project project)
    {
        String errorTitle = "Error";
        String observerClassName = dialog.getObserverClassName();
        if (observerClassName.length() == 0) {
            JOptionPane.showMessageDialog(null, "Observer Class Name must not be empty.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!observerClassName.matches(Regex.ALPHANUMERIC)) {
            JOptionPane.showMessageDialog(null, "Observer Class Name must contain letters and numbers only.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!Character.isUpperCase(observerClassName.charAt(0)) && !Character.isDigit(observerClassName.charAt(0))) {
            JOptionPane.showMessageDialog(null, "Observer Class Name must start from a number or a capital letter", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String observerDirectory = dialog.getObserverDirectory();
        if (observerDirectory.length() == 0) {
            JOptionPane.showMessageDialog(null, "Observer Directory must not be empty.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!observerDirectory.matches(Regex.DIRECTORY)) {
            JOptionPane.showMessageDialog(null, "Observer Directory is not valid.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }


        String observerModule = dialog.getObserverModule();
        if (observerModule.length() == 0) {
            JOptionPane.showMessageDialog(null, "Observer Module must not be empty.", errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        List<String> allModulesList = ModuleIndex.getInstance(project).getEditableModuleNames();
        if (!allModulesList.contains(observerModule)) {
            JOptionPane.showMessageDialog(null, "No such module '".concat(observerModule).concat("'."), errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }
}
